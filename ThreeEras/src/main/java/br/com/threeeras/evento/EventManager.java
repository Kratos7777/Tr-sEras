package br.com.threeeras.evento;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.item.ItemManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Eventos agendados: avisos narrativos, invasoes e descobertas.
 *
 * <p>Cada evento tem um intervalo aleatorio entre min e max minutos. Uma
 * tarefa verifica a cada 60 segundos quem deve disparar. Eventos sem
 * jogadores online nao disparam (o intervalo e reagendado).</p>
 */
public final class EventManager {

    private static final String FILE_NAME = "eventos.yml";
    private static final long VERIFICACAO_TICKS = 1200L;

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final ItemManager itemManager;
    private final File file;
    private final Random random = new Random();

    private final Map<String, EventoDefinition> eventos = new LinkedHashMap<>();
    private final Map<String, Long> proximoDisparo = new LinkedHashMap<>();
    private BukkitRunnable tarefa;

    public EventManager(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.itemManager = plugin.getItemManager();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
    }

    /** (Re)carrega eventos.yml e reagenda os disparos. */
    public void load() {
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, EventoDefinition> carregados = new LinkedHashMap<>();
        ConfigurationSection secao = config.getConfigurationSection("eventos");
        if (secao == null) {
            plugin.getLogger().warning("Nenhum evento definido em " + FILE_NAME + " (secao 'eventos' ausente).");
        } else {
            for (String id : secao.getKeys(false)) {
                String base = "eventos." + id;
                TipoEvento tipo;
                try {
                    tipo = TipoEvento.valueOf(
                            config.getString(base + ".tipo", "AVISO").toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Evento '" + id + "': tipo desconhecido; ignorado.");
                    continue;
                }
                carregados.put(id.toLowerCase(Locale.ROOT), new EventoDefinition(
                        id,
                        config.getString(base + ".nome", id),
                        config.getString(base + ".descricao", ""),
                        config.getBoolean(base + ".habilitado", true),
                        tipo,
                        config.getInt(base + ".intervalo-min-minutos", 30),
                        config.getInt(base + ".intervalo-max-minutos", 60),
                        config.getInt(base + ".duracao-segundos", 15),
                        config.getString(base + ".efeito", "cegueira"),
                        config.getInt(base + ".efeito-amplificador", 0),
                        config.getString(base + ".entidade", "ZOMBIE"),
                        config.getInt(base + ".quantidade", 3),
                        config.getString(base + ".item", ""),
                        config.getString(base + ".mensagem-inicio", ""),
                        config.getString(base + ".mensagem-fim", "")));
            }
        }

        eventos.clear();
        eventos.putAll(carregados);
        reagendarTodos();
        plugin.getLogger().info("Eventos carregados (" + eventos.size() + ").");
    }

    private void reagendarTodos() {
        long agora = System.currentTimeMillis();
        proximoDisparo.clear();
        for (EventoDefinition evento : eventos.values()) {
            if (evento.isHabilitado()) {
                proximoDisparo.put(evento.getId(), agora + intervaloAleatorio(evento));
            }
        }
    }

    private long intervaloAleatorio(EventoDefinition evento) {
        int min = evento.getIntervaloMinMinutos();
        int max = evento.getIntervaloMaxMinutos();
        int minutos = min + random.nextInt(Math.max(1, max - min + 1));
        return minutos * 60_000L;
    }

    /** Inicia a verificacao periodica. */
    public void start() {
        stop();
        tarefa = new BukkitRunnable() {
            @Override
            public void run() {
                verificar();
            }
        };
        tarefa.runTaskTimer(plugin, VERIFICACAO_TICKS, VERIFICACAO_TICKS);
    }

    public void stop() {
        if (tarefa != null) {
            tarefa.cancel();
            tarefa = null;
        }
    }

    private void verificar() {
        long agora = System.currentTimeMillis();
        for (EventoDefinition evento : eventos.values()) {
            if (!evento.isHabilitado()) {
                continue;
            }
            Long proximo = proximoDisparo.get(evento.getId());
            if (proximo == null) {
                proximoDisparo.put(evento.getId(), agora + intervaloAleatorio(evento));
                continue;
            }
            if (agora >= proximo) {
                disparar(evento);
                proximoDisparo.put(evento.getId(), agora + intervaloAleatorio(evento));
            }
        }
    }

    /** Dispara o evento; false se nao houver jogadores online. */
    public boolean disparar(String id) {
        EventoDefinition evento = porId(id);
        if (evento == null) {
            return false;
        }
        return disparar(evento);
    }

    private boolean disparar(EventoDefinition evento) {
        List<Player> online = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        if (online.isEmpty()) {
            return false;
        }
        Player alvo = online.get(random.nextInt(online.size()));
        switch (evento.getTipo()) {
            case AVISO -> aplicarAviso(evento, online);
            case INVASAO -> aplicarInvasao(evento, alvo);
            case ITEM -> aplicarItem(evento, alvo);
        }
        plugin.getLogger().info("Evento disparado: " + evento.getId() + " (" + evento.getTipo() + ").");
        return true;
    }

    private void aplicarAviso(EventoDefinition evento, List<Player> online) {
        PotionEffectType efeito = resolverEfeito(evento.getEfeito());
        for (Player player : online) {
            if (efeito != null) {
                player.addPotionEffect(new PotionEffect(
                        efeito, evento.getDuracaoSegundos() * 20, evento.getEfeitoAmplificador()));
            }
            if (!evento.getMensagemInicio().isBlank()) {
                player.sendMessage(messages.component(evento.getMensagemInicio()));
            }
        }
    }

    private void aplicarInvasao(EventoDefinition evento, Player alvo) {
        if (!evento.getMensagemInicio().isBlank()) {
            plugin.getServer().broadcast(messages.component(evento.getMensagemInicio()));
        }
        World mundo = alvo.getWorld();
        EntityType tipo;
        try {
            tipo = EntityType.valueOf(evento.getEntidade().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Evento '" + evento.getId()
                    + "': entidade '" + evento.getEntidade() + "' invalida; abortado.");
            return;
        }
        for (int i = 0; i < evento.getQuantidade(); i++) {
            double angulo = random.nextDouble() * 2 * Math.PI;
            double dist = 8 + random.nextDouble() * 8;
            Location pos = alvo.getLocation().clone().add(
                    Math.cos(angulo) * dist, 0, Math.sin(angulo) * dist);
            mundo.spawnEntity(pos, tipo);
        }
    }

    private void aplicarItem(EventoDefinition evento, Player alvo) {
        ItemStack item = itemManager.criar(evento.getItemId(), 1);
        if (item == null) {
            plugin.getLogger().warning("Evento '" + evento.getId()
                    + "': item '" + evento.getItemId() + "' inexistente; abortado.");
            return;
        }
        Location pos = alvo.getLocation().add(random.nextDouble() * 4 - 2, 1, random.nextDouble() * 4 - 2);
        alvo.getWorld().dropItem(pos, item);
        if (!evento.getMensagemInicio().isBlank()) {
            plugin.getServer().broadcast(messages.component(evento.getMensagemInicio()));
        }
    }

    private PotionEffectType resolverEfeito(String nome) {
        return switch (nome.toLowerCase(Locale.ROOT)) {
            case "cegueira" -> PotionEffectType.BLINDNESS;
            case "nausea" -> PotionEffectType.NAUSEA;
            case "lentidao" -> PotionEffectType.SLOWNESS;
            case "fraqueza" -> PotionEffectType.WEAKNESS;
            default -> null;
        };
    }

    public EventoDefinition porId(String id) {
        if (id == null) {
            return null;
        }
        return eventos.get(id.toLowerCase(Locale.ROOT));
    }

    public List<EventoDefinition> listar() {
        return List.copyOf(eventos.values());
    }

    public int total() {
        return eventos.size();
    }

    /** Desliga a tarefa (onDisable). */
    public void shutdown() {
        stop();
    }
}
