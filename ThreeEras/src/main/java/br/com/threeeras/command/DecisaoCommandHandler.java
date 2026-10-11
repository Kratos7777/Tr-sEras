package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.decisao.DecisaoDefinition;
import br.com.threeeras.decisao.DecisaoManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * Subcomandos /threeeras decisao: listar, ver e escolher.
 */
public final class DecisaoCommandHandler {

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final DecisaoManager decisaoManager;

    public DecisaoCommandHandler(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.decisaoManager = plugin.getDecisaoManager();
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            listar(sender);
            return;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "listar" -> listar(sender);
            case "ver" -> {
                if (args.length < 3) {
                    sender.sendMessage(messages.prefixed("comando.decisao.uso"));
                    return;
                }
                ver(sender, args[2]);
            }
            case "escolher" -> {
                if (args.length < 4) {
                    sender.sendMessage(messages.prefixed("comando.decisao.escolher.uso"));
                    return;
                }
                escolher(sender, args[2], args[3]);
            }
            default -> sender.sendMessage(messages.prefixed("comando.decisao.uso"));
        }
    }

    private void listar(CommandSender sender) {
        List<DecisaoDefinition> decisoes = decisaoManager.listar();
        if (decisoes.isEmpty()) {
            sender.sendMessage(messages.message("comando.decisao.listar.vazio"));
            return;
        }
        sender.sendMessage(messages.message("comando.decisao.listar.titulo",
                "{total}", String.valueOf(decisoes.size())));
        for (DecisaoDefinition decisao : decisoes) {
            sender.sendMessage(messages.message("comando.decisao.listar.linha",
                    "{id}", decisao.getId(),
                    "{nome}", decisao.getNome(),
                    "{opcoes}", String.valueOf(decisao.getOpcoes().size())));
        }
    }

    private void ver(CommandSender sender, String id) {
        DecisaoDefinition decisao = decisaoManager.porId(id);
        if (decisao == null) {
            sender.sendMessage(messages.message("comando.decisao.inexistente", "{id}", id));
            return;
        }
        sender.sendMessage(messages.message("comando.decisao.ver.titulo", "{nome}", decisao.getNome()));
        sender.sendMessage(messages.message("comando.decisao.ver.descricao",
                "{descricao}", decisao.getDescricao()));
        for (var opcao : decisao.getOpcoes()) {
            sender.sendMessage(messages.message("comando.decisao.ver.opcao",
                    "{opcao}", opcao.getId(), "{texto}", opcao.getTexto()));
        }
    }

    private void escolher(CommandSender sender, String id, String idOpcao) {
        Player player = sender instanceof Player p ? p : null;
        if (player == null) {
            sender.sendMessage(messages.prefixed("comando.decisao.escolher.console"));
            return;
        }
        var resultado = decisaoManager.escolher(player, id, idOpcao);
        switch (resultado) {
            case OK -> sender.sendMessage(messages.message("comando.decisao.escolher.ok",
                    "{opcao}", idOpcao));
            case NAO_ENCONTRADA -> sender.sendMessage(messages.message("comando.decisao.inexistente",
                    "{id}", id));
            case OPCAO_INVALIDA -> sender.sendMessage(messages.message("comando.decisao.escolher.opcao-invalida",
                    "{opcao}", idOpcao));
            case JA_ESCOLHIDA -> sender.sendMessage(messages.prefixed("comando.decisao.escolher.ja-escolhida"));
            case FALTAM_ITENS -> sender.sendMessage(messages.prefixed("comando.decisao.escolher.faltam-itens"));
        }
    }
}
