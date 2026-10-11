package br.com.threeeras.era;

import br.com.threeeras.ThreeErasPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Carrega as eras definidas em eras.yml (copia do resource na primeira execucao).
 *
 * <p>Definicoes de eras sao configuracao, nao estado de jogador: ficam no
 * arquivo do plugin e sao recarregadas em /threeeras reload.</p>
 */
public final class EraManager {

    private static final String FILE_NAME = "eras.yml";

    private final ThreeErasPlugin plugin;
    private final Map<String, Era> eras = new LinkedHashMap<>();
    private List<Era> ordenadas = List.of();

    public EraManager(ThreeErasPlugin plugin) {
        this.plugin = plugin;
    }

    /** (Re)carrega eras.yml do disco. */
    public void load() {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, Era> carregadas = new LinkedHashMap<>();
        ConfigurationSection secao = config.getConfigurationSection("eras");
        if (secao != null) {
            for (String id : secao.getKeys(false)) {
                String nome = secao.getString(id + ".nome", id);
                String mundo = secao.getString(id + ".mundo", "");
                int ordem = secao.getInt(id + ".ordem", 99);
                String descricao = secao.getString(id + ".descricao", "");
                String requisitoMissao = secao.getString(id + ".requisito-missao", "");
                carregadas.put(id.toLowerCase(Locale.ROOT),
                        new Era(id, nome, mundo, ordem, descricao, requisitoMissao));
            }
        }

        if (carregadas.isEmpty()) {
            plugin.getLogger().warning("Nenhuma era definida em " + FILE_NAME + ".");
        }

        eras.clear();
        eras.putAll(carregadas);
        ordenadas = carregadas.values().stream()
                .sorted(Comparator.comparingInt(Era::getOrdem).thenComparing(Era::getId))
                .toList();

        StringJoiner joiner = new StringJoiner(", ");
        for (Era era : ordenadas) {
            joiner.add(era.getId());
        }
        plugin.getLogger().info("Eras carregadas (" + ordenadas.size() + "): " + joiner + ".");
    }

    /** Eras ordenadas pela progressao narrativa. */
    public List<Era> listar() {
        return ordenadas;
    }

    public Era getPorId(String id) {
        if (id == null) {
            return null;
        }
        return eras.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean existe(String id) {
        return getPorId(id) != null;
    }

    public int total() {
        return ordenadas.size();
    }

    /** Primeira era da progressao (usada como padrao). */
    public Era getPrimeira() {
        return ordenadas.isEmpty() ? null : ordenadas.get(0);
    }

    /** Estado textual do mundo da era, para exibicao. */
    public String getEstadoMundo(Era era) {
        if (!era.temMundo()) {
            return "nao configurado";
        }
        return plugin.getServer().getWorld(era.getMundo()) != null ? "carregado" : "nao carregado";
    }

    /** Nome do mundo para exibicao ("-" quando nao configurado). */
    public String getMundoExibicao(Era era) {
        return era.temMundo() ? era.getMundo() : "-";
    }

    /** Localiza uma posicao segura perto do spawn; nunca cria nem descarrega mundos. */
    public Location getSpawnSeguro(World mundo) {
        Location origem = mundo.getSpawnLocation();
        for (int raio = 0; raio <= 8; raio++) {
            for (int dx = -raio; dx <= raio; dx++) {
                for (int dz = -raio; dz <= raio; dz++) {
                    if (raio > 0 && Math.abs(dx) != raio && Math.abs(dz) != raio) {
                        continue;
                    }
                    int x = origem.getBlockX() + dx;
                    int z = origem.getBlockZ() + dz;
                    int y = mundo.getHighestBlockYAt(x, z) + 1;
                    if (y + 1 >= mundo.getMaxHeight() || y <= mundo.getMinHeight()) {
                        continue;
                    }
                    var piso = mundo.getBlockAt(x, y - 1, z);
                    var pes = mundo.getBlockAt(x, y, z);
                    var cabeca = mundo.getBlockAt(x, y + 1, z);
                    Material materialPiso = piso.getType();
                    boolean perigoso = materialPiso == Material.MAGMA_BLOCK
                            || materialPiso == Material.CACTUS
                            || materialPiso == Material.CAMPFIRE
                            || materialPiso == Material.SOUL_CAMPFIRE
                            || materialPiso == Material.FIRE
                            || materialPiso == Material.SOUL_FIRE
                            || materialPiso == Material.POWDER_SNOW
                            || materialPiso == Material.SWEET_BERRY_BUSH;
                    if (materialPiso.isSolid() && !perigoso && !piso.isLiquid()
                            && pes.isPassable() && cabeca.isPassable()
                            && !pes.isLiquid() && !cabeca.isLiquid()) {
                        return new Location(mundo, x + 0.5, y, z + 0.5,
                                origem.getYaw(), origem.getPitch());
                    }
                }
            }
        }
        return null;
    }
}
