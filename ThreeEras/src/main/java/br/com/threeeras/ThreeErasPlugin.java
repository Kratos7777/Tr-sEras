package br.com.threeeras;

import br.com.threeeras.command.ThreeErasCommand;
import br.com.threeeras.config.Messages;
import br.com.threeeras.era.EraManager;
import br.com.threeeras.item.ItemManager;
import br.com.threeeras.lore.LoreManager;
import br.com.threeeras.npc.NpcInteractListener;
import br.com.threeeras.npc.NpcManager;
import br.com.threeeras.player.PlayerManager;
import br.com.threeeras.storage.StorageManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ThreeErasPlugin extends JavaPlugin {

    private Messages messages;
    private StorageManager storage;
    private PlayerManager playerManager;
    private EraManager eraManager;
    private LoreManager loreManager;
    private NpcManager npcManager;
    private ItemManager itemManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        messages = new Messages(this);
        messages.load();

        eraManager = new EraManager(this);
        eraManager.load();
        validarEraInicial();

        loreManager = new LoreManager(this, eraManager);
        loreManager.load();

        npcManager = new NpcManager(this, eraManager);
        npcManager.load();
        new NpcInteractListener(this, npcManager).register();

        itemManager = new ItemManager(this);
        itemManager.load();

        storage = new StorageManager(this);
        storage.load();
        storage.startAutosave(getConfig().getInt("armazenamento.autosave-segundos", 300));

        playerManager = new PlayerManager(this, storage);
        playerManager.register();

        PluginCommand command = getCommand("threeeras");
        if (command == null) {
            getLogger().severe("Comando 'threeeras' ausente no plugin.yml. Desabilitando plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ThreeErasCommand executor = new ThreeErasCommand(this, messages);
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        getLogger().info("ThreeEras foi iniciado! Use /threeeras ajuda");
        if (getConfig().getBoolean("geral.debug")) {
            getLogger().info("Modo debug ativo: diagnostico extras habilitado.");
        }
    }

    public Messages getMessages() {
        return messages;
    }

    public StorageManager getStorage() {
        return storage;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public EraManager getEraManager() {
        return eraManager;
    }

    public LoreManager getLoreManager() {
        return loreManager;
    }

    public NpcManager getNpcManager() {
        return npcManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    private void validarEraInicial() {
        String inicial = getConfig().getString("jogadores.era-inicial", "medieval");
        if (!eraManager.existe(inicial)) {
            getLogger().warning("jogadores.era-inicial ('" + inicial
                    + "') nao corresponde a nenhuma era de eras.yml. Jogadores novos usarao esse valor mesmo assim.");
        }
    }

    @Override
    public void onDisable() {
        // Joga o cache para a memoria do storage e grava sincronamente.
        playerManager.flushCacheToStorage();
        storage.shutdown();
        getLogger().info("ThreeEras foi desligado.");
    }
}
