package br.com.threeeras.quest;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.item.ItemDefinition;
import br.com.threeeras.item.ItemManager;
import br.com.threeeras.lore.LoreEntry;
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
 * Missoes encadeadas: descobertas de lore destravam passos, itens podem ser
 * entregues (consumidos) e a conclusao paga recompensas em itens.
 *
 * <p>Progresso por jogador vive no PlayerData (passo atual por missao +
 * missoes concluidas), persistido pelo StorageManager.</p>
 *
 * <p>So e chamado na thread principal (eventos e comandos).</p>
 */
public final class QuestManager {

    private static final String FILE_NAME = "quests.yml";

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final LoreManager loreManager;
    private final ItemManager itemManager;
    private final File file;
    private final Map<String, QuestDefinition> quests = new LinkedHashMap<>();

    public QuestManager(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.loreManager = plugin.getLoreManager();
        this.itemManager = plugin.getItemManager();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
    }

    /** (Re)carrega quests.yml do disco (copia do resource na primeira execucao). */
    public void load() {
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, QuestDefinition> carregadas = new LinkedHashMap<>();
        ConfigurationSection secao = config.getConfigurationSection("quests");
        if (secao == null) {
            plugin.getLogger().warning("Nenhuma missao definida em " + FILE_NAME + " (secao 'quests' ausente).");
        } else {
            for (String id : secao.getKeys(false)) {
                String base = "quests." + id;
                List<Objetivo> objetivos = new ArrayList<>();
                for (String spec : config.getStringList(base + ".objetivos")) {
                    Objetivo objetivo = parseObjetivo(spec);
                    if (objetivo == null) {
                        plugin.getLogger().warning("Missao '" + id + "': objetivo invalido ('" + spec + "').");
                        continue;
                    }
                    objetivos.add(objetivo);
                }
                List<Recompensa> recompensas = new ArrayList<>();
                for (String spec : config.getStringList(base + ".recompensas")) {
                    Recompensa recompensa = parseRecompensa(spec);
                    if (recompensa == null) {
                        plugin.getLogger().warning("Missao '" + id + "': recompensa invalida ('" + spec + "').");
                        continue;
                    }
                    recompensas.add(recompensa);
                }
                if (objetivos.isEmpty()) {
                    plugin.getLogger().warning("Missao '" + id
                            + "' sem objetivos validos; nunca podera ser concluida.");
                }
                carregadas.put(id.toLowerCase(Locale.ROOT), new QuestDefinition(
                        id,
                        config.getString(base + ".nome", id),
                        config.getString(base + ".era", ""),
                        config.getString(base + ".descricao", ""),
                        config.getString(base + ".requisito", ""),
                        config.getString(base + ".rota", ""),
                        objetivos,
                        recompensas));
            }
        }

        for (QuestDefinition quest : carregadas.values()) {
            if (quest.temRequisito() && !carregadas.containsKey(quest.getRequisito().toLowerCase(Locale.ROOT))) {
                plugin.getLogger().warning("Missao '" + quest.getId() + "': requisito '"
                        + quest.getRequisito() + "' nao existe.");
            }
        }

        quests.clear();
        quests.putAll(carregadas);
        plugin.getLogger().info("Missoes carregadas (" + quests.size() + ").");
    }

