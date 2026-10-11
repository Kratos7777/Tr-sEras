package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.boss.BossDefinition;
import br.com.threeeras.boss.BossManager;
import br.com.threeeras.config.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * Subcomando /threeeras boss: listar, info, criar, remover e ir.
 */
public final class BossCommandHandler {

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final BossManager bossManager;

    public BossCommandHandler(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.bossManager = plugin.getBossManager();
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(messages.prefixed("comando.boss.uso"));
            return;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "listar" -> listar(sender);
            case "info" -> info(sender, args);
            case "criar" -> criar(sender, args);
            case "remover" -> remover(sender, args);
            case "ir" -> ir(sender, args);
            default -> sender.sendMessage(messages.prefixed("comando.boss.uso"));
        }
    }

    private void listar(CommandSender sender) {
        List<BossDefinition> bosses = bossManager.listar();
        if (bosses.isEmpty()) {
            sender.sendMessage(messages.message("comando.boss.listar.vazio"));
            return;
        }
        sender.sendMessage(messages.message("comando.boss.listar.titulo",
                "{total}", String.valueOf(bosses.size())));
        for (BossDefinition boss : bosses) {
            boolean ativo = bossManager.getEntidade(boss.getId()) != null;
            sender.sendMessage(messages.message("comando.boss.listar.linha",
                    "{id}", boss.getId(),
                    "{nome}", boss.getNome(),
                    "{era}", boss.getEra(),
                    "{estado}", ativo ? "no mundo" : "dormente"));
        }
    }

    private void info(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.boss.uso"));
            return;
        }
        BossDefinition boss = bossManager.porId(args[2]);
        if (boss == null) {
            sender.sendMessage(messages.message("comando.boss.inexistente", "{id}", args[2]));
            return;
        }
        int pilares = bossManager.pilaresRestantes(boss.getId());
        sender.sendMessage(messages.message("comando.boss.info.titulo", "{nome}", boss.getNome()));
        sender.sendMessage(messages.message("comando.boss.info.linha",
                "{era}", boss.getEra(),
                "{vida}", String.valueOf((long) boss.getVida()),
                "{pilares}", String.valueOf(boss.getPilares()),
                "{restantes}", String.valueOf(pilares)));
    }

    private void criar(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.boss.criar.uso"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.boss.criar.console"));
            return;
        }
        if (!player.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        String id = args[2];
        BossDefinition boss = bossManager.porId(id);
        if (boss == null) {
            sender.sendMessage(messages.message("comando.boss.inexistente", "{id}", id));
            return;
        }
        if (bossManager.getEntidade(id) != null) {
            sender.sendMessage(messages.prefixed("comando.boss.criar.ja-ativo"));
            return;
        }
        if (bossManager.criar(id, player.getLocation())) {
            sender.sendMessage(messages.message("comando.boss.criar.ok",
                    "{nome}", boss.getNome(), "{pilares}", String.valueOf(boss.getPilares())));
        } else {
            sender.sendMessage(messages.prefixed("comando.boss.criar.falhou"));
        }
    }

    private void remover(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.boss.uso"));
            return;
        }
        if (!sender.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        String id = args[2];
        if (bossManager.porId(id) == null) {
            sender.sendMessage(messages.message("comando.boss.inexistente", "{id}", id));
            return;
        }
        bossManager.remover(id);
        sender.sendMessage(messages.message("comando.boss.remover.ok", "{id}", id));
    }

    private void ir(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.boss.uso"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.boss.criar.console"));
            return;
        }
        if (!player.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        String id = args[2];
        if (bossManager.porId(id) == null) {
            sender.sendMessage(messages.message("comando.boss.inexistente", "{id}", id));
            return;
        }
        var destino = bossManager.posicaoAtual(id);
        if (destino == null) {
            sender.sendMessage(messages.message("comando.boss.ir.dormente", "{id}", id));
            return;
        }
        player.teleport(destino);
        sender.sendMessage(messages.message("comando.boss.ir.ok", "{id}", id));
    }
}
