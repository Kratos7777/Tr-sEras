package br.com.threeeras.estrutura;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.BlockState;

import java.util.Random;

/**
 * Estruturas prontas, construidas por codigo (sem arquivos externos, sem
 * WorldEdit — funcionam em qualquer servidor Paper e para jogadores Bedrock).
 *
 * <p>Cada constante sabe se construir; oblocos usados sao classics do
 * vanilla (existem em qualquer versao moderna).</p>
 */
public enum TipoEstrutura {

    ACAMPAMENTO_MEDIEVAL(
            "acampamento-medieval",
            "Acampamento medieval",
            "medieval",
            "Fogueira, baú com suprimentos, tochas e bancadas — o ponto de partida"),
    ARENA_SOBERBA(
            "arena-soberba",
            "Arena do que nao se curva",
            "medieval",
            "Plataforma de combate com 3 pontos de pilar ja marcados"),
    RUINA_APOCALIPTICA(
            "ruina-apocaliptica",
            "Ruina apocaliptica",
            "apocaliptica",
            "Muros quebrados, escombros e um baú esquecido"),
    LABORATORIO_FUTURISTA(
            "laboratorio-futurista",
            "Laboratorio futurista",
            "futurista",
            "Plataforma tecnica com lanternas, estante e podium"),
    VILA_MEDIEVAL(
            "vila-medieval",
            "Vila medieval",
            "medieval",
            "Cinco casas, poco central e caminhos de pedra — pronta para os jogadores habitarem"),
    POSTO_APOCALIPTICO(
            "posto-apocaliptico",
            "Posto apocaliptico",
            "apocaliptica",
            "Fortificacao com muralha, torre de guarda e portao"),
    TORRE_FUTURISTA(
            "torre-futurista",
            "Torre futurista",
            "futurista",
            "Torre de tres andares com escada, lanternas e terraco");

    private static final Random RANDOM = new Random();

    private final String id;
    private final String nome;
    private final String era;
    private final String descricao;

