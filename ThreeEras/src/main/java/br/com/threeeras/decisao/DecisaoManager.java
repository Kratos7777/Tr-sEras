package br.com.threeeras.decisao;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.item.ItemManager;
import br.com.threeeras.lore.LoreManager;
import br.com.threeeras.player.PlayerData;
import br.com.threeeras.player.PlayerManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Decisoes narrativas persistentes (escolhas com consequencia).
 *
 * <p>Regra: uma escolha so pode ser feita uma vez. Os itens exigidos sao
 * consumidos; as consequencias (lore + itens) sao aplicadas na hora e o
 * registro fica salvo no jogador, visivel no diario.</p>
 */
public final class DecisaoManager {

    private static final String FILE_NAME = "decisoes.yml";

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final LoreManager loreManager;
    private final ItemManager itemManager;
    private final PlayerManager playerManager;
    private final File file;
    private final Map<String, DecisaoDefinition> decisoes = new LinkedHashMap<>();

    public DecisaoManager(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.loreManager = plugin.getLoreManager();
        this.itemManager = plugin.getItemManager();
        this.playerManager = plugin.getPlayerManager();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
    }

    /** (Re)carrega decisoes.yml do disco. */
    public void load() {
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, DecisaoDefinition> carregadas = new LinkedHashMap<>();
        ConfigurationSection secao = config.getConfigurationSection("decisoes");
        if (secao == null) {
            plugin.getLogger().warning("Nenhuma decisao em " + FILE_NAME + " (secao 'decisoes' ausente).");
        } else {
            for (String id : secao.getKeys(false)) {
                String base = "decisoes." + id;
                List<OpcaoDecisao> opcoes = new ArrayList<>();
                ConfigurationSection secaoOpcoes = config.getConfigurationSection(base + ".opcoes");
                if (secaoOpcoes != null) {
                    for (String idOpcao : secaoOpcoes.getKeys(false)) {
                        String baseOpcao = base + ".opcoes." + idOpcao;
                        opcoes.add(new OpcaoDecisao(
                                idOpcao,
                                config.getString(baseOpcao + ".texto", idOpcao),
                                config.getStringList(baseOpcao + ".exige"),
                                config.getStringList(baseOpcao + ".concede-lore"),
                                config.getStringList(baseOpcao + ".concede-itens")));
                    }
                }
                carregadas.put(id.toLowerCase(Locale.ROOT), new DecisaoDefinition(
                        id,
                        config.getString(base + ".nome", id),
                        config.getString(base + ".descricao", ""),
                        opcoes));
            }
        }

        for (DecisaoDefinition decisao : carregadas.values()) {
            for (OpcaoDecisao opcao : decisao.getOpcoes()) {
                for (String spec : opcao.getExige()) {
                    if (itemManager.porId(spec.split(":")[0]) == null) {
                        plugin.getLogger().warning("Decisao '" + decisao.getId() + "/" + opcao.getId()
                                + "': item exigido '" + spec + "' nao existe em itens.yml.");
                    }
                }
            }
        }

        decisoes.clear();
        decisoes.putAll(carregadas);
        plugin.getLogger().info("Decisoes carregadas (" + decisoes.size() + ").");
    }

    public DecisaoDefinition porId(String id) {
        if (id == null) {
            return null;
        }
        return decisoes.get(id.toLowerCase(Locale.ROOT));
    }

    public List<DecisaoDefinition> listar() {
        return List.copyOf(decisoes.values());
    }

    public int total() {
        return decisoes.size();
    }

    /** Resultado de uma tentativa de escolha. */
    public enum Resultado {
        OK,
        NAO_ENCONTRADA,
        OPCAO_INVALIDA,
        JA_ESCOLHIDA,
        FALTAM_ITENS
    }
    public Resultado escolher(Player player, String id, String idOpcao) {
        DecisaoDefinition decisao = porId(id);
        if (decisao == null) {
            return Resultado.NAO_ENCONTRADA;
        }
        OpcaoDecisao opcao = decisao.getOpcao(idOpcao);
        if (opcao == null) {
            return Resultado.OPCAO_INVALIDA;
        }
        PlayerData data = playerManager.get(player);
        if (data.getDecisoes().containsKey(decisao.getId())) {
            return Resultado.JA_ESCOLHIDA;
        }
        if (!possuiItens(player, opcao)) {
            return Resultado.FALTAM_ITENS;
        }
        removerItens(player, opcao);

        // Consequencias: lore (registro) + itens.
        for (String loreId : opcao.getConcedeLore()) {
            if (loreManager.existe(loreId)) {
                data.addDescoberta(loreId);
            }
        }
        List<ItemStack> concedidos = new ArrayList<>();
        for (String spec : opcao.getConcedeItens()) {
            String[] partes = spec.split(":");
            int qtd = partes.length > 1 ? inteiro(partes[1], 1) : 1;
            ItemStack stack = itemManager.criar(partes[0], qtd);
            if (stack != null) {
                concedidos.add(stack);
            }
        }
        if (!concedidos.isEmpty()) {
            for (ItemStack sobra : player.getInventory().addItem(concedidos.toArray(new ItemStack[0])).values()) {
                player.getWorld().dropItem(player.getLocation(), sobra);
            }
        }
        data.registrarDecisao(decisao.getId(), opcao.getId());
        playerManager.save(data);
        return Resultado.OK;
    }

    private boolean possuiItens(Player player, OpcaoDecisao opcao) {
        for (String spec : opcao.getExige()) {
            String[] partes = spec.split(":");
            int qtd = partes.length > 1 ? inteiro(partes[1], 1) : 1;
            int tem = 0;
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack != null && partes[0].equalsIgnoreCase(itemManager.porItem(stack))) {
                    tem += stack.getAmount();
                }
            }
            if (tem < qtd) {
                return false;
            }
        }
        return true;
    }

    private void removerItens(Player player, OpcaoDecisao opcao) {
        for (String spec : opcao.getExige()) {
            String[] partes = spec.split(":");
            int restante = partes.length > 1 ? inteiro(partes[1], 1) : 1;
            ItemStack[] conteudo = player.getInventory().getContents();
            for (int slot = 0; slot < conteudo.length && restante > 0; slot++) {
                ItemStack stack = conteudo[slot];
                if (stack == null || !partes[0].equalsIgnoreCase(itemManager.porItem(stack))) {
                    continue;
                }
                int remover = Math.min(restante, stack.getAmount());
                stack.setAmount(stack.getAmount() - remover);
                restante -= remover;
                if (stack.getAmount() <= 0) {
                    player.getInventory().setItem(slot, null);
                }
            }
        }
    }

    private int inteiro(String texto, int padrao) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }
}
