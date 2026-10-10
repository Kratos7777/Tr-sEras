package br.com.threeeras.boss;

/**
 * Definicao de um chefe (boss).
 *
 * <p>POJO puro; spawn, efeitos de dominio e limpeza sao responsabilidade
 * do {@link BossManager}.</p>
 */
public final class BossDefinition {

    private final String id;
    private final String nome;
    private final String era;
    private final String entidade;
    private final double vida;
    private final double dominioRaio;
    private final int lentidaoAmplificador;
    private final int fraquezaAmplificador;
    private final int pilares;

    public BossDefinition(String id, String nome, String era, String entidade, double vida,
                          double dominioRaio, int lentidaoAmplificador,
                          int fraquezaAmplificador, int pilares) {
        this.id = id;
        this.nome = nome;
        this.era = era;
        this.entidade = entidade;
        this.vida = vida;
        this.dominioRaio = dominioRaio;
        this.lentidaoAmplificador = lentidaoAmplificador;
        this.fraquezaAmplificador = fraquezaAmplificador;
        this.pilares = pilares;
    }

    public String getId() {
        return id;
    }

    /** Nome de exibicao (aceita codigos & de cor). */
    public String getNome() {
        return nome;
    }

    public String getEra() {
        return era;
    }

    /** Nome do EntityType do Bukkit (ex.: RAVAGER). */
    public String getEntidade() {
        return entidade;
    }

    public double getVida() {
        return vida;
    }

    /** Raio, em blocos, do efeito de dominio. */
    public double getDominioRaio() {
        return dominioRaio;
    }

    public int getLentidaoAmplificador() {
        return lentidaoAmplificador;
    }

    public int getFraquezaAmplificador() {
        return fraquezaAmplificador;
    }

    /** Quantidade de pilares que sustentam o dominio. */
    public int getPilares() {
        return pilares;
    }
}
