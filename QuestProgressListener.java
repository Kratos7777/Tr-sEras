package br.com.threeeras.quest;

import br.com.threeeras.ThreeErasPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Reavalia as missoes ativas do jogador em momentos uteis: ao entrar e ao
 * mexer no inventario. A avaliacao so age quando o passo atual fica
 * satisfeito, entao nao ha spam de mensagens.
 */
public final class QuestProgressListener implements Listener {

    private final ThreeErasPlugin plugin;
    private final QuestManager questManager;

    public QuestProgressListener(ThreeErasPlugin plugin, QuestManager questManager) {
        this.plugin = plugin;
        this.questManager = questManager;
    }

    /** Registra os ouvintes deste listener. */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        questManager.verificar(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        // InventoryClickEvent ocorre antes de o Bukkit aplicar a troca.
        verificarNoProximoTick(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            // A pilha so entra no inventario depois do evento.
            verificarNoProximoTick(player);
        }
    }

    private void verificarNoProximoTick(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                questManager.verificar(player);
            }
        }, 1L);
    }
}
