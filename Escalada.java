package br.com.threeeras.boss.comportamento;

import br.com.threeeras.boss.BossContexto;
import br.com.threeeras.boss.ComportamentoBoss;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Ira - "O Coracao da Ruina".
 *
 * <p>A resistencia alimenta a violencia: cada dano recebido aumenta a
 * forca da criatura. Atacar sem pensar escala o perigo.</p>
 */
public final class Escalada implements ComportamentoBoss {

    private int forca = 0;

    @Override
    public void executar(BossContexto ctx) {
        if (forca <= 0) {
            return;
        }
        ctx.getEntidade().addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH, 60, Math.min(4, forca / 20)));
    }

    @Override
    public void aoReceberDano(BossContexto ctx, EntityDamageByEntityEvent event) {
        int antes = Math.min(4, forca / 20);
        forca += Math.max(1, (int) event.getDamage());
        int depois = Math.min(4, forca / 20);
        if (depois > antes) {
            for (var player : ctx.jogadoresProximos(ctx.getDef().getDominioRaio())) {
                player.sendMessage(ctx.getMessages().prefixed("comando.boss.escalada"));
            }
        }
    }
}
