package br.com.threeeras.modulo;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.estrutura.SetupService;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Modulo de jogabilidade: kit sob demanda com cooldown por jogador.
 */
public final class JogabilidadeModule {

    private static final long COOLDOWN_MS = 30 * 60 * 1000L;

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final SetupService setupService;
    private final Map<UUID, Long> ultimoKit = new HashMap<>();

    public JogabilidadeModule(ThreeErasPlugin plugin, SetupService setupService) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.setupService = setupService;
    }

    /** Entrega o kit se o cooldown permite; retorna o resultado. */
    public ResultadoKit darKit(Player player) {
        Long ultimo = ultimoKit.get(player.getUniqueId());
        long agora = System.currentTimeMillis();
        if (ultimo != null && agora - ultimo < COOLDOWN_MS) {
            long minutos = (COOLDOWN_MS - (agora - ultimo)) / 60000L + 1;
            return new ResultadoKit(false, minutos);
        }
        setupService.darKit(player);
        ultimoKit.put(player.getUniqueId(), agora);
        return new ResultadoKit(true, 0);
    }

    /** Resultado de um pedido de kit. */
    public record ResultadoKit(boolean entregue, long minutosRestantes) {
    }
}
