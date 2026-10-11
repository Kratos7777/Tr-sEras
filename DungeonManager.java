package br.com.threeeras.dungeon;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.boss.BossManager;
import br.com.threeeras.config.Messages;
import br.com.threeeras.item.ItemManager;
import br.com.threeeras.lore.LoreManager;
import br.com.threeeras.player.PlayerData;
import br.com.threeeras.player.PlayerManager;
import br.com.threeeras.quest.Objetivo;
import br.com.threeeras.quest.QuestManager;
import br.com.threeeras.rota.RotaManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Dungeons (expedicoes) geradas por codigo + logica causal.
 *
 * <p>Rede causal implementada:</p>
 * <ol>
 *   <li>Dungeon concluida (chefe morto ou exploracao) -></li>
 *   <li>concede descobertas de lore -></li>
 *   <li>descobertas podem concluir objetivos de missao e</li>
 *   <li>satisfazer conjuntos de rotas -></li>
 *   <li>rotas destravam NPCs e entradas exclusivas.</li>
 * </ol>
 */
public final class DungeonManager {

    private static final String FILE_NAME = "dungeons.yml";
    private final Random random = new Random();

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final LoreManager loreManager;
    private final ItemManager itemManager;
    private final File file;
    private final Map<String, DungeonDefinition> dungeons = new LinkedHashMap<>();

    /** Jogadores dentro de cada dungeon (para recompensar na conclusao). */
    private final Map<String, Set<UUID>> exploradores = new LinkedHashMap<>();

    public DungeonManager(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.loreManager = plugin.getLoreManager();
        this.itemManager = plugin.getItemManager();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
    }

    /** (Re)carrega dungeons.yml do disco. */
    public void load() {
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        var config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);

        Map<String, DungeonDefinition> carregadas = new LinkedHashMap<>();
        var secao = config.getConfigurationSection("dungeons");
        if (secao == null) {
            plugin.getLogger().warning("Nenhuma dungeon em " + FILE_NAME + " (secao 'dungeons' ausente).");
        } else {
            for (String id : secao.getKeys(false)) {
                String base = "dungeons." + id;
                carregadas.put(id.toLowerCase(Locale.ROOT), new DungeonDefinition(
                        id,
                        config.getString(base + ".nome", id),
                        config.getString(base + ".era", ""),
                        config.getString(base + ".rota", ""),
                        config.getString(base + ".chefe", ""),
                        config.getStringList(base + ".recompensas-lore"),
                        config.getStringList(base + ".recompensas-itens"),
                        config.getInt(base + ".largura-salas", 9),
                        config.getInt(base + ".numero-salas", 4)));
            }
        }

