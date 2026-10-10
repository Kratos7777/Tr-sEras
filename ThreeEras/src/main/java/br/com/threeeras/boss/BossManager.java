package br.com.threeeras.boss;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Chefe piloto: Soberba ("O que nao se curva").
 *
 * <p>Mecanica do piloto:</p>
 * <ul>
 *   <li>Nasce com N pilares ao redor. Enquanto existir pilar de pe, o
 *       dominio sustenta a criatura: ela nao pode ser ferida e jogadores
 *       próximos sofrem lentidao e fraqueza.</li>
 *   <li>Cada pilar destruido enfraquece o dominio; com todos destruidos,
 *       a criatura fica vulneravel.</li>
 * </ul>
 *
 * <p>Registro em memoria: entidade do chefe, UUIDs dos pilares vivos e a
 * tarefa de dominio. Tudo na thread principal.</p>
 */
public final class BossManager {

    private static final String FILE_NAME = "bosses.yml";

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final File file;
    private final NamespacedKey chaveBoss;
    private final NamespacedKey chavePilar;
    private final Map<String, BossDefinition> definicoes = new LinkedHashMap<>();
    private final Map<String, LivingEntity> entidades = new HashMap<>();
    private final Map<String, Set<UUID>> pilaresVivos = new HashMap<>();
    private final Map<String, BukkitRunnable> tarefas = new HashMap<>();

    public BossManager(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        this.chaveBoss = new NamespacedKey(plugin, "boss_id");
        this.chavePilar = new NamespacedKey(plugin, "pilar_boss");
    }

    /** (Re)carrega bosses.yml do disco (copia do resource na primeira execucao). */
    public void load() {
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, BossDefinition> carregadas = new LinkedHashMap<>();
        ConfigurationSection secao = config.getConfigurationSection("bosses");
        if (secao == null) {
            plugin.getLogger().warning("Nenhum chefe definido em " + FILE_NAME + " (secao 'bosses' ausente).");
        } else {
            for (String id : secao.getKeys(false)) {
                String base = "bosses." + id;
                carregadas.put(id.toLowerCase(Locale.ROOT), new BossDefinition(
                        id,
                        config.getString(base + ".nome", id),
                        config.getString(base + ".era", ""),
                        config.getString(base + ".entidade", "RAVAGER"),
                        config.getDouble(base + ".vida", 100.0),
                        config.getDouble(base + ".dominio.raio", 20.0),
                        config.getInt(base + ".dominio.lentidao", 2),
                        config.getInt(base + ".dominio.fraqueza", 1),
                        config.getInt(base + ".pilares", 3)));
            }
        }

        definicoes.clear();
        definicoes.putAll(carregadas);
        plugin.getLogger().info("Chefes carregados (" + definicoes.size() + ").");
    }

    public BossDefinition porId(String id) {
        if (id == null) {
            return null;
        }
        return definicoes.get(id.toLowerCase(Locale.ROOT));
    }

    public List<BossDefinition> listar() {
        return List.copyOf(definicoes.values());
    }

    public int total() {
        return definicoes.size();
    }

    public NamespacedKey getChaveBoss() {
        return chaveBoss;
    }

    public NamespacedKey getChavePilar() {
        return chavePilar;
    }

    /** Id do chefe gravado na entidade, ou null. */
    public String getIdDoBoss(Entity entity) {
        return entity.getPersistentDataContainer().get(chaveBoss, PersistentDataType.STRING);
    }

    /** Id do chefe dono do pilar, ou null. */
    public String getDonoPilar(Entity entity) {
        return entity.getPersistentDataContainer().get(chavePilar, PersistentDataType.STRING);
    }

    /** Entidade ativa do chefe, ou null. */
    public LivingEntity getEntidade(String id) {
        LivingEntity entity = entidades.get(id.toLowerCase(Locale.ROOT));
        if (entity != null && entity.isDead()) {
            limpar(id.toLowerCase(Locale.ROOT));
            return null;
        }
        return entity;
    }

    /** Quantidade de pilares ainda de pe. */
    public int pilaresRestantes(String id) {
        return pilaresVivos.getOrDefault(id.toLowerCase(Locale.ROOT), Set.of()).size();
    }

    /** Cria o chefe e os pilares ao redor da posicao informada. */
    public boolean criar(String id, Location local) {
        BossDefinition def = porId(id);
        if (def == null) {
            return false;
        }
        String chave = def.getId().toLowerCase(Locale.ROOT);
        if (getEntidade(chave) != null) {
            return false;
        }
        EntityType tipo;
        try {
            tipo = EntityType.valueOf(def.getEntidade().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Chefe '" + def.getId() + "': entidade '"
                    + def.getEntidade() + "' invalida; abortado.");
            return false;
        }
        World mundo = local.getWorld();
        if (mundo == null) {
            return false;
        }
        Entity entidade = mundo.spawnEntity(local, tipo);
        if (!(entidade instanceof LivingEntity vivo)) {
            entidade.remove();
            return false;
        }
        AttributeInstance attr = vivo.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(def.getVida());
        }
        vivo.setHealth(def.getVida());
        vivo.setCustomName(formatarNome(def.getNome()));
        vivo.setCustomNameVisible(true);
        vivo.setGlowing(true);
        vivo.getPersistentDataContainer().set(chaveBoss, PersistentDataType.STRING, chave);

