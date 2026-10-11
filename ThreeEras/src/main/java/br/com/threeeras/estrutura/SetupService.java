package br.com.threeeras.estrutura;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.item.ItemManager;
import br.com.threeeras.npc.NpcManager;
import br.com.threeeras.quest.QuestManager;
import br.com.threeeras.config.Messages;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Prepara o servidor inteiro com um comando: estruturas, NPCs, kit inicial
 * e primeira missao. Feito para rodar uma vez (admin), inclusive pelo
 * console do painel de hospedagem.
 */
public final class SetupService {

    /** Itens do kit inicial (cobre todas as entregas de missao + basico). */
    private static final List<String> KIT = List.of(
            "fragmento-quarto-marco",
            "espada-da-fronteira",
            "marca-de-soldado",
            "mascara-do-culto",
            "mapa-rachado",
            "cristal-de-leitura",
            "estandarte-faixa-vermelha",
            "amostra-7-4");

    /** Primeira missao do jogo. */
    private static final String PRIMEIRA_MISSAO = "conta-que-nao-fecha";

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final ItemManager itemManager;
    private final NpcManager npcManager;
    private final QuestManager questManager;

    public SetupService(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.itemManager = plugin.getItemManager();
        this.npcManager = plugin.getNpcManager();
        this.questManager = plugin.getQuestManager();
    }

    /** Executa a preparacao completa ao redor do local do executor. */
    public boolean executar(Player alvo) {
        Location base = alvo.getLocation().getBlock().getLocation();
        int[] resultado = executarNoLocal(base);
        alvo.sendMessage(messages.message("comando.setup.resultado",
                "{estruturas}", String.valueOf(resultado[0]),
                "{npcs}", String.valueOf(resultado[1])));
        alvo.sendMessage(messages.prefixed("comando.setup.dica"));
        return true;
    }

    /**
     * Prepara o servidor no local informado (usado por jogador e console).
     * Retorna {estruturasConstruidas, npcsCriados}.
     */
    public int[] executarNoLocal(Location base) {
        World mundo = base.getWorld();
        if (mundo == null) {
            return new int[]{0, 0};
        }

        int construidas = 0;
        for (TipoEstrutura tipo : TipoEstrutura.values()) {
            tipo.construir(mundo, localDaEstrutura(base, tipo));
            construidas++;
        }

        int npcs = posicionarNpcs(base);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            darKit(player);
            if (questManager.porId(PRIMEIRA_MISSAO) != null) {
                questManager.iniciar(player, PRIMEIRA_MISSAO);
            }
        }

        plugin.getLogger().info("Setup concluido: " + construidas + " estruturas, "
                + npcs + " NPCs, kit e primeira missao para jogadores online.");
        return new int[]{construidas, npcs};
    }

    /** Espalha as estruturas para nao se sobreporem. */
    private Location localDaEstrutura(Location base, TipoEstrutura tipo) {
        int deslocamento = switch (tipo) {
            case ACAMPAMENTO_MEDIEVAL -> 0;
            case ARENA_SOBERBA -> 40;
            case RUINA_APOCALIPTICA -> 80;
            case LABORATORIO_FUTURISTA -> 120;
            case VILA_MEDIEVAL -> 160;
            case POSTO_APOCALIPTICO -> 200;
            case TORRE_FUTURISTA -> 240;
        };
        return base.clone().add(deslocamento, 0, 0);
    }

    /** Cria todos os NPCs em circulo ao redor da base, por era. */
    private int posicionarNpcs(Location base) {
        int criados = 0;
        double angulo = 0;
        for (br.com.threeeras.npc.NpcDefinition npc : npcManager.listar()) {
            double raio = 6.0 + (criados % 3) * 2.0;
            int x = base.getBlockX() + (int) Math.round(Math.cos(angulo) * raio);
            int z = base.getBlockZ() + (int) Math.round(Math.sin(angulo) * raio);
            Location local = new Location(base.getWorld(), x + 0.5, base.getBlockY() + 1, z + 0.5);
            // No setup os NPCs sao novos: pula a varredura de entidades antigas.
            if (npcManager.criar(npc.getId(), local, false)) {
                criados++;
            }
            angulo += Math.PI / 5;
        }
        return criados;
    }

    /** Entrega o kit inicial (itens de missao + basico). */
    public void darKit(Player player) {
        List<ItemStack> itens = new ArrayList<>();
        for (String id : KIT) {
            ItemStack stack = itemManager.criar(id, 1);
            if (stack != null) {
                itens.add(stack);
            }
        }
        itens.add(new ItemStack(org.bukkit.Material.STONE_SWORD));
        itens.add(new ItemStack(org.bukkit.Material.BREAD, 8));
        itens.add(new ItemStack(org.bukkit.Material.TORCH, 16));
        for (ItemStack sobra : player.getInventory().addItem(itens.toArray(new ItemStack[0])).values()) {
            player.getWorld().dropItem(player.getLocation(), sobra);
        }
    }

    /** Kit dado no primeiro acesso do jogador. */
    public boolean entregarKitPrimeiraVez(Player player) {
        darKit(player);
        player.sendMessage(messages.prefixed("comando.setup.primeira-vez"));
        return true;
    }
}
