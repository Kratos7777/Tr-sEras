package br.com.threeeras.player;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.storage.StorageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cache em memoria dos dados dos jogadores + ponte com o StorageManager.
 *
 * <p>Formato no dados.yml:</p>
 * <pre>
 * jogadores:
 *   &lt;uuid&gt;:
 *     nome: ...
 *     primeiro-join: 0
 *     ultimo-visto: 0
 *     era: medieval
 *     tempo-jogado: 0
 *     descobertas: [id-de-lore, ...]
 *     quests:
 *       <quest-id>:
 *         passo: 0
 *     quests-concluidas: [id-de-missao, ...]
 * </pre>
 *
 * <p>So e chamado na thread principal (eventos e comandos). Toda gravacao
 * em disco passa pelo StorageManager, que e assincrono.</p>
 */
public final class PlayerManager implements Listener {

    private static final String BASE = "jogadores";

    private final ThreeErasPlugin plugin;
    private final StorageManager storage;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public PlayerManager(ThreeErasPlugin plugin, StorageManager storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    /** Era inicial configurada; lida sob demanda para refletir /threeeras reload. */
    private String getEraInicial() {
        return plugin.getConfig().getString("jogadores.era-inicial", "medieval");
    }

    /** Registra os ouvintes de eventos deste manager. */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** Dados do jogador online; cria no primeiro acesso. */
    public PlayerData get(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), uuid -> loadOrCreate(uuid, player.getName()));
    }

    private PlayerData loadOrCreate(UUID uuid, String nome) {
        String base = BASE + "." + uuid;
        if (storage.contains(base)) {
            long agora = System.currentTimeMillis();
            Map<String, Integer> passos = new HashMap<>();
            for (String questId : storage.getKeys(base + ".quests", false)) {
                passos.put(questId, storage.getInt(base + ".quests." + questId + ".passo", 0));
            }
            String rota = storage.getString(base + ".rota", "");
            return new PlayerData(
                    uuid,
                    storage.getString(base + ".nome", nome),
                    storage.getLong(base + ".primeiro-join", agora),
                    storage.getLong(base + ".ultimo-visto", agora),
                    storage.getString(base + ".era", getEraInicial()),
                    storage.getLong(base + ".tempo-jogado", 0L),
                    storage.getStringList(base + ".descobertas"),
                    passos,
                    storage.getStringList(base + ".quests-concluidas"),
                    rota.isBlank() ? null : rota,
                    storage.getBoolean(base + ".rota-concluida", false));
        }
        PlayerData novo = PlayerData.novo(uuid, nome, System.currentTimeMillis(), getEraInicial());
        save(novo);
        return novo;
    }

    /**
     * Procura por nome: primeiro online, depois no cache, depois nos dados
     * do disco. Nao faz consulta a API da Mojang (nao bloqueia a thread).
     */
    public PlayerData findByName(String nome) {
        Player online = plugin.getServer().getPlayerExact(nome);
        if (online != null) {
            return get(online);
        }
        for (PlayerData data : cache.values()) {
            if (data.getNome().equalsIgnoreCase(nome)) {
                return data;
            }
        }
        for (String uuidTexto : storage.getKeys(BASE, false)) {
            String nomeSalvo = storage.getString(BASE + "." + uuidTexto + ".nome", "");
            if (!nomeSalvo.equalsIgnoreCase(nome)) {
                continue;
            }
            try {
                return loadOrCreate(UUID.fromString(uuidTexto), nomeSalvo);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("UUID invalido em dados.yml (" + BASE + "." + uuidTexto + ").");
            }
        }
        return null;
    }

    /** Escreve os dados do jogador na memoria do StorageManager e dispara save async. */
    public void save(PlayerData data) {
        gravar(data);
        storage.saveAsync();
    }

    /** Escreve apenas na memoria (usado no desligamento). */
    public void flushToStorage(PlayerData data) {
        gravar(data);
    }

    /** Desligamento: escreve todo o cache na memoria do storage, sem I/O. */
    public void flushCacheToStorage() {
        for (PlayerData data : cache.values()) {
            gravar(data);
        }
    }

    private void gravar(PlayerData data) {
        String base = BASE + "." + data.getUuid();
        storage.set(base + ".nome", data.getNome());
        storage.set(base + ".primeiro-join", data.getPrimeiroJoin());
        storage.set(base + ".ultimo-visto", data.getUltimoVisto());
        storage.set(base + ".era", data.getEraAtual());
        storage.set(base + ".tempo-jogado", data.getTempoJogadoSegundos());
        storage.set(base + ".descobertas", new ArrayList<>(data.getDescobertas()));
        for (Map.Entry<String, Integer> passo : data.getPassosQuests().entrySet()) {
            storage.set(base + ".quests." + passo.getKey() + ".passo", passo.getValue());
        }
        storage.set(base + ".quests-concluidas", new ArrayList<>(data.getQuestsConcluidas()));
        if (data.getRotaAtual() != null) {
            storage.set(base + ".rota", data.getRotaAtual());
        }
        storage.set(base + ".rota-concluida", data.isRotaConcluida());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = get(player);
        long agora = System.currentTimeMillis();
        data.setNome(player.getName());
        data.setUltimoVisto(agora);
        if (data.getPrimeiroJoin() <= 0L) {
            data.setPrimeiroJoin(agora);
        }
        save(data);
        if (plugin.getConfig().getBoolean("geral.debug")) {
            plugin.getLogger().info("Dados de " + player.getName()
                    + " carregados (uuid " + data.getUuid() + ", era " + data.getEraAtual() + ").");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = cache.get(player.getUniqueId());
        if (data == null) {
            return;
        }
        long agora = System.currentTimeMillis();
        long sessao = (agora - data.getUltimoVisto()) / 1000L;
        data.adicionarTempo(sessao);
        data.setUltimoVisto(agora);
        save(data);
    }
}
