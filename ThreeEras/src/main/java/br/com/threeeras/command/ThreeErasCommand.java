package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.storage.StorageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Comando /threeeras com subcomandos info, ajuda, reload e armazenamento.
 */
public final class ThreeErasCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "threeeras.admin";
    private static final List<String> SUBCOMMANDS =
            List.of("info", "ajuda", "reload", "armazenamento", "storage");

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final StorageManager storage;

    public ThreeErasCommand(ThreeErasPlugin plugin, Messages messages, StorageManager storage) {
        this.plugin = plugin;
        this.messages = messages;
        this.storage = storage;
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
            case "armazenamento", "storage" -> handleStorage(sender, args);
            default -> sender.sendMessage(messages.prefixed("comando.desconhecido"));
        }
        return true;
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(messages.message("comando.info.nome"));
        sender.sendMessage(messages.message("comando.info.autor",
                "{versao}", messages.raw("informacoes.versao"),
                "{autor}", messages.raw("informacoes.autor")));
        sender.sendMessage(messages.message("comando.info.descricao",
                "{descricao}", messages.raw("informacoes.descricao")));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(messages.plain("comando.ajuda.cabecalho"));
        sender.sendMessage(messages.plain("comando.ajuda.info"));
        sender.sendMessage(messages.plain("comando.ajuda.ajuda"));
        sender.sendMessage(messages.plain("comando.ajuda.armazenamento"));
        if (sender.hasPermission(PERMISSION)) {
            sender.sendMessage(messages.plain("comando.ajuda.reload"));
        }
    }

    private void reload(CommandSender sender) {
        if (!checkAdmin(sender)) {
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

    private void handleStorage(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sendStorageInfo(sender);
            return;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "info" -> sendStorageInfo(sender);
            case "salvar" -> saveStorage(sender);
            case "recarregar" -> reloadStorage(sender);
            case "definir" -> defineStorage(sender, args);
            default -> sender.sendMessage(messages.prefixed("comando.armazenamento.uso"));
        }
    }

    private void sendStorageInfo(CommandSender sender) {
        File file = storage.getFile();
        boolean existe = file.exists();
        long tamanho = existe ? file.length() : 0L;
        String autosave = storage.isAutosaveAtivo()
                ? "ligado (a cada " + storage.getAutosaveSegundos() + "s)"
                : "desligado";
        sender.sendMessage(messages.plain("comando.armazenamento.info.titulo"));
        sender.sendMessage(messages.message("comando.armazenamento.info.arquivo",
                "{caminho}", file.getPath()));
        sender.sendMessage(messages.message("comando.armazenamento.info.existe",
                "{existe}", existe ? "sim" : "nao (criado ao salvar)",
                "{tamanho}", String.valueOf(tamanho)));
        sender.sendMessage(messages.message("comando.armazenamento.info.sujo",
                "{sujo}", storage.isDirty() ? "sim" : "nao"));
        sender.sendMessage(messages.message("comando.armazenamento.info.autosave",
                "{autosave}", autosave));
        sender.sendMessage(messages.message("comando.armazenamento.info.chaves",
                "{chaves}", String.valueOf(storage.getRootKeys().size())));
    }

    private void saveStorage(CommandSender sender) {
        if (!checkAdmin(sender)) {
            return;
        }
        if (!storage.isDirty()) {
            sender.sendMessage(messages.prefixed("comando.armazenamento.salvar.vazio"));
            return;
        }
        storage.saveAsync();
        sender.sendMessage(messages.prefixed("comando.armazenamento.salvar.ok"));
    }

    private void reloadStorage(CommandSender sender) {
        if (!checkAdmin(sender)) {
            return;
        }
        boolean haviaAlteracoes = storage.isDirty();
        storage.load();
        sender.sendMessage(messages.prefixed("comando.armazenamento.recarregar.ok"));
        if (haviaAlteracoes) {
            sender.sendMessage(messages.prefixed("comando.armazenamento.recarregar.aviso"));
        }
    }

    private void defineStorage(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) {
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(messages.prefixed("comando.armazenamento.definir.uso"));
            return;
        }
        String chave = args[2];
        String valor = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        storage.set("debug." + chave, valor);
        sender.sendMessage(messages.message("comando.armazenamento.definir.ok",
                "{chave}", chave, "{valor}", valor));
    }

    private boolean checkAdmin(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
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
        if (args.length == 2 && (args[0].equalsIgnoreCase("armazenamento")
                || args[0].equalsIgnoreCase("storage"))) {
            String input = args[1].toLowerCase(Locale.ROOT);
            List<String> suggestions = new ArrayList<>();
            for (String sub : List.of("info", "salvar", "recarregar", "definir")) {
                if (sub.startsWith(input)) {
                    suggestions.add(sub);
                }
            }
            return suggestions;
        }
        return List.of();
    }
}
