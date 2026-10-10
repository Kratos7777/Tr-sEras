package br.com.threeeras.boss;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.boss.comportamento.Absorcao;
import br.com.threeeras.boss.comportamento.Consumo;
import br.com.threeeras.boss.comportamento.DominioPilares;
import br.com.threeeras.boss.comportamento.Escalada;
import br.com.threeeras.boss.comportamento.Estase;
import br.com.threeeras.boss.comportamento.Percepcao;
import br.com.threeeras.boss.comportamento.Reflexo;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Gerencia os chefes: definicoes (bosses.yml), spawn, comportamentos
 * (regras anomalas), pilares, copias e limpeza.
 *
 * <p>Registro em memoria: entidade do chefe, pilares vivos, copias vivas,
 * comportamento e tarefa de tick. Tudo na thread principal.</p>
 */
public final class BossManager {

    private static final String FILE_NAME = "bosses.yml";
    private static final long INTERVALO_TICKS = 40L;

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final File file;
    private final NamespacedKey chaveBoss;
    private final NamespacedKey chavePilar;
    private final NamespacedKey chaveCopia;
    private final Random random = new Random();

    private final Map<String, BossDefinition> definicoes = new LinkedHashMap<>();
    private final Map<String, ComportamentoBoss> comportamentos = new HashMap<>();
    private final Map<String, LivingEntity> entidades = new HashMap<>();
    private final Map<String, Set<UUID>> pilaresVivos = new HashMap<>();
    private final Map<String, Set<UUID>> copiasVivas = new HashMap<>();
    private final Map<String, BukkitRunnable> tarefas = new HashMap<>();
    private final Map<String, Long> ticks = new HashMap<>();

    public BossManager(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        this.chaveBoss = new NamespacedKey(plugin, "boss_id");
        this.chavePilar = new NamespacedKey(plugin, "pilar_boss");
        this.chaveCopia = new NamespacedKey(plugin, "copia_boss");
    }

