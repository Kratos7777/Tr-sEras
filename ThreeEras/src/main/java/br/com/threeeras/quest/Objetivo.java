package br.com.threeeras.quest;

/**
 * Objetivo de uma missao.
 *
 * <p>Formato no quests.yml: {@code descoberta:<lore-id>},
 * {@code item:<item-id>} ou {@code entregar:<item-id>:<quantidade>}.</p>
 */
public final class Objetivo {

    private final TipoObjetivo tipo;
    private final String alvo;
    private final int quantidade;

    public Objetivo(TipoObjetivo tipo, String alvo, int quantidade) {
        this.tipo = tipo;
        this.alvo = alvo;
        this.quantidade = Math.max(1, quantidade);
    }

    public TipoObjetivo getTipo() {
        return tipo;
    }

    /** Id da entrada de lore ou do item, conforme o tipo. */
    public String getAlvo() {
        return alvo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    /** Representacao original usada no arquivo. */
    public String getSpec() {
        return tipo.name().toLowerCase() + ":" + alvo
                + (tipo == TipoObjetivo.ENTREGAR && quantidade > 1 ? ":" + quantidade : "");
    }
}
