package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Comando /threeeras com subcomandos info, ajuda e reload.
 */
public final class ThreeErasCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "threeeras.admin";
    private static final List<String> SUBCOMMANDS = List.of("info", "ajuda", "reload");

    private final ThreeErasPlugin plugin;
    private final Messages messages;

    public ThreeErasCommand(ThreeErasPlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "info" -> sendInfo(sender);
            case "ajuda", "help" -> sendHelp(sender);
            case "reload" -> reload(sender);
            default -> sender.sendMessage(messages.prefixed("comando.desconhecido"));
        }
        return true;
    }

    private void sendInfo(CommandSender sender) {
        String nome = messages.raw("comando.info.nome");
        String autor = messages.raw("comando.info.autor")
                .replace("{versao}", messages.raw("informacoes.versao"))
                .replace("{autor}", messages.raw("informacoes.autor"));
        String descricao = messages.raw("comando.info.descricao")
                .replace("{descricao}", messages.raw("informacoes.descricao"));
        sender.sendMessage(messages.component(nome));
        sender.sendMessage(messages.component(autor));
        sender.sendMessage(messages.component(descricao));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(messages.plain("comando.ajuda.cabecalho"));
        sender.sendMessage(messages.plain("comando.ajuda.info"));
        sender.sendMessage(messages.plain("comando.ajuda.ajuda"));
        if (sender.hasPermission(PERMISSION)) {
            sender.sendMessage(messages.plain("comando.ajuda.reload"));
        }
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        try {
            plugin.reloadConfig();
            messages.load();
            sender.sendMessage(messages.prefixed("comando.reload.sucesso"));
        } catch (RuntimeException e) {
            plugin.getLogger().severe("Falha ao recarregar configuracoes: " + e.getMessage());
            sender.sendMessage(messages.prefixed("comando.reload.falha"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String input = args[0].toLowerCase(Locale.ROOT);
        List<String> suggestions = new ArrayList<>();
        for (String sub : SUBCOMMANDS) {
            if (sub.equals("reload") && !sender.hasPermission(PERMISSION)) {
                continue;
            }
            if (sub.startsWith(input)) {
                suggestions.add(sub);
            }
        }
        return suggestions;
    }
}
