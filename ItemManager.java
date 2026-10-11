package br.com.threeeras.item;

import br.com.threeeras.ThreeErasPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Registro de itens personalizados (itens.yml) e construcao dos ItemStacks.
 *
 * <p>Identificacao por PersistentDataContainer: itens do ThreeEras sempre
 * carregam a chave {@code threeeras:item_id}, o que permite reconhece-los
 * mesmo depois de renomeacoes ou copias.</p>
 */
public final class ItemManager {

    private static final String FILE_NAME = "itens.yml";
    private static final char COR_LEGADO = '\u00A7';

    private final ThreeErasPlugin plugin;
    private final File file;
    private final NamespacedKey chaveId;
    private final Map<String, ItemDefinition> itens = new LinkedHashMap<>();

    public ItemManager(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        this.chaveId = new NamespacedKey(plugin, "item_id");
    }

    /** (Re)carrega itens.yml do disco (copia do resource na primeira execucao). */
    public void load() {
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, ItemDefinition> carregados = new LinkedHashMap<>();
        ConfigurationSection secao = config.getConfigurationSection("itens");
        if (secao == null) {
            plugin.getLogger().warning("Nenhum item definido em " + FILE_NAME + " (secao 'itens' ausente).");
        } else {
            for (String id : secao.getKeys(false)) {
                String base = "itens." + id;
                String material = config.getString(base + ".material", "PAPER");
                if (Material.matchMaterial(material) == null) {
                    plugin.getLogger().warning("Item '" + id + "': material '" + material
                            + "' invalido; item ignorado.");
                    continue;
                }
                List<String> lore = config.getStringList(base + ".lore");
                List<String> paginas = config.getStringList(base + ".paginas");
                carregados.put(id.toLowerCase(Locale.ROOT), new ItemDefinition(
                        id,
                        config.getString(base + ".era", ""),
                        material,
                        config.getString(base + ".nome", id),
                        Math.max(1, config.getInt(base + ".quantidade", 1)),
                        lore,
                        paginas));
            }
        }

        itens.clear();
        itens.putAll(carregados);
        plugin.getLogger().info("Itens carregados (" + itens.size() + ").");
    }

    public ItemDefinition porId(String id) {
        if (id == null) {
            return null;
        }
        return itens.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean existe(String id) {
        return porId(id) != null;
    }

    public List<ItemDefinition> listar() {
        return List.copyOf(itens.values());
    }

    public int total() {
        return itens.size();
    }

    /** Id do item do ThreeEras no ItemStack, ou null. */
    public String porItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(chaveId, PersistentDataType.STRING);
    }

    /** Cria o ItemStack do item (com nome, lore e marcador). */
    public ItemStack criar(String id, int quantidade) {
        ItemDefinition def = porId(id);
        if (def == null) {
            return null;
        }
        Material material = Material.matchMaterial(def.getMaterial());
        if (material == null) {
            plugin.getLogger().warning("Item '" + def.getId() + "' com material invalido; nao criado.");
            return null;
        }
        ItemStack stack = new ItemStack(material, Math.max(1, quantidade));
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorir(def.getNome()));
            if (!def.getLore().isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String linha : def.getLore()) {
                    lore.add(colorir(linha));
                }
                meta.setLore(lore);
            }
            if (!def.getPaginas().isEmpty() && meta instanceof BookMeta livro) {
                livro.setTitle(def.getNome());
                livro.setAuthor("ThreeEras");
                List<String> paginas = new ArrayList<>();
                for (String pagina : def.getPaginas()) {
                    paginas.add(colorir(pagina));
                }
                livro.setPages(paginas);
            }
            meta.getPersistentDataContainer().set(
                    chaveId, PersistentDataType.STRING, def.getId().toLowerCase(Locale.ROOT));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /** Converte codigos & para o caractere de cor legado dos itens. */
    static String colorir(String texto) {
        return texto == null ? "" : texto.replace('&', COR_LEGADO);
    }
}
