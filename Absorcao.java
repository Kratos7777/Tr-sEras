package br.com.threeeras.boss.comportamento;

import br.com.threeeras.boss.BossContexto;
import br.com.threeeras.boss.ComportamentoBoss;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Avareza - "O Cofre Vivo".
 *
 * <p>Absorve itens dropados ao redor e se cura com eles. O grupo precisa
 * impedir que equipamentos e recursos sejam engolidos.</p>
 */
public final class Absorcao implements ComportamentoBoss {

    private long proximoAviso = 0L;

    @Override
    public void executar(BossContexto ctx) {
        LivingEntity boss = ctx.getEntidade();
        List<Item> itens = new ArrayList<>();
        for (var entity : boss.getNearbyEntities(10.0, 6.0, 10.0)) {
            if (entity instanceof Item item) {
                itens.add(item);
            }
        }
        if (itens.isEmpty()) {
            return;
        }
        double maxVida = boss.getMaxHealth();
        for (Item item : itens) {
            item.remove();
            boss.setHealth(Math.min(maxVida, boss.getHealth() + 2.0));
        }
        long agora = System.currentTimeMillis();
        if (agora >= proximoAviso) {
            proximoAviso = agora + 8000L;
            for (Player player : ctx.jogadoresProximos(ctx.getDef().getDominioRaio())) {
                player.sendMessage(ctx.getMessages().prefixed("comando.boss.absorcao"));
            }
        }
    }
}
