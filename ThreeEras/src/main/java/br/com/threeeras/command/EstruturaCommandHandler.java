package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.estrutura.GerenciadorEstruturas;
import br.com.threeeras.estrutura.SetupService;
import br.com.threeeras.estrutura.TipoEstrutura;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * Subcomandos /threeeras estrutura e /threeeras setup.
 *
 * <p>Ambos funcionam tambem pelo console (painel da hospedagem): as
 * estruturas sao construidas no spawn do mundo principal.</p>
 */
public final class EstruturaCommandHandler {

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final GerenciadorEstruturas estruturas;
    private final SetupService setup;

    public EstruturaCommandHandler(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.estruturas = plugin.getGerenciadorEstruturas();
        this.setup = plugin.getSetupService();
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(messages.prefixed("comando.estrutura.uso"));
            return;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "listar" -> listar(sender);
            case "construir" -> construir(sender, args);
            default -> sender.sendMessage(messages.prefixed("comando.estrutura.uso"));
        }
    }

    private void listar(CommandSender sender) {
        List<TipoEstrutura> todas = estruturas.listar();
        if (todas.isEmpty()) {
            sender.sendMessage(messages.message("comando.estrutura.listar.vazio"));
            return;
        }
        sender.sendMessage(messages.message("comando.estrutura.listar.titulo",
                "{total}", String.valueOf(todas.size())));
        for (TipoEstrutura tipo : todas) {
            sender.sendMessage(messages.message("comando.estrutura.listar.linha",
                    "{id}", tipo.getId(),
                    "{nome}", tipo.getNome(),
                    "{era}", tipo.getEra(),
                    "{descricao}", tipo.getDescricao()));
        }
    }

    private void construir(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.estrutura.construir.uso"));
            return;
        }
        Location local = estruturas.construir(sender, args[2]);
        if (local == null) {
            sender.sendMessage(messages.message("comando.estrutura.inexistente", "{id}", args[2]));
            return;
        }
        TipoEstrutura tipo = TipoEstrutura.porId(args[2]);
        sender.sendMessage(messages.message("comando.estrutura.construida",
                "{nome}", tipo != null ? tipo.getNome() : args[2],
                "{x}", String.valueOf(local.getBlockX()),
                "{y}", String.valueOf(local.getBlockY()),
                "{z}", String.valueOf(local.getBlockZ())));
    }

    /** /threeeras setup — prepara o servidor inteiro. */
    public void handleSetup(CommandSender sender) {
        if (!sender.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        Player alvo = sender instanceof Player player ? player : null;
        if (alvo == null) {
            // Console: usa o spawn do mundo principal como base.
            var mundos = plugin.getServer().getWorlds();
            if (mundos.isEmpty()) {
                sender.sendMessage(messages.prefixed("comando.setup.sem-mundo"));
                return;
            }
            var spawn = mundos.get(0).getSpawnLocation();
            setup.executarNoLocal(spawn);
            sender.sendMessage(messages.prefixed("comando.setup.ok-console"));
            return;
        }
        if (setup.executar(alvo)) {
            sender.sendMessage(messages.prefixed("comando.setup.ok"));
        } else {
            sender.sendMessage(messages.prefixed("comando.setup.falhou"));
        }
    }
}
