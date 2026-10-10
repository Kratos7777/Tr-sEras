package br.com.threeeras.boss.comportamento;

import br.com.threeeras.boss.BossContexto;
import br.com.threeeras.boss.ComportamentoBoss;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Preguica - "A Coisa que Permanece".
 *
 * <p>O entorno perde movimento: jogadores proximos ficam lentos e
 * exaustos; criaturas passivas da regiao param de existir. A zona
 * "esquece de continuar".</p>
 */
public final class Estase implements ComportamentoBoss {

    private boolean avisou = false;

    @Override
    public void executar(BossContexto ctx) {
        for (Player player : ctx.jogadoresProximos(ctx.getDef().getDominioRaio())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 2));
        }
        // A cada 5 execucoes (~10s), criaturas passivas proximas param.
        if (ctx.getTick() % 5 == 0) {
            for (Entity entity : ctx.getEntidade().getNearbyEntities(12.0, 8.0, 12.0)) {
                if (entity instanceof Animals animal && !animal.isDead()) {
                    animal.setHealth(0.0);
                }
            }
        }
        if (!avisou) {
            avisou = true;
            ctx.avisarProximos("comando.boss.estase", ctx.getDef().getDominioRaio());
        }
    }
}
