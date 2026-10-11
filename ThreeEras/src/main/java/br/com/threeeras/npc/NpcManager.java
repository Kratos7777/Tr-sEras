package br.com.threeeras.npc;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.era.EraManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Gerencia definicoes de NPC (npcs.yml) e as entidades vanilla no mundo.
 *
 * <p>A entidade carrega o id do NPC no PersistentDataContainer, o que
 * permite encontra-la mesmo apos reinicios (a entidade e vanilla e persiste
 * com o mundo). Posicoes sao salvas em npcs.yml na pasta do plugin.</p>
 */
public final class NpcManager {

    private static final String FILE_NAME = "npcs.yml";
    private final NamespacedKey chaveId;

    private final ThreeErasPlugin plugin;
    private final EraManager eraManager;
    private final File file;
    private final Map<String, NpcDefinition> definicoes = new LinkedHashMap<>();

    public NpcManager(ThreeErasPlugin plugin, EraManager eraManager) {
        this.plugin = plugin;
        this.eraManager = eraManager;
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        this.chaveId = new NamespacedKey(plugin, "npc_id");
    }

    /** (Re)carrega npcs.yml do disco (copia do resource na primeira execucao). */
    public void load() {
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, NpcDefinition> carregadas = new LinkedHashMap<>();
        ConfigurationSection secao = config.getConfigurationSection("npcs");
        if (secao == null) {
            plugin.getLogger().warning("Nenhum NPC definido em " + FILE_NAME + " (secao 'npcs' ausente).");
        } else {
            for (String id : secao.getKeys(false)) {
                String base = "npcs." + id;
                String nome = config.getString(base + ".nome", id);
                String era = config.getString(base + ".era", "");
                String tipo = config.getString(base + ".tipo", "VILLAGER");
                List<String> dialogo = config.getStringList(base + ".dialogo");

                if (!eraManager.existe(era)) {
                    plugin.getLogger().warning("NPC '" + id + "': era '" + era
                            + "' nao existe em eras.yml.");
                }

                Map<String, String> porDescoberta = new LinkedHashMap<>();
                ConfigurationSection secaoDesc = config.getConfigurationSection(base + ".por-descoberta");
                if (secaoDesc != null) {
                    for (String loreId : secaoDesc.getKeys(false)) {
                        porDescoberta.put(loreId, secaoDesc.getString(loreId, ""));
                    }
                }

                String rota = config.getString(base + ".rota", "");
                if (!rota.isBlank() && plugin.getRotaManager() != null
                        && plugin.getRotaManager().isCarregado()
                        && !plugin.getRotaManager().existe(rota)) {
                    plugin.getLogger().warning("NPC '" + id + "': rota '" + rota
                            + "' nao existe em rotas.yml.");
                }

                Location local = lerLocal(config, base + ".local");

                carregadas.put(id.toLowerCase(Locale.ROOT),
                        new NpcDefinition(id, nome, era, tipo, dialogo, porDescoberta, local, rota));
            }
        }

        definicoes.clear();
        definicoes.putAll(carregadas);
        plugin.getLogger().info("NPCs carregados (" + definicoes.size() + ").");
    }

    private Location lerLocal(YamlConfiguration config, String base) {
        String mundo = config.getString(base + ".mundo", "");
        if (mundo.isBlank()) {
            return null;
        }
        World world = plugin.getServer().getWorld(mundo);
        if (world == null) {
            plugin.getLogger().warning("NPC com local em mundo nao carregado: " + mundo + ".");
            return null;
        }
        return new Location(world,
                config.getDouble(base + ".x"),
                config.getDouble(base + ".y"),
                config.getDouble(base + ".z"),
                (float) config.getDouble(base + ".yaw"),
                (float) config.getDouble(base + ".pitch"));
    }

    public NpcDefinition porId(String id) {
        if (id == null) {
            return null;
        }
        return definicoes.get(id.toLowerCase(Locale.ROOT));
    }

    public List<NpcDefinition> listar() {
        return List.copyOf(definicoes.values());
    }

    public int total() {
        return definicoes.size();
    }

    public NamespacedKey getChaveId() {
        return chaveId;
    }

    /** Id do NPC gravado na entidade, ou null. */
    public String getIdDaEntidade(Entity entity) {
        return entity.getPersistentDataContainer().get(chaveId, PersistentDataType.STRING);
    }

