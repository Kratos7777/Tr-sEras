package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.npc.NpcDefinition;
import br.com.threeeras.npc.NpcManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * Subcomando /threeeras npc: listar, criar, remover e ir.
 */
public final class NpcCommandHandler {

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final NpcManager npcManager;

    public NpcCommandHandler(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.npcManager = plugin.getNpcManager();
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(messages.prefixed("comando.npc.uso"));
            return;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "listar" -> listar(sender);
            case "criar" -> criar(sender, args);
            case "remover" -> remover(sender, args);
            case "ir" -> ir(sender, args);
            default -> sender.sendMessage(messages.prefixed("comando.npc.uso"));
        }
    }

    private void listar(CommandSender sender) {
        List<NpcDefinition> npcs = npcManager.listar();
        if (npcs.isEmpty()) {
            sender.sendMessage(messages.message("comando.npc.listar.vazio"));
            return;
        }
        sender.sendMessage(messages.message("comando.npc.listar.titulo",
                "{total}", String.valueOf(npcs.size())));
        // Uma unica varredura dos mundos para todos os NPCs.
        var entidades = npcManager.entidadesPorId();
        for (NpcDefinition npc : npcs) {
            sender.sendMessage(messages.message("comando.npc.listar.linha",
                    "{id}", npc.getId(),
                    "{nome}", npc.getNome(),
                    "{era}", npc.getEra(),
                    "{estado}", estado(npc, entidades)));
        }
    }

    private String estado(NpcDefinition npc, java.util.Map<String, List<org.bukkit.entity.Entity>> entidades) {
        List<org.bukkit.entity.Entity> vivas =
                entidades.getOrDefault(npc.getId().toLowerCase(java.util.Locale.ROOT), List.of());
        if (!vivas.isEmpty()) {
            return "no mundo";
        }
        return npc.getLocal() != null ? "local salvo" : "sem local";
    }

    private void criar(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.npc.criar.uso"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.npc.criar.console"));
            return;
        }
        if (!player.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        String id = args[2];
        NpcDefinition npc = npcManager.porId(id);
        if (npc == null) {
            sender.sendMessage(messages.message("comando.npc.criar.inexistente", "{id}", id));
            return;
        }
        if (npcManager.criar(id, player.getLocation())) {
            sender.sendMessage(messages.message("comando.npc.criar.ok", "{nome}", npc.getNome()));
        } else {
            sender.sendMessage(messages.prefixed("comando.npc.criar.uso"));
        }
    }

    private void remover(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.npc.uso"));
            return;
        }
        if (!sender.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        String id = args[2];
        if (npcManager.porId(id) == null) {
            sender.sendMessage(messages.message("comando.npc.criar.inexistente", "{id}", id));
            return;
        }
        if (npcManager.remover(id)) {
            sender.sendMessage(messages.message("comando.npc.remover.ok", "{id}", id));
        } else {
            sender.sendMessage(messages.message("comando.npc.remover.ausente", "{id}", id));
        }
    }

    private void ir(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.npc.uso"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.npc.criar.console"));
            return;
        }
        if (!player.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        String id = args[2];
        NpcDefinition npc = npcManager.porId(id);
        if (npc == null) {
            sender.sendMessage(messages.message("comando.npc.criar.inexistente", "{id}", id));
            return;
        }
        Location destino = npcManager.posicaoAtual(id);
        if (destino == null) {
            sender.sendMessage(messages.message("comando.npc.ir.inexistente", "{id}", id));
            return;
        }
        player.teleport(destino);
        sender.sendMessage(messages.message("comando.npc.ir.ok", "{nome}", npc.getNome()));
    }
}
