package br.com.threeeras;

import br.com.threeeras.boss.BossListener;
import br.com.threeeras.boss.BossManager;
import br.com.threeeras.command.ThreeErasCommand;
import br.com.threeeras.config.Messages;
import br.com.threeeras.era.Era;
import br.com.threeeras.era.EraManager;
import br.com.threeeras.estrutura.GerenciadorEstruturas;
import br.com.threeeras.estrutura.SetupService;
import br.com.threeeras.evento.EventoManager;
import br.com.threeeras.dungeon.DungeonManager;
import br.com.threeeras.modulo.ArmazenamentoModule;
import br.com.threeeras.modulo.CompatibilidadeModule;
import br.com.threeeras.modulo.JogabilidadeModule;
import br.com.threeeras.item.ItemManager;
import br.com.threeeras.lore.LoreManager;
import br.com.threeeras.npc.NpcInteractListener;
import br.com.threeeras.npc.NpcManager;
import br.com.threeeras.player.PlayerManager;
import br.com.threeeras.quest.QuestManager;
import br.com.threeeras.quest.QuestProgressListener;
import br.com.threeeras.rota.RotaManager;
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
    private QuestManager questManager;
    private BossManager bossManager;
    private EventoManager eventoManager;
    private RotaManager rotaManager;
    private GerenciadorEstruturas gerenciadorEstruturas;
    private SetupService setupService;
    private DungeonManager dungeonManager;
    private CompatibilidadeModule compatibilidade;
    private ArmazenamentoModule armazenamentoModule;
    private JogabilidadeModule jogabilidade;

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

        questManager = new QuestManager(this);
        questManager.load();
        new QuestProgressListener(this, questManager).register();

        dungeonManager = new DungeonManager(this);
        dungeonManager.load();

        bossManager = new BossManager(this);
        bossManager.load();
        new BossListener(this, bossManager).register();

        eventoManager = new EventoManager(this);
        eventoManager.load();
        eventoManager.start();

        rotaManager = new RotaManager(this);
        rotaManager.load();
        rotaManager.register();

        gerenciadorEstruturas = new GerenciadorEstruturas(this);
        setupService = new SetupService(this);

        compatibilidade = new CompatibilidadeModule(this);
        compatibilidade.detectar();
        armazenamentoModule = new ArmazenamentoModule(this);

        storage = new StorageManager(this);
        storage.load();
        storage.startAutosave(getConfig().getInt("armazenamento.autosave-segundos", 300));

        playerManager = new PlayerManager(this, storage, setupService);
        playerManager.register();

        jogabilidade = new JogabilidadeModule(this, setupService);

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
        getLogger().info("Pasta de dados: " + getDataFolder().getAbsolutePath());
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

    public QuestManager getQuestManager() {
        return questManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public EventoManager getEventoManager() {
        return eventoManager;
    }

    public RotaManager getRotaManager() {
        return rotaManager;
    }

    public GerenciadorEstruturas getGerenciadorEstruturas() {
        return gerenciadorEstruturas;
    }

    public SetupService getSetupService() {
        return setupService;
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    public CompatibilidadeModule getCompatibilidade() {
        return compatibilidade;
    }

    public ArmazenamentoModule getArmazenamentoModule() {
        return armazenamentoModule;
    }

    public JogabilidadeModule getJogabilidade() {
        return jogabilidade;
    }

    private void validarEraInicial() {
        String inicial = getConfig().getString("jogadores.era-inicial", "medieval");
        if (!eraManager.existe(inicial)) {
            Era primeira = eraManager.getPrimeira();
            String sugestao = primeira != null
                    ? " Exemplo valido: " + primeira.getId() + "."
                    : "";
            getLogger().warning("jogadores.era-inicial ('" + inicial
                    + "') nao corresponde a nenhuma era de eras.yml."
                    + " Jogadores novos usarao esse valor mesmo assim." + sugestao);
        }
    }

    @Override
    public void onDisable() {
        // Cada etapa protegida por null: se o onEnable falhou no meio da
        // inicializacao, os campos podem nao ter sido atribuidos e o
        // desligamento nao deve lancar excecao que mascare o erro original.
        if (playerManager != null) {
            playerManager.flushCacheToStorage();
        }
        if (bossManager != null) {
            bossManager.shutdown();
        }
        if (eventoManager != null) {
            eventoManager.shutdown();
        }
        if (storage != null) {
            storage.shutdown();
        }
        getLogger().info("ThreeEras foi desligado.");
    }
}
