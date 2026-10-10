package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.quest.Objetivo;
import br.com.threeeras.quest.QuestDefinition;
import br.com.threeeras.quest.QuestManager;
import br.com.threeeras.player.PlayerData;
import br.com.threeeras.player.PlayerManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * Subcomando /threeeras quest: listar, info, iniciar e abandonar.
 */
public final class QuestCommandHandler {

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final QuestManager questManager;
    private final PlayerManager playerManager;

    public QuestCommandHandler(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.questManager = plugin.getQuestManager();
        this.playerManager = plugin.getPlayerManager();
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(messages.prefixed("comando.quest.uso"));
            return;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "listar" -> listar(sender);
            case "info" -> info(sender, args);
            case "iniciar" -> iniciar(sender, args);
            case "abandonar" -> abandonar(sender, args);
            default -> sender.sendMessage(messages.prefixed("comando.quest.uso"));
        }
    }

    private void listar(CommandSender sender) {
        List<QuestDefinition> quests = questManager.listar();
        if (quests.isEmpty()) {
            sender.sendMessage(messages.message("comando.quest.listar.vazio"));
            return;
        }
        PlayerData data = dadosDoSender(sender);
        sender.sendMessage(messages.message("comando.quest.listar.titulo",
                "{total}", String.valueOf(quests.size())));
        for (QuestDefinition quest : quests) {
            sender.sendMessage(messages.message("comando.quest.listar.linha",
                    "{id}", quest.getId(),
                    "{nome}", quest.getNome(),
                    "{era}", quest.getEra(),
                    "{estado}", estado(quest, data)));
        }
    }

    private String estado(QuestDefinition quest, PlayerData data) {
        if (data == null) {
            return "-";
        }
        if (data.questConcluida(quest.getId())) {
            return "concluida";
        }
        if (data.questAtiva(quest.getId())) {
            return "em andamento (" + data.getPasso(quest.getId()) + "/" + quest.getObjetivos().size() + ")";
        }
        return "nao iniciada";
    }

    private void info(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.quest.uso"));
            return;
        }
        QuestDefinition quest = questManager.porId(args[2]);
        if (quest == null) {
            sender.sendMessage(messages.message("comando.quest.nao-encontrada", "{id}", args[2]));
            return;
        }
        PlayerData data = dadosDoSender(sender);
        sender.sendMessage(messages.message("comando.quest.info.titulo", "{nome}", quest.getNome()));
        sender.sendMessage(messages.message("comando.quest.info.descricao", "{descricao}", quest.getDescricao()));
        int passoAtual = data != null ? data.getPasso(quest.getId()) : 0;
        List<Objetivo> objetivos = quest.getObjetivos();
        for (int i = 0; i < objetivos.size(); i++) {
            boolean feito = passoAtual > i;
            sender.sendMessage(messages.message("comando.quest.info.objetivo",
                    "{marca}", feito ? "&a[x]" : "&7[ ]",
                    "{objetivo}", questManager.descreverObjetivo(objetivos.get(i))));
        }
        if (quest.temRequisito()) {
            QuestDefinition requisito = questManager.porId(quest.getRequisito());
            String nomeReq = requisito != null ? requisito.getNome() : quest.getRequisito();
            sender.sendMessage(messages.message("comando.quest.info.requisito", "{quest}", nomeReq));
        }
        for (var recompensa : quest.getRecompensas()) {
            sender.sendMessage(messages.message("comando.quest.info.recompensa",
                    "{item}", nomeItem(recompensa.getItemId()),
                    "{quantidade}", String.valueOf(recompensa.getQuantidade())));
        }
    }

    private void iniciar(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.quest.iniciar.uso"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.quest.iniciar.console"));
            return;
        }
        QuestDefinition quest = questManager.porId(args[2]);
        if (quest == null) {
            sender.sendMessage(messages.message("comando.quest.nao-encontrada", "{id}", args[2]));
            return;
        }
        QuestManager.ResultadoInicio resultado = questManager.iniciar(player, quest.getId());
        switch (resultado) {
            case OK -> {
                sender.sendMessage(messages.message("comando.quest.iniciar.ok", "{nome}", quest.getNome()));
                // Se o primeiro objetivo ja estava satisfeito, avanca agora.
                questManager.verificar(player);
            }
            case JA_ATIVA -> sender.sendMessage(messages.prefixed("comando.quest.iniciar.ja-ativa"));
            case JA_CONCLUIDA -> sender.sendMessage(messages.prefixed("comando.quest.iniciar.ja-concluida"));
            case REQUISITO -> {
                QuestDefinition requisito = questManager.porId(quest.getRequisito());
                sender.sendMessage(messages.message("comando.quest.iniciar.requisito",
                        "{quest}", requisito != null ? requisito.getNome() : quest.getRequisito()));
            }
            case NAO_ENCONTRADA -> sender.sendMessage(messages.message("comando.quest.nao-encontrada",
                    "{id}", args[2]));
        }
    }

    private void abandonar(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.quest.uso"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.quest.iniciar.console"));
            return;
        }
        if (questManager.abandonar(player, args[2])) {
            sender.sendMessage(messages.message("comando.quest.abandonar.ok", "{id}", args[2]));
        } else {
            sender.sendMessage(messages.message("comando.quest.abandonar.nao-iniciada", "{id}", args[2]));
        }
    }

    private PlayerData dadosDoSender(CommandSender sender) {
        if (sender instanceof Player player) {
            return playerManager.get(player);
        }
        return null;
    }

    private String nomeItem(String itemId) {
        var item = plugin.getItemManager().porId(itemId);
        return item != null ? item.getNome() : itemId;
    }
}
