package br.com.threeeras.estrutura;

import br.com.threeeras.ThreeErasPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Constroi estruturas prontas no mundo.
 *
 * <p>Funciona a partir do console tambem (usa o spawn do mundo principal),
 * o que permite preparar o servidor pelo painel da hospedagem sem entrar
 * no jogo.</p>
 */
public final class GerenciadorEstruturas {

    private final ThreeErasPlugin plugin;

    public GerenciadorEstruturas(ThreeErasPlugin plugin) {
        this.plugin = plugin;
    }

    public List<TipoEstrutura> listar() {
        return List.of(TipoEstrutura.values());
    }

    /**
     * Constroi a estrutura. De player: na posicao dele. De console: no spawn
     * do mundo principal. Retorna o local da base ou null se falhou.
     */
    public Location construir(CommandSender sender, String id) {
        TipoEstrutura tipo = TipoEstrutura.porId(id);
        if (tipo == null) {
            return null;
        }
        Location base = resolverLocal(sender);
        if (base == null) {
            return null;
        }
        World mundo = base.getWorld();
        if (mundo == null) {
            return null;
        }
        tipo.construir(mundo, base);
        return base;
    }

    private Location resolverLocal(CommandSender sender) {
        if (sender instanceof Player player) {
            return player.getLocation().getBlock().getLocation().add(0, 1, 0);
        }
        // Console: spawn do mundo principal, deslocado para nao sobrescrever.
        World mundo = plugin.getServer().getWorlds().isEmpty()
                ? null
                : plugin.getServer().getWorlds().get(0);
        if (mundo == null) {
            return null;
        }
        return mundo.getSpawnLocation().getBlock().getLocation().add(20, 1, 20);
    }
}
