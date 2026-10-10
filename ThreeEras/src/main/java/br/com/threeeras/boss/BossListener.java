package br.com.threeeras.boss;

import br.com.threeeras.ThreeErasPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Eventos dos chefes:
 *
 * <ul>
 *   <li>Dano no chefe com pilares de pe e cancelado (o dominio sustenta
 *       a criatura).</li>
 *   <li>Pilar destruido atualiza o contador e avisa os jogadores.</li>
 *   <li>Chefe morto dispara limpeza e aviso geral.</li>
 * </ul>
 */
public final class BossListener implements Listener {

    private static final long AVISO_MS = 5000L;

    private final ThreeErasPlugin plugin;
    private final BossManager bossManager;
    private final Map<UUID, Long> ultimoAviso = new HashMap<>();

    public BossListener(ThreeErasPlugin plugin, BossManager bossManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
    }

    /** Registra os ouvintes deste listener. */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity vitima = event.getEntity();
        String bossId = bossManager.getIdDoBoss(vitima);
        if (bossId == null || !bossManager.dominioAtivo(bossId)) {
            return;
        }
        event.setCancelled(true);
        int restantes = bossManager.pilaresRestantes(bossId);
        String nome = bossManager.nomeLimpo(bossId);
        for (Player player : atacantes(event)) {
            Long ultimo = ultimoAviso.get(player.getUniqueId());
            long agora = System.currentTimeMillis();
            if (ultimo != null && agora - ultimo < AVISO_MS) {
                continue;
            }
            ultimoAviso.put(player.getUniqueId(), agora);
            player.sendMessage(plugin.getMessages().message("comando.boss.protegido",
                    "{nome}", nome, "{restantes}", String.valueOf(restantes)));
        }
    }

    private List<Player> atacantes(EntityDamageByEntityEvent event) {
        List<Player> jogadores = new ArrayList<>();
        if (event.getDamager() instanceof Player player) {
            jogadores.add(player);
        } else if (event.getDamager().getShooter() instanceof Player player) {
            jogadores.add(player);
        }
        return jogadores;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Entity morto = event.getEntity();

        String donoPilar = bossManager.getDonoPilar(morto);
        if (donoPilar != null) {
            bossManager.pilarDestruido(donoPilar, morto.getUniqueId());
            return;
        }

        String bossId = bossManager.getIdDoBoss(morto);
        if (bossId != null) {
            bossManager.bossDerrotado(bossId);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ultimoAviso.remove(event.getPlayer().getUniqueId());
    }
}
