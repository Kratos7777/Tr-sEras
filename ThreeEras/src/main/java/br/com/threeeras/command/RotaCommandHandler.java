package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.player.PlayerData;
import br.com.threeeras.rota.RotaDefinition;
import br.com.threeeras.rota.RotaManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * Subcomando /threeeras rota: info, listar, definir e limpar.
 */
public final class RotaCommandHandler {

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final RotaManager rotaManager;

    public RotaCommandHandler(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.rotaManager = plugin.getRotaManager();
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            info(sender);
            return;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "info" -> info(sender);
            case "listar" -> listar(sender);
            case "definir" -> definir(sender, args);
            case "limpar" -> limpar(sender);
            default -> sender.sendMessage(messages.prefixed("comando.rota.uso"));
        }
    }

    private void info(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.rota.uso"));
            return;
        }
        PlayerData data = plugin.getPlayerManager().get(player);
        String idAtual = data.getRotaAtual();
        if (idAtual == null) {
            sender.sendMessage(messages.prefixed("comando.rota.nenhuma"));
            return;
        }
        RotaDefinition rota = rotaManager.porId(idAtual);
        if (rota == null) {
            sender.sendMessage(messages.message("comando.rota.invalida", "{id}", idAtual));
            return;
        }
        sender.sendMessage(messages.message("comando.rota.info.titulo", "{nome}", rota.getNome()));
        sender.sendMessage(messages.message("comando.rota.info.descricao",
                "{descricao}", rota.getDescricao()));
        sender.sendMessage(messages.message("comando.rota.info.era", "{era}", rota.getEra()));
        int feitas = rotaManager.progressoFechamento(data);
        int total = rotaManager.totalFechamento(data);
        String estado = data.isRotaConcluida() ? "concluida" : "em andamento " + feitas + "/" + total;
        sender.sendMessage(messages.message("comando.rota.info.estado", "{estado}", estado));
    }

    private void listar(CommandSender sender) {
        List<RotaDefinition> rotas = rotaManager.listar();
        if (rotas.isEmpty()) {
            sender.sendMessage(messages.message("comando.rota.listar.vazio"));
            return;
        }
        sender.sendMessage(messages.message("comando.rota.listar.titulo",
                "{total}", String.valueOf(rotas.size())));
        for (RotaDefinition rota : rotas) {
            sender.sendMessage(messages.message("comando.rota.listar.linha",
                    "{id}", rota.getId(),
                    "{nome}", rota.getNome(),
                    "{era}", rota.getEra(),
                    "{requisitos}", String.valueOf(rota.getRequisitos().size())));
        }
    }

    private void definir(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.rota.definir.console"));
            return;
        }
        if (!player.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.rota.definir.uso"));
            return;
        }
        if (rotaManager.definir(player, args[2])) {
            sender.sendMessage(messages.message("comando.rota.definir.ok", "{id}", args[2]));
        } else {
            sender.sendMessage(messages.message("comando.rota.invalida", "{id}", args[2]));
        }
    }

    private void limpar(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.rota.definir.console"));
            return;
        }
        if (!player.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        if (rotaManager.limpar(player)) {
            sender.sendMessage(messages.prefixed("comando.rota.limpar.ok"));
        } else {
            sender.sendMessage(messages.prefixed("comando.rota.limpar.vazio"));
        }
    }
}