    /** (Re)carrega bosses.yml e remonta os comportamentos. */
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
        comportamentos.clear();
        for (String id : definicoes.keySet()) {
            ComportamentoBoss comportamento = comportamentoPara(id);
            if (comportamento != null) {
                comportamentos.put(id, comportamento);
            }
        }
        plugin.getLogger().info("Chefes carregados (" + definicoes.size()
                + "); comportamentos: " + comportamentos.size() + ".");
    }

    /** Mapeia cada chefe a sua regra anomala. */
    private ComportamentoBoss comportamentoPara(String id) {
        return switch (id.toLowerCase(Locale.ROOT)) {
            case "soberba" -> new DominioPilares();
            case "avareza" -> new Absorcao();
            case "ira" -> new Escalada();
            case "preguica" -> new Estase();
            case "inveja" -> new Reflexo();
            case "gula" -> new Consumo();
            case "luxuria" -> new Percepcao();
            default -> null;
        };
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

    public ComportamentoBoss getComportamento(String id) {
        return comportamentos.get(id.toLowerCase(Locale.ROOT));
    }

    public NamespacedKey getChaveBoss() {
        return chaveBoss;
    }

    /** Id do chefe gravado na entidade, ou null. */
    public String getIdDoBoss(Entity entity) {
        return entity.getPersistentDataContainer().get(chaveBoss, PersistentDataType.STRING);
    }

    /** Id do chefe dono do pilar, ou null. */
    public String getDonoPilar(Entity entity) {
        return entity.getPersistentDataContainer().get(chavePilar, PersistentDataType.STRING);
    }

    /** Id do chefe dono da copia, ou null. */
    public String getDonoCopia(Entity entity) {
        return entity.getPersistentDataContainer().get(chaveCopia, PersistentDataType.STRING);
    }

    /** Entidade ativa do chefe, ou null. */
    public LivingEntity getEntidade(String id) {
        String chave = id.toLowerCase(Locale.ROOT);
        LivingEntity entity = entidades.get(chave);
        if (entity != null && entity.isDead()) {
            limpar(chave);
            return null;
        }
        return entity;
    }

    /** Quantidade de pilares ainda de pe. */
    public int pilaresRestantes(String id) {
        return pilaresVivos.getOrDefault(id.toLowerCase(Locale.ROOT), Set.of()).size();
    }

    /** Quantidade de copias vivas. */
    public int contarCopias(String id) {
        return copiasVivas.getOrDefault(id.toLowerCase(Locale.ROOT), Set.of()).size();
    }

    /** Cria o chefe (e pilares, quando houver) ao redor da posicao. */
    public boolean criar(String id, Location local) {
        BossDefinition def = porId(id);
        if (def == null) {
            return false;
        }
        String chave = def.getId().toLowerCase(Locale.ROOT);
        if (getEntidade(chave) != null) {
            return false;
        }
        World mundo = local.getWorld();
        if (mundo == null) {
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
                pilarVivo.getPersistentDataContainer().set(chavePilar, PersistentDataType.STRING, chave);
                pilares.add(pilarVivo.getUniqueId());
            } else {
                pilar.remove();
            }
        }
        pilaresVivos.put(chave, pilares);
        copiasVivas.put(chave, new HashSet<>());
        entidades.put(chave, vivo);
        ticks.put(chave, 0L);

        ComportamentoBoss comportamento = comportamentos.get(chave);
        if (comportamento != null) {
            comportamento.aoNascer(contexto(chave, vivo, 0L));
        }
        iniciarTarefa(chave, def);
        return true;
    }

    /** Cria uma copia (manifestacao falsa) proxima ao chefe. */
    public boolean criarCopia(String id) {
        String chave = id.toLowerCase(Locale.ROOT);
        LivingEntity boss = getEntidade(chave);
        BossDefinition def = porId(chave);
        if (boss == null || def == null) {
            return false;
        }
        EntityType tipo;
        try {
            tipo = EntityType.valueOf(def.getEntidade().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return false;
        }
        Location pos = boss.getLocation().clone().add(
                (random.nextDouble() - 0.5) * 6.0, 0, (random.nextDouble() - 0.5) * 6.0);
        Entity entidade = pos.getWorld().spawnEntity(pos, tipo);
        if (!(entidade instanceof LivingEntity copia)) {
            entidade.remove();
            return false;
        }
        AttributeInstance attr = copia.getAttribute(Attribute.MAX_HEALTH);
        double vida = Math.max(10.0, def.getVida() * 0.2);
        if (attr != null) {
            attr.setBaseValue(vida);
        }
        copia.setHealth(vida);
        copia.setCustomName(formatarNome(def.getNome()));
        copia.setCustomNameVisible(true);
        copia.setGlowing(true);
        copia.getPersistentDataContainer().set(chaveCopia, PersistentDataType.STRING, chave);
        copiasVivas.computeIfAbsent(chave, k -> new HashSet<>()).add(copia.getUniqueId());
        return true;
    }

    /** Remove a copia do registro (chamado quando ela morre/desfeita). */
    public void copiaRemovida(String bossId, UUID copiaUuid) {
        Set<UUID> copias = copiasVivas.get(bossId.toLowerCase(Locale.ROOT));
        if (copias != null) {
            copias.remove(copiaUuid);
        }
    }

    /** Remove chefe, pilares, copias e tarefa. */
    public void remover(String id) {
        limpar(id.toLowerCase(Locale.ROOT));
    }

    /** Chamado pelo listener quando um pilar morre. */
    public void pilarDestruido(String bossId, UUID pilarUuid) {
        String chave = bossId.toLowerCase(Locale.ROOT);
        Set<UUID> pilares = pilaresVivos.get(chave);
        if (pilares != null) {
            pilares.remove(pilarUuid);
        }
        int restantes = pilaresRestantes(chave);
        for (var player : plugin.getServer().getOnlinePlayers()) {
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
        String chave = bossId.toLowerCase(Locale.ROOT);
        String nome = nomeLimpo(chave);
        limpar(chave);
        for (var player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(messages.message("comando.boss.derrotado", "{nome}", nome));
        }
    }

    /** True se o chefe ainda estiver protegido por pilares. */
    public boolean dominioAtivo(String bossId) {
        return pilaresRestantes(bossId) > 0;
    }

    /** Delega o evento de dano ao comportamento do chefe. */
    public void aoReceberDano(String bossId, EntityDamageByEntityEvent event) {
        String chave = bossId.toLowerCase(Locale.ROOT);
        LivingEntity boss = entidades.get(chave);
        ComportamentoBoss comportamento = comportamentos.get(chave);
        if (boss == null || comportamento == null) {
            return;
        }
        comportamento.aoReceberDano(contexto(chave, boss, 0L), event);
    }

    private void limpar(String chave) {
        LivingEntity entity = entidades.remove(chave);
        BukkitRunnable tarefa = tarefas.remove(chave);
        if (tarefa != null) {
            tarefa.cancel();
        }
        ticks.remove(chave);
        for (UUID uuid : pilaresVivos.getOrDefault(chave, Set.of())) {
            Entity pilar = plugin.getServer().getEntity(uuid);
            if (pilar != null && !pilar.isDead()) {
                pilar.remove();
            }
        }
        for (UUID uuid : copiasVivas.getOrDefault(chave, Set.of())) {
            Entity copia = plugin.getServer().getEntity(uuid);
            if (copia != null && !copia.isDead()) {
                copia.remove();
            }
        }
        pilaresVivos.remove(chave);
        copiasVivas.remove(chave);
    }

    private BossContexto contexto(String chave, LivingEntity boss, long tick) {
        BossDefinition def = porId(chave);
        return new BossContexto(plugin, messages, def, boss, this, tick);
    }

    private void iniciarTarefa(String chave, BossDefinition def) {
        BukkitRunnable tarefa = new BukkitRunnable() {
            @Override
            public void run() {
                LivingEntity boss = entidades.get(chave);
                if (boss == null || boss.isDead()) {
                    cancel();
                    return;
                }
                long tick = ticks.merge(chave, 1L, Long::sum) - 1;
                ComportamentoBoss comportamento = comportamentos.get(chave);
                if (comportamento != null) {
                    try {
                        comportamento.executar(contexto(chave, boss, tick));
                    } catch (RuntimeException e) {
                        plugin.getLogger().severe("Erro no comportamento do chefe '" + chave
                                + "': " + e.getMessage());
                    }
                }
            }
        };
        tarefa.runTaskTimer(plugin, INTERVALO_TICKS, INTERVALO_TICKS);
        tarefas.put(chave, tarefa);
    }

    private String formatarNome(String nome) {
        return nome.replace('&', '\u00A7');
    }

    /** Para todas as tarefas e remove entidades (onDisable). */
    public void shutdown() {
        List<String> chaves = new ArrayList<>(entidades.keySet());
        chaves.addAll(new ArrayList<>(tarefas.keySet()));
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
        copiasVivas.clear();
        ticks.clear();
    }

    /** Posicao do chefe para teleporte, ou null. */
    public Location posicaoAtual(String id) {
        LivingEntity entity = getEntidade(id);
        return entity != null ? entity.getLocation() : null;
    }

    /** Nome de exibicao sem codigos de cor, para mensagens. */
    public String nomeLimpo(String id) {
        BossDefinition def = porId(id);
        if (def == null) {
            return id;
        }
        return def.getNome().replaceAll("(?i)&[0-9a-fk-or]", "");
    }
}
