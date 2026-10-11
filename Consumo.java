package br.com.threeeras.boss.comportamento;

import br.com.threeeras.boss.BossContexto;
import br.com.threeeras.boss.ComportamentoBoss;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Gula - "A Boca sem Fundo".
 *
 * <p>Consome o espaco: blocos do terreno ao redor desaparecem e a arena
 * muda continuamente. O grupo precisa preservar rotas de fuga.</p>
 */
public final class Consumo implements ComportamentoBoss {

    private static final double RAIO = 6.0;
    private static final int PONTOS = 12;

    @Override
    public void executar(BossContexto ctx) {
        if (ctx.getTick() % 5 != 0) {
            return;
        }
        Location centro = ctx.getEntidade().getLocation();
        var mundo = centro.getWorld();
        if (mundo == null) {
            return;
        }
        int removidos = 0;
        for (int i = 0; i < PONTOS; i++) {
            double angulo = (2 * Math.PI / PONTOS) * (i + ctx.getTick());
            int x = centro.getBlockX() + (int) Math.round(Math.cos(angulo) * RAIO);
            int z = centro.getBlockZ() + (int) Math.round(Math.sin(angulo) * RAIO);
            for (int dy = -1; dy <= 1; dy++) {
                int y = centro.getBlockY() + dy;
                if (y <= 0) {
                    continue;
                }
                var bloco = mundo.getBlockAt(x, y, z);
                if (bloco.getType().isAir() || bloco.getType() == Material.BEDROCK) {
                    continue;
                }
                bloco.setType(Material.AIR);
                removidos++;
            }
        }
        if (removidos > 0) {
            ctx.avisarProximos("comando.boss.consumo", ctx.getDef().getDominioRaio());
        }
    }
}
