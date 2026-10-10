package br.com.threeeras.boss.comportamento;

import br.com.threeeras.boss.BossContexto;
import br.com.threeeras.boss.ComportamentoBoss;

/**
 * Inveja - "O Reflexo Incompleto".
 *
 * <p>Cria manifestacoes falsas (copias mais fracas e brilhantes). A
 * criatura real nao brilha. Quem ataca uma copia a desfaz e perde tempo.</p>
 */
public final class Reflexo implements ComportamentoBoss {

    private static final int MAX_COPIAS = 2;

    @Override
    public void aoNascer(BossContexto ctx) {
        // A verdadeira nao brilha; as copias, sim.
        ctx.getEntidade().setGlowing(false);
    }

    @Override
    public void executar(BossContexto ctx) {
        if (ctx.getTick() % 10 != 0) {
            return;
        }
        if (ctx.getManager().contarCopias(ctx.getDef().getId()) >= MAX_COPIAS) {
            return;
        }
        if (!ctx.getManager().criarCopia(ctx.getDef().getId())) {
            return;
        }
        ctx.avisarProximos("comando.boss.reflexo-criada", ctx.getDef().getDominioRaio());
    }
}