        Set<UUID> pilares = new HashSet<>();
        double raioPilares = 5.0;
        for (int i = 0; i < def.getPilares(); i++) {
            double angulo = (2 * Math.PI / Math.max(1, def.getPilares())) * i;
            Location posPilar = local.clone().add(
                    Math.cos(angulo) * raioPilares, 0, Math.sin(angulo) * raioPilares);
            Entity pilar = mundo.spawnEntity(posPilar, EntityType.SHULKER);
            if (pilar instanceof LivingEntity pilarVivo) {
                pilarVivo.setCustomName("Pilar do Dominio");
                pilarVivo.setCustomNameVisible(true);
                pilarVivo.setGlowing(true);
                pilarVivo.getPersistentDataContainer().set(
                        chavePilar, PersistentDataType.STRING, chave);
                pilares.add(pilarVivo.getUniqueId());
            } else {
                pilar.remove();
            }
        }
        pilaresVivos.put(chave, pilares);
        entidades.put(chave, vivo);
        iniciarTarefaDominio(def);
        return true;
    }

    /** Remove chefe, pilares e tarefa de dominio. */
    public void remover(String id) {
        String chave = id.toLowerCase(Locale.ROOT);
        LivingEntity entity = entidades.remove(chave);
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
        BukkitRunnable tarefa = tarefas.remove(chave);
        if (tarefa != null) {
            tarefa.cancel();
        }
        for (UUID uuid : pilaresVivos.getOrDefault(chave, Set.of())) {
            Entity pilar = plugin.getServer().getEntity(uuid);
            if (pilar != null && !pilar.isDead()) {
                pilar.remove();
            }
        }
        pilaresVivos.remove(chave);
    }

    /** Chamado pelo listener quando um pilar morre. */
    public void pilarDestruido(String bossId, UUID pilarUuid) {
        String chave = bossId.toLowerCase(Locale.ROOT);
        Set<UUID> pilares = pilaresVivos.get(chave);
        if (pilares != null) {
            pilares.remove(pilarUuid);
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            int restantes = pilaresRestantes(chave);
            if (restantes > 0) {
                player.sendMessage(messages.message("comando.boss.pilar-destruido",
                        "{restantes}", String.valueOf(restantes)));
            } else {
                player.sendMessage(messages.prefixed("comando.boss.dominio-cedeu"));
            }
        }
    }

    /** Chamado pelo listener quando o chefe morre. */
    public void bossDerrotado(String bossId) {
        limpar(bossId.toLowerCase(Locale.ROOT));
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(messages.prefixed("comando.boss.derrotado"));
        }
    }

    /** True se o chefe ainda estiver protegido por pilares. */
    public boolean dominioAtivo(String bossId) {
        return pilaresRestantes(bossId) > 0;
    }

    private void limpar(String chave) {
        LivingEntity entity = entidades.remove(chave);
        BukkitRunnable tarefa = tarefas.remove(chave);
        if (tarefa != null) {
            tarefa.cancel();
        }
        for (UUID uuid : pilaresVivos.getOrDefault(chave, Set.of())) {
            Entity pilar = plugin.getServer().getEntity(uuid);
            if (pilar != null && !pilar.isDead()) {
                pilar.remove();
            }
        }
        pilaresVivos.remove(chave);
    }

    private void iniciarTarefaDominio(BossDefinition def) {
        String chave = def.getId().toLowerCase(Locale.ROOT);
        BukkitRunnable tarefa = new BukkitRunnable() {
            @Override
            public void run() {
                LivingEntity boss = entidades.get(chave);
                if (boss == null || boss.isDead()) {
                    cancel();
                    return;
                }
                if (!dominioAtivo(chave)) {
                    return;
                }
                for (Player player : boss.getWorld().getPlayers()) {
                    if (player.getLocation().distanceSquared(boss.getLocation())
                            > def.getDominioRaio() * def.getDominioRaio()) {
                        continue;
                    }
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOWNESS, 60, def.getLentidaoAmplificador()));
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.WEAKNESS, 60, def.getFraquezaAmplificador()));
                }
            }
        };
        tarefa.runTaskTimer(plugin, 40L, 40L);
        tarefas.put(chave, tarefa);
    }

    private String formatarNome(String nome) {
        return nome.replace('&', '\u00A7');
    }

    /** Para todas as tarefas ativas (usado no onDisable). */
    public void shutdown() {
        List<String> chaves = new ArrayList<>(tarefas.keySet());
        for (String chave : chaves) {
            LivingEntity entity = entidades.get(chave);
            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
            BukkitRunnable tarefa = tarefas.remove(chave);
            if (tarefa != null) {
                tarefa.cancel();
            }
        }
        entidades.clear();
        pilaresVivos.clear();
    }

    /** Posicao do chefe para teleporte, ou null. */
    public Location posicaoAtual(String id) {
        LivingEntity entity = getEntidade(id);
        return entity != null ? entity.getLocation() : null;
    }

    /** Nomes de exibicao sem codigos de cor, para mensagens. */
    public String nomeLimpo(String id) {
        BossDefinition def = porId(id);
        if (def == null) {
            return id;
        }
        return def.getNome().replaceAll("(?i)&[0-9a-fk-or]", "");
    }
}
