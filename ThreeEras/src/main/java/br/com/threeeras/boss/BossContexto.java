package br.com.threeeras.boss;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Contexto passado aos comportamentos de chefe: acesso ao plugin, as
 * mensagens, a definicao e a entidade viva do chefe.
 */
public final class BossContexto {

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final BossDefinition def;
    private final LivingEntity entidade;
    private final BossManager manager;
    private long tick;

    public BossContexto(ThreeErasPlugin plugin, Messages messages, BossDefinition def,
                        LivingEntity entidade, BossManager manager, long tick) {
        this.plugin = plugin;
        this.messages = messages;
        this.def = def;
        this.entidade = entidade;
        this.manager = manager;
        this.tick = tick;
    }

    public ThreeErasPlugin getPlugin() {
        return plugin;
    }

    public Messages getMessages() {
        return messages;
    }

    public BossDefinition getDef() {
        return def;
    }

    public LivingEntity getEntidade() {
        return entidade;
    }

    public BossManager getManager() {
        return manager;
    }

    /** Contador de execucoes do comportamento (cada 2 segundos). */
    public long getTick() {
        return tick;
    }

    /** Atualiza o contador (o contexto e reutilizado a cada tick). */
    public void setTick(long tick) {
        this.tick = tick;
    }

    public int pilaresRestantes() {
        return manager.pilaresRestantes(def.getId());
    }

    /** Jogadores dentro do raio de efeito do chefe. */
    public List<Player> jogadoresProximos(double raio) {
        List<Player> proximos = new ArrayList<>();
        double raioAoQuadrado = raio * raio;
        for (Player player : entidade.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(entidade.getLocation()) <= raioAoQuadrado) {
                proximos.add(player);
            }
        }
        return proximos;
    }

    /** Envia mensagem (com prefixo) aos jogadores proximos. */
    public void avisarProximos(String path, double raio, String... replacements) {
        for (Player player : jogadoresProximos(raio)) {
            player.sendMessage(messages.message(path, replacements));
        }
    }
}
