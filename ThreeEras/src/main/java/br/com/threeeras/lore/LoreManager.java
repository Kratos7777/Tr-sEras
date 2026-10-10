package br.com.threeeras.lore;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.era.EraManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Carrega as entradas de lore de lore.yml (copia do resource na primeira
 * execucao) e valida suas referencias.
 *
 * <p>Validacoes na carga: era inexistente, camada fora de 1..5, status
 * desconhecido e referencia de contradicao inexistente — todos geram
 * aviso no console, mas nao impedem o carregamento das demais entradas.</p>
 */
public final class LoreManager {

    private static final String FILE_NAME = "lore.yml";
    private static final Set<Integer> CAMADAS_VALIDAS = Set.of(1, 2, 3, 4, 5);
    private static final Set<String> STATUS_VALIDOS = Set.of("canon", "diretriz", "proposta", "aberto");

    private final ThreeErasPlugin plugin;
    private final EraManager eraManager;
    private final Map<String, LoreEntry> entradas = new LinkedHashMap<>();

    public LoreManager(ThreeErasPlugin plugin, EraManager eraManager) {
        this.plugin = plugin;
        this.eraManager = eraManager;
    }

    /** (Re)carrega lore.yml do disco. */
    public void load() {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, LoreEntry> carregadas = new LinkedHashMap<>();
        ConfigurationSection secao = config.getConfigurationSection("entradas");
        if (secao == null) {
            plugin.getLogger().warning("Nenhuma entrada de lore em " + FILE_NAME
                    + " (secao 'entradas' ausente ou vazia).");
        } else {
            for (String id : secao.getKeys(false)) {
                String base = "entradas." + id;
                String era = config.getString(base + ".era", "");
                int camada = config.getInt(base + ".camada", 1);
                String tipo = config.getString(base + ".tipo", "indefinido");
                String titulo = config.getString(base + ".titulo", id);
                String texto = config.getString(base + ".texto", "");
                String status = config.getString(base + ".status", "proposta").toLowerCase(Locale.ROOT);
                String contradiz = config.getString(base + ".contradiz", "");

                if (!eraManager.existe(era)) {
                    plugin.getLogger().warning("Lore '" + id + "': era '" + era
                            + "' nao existe em eras.yml.");
                }
                if (!CAMADAS_VALIDAS.contains(camada)) {
                    plugin.getLogger().warning("Lore '" + id + "': camada " + camada
                            + " fora de 1..5; usando 1.");
                    camada = 1;
                }
                if (!STATUS_VALIDOS.contains(status)) {
                    plugin.getLogger().warning("Lore '" + id + "': status '" + status
                            + "' desconhecido; usando 'proposta'.");
                    status = "proposta";
                }
                carregadas.put(id.toLowerCase(Locale.ROOT),
                        new LoreEntry(id, era, camada, tipo, titulo, texto, status, contradiz));
            }
        }

        // Segunda passada: valida as referencias de contradicao.
        for (LoreEntry entry : carregadas.values()) {
            if (entry.temContradiz() && !carregadas.containsKey(entry.getContradiz().toLowerCase(Locale.ROOT))) {
                plugin.getLogger().warning("Lore '" + entry.getId() + "': contradiz '"
                        + entry.getContradiz() + "' que nao existe.");
            }
        }

        entradas.clear();
        entradas.putAll(carregadas);
        plugin.getLogger().info("Entradas de lore carregadas (" + entradas.size() + ").");
    }

    /** Todas as entradas, na ordem do arquivo. */
    public List<LoreEntry> listar() {
        return List.copyOf(entradas.values());
    }

    /** Entradas de uma era especifica. */
    public List<LoreEntry> listarPorEra(String era) {
        return entradas.values().stream()
                .filter(e -> e.getEra().equalsIgnoreCase(era))
                .toList();
    }

    /** Entradas de uma camada (1..5). */
    public List<LoreEntry> listarPorCamada(int camada) {
        return entradas.values().stream()
                .filter(e -> e.getCamada() == camada)
                .toList();
    }

    /** Entradas com um status especifico (canon/diretriz/proposta/aberto). */
    public List<LoreEntry> listarPorStatus(String status) {
        return entradas.values().stream()
                .filter(e -> e.getStatus().equalsIgnoreCase(status))
                .toList();
    }

    public LoreEntry porId(String id) {
        if (id == null) {
            return null;
        }
        return entradas.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean existe(String id) {
        return porId(id) != null;
    }

    public int total() {
        return entradas.size();
    }

    /** Ids existentes (para validacao externa). */
    public Collection<String> getIds() {
        return entradas.keySet();
    }
}
