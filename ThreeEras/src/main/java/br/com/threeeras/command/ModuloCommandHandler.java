package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.dungeon.DungeonDefinition;
import br.com.threeeras.dungeon.DungeonManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * Subcomandos /threeeras dungeon e /threeeras compat|backup|kit.
 */
public final class ModuloCommandHandler {

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final DungeonManager dungeonManager;

    public ModuloCommandHandler(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.dungeonManager = plugin.getDungeonManager();
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(messages.prefixed("comando.dungeon.uso"));
            return;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "listar" -> listar(sender);
            case "info" -> info(sender, args);
            case "entrar" -> entrar(sender, args);
            case "concluir" -> concluir(sender, args);
            default -> sender.sendMessage(messages.prefixed("comando.dungeon.uso"));
        }
    }

    private void listar(CommandSender sender) {
        List<DungeonDefinition> dungeons = dungeonManager.listar();
        if (dungeons.isEmpty()) {
            sender.sendMessage(messages.message("comando.dungeon.listar.vazio"));
            return;
        }
        sender.sendMessage(messages.message("comando.dungeon.listar.titulo",
                "{total}", String.valueOf(dungeons.size())));
        for (DungeonDefinition dungeon : dungeons) {
            sender.sendMessage(messages.message("comando.dungeon.listar.linha",
                    "{id}", dungeon.getId(),
                    "{nome}", dungeon.getNome(),
                    "{era}", dungeon.getEra(),
                    "{rota}", dungeon.temRota() ? dungeon.getRota() : "livre",
                    "{salas}", String.valueOf(dungeon.getNumeroSalas())));
        }
    }

    private void info(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.dungeon.uso"));
            return;
        }
        DungeonDefinition dungeon = dungeonManager.porId(args[2]);
        if (dungeon == null) {
            sender.sendMessage(messages.message("comando.dungeon.inexistente", "{id}", args[2]));
            return;
        }
        sender.sendMessage(messages.message("comando.dungeon.info.titulo", "{nome}", dungeon.getNome()));
        sender.sendMessage(messages.message("comando.dungeon.info.linha",
                "{era}", dungeon.getEra(),
                "{rota}", dungeon.temRota() ? dungeon.getRota() : "livre",
                "{chefe}", dungeon.temChefe() ? dungeon.getChefe() : "nenhum",
                "{salas}", String.valueOf(dungeon.getNumeroSalas())));
    }

    private void entrar(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.dungeon.entrar.uso"));
            return;
        }
        Player player = sender instanceof Player p ? p : null;
        if (player == null) {
            sender.sendMessage(messages.prefixed("comando.dungeon.entrar.console"));
            return;
        }
        String id = args[2];
        if (dungeonManager.porId(id) == null) {
            sender.sendMessage(messages.message("comando.dungeon.inexistente", "{id}", id));
            return;
        }
        Location entrada = dungeonManager.entrar(player, id);
        if (entrada == null) {
            sender.sendMessage(messages.message("comando.dungeon.rota-bloqueada", "{id}", id));
            return;
        }
        sender.sendMessage(messages.message("comando.dungeon.entrada-ok",
                "{nome}", dungeonManager.porId(id).getNome()));
    }

    private void concluir(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.dungeon.concluir.uso"));
            return;
        }
        if (!sender.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        Player player = sender instanceof Player p ? p : null;
        if (player == null) {
            sender.sendMessage(messages.prefixed("comando.dungeon.entrar.console"));
            return;
        }
        String id = args[2];
        if (dungeonManager.porId(id) == null) {
            sender.sendMessage(messages.message("comando.dungeon.inexistente", "{id}", id));
            return;
        }
        dungeonManager.concluir(player, id);
    }

    /** /threeeras compat — relatorio de ambiente. */
    public void handleCompat(CommandSender sender) {
        plugin.getCompatibilidade().relatorio(sender);
    }

    /** /threeeras backup — cria backup. */
    public void handleBackup(CommandSender sender) {
        if (!sender.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        var arquivo = plugin.getArmazenamentoModule().criarBackup();
        if (arquivo == null) {
            sender.sendMessage(messages.prefixed("comando.backup.falhou"));
            return;
        }
        sender.sendMessage(messages.message("comando.backup.ok", "{arquivo}", arquivo.getName()));
    }

    /** /threeeras kit — kit do jogador. */
    public void handleKit(CommandSender sender) {
        Player player = sender instanceof Player p ? p : null;
        if (player == null) {
            sender.sendMessage(messages.prefixed("comando.kit.console"));
            return;
        }
        var resultado = plugin.getJogabilidade().darKit(player);
        if (resultado.entregue()) {
            sender.sendMessage(messages.prefixed("comando.kit.ok"));
        } else {
            sender.sendMessage(messages.message("comando.kit.cooldown",
                    "{minutos}", String.valueOf(resultado.minutosRestantes())));
        }
    }
}
