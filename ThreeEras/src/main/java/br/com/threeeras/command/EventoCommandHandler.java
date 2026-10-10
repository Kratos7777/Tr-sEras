package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.evento.EventoDefinition;
import br.com.threeeras.evento.EventoManager;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;

/**
 * Subcomando /threeeras evento: listar e forcars.
 */
public final class EventoCommandHandler {

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final EventoManager eventoManager;

    public EventoCommandHandler(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.eventoManager = plugin.getEventoManager();
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(messages.prefixed("comando.evento.uso"));
            return;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "listar" -> listar(sender);
            case "forcars", "forcar" -> forcars(sender, args);
            default -> sender.sendMessage(messages.prefixed("comando.evento.uso"));
        }
    }

    private void listar(CommandSender sender) {
        List<EventoDefinition> eventos = eventoManager.listar();
        if (eventos.isEmpty()) {
            sender.sendMessage(messages.message("comando.evento.listar.vazio"));
            return;
        }
        sender.sendMessage(messages.message("comando.evento.listar.titulo",
                "{total}", String.valueOf(eventos.size())));
        for (EventoDefinition evento : eventos) {
            sender.sendMessage(messages.message("comando.evento.listar.linha",
                    "{id}", evento.getId(),
                    "{nome}", evento.getNome(),
                    "{tipo}", evento.getTipo().name().toLowerCase(Locale.ROOT),
                    "{estado}", evento.isHabilitado() ? "ativo" : "desativado"));
        }
    }

    private void forcars(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.evento.forcars.uso"));
            return;
        }
        String id = args[2];
        if (eventoManager.porId(id) == null) {
            sender.sendMessage(messages.message("comando.evento.inexistente", "{id}", id));
            return;
        }
        if (eventoManager.disparar(id)) {
            sender.sendMessage(messages.message("comando.evento.forcars.ok", "{id}", id));
        } else {
            sender.sendMessage(messages.prefixed("comando.evento.forcars.sem-jogadores"));
        }
    }
}