    /** Entidades no mundo correspondentes ao id (varre os mundos carregados). */
    public List<Entity> encontrarEntidades(String id) {
        List<Entity> encontradas = new ArrayList<>();
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (id.equalsIgnoreCase(getIdDaEntidade(entity))) {
                    encontradas.add(entity);
                }
            }
        }
        return encontradas;
    }

    /**
     * Agrupa todas as entidades de NPC por id em UMA varredura dos mundos.
     * Usado por listagens: evita uma varredura completa por NPC
     * (listar 22 NPCs varria o mundo 22 vezes).
     */
    public Map<String, List<Entity>> entidadesPorId() {
        Map<String, List<Entity>> grupo = new LinkedHashMap<>();
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                String id = getIdDaEntidade(entity);
                if (id == null) {
                    continue;
                }
                grupo.computeIfAbsent(id.toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(entity);
            }
        }
        return grupo;
    }

    /**
     * Cria a entidade na posicao informada e persiste o local.
     * {@code substituir=false} pula a varredura de entidades antigas
     * (usado no setup, onde os NPCs sao novos).
     */
    public boolean criar(String id, Location local) {
        return criar(id, local, true);
    }

    public boolean criar(String id, Location local, boolean substituir) {
        NpcDefinition npc = porId(id);
        if (npc == null) {
            return false;
        }
        // Remove entidades anteriores do mesmo NPC para nao duplicar.
        if (substituir) {
            for (Entity anterior : encontrarEntidades(id)) {
                anterior.remove();
            }
        }
        EntityType tipo = resolverTipo(npc.getTipo());
        Entity entity = local.getWorld().spawnEntity(local, tipo);
        if (!(entity instanceof LivingEntity vivo)) {
            entity.remove();
            return false;
        }
        vivo.setAI(false);
        vivo.setSilent(true);
        vivo.setRemoveWhenFarAway(false);
        vivo.setInvulnerable(true);
        vivo.setCustomName(npc.getNome());
        vivo.setCustomNameVisible(true);
        vivo.getPersistentDataContainer().set(chaveId, PersistentDataType.STRING, npc.getId().toLowerCase(Locale.ROOT));
        npc.setLocal(local);
        salvar();
        return true;
    }

    private EntityType resolverTipo(String nome) {
        try {
            return EntityType.valueOf(nome.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Tipo de entidade desconhecido ('" + nome + "'); usando VILLAGER.");
            return EntityType.VILLAGER;
        }
    }

    /** Remove as entidades do NPC e esquece o local salvo. */
    public boolean remover(String id) {
        NpcDefinition npc = porId(id);
        if (npc == null) {
            return false;
        }
        boolean removeuAlguma = false;
        for (Entity entity : encontrarEntidades(id)) {
            entity.remove();
            removeuAlguma = true;
        }
        npc.setLocal(null);
        salvar();
        return removeuAlguma;
    }

    /** Posicao para teleporte: entidade encontrada ou local salvo. */
    public Location posicaoAtual(String id) {
        for (Entity entity : encontrarEntidades(id)) {
            return entity.getLocation();
        }
        NpcDefinition npc = porId(id);
        return npc != null ? npc.getLocal() : null;
    }

    private void salvar() {
        YamlConfiguration out = new YamlConfiguration();
        for (NpcDefinition npc : definicoes.values()) {
            String base = "npcs." + npc.getId();
            out.set(base + ".nome", npc.getNome());
            out.set(base + ".era", npc.getEra());
            out.set(base + ".tipo", npc.getTipo());
            out.set(base + ".dialogo", npc.getDialogo());
            for (Map.Entry<String, String> entry : npc.getPorDescoberta().entrySet()) {
                out.set(base + ".por-descoberta." + entry.getKey(), entry.getValue());
            }
            Location local = npc.getLocal();
            if (local != null && local.getWorld() != null) {
                out.set(base + ".local.mundo", local.getWorld().getName());
                out.set(base + ".local.x", local.getX());
                out.set(base + ".local.y", local.getY());
                out.set(base + ".local.z", local.getZ());
                out.set(base + ".local.yaw", local.getYaw());
                out.set(base + ".local.pitch", local.getPitch());
            }
        }
        try {
            out.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Falha ao salvar npcs.yml: " + e.getMessage());
        }
    }
}
