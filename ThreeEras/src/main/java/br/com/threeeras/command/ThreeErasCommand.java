package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.boss.BossManager;
import br.com.threeeras.config.Messages;
import br.com.threeeras.evento.EventoManager;
import br.com.threeeras.era.Era;
import br.com.threeeras.era.EraManager;
import br.com.threeeras.item.ItemManager;
import br.com.threeeras.lore.LoreManager;
import br.com.threeeras.npc.NpcManager;
import br.com.threeeras.player.PlayerData;
import br.com.threeeras.player.PlayerManager;
import br.com.threeeras.quest.QuestManager;
import br.com.threeeras.storage.StorageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.World;

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
 * jogador, era, lore, npc, item, quest, boss e evento.
 *
 * <p>Os managers sao resolvidos a partir dos getters do plugin: adicionar
 * um subsistema novo nao exige mudar a assinatura do construtor.</p>
 */
public final class ThreeErasCommand implements CommandExecutor, TabCompleter {

    static final String PERMISSION = "threeeras.admin";
    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());
    private static final List<String> SUBCOMMANDS =
            List.of("info", "ajuda", "reload", "armazenamento", "storage", "jogador", "era",
                    "lore", "npc", "npcs", "item", "itens", "quest", "quests", "boss", "bosses",
                    "evento", "eventos");

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final StorageManager storage;
    private final PlayerManager playerManager;
    private final EraManager eraManager;
    private final LoreManager loreManager;
    private final NpcManager npcManager;
    private final ItemManager itemManager;
    private final QuestManager questManager;
    private final BossManager bossManager;
    private final EventoManager eventoManager;
    private final LoreCommandHandler loreHandler;
    private final NpcCommandHandler npcHandler;
    private final ItemCommandHandler itemHandler;
    private final QuestCommandHandler questHandler;
    private final BossCommandHandler bossHandler;
    private final EventoCommandHandler eventoHandler;

    public ThreeErasCommand(ThreeErasPlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
        this.storage = plugin.getStorage();
        this.playerManager = plugin.getPlayerManager();
        this.eraManager = plugin.getEraManager();
        this.loreManager = plugin.getLoreManager();
        this.npcManager = plugin.getNpcManager();
        this.itemManager = plugin.getItemManager();
        this.questManager = plugin.getQuestManager();
        this.bossManager = plugin.getBossManager();
        this.eventoManager = plugin.getEventoManager();
        this.loreHandler = new LoreCommandHandler(plugin);
        this.npcHandler = new NpcCommandHandler(plugin);
        this.itemHandler = new ItemCommandHandler(plugin);
        this.questHandler = new QuestCommandHandler(plugin);
        this.bossHandler = new BossCommandHandler(plugin);
        this.eventoHandler = new EventoCommandHandler(plugin);
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
            case "lore" -> loreHandler.handle(sender, args);
            case "npc", "npcs" -> npcHandler.handle(sender, args);
            case "item", "itens" -> itemHandler.handle(sender, args);
            case "quest", "quests" -> questHandler.handle(sender, args);
            case "boss", "bosses" -> bossHandler.handle(sender, args);
            case "evento", "eventos" -> eventoHandler.handle(sender, args);
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
        sender.sendMessage(messages.plain("comando.ajuda.lore"));
        sender.sendMessage(messages.plain("comando.ajuda.npc"));
        sender.sendMessage(messages.plain("comando.ajuda.item"));
        sender.sendMessage(messages.plain("comando.ajuda.quest"));
        sender.sendMessage(messages.plain("comando.ajuda.boss"));
        sender.sendMessage(messages.plain("comando.ajuda.evento"));
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
            loreManager.load();
            npcManager.load();
            itemManager.load();
            questManager.load();
            bossManager.load();
            eventoManager.load();
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
        if (args[1].equalsIgnoreCase("ir")) {
            if (args.length < 3) {
                sender.sendMessage(messages.prefixed("comando.era.ir.uso"));
                return;
            }
            irParaEra(sender, args[2]);
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

    /**
     * Teleporta o jogador para o mundo da era e atualiza a era do jogador.
     * Restrito a admin enquanto nao existir regra de progressao.
     */
    private void irParaEra(CommandSender sender, String id) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.era.ir.console"));
            return;
        }
        if (!player.hasPermission(PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        Era era = eraManager.getPorId(id);
        if (era == null) {
            sender.sendMessage(messages.message("comando.era.nao-encontrada", "{id}", id));
            return;
        }
        if (!era.temMundo()) {
            sender.sendMessage(messages.message("comando.era.ir.sem-mundo", "{nome}", era.getNome()));
            return;
        }
        World mundo = plugin.getServer().getWorld(era.getMundo());
        if (mundo == null) {
            sender.sendMessage(messages.message("comando.era.ir.mundo-offline",
                    "{mundo}", era.getMundo(), "{nome}", era.getNome()));
            return;
        }
        boolean teleportou = player.teleport(mundo.getSpawnLocation());
        if (!teleportou) {
            plugin.getLogger().severe("Falha ao teleportar " + player.getName()
                    + " para o mundo " + mundo.getName() + ".");
            sender.sendMessage(messages.prefixed("comando.era.ir.falhou"));
            return;
        }
        PlayerData data = playerManager.get(player);
        data.setEraAtual(era.getId());
        playerManager.save(data);
        sender.sendMessage(messages.message("comando.era.ir.ok",
                "{nome}", era.getNome(), "{mundo}", mundo.getName()));
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
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("armazenamento") || args[0].equalsIgnoreCase("storage")) {
                return completarDe(args[1], "info", "salvar", "recarregar", "definir");
            }
            if (args[0].equalsIgnoreCase("era")) {
                return completarDe(args[1], "listar", "info", "ir");
            }
            if (args[0].equalsIgnoreCase("lore")) {
                return completarDe(args[1], "listar", "ver", "conceder");
            }
            if (args[0].equalsIgnoreCase("npc") || args[0].equalsIgnoreCase("npcs")) {
                return completarDe(args[1], "listar", "criar", "remover", "ir");
            }
            if (args[0].equalsIgnoreCase("item") || args[0].equalsIgnoreCase("itens")) {
                return completarDe(args[1], "listar", "info", "dar", "identificar");
            }
            if (args[0].equalsIgnoreCase("quest") || args[0].equalsIgnoreCase("quests")) {
                return completarDe(args[1], "listar", "info", "iniciar", "abandonar");
            }
            if (args[0].equalsIgnoreCase("boss") || args[0].equalsIgnoreCase("bosses")) {
                return completarDe(args[1], "listar", "info", "criar", "remover", "ir");
            }
            if (args[0].equalsIgnoreCase("evento") || args[0].equalsIgnoreCase("eventos")) {
                return completarDe(args[1], "listar", "forcars");
            }
        }
        return List.of();
    }

    private List<String> completarDe(String entrada, String... opcoes) {
        String input = entrada.toLowerCase(Locale.ROOT);
        List<String> suggestions = new ArrayList<>();
        for (String opcao : opcoes) {
            if (opcao.startsWith(input)) {
                suggestions.add(opcao);
            }
        }
        return suggestions;
    }
}
