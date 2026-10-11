package br.com.threeeras.npc;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.player.PlayerData;
import br.com.threeeras.player.PlayerManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dialogo com NPCs: clique direito na entidade inicia a conversa.
 *
 * <p>Linhas condicionais so aparecem se o jogador ja descobriu a entrada de
 * lore correspondente — o NPC reage ao que voce leu.</p>
 */
public final class NpcInteractListener implements Listener {

    private static final long COOLDOWN_MS = 2000L;

    private final ThreeErasPlugin plugin;
    private final NpcManager npcManager;
    private final PlayerManager playerManager;
    private final Messages messages;
    private final Map<UUID, Long> ultimaInteracao = new HashMap<>();

    public NpcInteractListener(ThreeErasPlugin plugin, NpcManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
        this.playerManager = plugin.getPlayerManager();
        this.messages = plugin.getMessages();
    }

    /** Registra os ouvintes deste listener. */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        String id = npcManager.getIdDaEntidade(entity);
        if (id == null) {
            return;
        }
        NpcDefinition npc = npcManager.porId(id);
        if (npc == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();

        // NPC de rota: so conversa com quem esta na rota certa.
        if (npc.temRota()) {
            PlayerData dados = playerManager.get(player);
            String rota = dados.getRotaAtual();
            if (rota == null || !rota.equalsIgnoreCase(npc.getRota())) {
                player.sendMessage(plugin.getMessages().message("comando.npc.rota-bloqueada",
                        "{nome}", npc.getNome()));
                return;
            }
        }

        long agora = System.currentTimeMillis();
        Long ultima = ultimaInteracao.get(player.getUniqueId());
        if (ultima != null && agora - ultima < COOLDOWN_MS) {
            return;
        }
        ultimaInteracao.put(player.getUniqueId(), agora);

        for (String linha : npc.getDialogo()) {
            player.sendMessage(messages.component(linha));
        }

        PlayerData data = playerManager.get(player);
        for (Map.Entry<String, String> entry : npc.getPorDescoberta().entrySet()) {
            if (data.temDescoberta(entry.getKey())) {
                player.sendMessage(messages.component(entry.getValue()));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ultimaInteracao.remove(event.getPlayer().getUniqueId());
    }
}
