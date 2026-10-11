package br.com.threeeras.boss.comportamento;

import br.com.threeeras.boss.BossContexto;
import br.com.threeeras.boss.ComportamentoBoss;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Luxuria - "A Presenca Desejada".
 *
 * <p>Percepcao divergente: cada jogador proximo recebe uma pista
 * diferente sobre onde ve a criatura. Periodicamente, uma fascinacao
 * puxa o jogador em sua direcao.</p>
 */
public final class Percepcao implements ComportamentoBoss {

    private static final String[] DIRECOES = {"ao norte", "ao sul", "a leste", "a oeste"};
    private final Random random = new Random();

    @Override
    public void executar(BossContexto ctx) {
        var boss = ctx.getEntidade();
        var raio = ctx.getDef().getDominioRaio();
        // Vetor da posicao do chefe computado uma vez por execucao.
        var posBoss = boss.getLocation().toVector();
        for (Player player : ctx.jogadoresProximos(raio)) {
            if (ctx.getTick() % 3 == 0) {
                // Fascinacao: puxa o jogador em direcao a criatura.
                Vector direcao = posBoss.clone().subtract(player.getLocation().toVector());
                if (direcao.lengthSquared() > 4.0) {
                    player.setVelocity(direcao.normalize().multiply(0.45));
                    player.sendMessage(ctx.getMessages().prefixed("comando.boss.percepcao-atrai"));
                    continue;
                }
            }
            String direcaoRelativa = DIRECOES[random.nextInt(DIRECOES.length)];
            int distancia = 5 + random.nextInt(21);
            player.sendMessage(ctx.getMessages().message("comando.boss.percepcao",
                    "{direcao}", direcaoRelativa, "{distancia}", String.valueOf(distancia)));
        }
    }
}