        dungeons.clear();
        dungeons.putAll(carregadas);
        plugin.getLogger().info("Dungeons carregadas (" + dungeons.size() + ").");
    }

    public DungeonDefinition porId(String id) {
        if (id == null) {
            return null;
        }
        return dungeons.get(id.toLowerCase(Locale.ROOT));
    }

    public List<DungeonDefinition> listar() {
        return List.copyOf(dungeons.values());
    }

    public int total() {
        return dungeons.size();
    }

    /** Verifica se o jogador pode entrar (rota exigida). */
    public boolean podeEntrar(Player player, DungeonDefinition dungeon) {
        if (!dungeon.temRota()) {
            return true;
        }
        PlayerData data = plugin.getPlayerManager().get(player);
        String rota = data.getRotaAtual();
        return rota != null && rota.equalsIgnoreCase(dungeon.getRota());
    }

    /**
     * Gera a dungeon no local do jogador e o teleporta para a entrada.
     * Retorna o local de entrada ou null se nao pode entrar.
     */
    public Location entrar(Player player, String id) {
        DungeonDefinition dungeon = porId(id);
        if (dungeon == null) {
            return null;
        }
        if (!podeEntrar(player, dungeon)) {
            return null;
        }
        World mundo = player.getWorld();
        Location base = player.getLocation().getBlock().getLocation().add(0, 1, 0);
        Location entrada = gerar(mundo, base, dungeon);

        exploradores.computeIfAbsent(dungeon.getId().toLowerCase(Locale.ROOT), k -> new java.util.HashSet<>())
                .add(player.getUniqueId());
        player.teleport(entrada);
        return entrada;
    }

    /** Gera as salas em linha com corredores, baús e ponto de chefe. */
    private Location gerar(World mundo, Location base, DungeonDefinition dungeon) {
        int largura = dungeon.getLarguraSalas();
        int passo = largura + 3;
        Material parede = paredeDaEra(dungeon.getEra());
        Material piso = pisoDaEra(dungeon.getEra());

        for (int sala = 0; sala < dungeon.getNumeroSalas(); sala++) {
            int offset = sala * passo;
            int cx = base.getBlockX() + offset;
            int cy = base.getBlockY();
            int cz = base.getBlockZ();
            int meio = largura / 2;

            // Piso e teto da sala.
            for (int dx = -meio; dx <= meio; dx++) {
                for (int dz = -meio; dz <= meio; dz++) {
                    mundo.getBlockAt(cx + dx, cy, cz + dz).setType(piso);
                    mundo.getBlockAt(cx + dx, cy + 4, cz + dz).setType(parede);
                }
            }
            // Paredes.
            for (int i = -meio; i <= meio; i++) {
                for (int h = 1; h <= 3; h++) {
                    mundo.getBlockAt(cx + i, cy + h, cz - meio).setType(parede);
                    mundo.getBlockAt(cx + i, cy + h, cz + meio).setType(parede);
                    mundo.getBlockAt(cx - meio, cy + h, cz + i).setType(parede);
                    mundo.getBlockAt(cx + meio, cy + h, cz + i).setType(parede);
                }
            }
            // Corredor para a proxima sala (buraco na parede do fundo).
            if (sala < dungeon.getNumeroSalas() - 1) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int h = 1; h <= 2; h++) {
                        mundo.getBlockAt(cx + dx, cy + h, cz + meio).setType(Material.AIR);
                        mundo.getBlockAt(cx + passo + dx, cy + h, cz - meio).setType(Material.AIR);
                    }
                }
            }
            // Bau de recompensa em cada sala.
            if (sala > 0 && random.nextBoolean()) {
                int bx = cx + (random.nextBoolean() ? meio - 1 : -(meio - 1));
                int bz = cz + (random.nextBoolean() ? meio - 1 : -(meio - 1));
                mundo.getBlockAt(bx, cy + 1, bz).setType(Material.CHEST);
                encherBau(mundo, bx, cy + 1, bz, dungeon);
            }
            // Tocha central.
            mundo.getBlockAt(cx, cy + 1, cz).setType(Material.TORCH);
        }

        // Ponta de chefe na ultima sala: plataforma marcada.
        int ultima = (dungeon.getNumeroSalas() - 1) * passo;
        int cx = base.getBlockX() + ultima;
        int cy = base.getBlockY();
        int cz = base.getBlockZ();
        int meio = largura / 2;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                mundo.getBlockAt(cx + dx, cy, cz + dz).setType(Material.IRON_BLOCK);
            }
        }
        // Marca de spawn do chefe.
        mundo.getBlockAt(cx, cy + 1, cz).setType(Material.COBBLESTONE);

        Location entrada = new Location(mundo, base.getBlockX() + 0.5, base.getBlockY(), base.getBlockZ() + 0.5);
        if (dungeon.temChefe()) {
            var def = plugin.getBossManager().porId(dungeon.getChefe());
            if (def != null) {
                plugin.getLogger().info("Dungeon '" + dungeon.getId() + "': chefe '"
                        + dungeon.getChefe() + "' deve ser criado na sala final com /threeeras boss criar "
                        + dungeon.getChefe());
            }
        }
        plugin.getLogger().info("Dungeon '" + dungeon.getId() + "' gerada (" + dungeon.getNumeroSalas()
                + " salas) em " + entrada.getBlockX() + "," + entrada.getBlockY() + "," + entrada.getBlockZ() + ".");
        return entrada;
    }

    private Material paredeDaEra(String era) {
        return switch (era) {
            case "apocaliptica" -> Material.MOSSY_COBBLESTONE;
            case "futurista" -> Material.QUARTZ_BLOCK;
            default -> Material.COBBLESTONE;
        };
    }

    private Material pisoDaEra(String era) {
        return switch (era) {
            case "apocaliptica" -> Material.COBBLESTONE;
            case "futurista" -> Material.IRON_BLOCK;
            default -> Material.STONE;
        };
    }

    private void encherBau(World mundo, int x, int y, int z, DungeonDefinition dungeon) {
        BlockState estado = mundo.getBlockAt(x, y, z).getState();
        if (!(estado instanceof Chest chest)) {
            return;
        }
        List<ItemStack> conteudo = new ArrayList<>();
        for (String spec : dungeon.getRecompensasItens()) {
            String[] partes = spec.split(":");
            int qtd = partes.length > 1 ? inteiro(partes[1], 1) : 1;
            ItemStack stack = itemManager.criar(partes[0], qtd);
            if (stack != null) {
                conteudo.add(stack);
            }
        }
        if (conteudo.isEmpty()) {
            conteudo.add(new ItemStack(Material.BREAD, 4));
            conteudo.add(new ItemStack(Material.TORCH, 8));
        }
        chest.getInventory().addItem(conteudo.toArray(new ItemStack[0]));
        chest.update();
    }

    private int inteiro(String texto, int padrao) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    /** Conclui a dungeon para o jogador: paga lore, itens, missoes e rotas. */
    public boolean concluir(Player player, String id) {
        DungeonDefinition dungeon = porId(id);
        if (dungeon == null) {
            return false;
        }
        PlayerData data = plugin.getPlayerManager().get(player);
        if (data.dungeonConcluida(dungeon.getId())) {
            return false;
        }
        // 1. Descobertas de lore (rede causal comeca aqui).
        for (String loreId : dungeon.getRecompensasLore()) {
            if (loreManager.existe(loreId) && data.addDescoberta(loreId)) {
                player.sendMessage(messages.message("comando.dungeon.lore-descoberta",
                        "{lore}", loreId));
            }
        }

        // Marca a dungeon como concluida (objetivo de missao).
        data.marcarDungeonConcluida(dungeon.getId());

        // 2. Itens de recompensa.
        for (String spec : dungeon.getRecompensasItens()) {
            String[] partes = spec.split(":");
            int qtd = partes.length > 1 ? inteiro(partes[1], 1) : 1;
            ItemStack stack = itemManager.criar(partes[0], qtd);
            if (stack != null) {
                for (ItemStack sobra : player.getInventory().addItem(stack).values()) {
                    player.getWorld().dropItem(player.getLocation(), sobra);
                }
            }
        }

        // 3. Progresso de missoes (objetivo do tipo dungeon).
        plugin.getQuestManager().verificar(player);

        // 4. Rotas (conjunto de descobertas pode fechar uma rota).
        plugin.getRotaManager().avaliar(player);

        String chave = dungeon.getId().toLowerCase(Locale.ROOT);
        exploradores.remove(chave);

        plugin.getPlayerManager().save(data);
        player.sendMessage(messages.message("comando.dungeon.concluida", "{nome}", dungeon.getNome()));
        return true;
    }

    /** Chamado quando o chefe de uma dungeon morre (rede causal). */
    public void chefeDerrotado(String dungeonId, Player jogadorProximo) {
        DungeonDefinition dungeon = porId(dungeonId);
        if (dungeon == null || jogadorProximo == null) {
            return;
        }
        concluir(jogadorProximo, dungeonId);
    }

    /** Jogadores registrados como exploradores de uma dungeon. */
    public Set<UUID> getExploradores(String id) {
        return exploradores.getOrDefault(id.toLowerCase(Locale.ROOT), Set.of());
    }
}
