package br.com.threeeras.boss.comportamento;

import br.com.threeeras.boss.BossContexto;
import br.com.threeeras.boss.ComportamentoBoss;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Soberba - "O que nao se curva".
 *
 * <p>Enquanto houver pilares de pe, o dominio desencoraja a oposicao:
 * jogadores proximos sofrem lentidao e fraqueza.</p>
 */
public final class DominioPilares implements ComportamentoBoss {

    @Override
    public void executar(BossContexto ctx) {
        if (ctx.pilaresRestantes() <= 0) {
            return;
        }
        for (var player : ctx.jogadoresProximos(ctx.getDef().getDominioRaio())) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS, 60, ctx.getDef().getLentidaoAmplificador()));
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.WEAKNESS, 60, ctx.getDef().getFraquezaAmplificador()));
        }
    }
}