    TipoEstrutura(String id, String nome, String era, String descricao) {
        this.id = id;
        this.nome = nome;
        this.era = era;
        this.descricao = descricao;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEra() { return era; }
    public String getDescricao() { return descricao; }

    public static TipoEstrutura porId(String id) {
        if (id == null) {
            return null;
        }
        for (TipoEstrutura tipo : values()) {
            if (tipo.id.equalsIgnoreCase(id)) {
                return tipo;
            }
        }
        return null;
    }

    /** Constroi a estrutura com a base no local informado. */
    public void construir(World world, Location base) {
        switch (this) {
            case ACAMPAMENTO_MEDIEVAL -> construirAcampamento(world, base);
            case ARENA_SOBERBA -> construirArena(world, base);
            case RUINA_APOCALIPTICA -> construirRuina(world, base);
            case LABORATORIO_FUTURISTA -> construirLaboratorio(world, base);
            case VILA_MEDIEVAL -> construirVila(world, base);
            case POSTO_APOCALIPTICO -> construirPosto(world, base);
            case TORRE_FUTURISTA -> construirTorre(world, base);
        }
    }

    private void construirAcampamento(World world, Location base) {
        int y = base.getBlockY();
        int x0 = base.getBlockX();
        int z0 = base.getBlockZ();
        // Piso 7x7 de madeira com borda de pedra.
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                boolean borda = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                world.getBlockAt(x0 + dx, y, z0 + dz).setType(borda ? Material.COBBLESTONE : Material.OAK_PLANKS);
            }
        }
        // Fogueira no centro.
        world.getBlockAt(x0, y + 1, z0).setType(Material.CAMPFIRE);
        // 4 tochas em postes nos cantos.
        for (int[] canto : new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}}) {
            int x = x0 + canto[0];
            int z = z0 + canto[1];
            world.getBlockAt(x, y + 1, z).setType(Material.OAK_FENCE);
            world.getBlockAt(x, y + 2, z).setType(Material.TORCH);
        }
        // Bau com suprimentos iniciais.
        Location bau = new Location(world, x0 + 2, y + 1, z0 + 2);
        bau.getBlock().setType(Material.CHEST);
        encherBau(world, x0 + 2, y + 1, z0 + 2,
                new org.bukkit.inventory.ItemStack(Material.STONE_SWORD),
                new org.bukkit.inventory.ItemStack(Material.BREAD, 8),
                new org.bukkit.inventory.ItemStack(Material.TORCH, 16));
        // Bancadas de trabalho e fornalha.
        world.getBlockAt(x0 - 2, y + 1, z0 + 2).setType(Material.CRAFTING_TABLE);
        world.getBlockAt(x0 - 2, y + 1, z0 + 1).setType(Material.FURNACE);
        // Troncos decorativos.
        world.getBlockAt(x0 - 3, y + 1, z0).setType(Material.OAK_LOG);
        world.getBlockAt(x0 + 3, y + 1, z0).setType(Material.OAK_LOG);
    }

    private void construirArena(World world, Location base) {
        int y = base.getBlockY();
        int x0 = base.getBlockX();
        int z0 = base.getBlockZ();
        int raio = 8;
        // Plataforma 17x17.
        for (int dx = -raio; dx <= raio; dx++) {
            for (int dz = -raio; dz <= raio; dz++) {
                boolean borda = Math.abs(dx) == raio || Math.abs(dz) == raio;
                world.getBlockAt(x0 + dx, y, z0 + dz).setType(borda ? Material.COBBLESTONE : Material.STONE);
            }
        }
        // Tochas na borda a cada 4 blocos.
        for (int i = -raio; i <= raio; i += 4) {
            world.getBlockAt(x0 + i, y + 1, z0 - raio).setType(Material.TORCH);
            world.getBlockAt(x0 + i, y + 1, z0 + raio).setType(Material.TORCH);
            world.getBlockAt(x0 - raio, y + 1, z0 + i).setType(Material.TORCH);
            world.getBlockAt(x0 + raio, y + 1, z0 + i).setType(Material.TORCH);
        }
        // 3 pontos de pilar em circulo (raio 5), marcados com ferro.
        for (int i = 0; i < 3; i++) {
            double angulo = (2 * Math.PI / 3) * i;
            int px = x0 + (int) Math.round(Math.cos(angulo) * 5);
            int pz = z0 + (int) Math.round(Math.sin(angulo) * 5);
            world.getBlockAt(px, y + 1, pz).setType(Material.IRON_BLOCK);
            world.getBlockAt(px, y + 2, pz).setType(Material.IRON_BLOCK);
            world.getBlockAt(px, y + 3, pz).setType(Material.IRON_BLOCK);
        }
        // Marca central do chefe.
        world.getBlockAt(x0, y + 1, z0).setType(Material.COBBLESTONE);
    }

    private void construirRuina(World world, Location base) {
        int y = base.getBlockY();
        int x0 = base.getBlockX();
        int z0 = base.getBlockZ();
        int raio = 5;
        // Piso irregular de pedra musgosa.
        for (int dx = -raio; dx <= raio; dx++) {
            for (int dz = -raio; dz <= raio; dz++) {
                if (RANDOM.nextInt(10) == 0) {
                    continue;
                }
                world.getBlockAt(x0 + dx, y, z0 + dz)
                        .setType(RANDOM.nextBoolean() ? Material.MOSSY_COBBLESTONE : Material.COBBLESTONE);
            }
        }
        // Muros quebrados no perimetro (altura aleatoria 1..3).
        for (int i = -raio; i <= raio; i++) {
            int altura = 1 + RANDOM.nextInt(3);
            for (int h = 1; h <= altura; h++) {
                world.getBlockAt(x0 + i, y + h, z0 - raio).setType(Material.MOSSY_COBBLESTONE);
                world.getBlockAt(x0 + i, y + h, z0 + raio).setType(Material.MOSSY_COBBLESTONE);
                world.getBlockAt(x0 - raio, y + h, z0 + i).setType(Material.COBBLESTONE);
                world.getBlockAt(x0 + raio, y + h, z0 + i).setType(Material.COBBLESTONE);
            }
        }
        // Dois tocos de parede e um baú.
        world.getBlockAt(x0 - 2, y + 1, z0 - 2).setType(Material.MOSSY_COBBLESTONE);
        world.getBlockAt(x0 - 2, y + 2, z0 - 2).setType(Material.MOSSY_COBBLESTONE);
        world.getBlockAt(x0 + 2, y + 1, z0 + 2).setType(Material.COBBLESTONE);
        world.getBlockAt(x0 + 2, y + 2, z0 + 2).setType(Material.COBBLESTONE);
        world.getBlockAt(x0 + 3, y + 1, z0 + 3).setType(Material.CHEST);
        world.getBlockAt(x0, y + 1, z0).setType(Material.TORCH);
    }

    private void construirLaboratorio(World world, Location base) {
        int y = base.getBlockY();
        int x0 = base.getBlockX();
        int z0 = base.getBlockZ();
        int raio = 5;
        // Piso de quartzo com borda de ferro.
        for (int dx = -raio; dx <= raio; dx++) {
            for (int dz = -raio; dz <= raio; dz++) {
                boolean borda = Math.abs(dx) == raio || Math.abs(dz) == raio;
                world.getBlockAt(x0 + dx, y, z0 + dz).setType(borda ? Material.IRON_BLOCK : Material.QUARTZ_BLOCK);
            }
        }
        // 4 lanternas nos cantos.
        for (int[] canto : new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}}) {
            int x = x0 + canto[0];
            int z = z0 + canto[1];
            world.getBlockAt(x, y + 1, z).setType(Material.IRON_BLOCK);
            world.getBlockAt(x, y + 2, z).setType(Material.REDSTONE_LAMP);
        }
        // "Janelas" de vidro nas laterais.
        for (int i = -3; i <= 3; i += 2) {
            world.getBlockAt(x0 + i, y + 1, z0 - raio).setType(Material.GLASS_PANE);
            world.getBlockAt(x0 + i, y + 1, z0 + raio).setType(Material.GLASS_PANE);
            world.getBlockAt(x0 - raio, y + 1, z0 + i).setType(Material.GLASS_PANE);
            world.getBlockAt(x0 + raio, y + 1, z0 + i).setType(Material.GLASS_PANE);
        }
        // Estante e podium de pesquisa no centro.
        world.getBlockAt(x0, y + 1, z0).setType(Material.LECTERN);
        world.getBlockAt(x0 - 1, y + 1, z0).setType(Material.BOOKSHELF);
        world.getBlockAt(x0 + 1, y + 1, z0).setType(Material.BOOKSHELF);
        world.getBlockAt(x0 - 1, y + 2, z0).setType(Material.BOOKSHELF);
        world.getBlockAt(x0 + 1, y + 2, z0).setType(Material.BOOKSHELF);
        // Bau de arquivo.
        world.getBlockAt(x0 + 3, y + 1, z0 + 3).setType(Material.CHEST);
    }

    private void construirVila(World world, Location base) {
        int y = base.getBlockY();
        int x0 = base.getBlockX();
        int z0 = base.getBlockZ();
        // Poco central.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.getBlockAt(x0 + dx, y, z0 + dz).setType(Material.COBBLESTONE);
            }
        }
        world.getBlockAt(x0, y + 1, z0).setType(Material.WATER);
        world.getBlockAt(x0, y + 2, z0).setType(Material.OAK_FENCE);
        // 5 casas em cruz, cada uma 5x5 com porta e janela.
        int[][] posicoes = {{0, -8}, {8, 0}, {0, 8}, {-8, 0}, {6, 6}};
        for (int[] pos : posicoes) {
            int hx = x0 + pos[0];
            int hz = z0 + pos[1];
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    boolean borda = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    world.getBlockAt(hx + dx, y, hz + dz).setType(borda ? Material.COBBLESTONE : Material.OAK_PLANKS);
                    if (borda) {
                        world.getBlockAt(hx + dx, y + 1, hz + dz).setType(Material.OAK_PLANKS);
                        world.getBlockAt(hx + dx, y + 2, hz + dz).setType(Material.OAK_PLANKS);
                    }
                }
            }
            // Telhado.
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    world.getBlockAt(hx + dx, y + 3, hz + dz).setType(Material.OAK_PLANKS);
                }
            }
            // Porta (virada para o centro) e vela.
            int px = hx;
            int pz = hz + (pos[1] < 0 ? 2 : pos[1] > 0 ? -2 : 0);
            world.getBlockAt(px, y + 1, pz).setType(Material.AIR);
            world.getBlockAt(px, y + 2, pz).setType(Material.AIR);
            world.getBlockAt(hx - 1, y + 1, hz - 1).setType(Material.TORCH);
            // Bau dentro de uma das casas.
            if (pos[0] == 8 && pos[1] == 0) {
                world.getBlockAt(hx - 1, y + 1, hz + 1).setType(Material.CHEST);
                encherBau(world, hx - 1, y + 1, hz + 1,
                        new org.bukkit.inventory.ItemStack(Material.BREAD, 4),
                        new org.bukkit.inventory.ItemStack(Material.TORCH, 8));
            }
        }
        // Caminhos de pedra ligando as casas ao poco.
        for (int[] pos : posicoes) {
            int passos = Math.abs(pos[0]) + Math.abs(pos[1]);
            for (int i = 1; i < passos; i++) {
                int cx = x0 + (int) Math.round(pos[0] * (double) i / passos);
                int cz = z0 + (int) Math.round(pos[1] * (double) i / passos);
                world.getBlockAt(cx, y, cz).setType(Material.COBBLESTONE);
            }
        }
    }

    private void construirPosto(World world, Location base) {
        int y = base.getBlockY();
        int x0 = base.getBlockX();
        int z0 = base.getBlockZ();
        int raio = 7;
        // Piso interno.
        for (int dx = -raio + 1; dx <= raio - 1; dx++) {
            for (int dz = -raio + 1; dz <= raio - 1; dz++) {
                world.getBlockAt(x0 + dx, y, z0 + dz).setType(Material.MOSSY_COBBLESTONE);
            }
        }
        // Muralha 4 de altura com ameacas alternadas.
        for (int i = -raio; i <= raio; i++) {
            for (int h = 1; h <= 4; h++) {
                Material bloco = h == 4 && i % 2 == 0 ? Material.COBBLESTONE : Material.MOSSY_COBBLESTONE;
                world.getBlockAt(x0 + i, y + h, z0 - raio).setType(bloco);
                world.getBlockAt(x0 + i, y + h, z0 + raio).setType(bloco);
                world.getBlockAt(x0 - raio, y + h, z0 + i).setType(bloco);
                world.getBlockAt(x0 + raio, y + h, z0 + i).setType(bloco);
            }
        }
        // Portao duplo ao sul.
        world.getBlockAt(x0 - 1, y + 1, z0 + raio).setType(Material.AIR);
        world.getBlockAt(x0 - 1, y + 2, z0 + raio).setType(Material.AIR);
        world.getBlockAt(x0, y + 1, z0 + raio).setType(Material.AIR);
        world.getBlockAt(x0, y + 2, z0 + raio).setType(Material.AIR);
        // Torre de guarda (canto noroeste).
        for (int h = 1; h <= 6; h++) {
            world.getBlockAt(x0 - raio + 1, y + h, z0 - raio + 1).setType(Material.MOSSY_COBBLESTONE);
        }
        world.getBlockAt(x0 - raio + 1, y + 7, z0 - raio + 1).setType(Material.OAK_FENCE);
        world.getBlockAt(x0 - raio + 1, y + 8, z0 - raio + 1).setType(Material.TORCH);
        // Fogueira e baus internos.
        world.getBlockAt(x0, y + 1, z0).setType(Material.CAMPFIRE);
        world.getBlockAt(x0 + 2, y + 1, z0 + 2).setType(Material.CHEST);
        encherBau(world, x0 + 2, y + 1, z0 + 2,
                new org.bukkit.inventory.ItemStack(Material.IRON_SWORD),
                new org.bukkit.inventory.ItemStack(Material.BREAD, 6),
                new org.bukkit.inventory.ItemStack(Material.TORCH, 12));
    }

    private void construirTorre(World world, Location base) {
        int y = base.getBlockY();
        int x0 = base.getBlockX();
        int z0 = base.getBlockZ();
        // Tres andares 5x5 de quartzo com borda de ferro.
        for (int andar = 0; andar < 3; andar++) {
            int cy = y + andar * 4;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    boolean borda = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    world.getBlockAt(x0 + dx, cy, z0 + dz).setType(borda ? Material.IRON_BLOCK : Material.QUARTZ_BLOCK);
                    if (borda) {
                        world.getBlockAt(x0 + dx, cy + 1, z0 + dz).setType(Material.IRON_BLOCK);
                        world.getBlockAt(x0 + dx, cy + 2, z0 + dz).setType(Material.IRON_BLOCK);
                    }
                }
            }
            // Abertura para escada (buraco no piso do canto).
            world.getBlockAt(x0 + 1, cy, z0 + 1).setType(Material.AIR);
            world.getBlockAt(x0 + 1, cy + 1, z0 + 1).setType(Material.LADDER);
            world.getBlockAt(x0 + 1, cy + 2, z0 + 1).setType(Material.LADDER);
            // Lanterna suspensa no centro.
            world.getBlockAt(x0, cy + 3, z0).setType(Material.REDSTONE_LAMP);
        }
        // Terraço no topo com parapeito.
        int topo = y + 12;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                boolean borda = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                world.getBlockAt(x0 + dx, topo, z0 + dz).setType(borda ? Material.IRON_BLOCK : Material.QUARTZ_BLOCK);
                if (borda) {
                    world.getBlockAt(x0 + dx, topo + 1, z0 + dz).setType(Material.IRON_BLOCK);
                }
            }
        }
        // Observatorio no topo.
        world.getBlockAt(x0, topo + 1, z0).setType(Material.LECTERN);
        world.getBlockAt(x0 - 1, topo + 1, z0).setType(Material.BOOKSHELF);
        world.getBlockAt(x0 + 1, topo + 1, z0).setType(Material.BOOKSHELF);
    }

    private void encherBau(World world, int x, int y, int z, org.bukkit.inventory.ItemStack... itens) {
        BlockState estado = world.getBlockAt(x, y, z).getState();
        if (estado instanceof Chest chest) {
            chest.getInventory().addItem(itens);
            chest.update();
        }
    }
}
