package br.com.threeeras.boss;

/**
 * Regra anomala de um chefe: o comportamento executado a cada tick da
 * tarefa de dominio (2 em 2 segundos) enquanto o chefe existe.
 *
 * <p>Cada implementacao mantem seu proprio estado (contadores, copias);
 * existe uma instancia por chefe, criada na carga.</p>
 */
public interface ComportamentoBoss {

    /** Executa o comportamento periodico. */
    void executar(BossContexto ctx);

    /** Hook opcional chamado quando o chefe recebe dano. */
    default void aoReceberDano(BossContexto ctx, org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        // padrao: nao faz nada
    }

    /** Hook opcional chamado logo apos o spawn do chefe. */
    default void aoNascer(BossContexto ctx) {
        // padrao: nao faz nada
    }
}
