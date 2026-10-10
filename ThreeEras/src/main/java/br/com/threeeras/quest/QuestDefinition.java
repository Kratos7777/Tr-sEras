package br.com.threeeras.quest;

import java.util.List;

/**
 * Definicao de uma missao.
 *
 * <p>POJO puro; a avaliacao de progresso e responsabilidade do
 * {@link QuestManager}.</p>
 */
public final class QuestDefinition {

    private final String id;
    private final String nome;
    private final String era;
    private final String descricao;
    private final String requisito;
    private final List<Objetivo> objetivos;
    private final List<Recompensa> recompensas;

    public QuestDefinition(String id, String nome, String era, String descricao,
                           String requisito, List<Objetivo> objetivos, List<Recompensa> recompensas) {
        this.id = id;
        this.nome = nome;
        this.era = era;
        this.descricao = descricao;
        this.requisito = requisito == null ? "" : requisito;
        this.objetivos = objetivos;
        this.recompensas = recompensas;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEra() {
        return era;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Id de outra missao que precisa estar concluida; "" se nao houver. */
    public String getRequisito() {
        return requisito;
    }

    public boolean temRequisito() {
        return !requisito.isBlank();
    }

    public List<Objetivo> getObjetivos() {
        return objetivos;
    }

    public List<Recompensa> getRecompensas() {
        return recompensas;
    }
}
