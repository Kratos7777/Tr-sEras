package br.com.threeeras.rota;

import java.util.List;

/**
 * Definicao de uma rota narrativa.
 *
 * <p>POJO puro. Uma rota e definida por um CONJUNTO de descobertas: quando
 * o jogador descobriu todas as entradas de {@code requisitos}, a rota e
 * atribuida. O {@code fechamento} lista as descobertas que encerram a rota
 * (o "final" deste caminho).</p>
 */
public final class RotaDefinition {

    private final String id;
    private final String nome;
    private final String era;
    private final String descricao;
    private final List<String> requisitos;
    private final List<String> fechamento;

    public RotaDefinition(String id, String nome, String era, String descricao,
                          List<String> requisitos, List<String> fechamento) {
        this.id = id;
        this.nome = nome;
        this.era = era;
        this.descricao = descricao;
        this.requisitos = requisitos;
        this.fechamento = fechamento;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    /** Era de ancoragem da rota. */
    public String getEra() {
        return era;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Conjunto minimo de descobertas para entrar na rota. */
    public List<String> getRequisitos() {
        return requisitos;
    }

    /** Descobertas que encerram a rota. */
    public List<String> getFechamento() {
        return fechamento;
    }

    public boolean temFechamento() {
        return !fechamento.isEmpty();
    }
}
