package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.era.Era;
import br.com.threeeras.era.EraManager;
import br.com.threeeras.player.PlayerData;
import br.com.threeeras.player.PlayerManager;
import br.com.threeeras.storage.StorageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Comando /threeeras com subcomandos info, ajuda, reload, armazenamento,
 * jogador e era.
 */
public final class ThreeErasCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "threeeras.admin";
    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());
    private static final List<String> SUBCOMMANDS =
            List.of("info", "ajuda", "reload", "armazenamento", "storage", "jogador", "era");

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final StorageManager storage;
    private final PlayerManager playerManager;
    private final EraManager eraManager;

    public ThreeErasCommand(ThreeErasPlugin plugin, Messages messages,
                            StorageManager storage, PlayerManager playerManager,
                            EraManager eraManager) {
        this.plugin = plugin;
        this.messages = messages;
        this.storage = storage;
        this.playerManager = playerManager;
        this.eraManager = eraManager;
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
            case "jogador" -> handleJogador(sender, args);
            case "era" -> handleEra(sender, args);
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
        sender.sendMessage(messages.plain("comando.ajuda.jogador"));
        sender.sendMessage(messages.plain("comando.ajuda.era"));
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
            eraManager.load();
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

    private void handleJogador(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(messages.prefixed("comando.jogador.uso"));
                return;
            }
            sendPlayerInfo(sender, playerManager.get(player));
            return;
        }
        String nick = args[1];
        if (!sender.hasPermission(PERMISSION) && sender instanceof Player self
                && !self.getName().equalsIgnoreCase(nick)) {
            sender.sendMessage(messages.prefixed("comando.jogador.outro-sem-permissao"));
            return;
        }
        PlayerData data = playerManager.findByName(nick);
        if (data == null) {
            sender.sendMessage(messages.message("comando.jogador.nao-encontrado", "{nick}", nick));
            return;
        }
        sendPlayerInfo(sender, data);
    }

    private void sendPlayerInfo(CommandSender sender, PlayerData data) {
        sender.sendMessage(messages.message("comando.jogador.info.titulo", "{nick}", data.getNome()));
        sender.sendMessage(messages.message("comando.jogador.info.era", "{era}", data.getEraAtual()));
        sender.sendMessage(messages.message("comando.jogador.info.primeiro",
                "{data}", DATA_HORA.format(Instant.ofEpochMilli(data.getPrimeiroJoin()))));
        sender.sendMessage(messages.message("comando.jogador.info.visto",
                "{data}", DATA_HORA.format(Instant.ofEpochMilli(data.getUltimoVisto()))));
        sender.sendMessage(messages.message("comando.jogador.info.tempo",
                "{tempo}", formatarTempo(data.getTempoJogadoSegundos())));
    }

    private static String formatarTempo(long segundos) {
        long horas = segundos / 3600L;
        long minutos = (segundos % 3600L) / 60L;
        long segs = segundos % 60L;
        if (horas > 0) {
            return String.format(Locale.ROOT, "%dh %02dmin %02ds", horas, minutos, segs);
        }
        if (minutos > 0) {
            return String.format(Locale.ROOT, "%dmin %02ds", minutos, segs);
        }
        return String.format(Locale.ROOT, "%ds", segs);
    }

    private void handleEra(CommandSender sender, String[] args) {
        if (args.length == 1 || args[1].equalsIgnoreCase("listar")) {
            sendEraLista(sender);
            return;
        }
        if (args[1].equalsIgnoreCase("info")) {
            if (args.length < 3) {
                sender.sendMessage(messages.prefixed("comando.era.uso"));
                return;
            }
            sendEraInfo(sender, args[2]);
            return;
        }
        sender.sendMessage(messages.prefixed("comando.era.uso"));
    }

    private void sendEraLista(CommandSender sender) {
        if (eraManager.total() == 0) {
            sender.sendMessage(messages.message("comando.era.listar.vazio"));
            return;
        }
        sender.sendMessage(messages.message("comando.era.listar.titulo",
                "{total}", String.valueOf(eraManager.total())));
        for (Era era : eraManager.listar()) {
            sender.sendMessage(messages.message("comando.era.listar.linha",
                    "{ordem}", String.valueOf(era.getOrdem()),
                    "{nome}", era.getNome(),
                    "{mundo}", eraManager.getMundoExibicao(era),
                    "{estado}", eraManager.getEstadoMundo(era)));
        }
    }

    private void sendEraInfo(CommandSender sender, String id) {
        Era era = eraManager.getPorId(id);
        if (era == null) {
            sender.sendMessage(messages.message("comando.era.nao-encontrada", "{id}", id));
            return;
        }
        sender.sendMessage(messages.message("comando.era.info.titulo", "{nome}", era.getNome()));
        sender.sendMessage(messages.message("comando.era.info.ordem",
                "{ordem}", String.valueOf(era.getOrdem())));
        sender.sendMessage(messages.message("comando.era.info.mundo",
                "{mundo}", eraManager.getMundoExibicao(era)));
        sender.sendMessage(messages.message("comando.era.info.estado",
                "{estado}", eraManager.getEstadoMundo(era)));
        sender.sendMessage(messages.message("comando.era.info.descricao",
                "{descricao}", era.getDescricao()));
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
