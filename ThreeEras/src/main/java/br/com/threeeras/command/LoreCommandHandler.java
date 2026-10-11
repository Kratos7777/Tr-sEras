package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.lore.LoreEntry;
import br.com.threeeras.lore.LoreManager;
import br.com.threeeras.player.PlayerData;
import br.com.threeeras.player.PlayerManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Subcomando /threeeras lore: listar, ver (marca descoberta) e conceder.
 */
public final class LoreCommandHandler {

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final LoreManager loreManager;
    private final PlayerManager playerManager;

    public LoreCommandHandler(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.loreManager = plugin.getLoreManager();
        this.playerManager = plugin.getPlayerManager();
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(messages.prefixed("comando.lore.uso"));
            return;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "listar" -> listar(sender, args);
            case "ver" -> {
                if (args.length < 3) {
                    sender.sendMessage(messages.prefixed("comando.lore.uso"));
                    return;
                }
                ver(sender, args[2]);
            }
            case "conceder" -> conceder(sender, args);
            default -> sender.sendMessage(messages.prefixed("comando.lore.uso"));
        }
    }

    private void listar(CommandSender sender, String[] args) {
        String filtro = args.length > 2 ? args[2].toLowerCase(Locale.ROOT) : null;
        // Jogador ve universais + exclusivas da sua rota; console ve tudo.
        List<LoreEntry> candidatas = sender instanceof Player player
                ? loreManager.listarPara(playerManager.get(player).getRotaAtual())
                : loreManager.listar();
        List<LoreEntry> selecionadas = new ArrayList<>();
        for (LoreEntry entry : candidatas) {
            if (filtro == null
                    || entry.getEra().equalsIgnoreCase(filtro)
                    || String.valueOf(entry.getCamada()).equals(filtro)
                    || entry.getStatus().equalsIgnoreCase(filtro)
                    || entry.getRota().equalsIgnoreCase(filtro)) {
                selecionadas.add(entry);
            }
        }
        if (selecionadas.isEmpty()) {
            sender.sendMessage(messages.message("comando.lore.listar.vazio"));
            return;
        }
        sender.sendMessage(messages.message("comando.lore.listar.titulo",
                "{total}", String.valueOf(selecionadas.size())));
        // Resolve os dados do jogador uma vez (nao por entrada).
        PlayerData dados = sender instanceof Player player ? playerManager.get(player) : null;
        for (LoreEntry entry : selecionadas) {
            sender.sendMessage(messages.message("comando.lore.listar.linha",
                    "{id}", entry.getId(),
                    "{titulo}", entry.getTitulo(),
                    "{era}", entry.getEra(),
                    "{camada}", String.valueOf(entry.getCamada()),
                    "{status}", entry.getStatus(),
                    "{marca}", marca(dados, entry.getId())));
        }
    }

    private void ver(CommandSender sender, String id) {
        LoreEntry entry = loreManager.porId(id);
        if (entry == null) {
            sender.sendMessage(messages.message("comando.lore.nao-encontrada", "{id}", id));
            return;
        }
        // Entrada exclusiva de rota: so quem esta na rota pode ler.
        if (entry.temRota() && sender instanceof Player player) {
            PlayerData dados = playerManager.get(player);
            if (!loreManager.acessoPermitido(entry, dados.getRotaAtual())) {
                sender.sendMessage(messages.message("comando.lore.rota-bloqueada",
                        "{id}", entry.getId()));
                return;
            }
        }
        sender.sendMessage(messages.message("comando.lore.ver.titulo", "{titulo}", entry.getTitulo()));
        sender.sendMessage(messages.message("comando.lore.ver.texto", "{texto}", entry.getTexto()));
        sender.sendMessage(messages.message("comando.lore.ver.meta",
                "{era}", entry.getEra(),
                "{camada}", String.valueOf(entry.getCamada()),
                "{tipo}", entry.getTipo(),
                "{status}", entry.getStatus()));
        if (!(sender instanceof Player player)) {
            return;
        }
        PlayerData data = playerManager.get(player);
        if (data.addDescoberta(entry.getId())) {
            playerManager.save(data);
            sender.sendMessage(messages.message("comando.lore.ver.nova", "{titulo}", entry.getTitulo()));
            // Nova descoberta pode completar objetivo de missao...
            plugin.getQuestManager().verificar(player);
            // ...ou satisfazer o conjunto de descobertas de uma rota.
            plugin.getRotaManager().avaliar(player);
        } else {
            sender.sendMessage(messages.prefixed("comando.lore.ver.repetida"));
        }
    }

    private void conceder(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(messages.prefixed("comando.lore.conceder.uso"));
            return;
        }
        String nick = args[2];
        String id = args[3];
        LoreEntry entry = loreManager.porId(id);
        if (entry == null) {
            sender.sendMessage(messages.message("comando.lore.nao-encontrada", "{id}", id));
            return;
        }
        PlayerData data = playerManager.findByName(nick);
        if (data == null) {
            sender.sendMessage(messages.message("comando.jogador.nao-encontrado", "{nick}", nick));
            return;
        }
        data.addDescoberta(entry.getId());
        playerManager.save(data);
        sender.sendMessage(messages.message("comando.lore.conceder.ok",
                "{id}", entry.getId(), "{nick}", data.getNome()));
    }

    private String marca(PlayerData data, String id) {
        if (data == null) {
            return "&8-";
        }
        return data.temDescoberta(id) ? "&a[lido]" : "&7[nao lido]";
    }
}
