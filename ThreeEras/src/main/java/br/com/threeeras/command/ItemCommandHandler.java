package br.com.threeeras.command;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.item.ItemDefinition;
import br.com.threeeras.item.ItemManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

/**
 * Subcomando /threeeras item: listar, info, dar e identificar.
 */
public final class ItemCommandHandler {

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final ItemManager itemManager;

    public ItemCommandHandler(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.itemManager = plugin.getItemManager();
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(messages.prefixed("comando.item.uso"));
            return;
        }
        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "listar" -> listar(sender);
            case "info" -> info(sender, args);
            case "dar" -> dar(sender, args);
            case "identificar" -> identificar(sender);
            default -> sender.sendMessage(messages.prefixed("comando.item.uso"));
        }
    }

    private void listar(CommandSender sender) {
        List<ItemDefinition> itens = itemManager.listar();
        if (itens.isEmpty()) {
            sender.sendMessage(messages.message("comando.item.listar.vazio"));
            return;
        }
        sender.sendMessage(messages.message("comando.item.listar.titulo",
                "{total}", String.valueOf(itens.size())));
        for (ItemDefinition item : itens) {
            sender.sendMessage(messages.message("comando.item.listar.linha",
                    "{id}", item.getId(),
                    "{nome}", item.getNome(),
                    "{era}", item.getEra(),
                    "{material}", item.getMaterial()));
        }
    }

    private void info(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.item.uso"));
            return;
        }
        ItemDefinition item = itemManager.porId(args[2]);
        if (item == null) {
            sender.sendMessage(messages.message("comando.item.inexistente", "{id}", args[2]));
            return;
        }
        sender.sendMessage(messages.message("comando.item.info.titulo", "{nome}", item.getNome()));
        sender.sendMessage(messages.message("comando.item.info.linha",
                "{material}", item.getMaterial(),
                "{quantidade}", String.valueOf(item.getQuantidade()),
                "{era}", item.getEra()));
        for (String linha : item.getLore()) {
            sender.sendMessage(messages.message("comando.item.info.lore", "{linha}", linha));
        }
    }

    private void dar(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.prefixed("comando.item.dar.uso"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.item.dar.console"));
            return;
        }
        if (!player.hasPermission(ThreeErasCommand.PERMISSION)) {
            sender.sendMessage(messages.prefixed("comando.sem-permissao"));
            return;
        }
        ItemDefinition item = itemManager.porId(args[2]);
        if (item == null) {
            sender.sendMessage(messages.message("comando.item.inexistente", "{id}", args[2]));
            return;
        }
        int quantidade = item.getQuantidade();
        if (args.length >= 4) {
            try {
                quantidade = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(messages.message("comando.item.dar.quantidade-invalida",
                        "{valor}", args[3]));
                return;
            }
            if (quantidade < 1 || quantidade > 64) {
                sender.sendMessage(messages.message("comando.item.dar.quantidade-invalida",
                        "{valor}", args[3]));
                return;
            }
        }
        ItemStack stack = itemManager.criar(item.getId(), quantidade);
        if (stack == null) {
            sender.sendMessage(messages.message("comando.item.inexistente", "{id}", item.getId()));
            return;
        }
        for (ItemStack sobra : player.getInventory().addItem(stack).values()) {
            player.getWorld().dropItem(player.getLocation(), sobra);
        }
        sender.sendMessage(messages.message("comando.item.dar.ok",
                "{nome}", item.getNome(),
                "{quantidade}", String.valueOf(quantidade)));
    }

    private void identificar(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.prefixed("comando.item.dar.console"));
            return;
        }
        ItemStack naMao = player.getInventory().getItemInMainHand();
        if (naMao == null || naMao.getType().isAir()) {
            sender.sendMessage(messages.prefixed("comando.item.identificar.mao-vazia"));
            return;
        }
        String id = itemManager.porItem(naMao);
        if (id == null) {
            sender.sendMessage(messages.prefixed("comando.item.identificar.desconhecido"));
            return;
        }
        ItemDefinition item = itemManager.porId(id);
        String nome = item != null ? item.getNome() : id;
        sender.sendMessage(messages.message("comando.item.identificar.linha",
                "{nome}", nome, "{id}", id));
    }
}