    private Objetivo parseObjetivo(String spec) {
        String[] partes = spec.split(":");
        if (partes.length < 2 || partes[0].isBlank() || partes[1].isBlank()) {
            return null;
        }
        String tipoStr = partes[0].toLowerCase(Locale.ROOT);
        String alvo = partes[1];
        int quantidade = 1;
        if (partes.length >= 3) {
            try {
                quantidade = Integer.parseInt(partes[2]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return switch (tipoStr) {
            case "descoberta" -> new Objetivo(TipoObjetivo.DESCOBERTA, alvo, quantidade);
            case "item" -> new Objetivo(TipoObjetivo.ITEM, alvo, quantidade);
            case "entregar" -> new Objetivo(TipoObjetivo.ENTREGAR, alvo, quantidade);
            case "dungeon" -> new Objetivo(TipoObjetivo.DUNGEON, alvo, 1);
            default -> null;
        };
    }

    private Recompensa parseRecompensa(String spec) {
        String[] partes = spec.split(":");
        if (partes[0].isBlank()) {
            return null;
        }
        int quantidade = 1;
        if (partes.length >= 2) {
            try {
                quantidade = Integer.parseInt(partes[1]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return new Recompensa(partes[0], quantidade);
    }

    public QuestDefinition porId(String id) {
        if (id == null) {
            return null;
        }
        return quests.get(id.toLowerCase(Locale.ROOT));
    }

    public List<QuestDefinition> listar() {
        return List.copyOf(quests.values());
    }

    /** Objetivo atual legivel para o diario; null quando a missao nao esta ativa. */
    public String objetivoAtual(PlayerData data, String questId) {
        QuestDefinition quest = porId(questId);
        if (quest == null || !data.questAtiva(quest.getId())) {
            return null;
        }
        int passo = data.getPasso(quest.getId());
        if (passo < 0 || passo >= quest.getObjetivos().size()) {
            return "Aguardando verificacao de progresso";
        }
        return descreverObjetivo(quest.getObjetivos().get(passo));
    }

    public int total() {
        return quests.size();
    }

    /** Descricao legivel de um objetivo, usando nomes de lore/itens. */
    public String descreverObjetivo(Objetivo objetivo) {
        return switch (objetivo.getTipo()) {
            case DESCOBERTA -> {
                LoreEntry entry = loreManager.porId(objetivo.getAlvo());
                String titulo = entry != null ? entry.getTitulo() : objetivo.getAlvo();
                yield "Ler o registro: " + titulo;
            }
            case ITEM -> "Obter: " + nomeItem(objetivo.getAlvo());
            case ENTREGAR -> "Entregar: " + nomeItem(objetivo.getAlvo())
                    + (objetivo.getQuantidade() > 1 ? " (x" + objetivo.getQuantidade() + ")" : "");
            case DUNGEON -> "Concluir a expedicao: " + nomeDungeon(objetivo.getAlvo());
        };
    }

    private String nomeDungeon(String dungeonId) {
        var manager = plugin.getDungeonManager();
        if (manager == null) {
            return dungeonId;
        }
        var dungeon = manager.porId(dungeonId);
        return dungeon != null ? dungeon.getNome() : dungeonId;
    }

    private String nomeItem(String itemId) {
        ItemDefinition item = itemManager.porId(itemId);
        return item != null ? item.getNome() : itemId;
    }

    /** Resultado de uma tentativa de inicio. */
    public enum ResultadoInicio {
        OK,
        NAO_ENCONTRADA,
        JA_ATIVA,
        JA_CONCLUIDA,
        REQUISITO,
        ROTA
    }

    public ResultadoInicio iniciar(Player player, String id) {
        QuestDefinition quest = porId(id);
        if (quest == null) {
            return ResultadoInicio.NAO_ENCONTRADA;
        }
        PlayerData data = plugin.getPlayerManager().get(player);
        if (data.questConcluida(quest.getId())) {
            return ResultadoInicio.JA_CONCLUIDA;
        }
        if (data.questAtiva(quest.getId())) {
            return ResultadoInicio.JA_ATIVA;
        }
        if (quest.temRequisito() && !data.questConcluida(quest.getRequisito())) {
            return ResultadoInicio.REQUISITO;
        }
        if (quest.temRota()) {
            String rota = data.getRotaAtual();
            if (rota == null || !rota.equalsIgnoreCase(quest.getRota())) {
                return ResultadoInicio.ROTA;
            }
        }
        data.iniciarQuest(quest.getId());
        plugin.getPlayerManager().save(data);
        return ResultadoInicio.OK;
    }

    public boolean abandonar(Player player, String id) {
        QuestDefinition quest = porId(id);
        if (quest == null) {
            return false;
        }
        PlayerData data = plugin.getPlayerManager().get(player);
        if (!data.questAtiva(quest.getId())) {
            return false;
        }
        data.abandonarQuest(quest.getId());
        plugin.getPlayerManager().save(data);
        return true;
    }

    /**
     * Avalia as missoes ativas do jogador no passo atual; avanca, consome
     * itens de entrega, conclui e paga recompensas conforme necessario.
     */
    public void verificar(Player player) {
        PlayerData data = plugin.getPlayerManager().get(player);
        if (data.getQuestsAtivas().isEmpty()) {
            // Fast-path: nada em andamento, nada a avaliar.
            return;
        }
        boolean mudou = false;
        // Copia dos ids: o mapa de progresso e modificado durante o laco
        // (avanco de passo e conclusao), o que quebraria a iteracao.
        for (String questId : new ArrayList<>(data.getQuestsAtivas())) {
            QuestDefinition quest = porId(questId);
            if (quest == null) {
                continue;
            }
            // Avanca todos os passos satisfeitos de uma vez: se o jogador
            // chegou com varios objetivos prontos, nao precisa de varios cliques.
            while (true) {
                int passo = data.getPasso(questId);
                if (passo < 0 || passo >= quest.getObjetivos().size()) {
                    break;
                }
                Objetivo objetivo = quest.getObjetivos().get(passo);
                if (!satisfeito(player, data, objetivo)) {
                    break;
                }
                String descricao = descreverObjetivo(objetivo);
                if (objetivo.getTipo() == TipoObjetivo.ENTREGAR) {
                    removerItens(player, objetivo);
                }
                data.setPasso(questId, passo + 1);
                mudou = true;

                if (passo + 1 >= quest.getObjetivos().size()) {
                    concluir(player, data, quest);
                    break;
                }
                String proximo = descreverObjetivo(quest.getObjetivos().get(passo + 1));
                player.sendMessage(messages.message("comando.quest.progresso",
                        "{descricao}", descricao, "{proximo}", proximo));
            }
        }
        if (mudou) {
            plugin.getPlayerManager().save(data);
        }
    }

    private void concluir(Player player, PlayerData data, QuestDefinition quest) {
        data.marcarQuestConcluida(quest.getId());
        player.sendMessage(messages.message("comando.quest.conclusao", "{nome}", quest.getNome()));
        for (Recompensa recompensa : quest.getRecompensas()) {
            ItemDefinition item = itemManager.porId(recompensa.getItemId());
            if (item == null) {
                plugin.getLogger().warning("Missao '" + quest.getId()
                        + "': recompensa com item inexistente (" + recompensa.getItemId() + ").");
                continue;
            }
            ItemStack stack = itemManager.criar(item.getId(), recompensa.getQuantidade());
            if (stack == null) {
                continue;
            }
            for (ItemStack sobra : player.getInventory().addItem(stack).values()) {
                player.getWorld().dropItem(player.getLocation(), sobra);
            }
            player.sendMessage(messages.message("comando.quest.recompensa",
                    "{item}", item.getNome(),
                    "{quantidade}", String.valueOf(recompensa.getQuantidade())));
        }
    }

    private boolean satisfeito(Player player, PlayerData data, Objetivo objetivo) {
        return switch (objetivo.getTipo()) {
            case DESCOBERTA -> data.temDescoberta(objetivo.getAlvo());
            case ITEM, ENTREGAR -> contarItens(player, objetivo.getAlvo()) >= objetivo.getQuantidade();
            case DUNGEON -> data.dungeonConcluida(objetivo.getAlvo());
        };
    }

    private int contarItens(Player player, String itemId) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) {
                continue;
            }
            if (itemId.equalsIgnoreCase(itemManager.porItem(stack))) {
                total += stack.getAmount();
            }
        }
        // A mao secundaria tambem conta ( jogadores guardam itens la).
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && itemId.equalsIgnoreCase(itemManager.porItem(offhand))) {
            total += offhand.getAmount();
        }
        return total;
    }

    private void removerItens(Player player, Objetivo objetivo) {
        int restante = objetivo.getQuantidade();
        ItemStack[] conteudo = player.getInventory().getContents();
        for (int slot = 0; slot < conteudo.length && restante > 0; slot++) {
            ItemStack stack = conteudo[slot];
            if (stack == null) {
                continue;
            }
            if (!objetivo.getAlvo().equalsIgnoreCase(itemManager.porItem(stack))) {
                continue;
            }
            int remover = Math.min(restante, stack.getAmount());
            stack.setAmount(stack.getAmount() - remover);
            restante -= remover;
            if (stack.getAmount() <= 0) {
                player.getInventory().setItem(slot, null);
            }
        }
        if (restante > 0) {
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (offhand != null && objetivo.getAlvo().equalsIgnoreCase(itemManager.porItem(offhand))) {
                int remover = Math.min(restante, offhand.getAmount());
                int novaQuantidade = offhand.getAmount() - remover;
                player.getInventory().setItemInOffHand(
                        novaQuantidade <= 0 ? null : offhand.asQuantity(novaQuantidade));
            }
        }
    }
}
