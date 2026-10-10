package br.com.threeeras.lore;

/**
 * Uma entrada de lore (documento, registro, pista).
 *
 * <p>POJO puro. O campo {@code status} congela a origem da informacao:
 * canon, diretriz, proposta ou aberto — nenhum tratamento de lore pode
 * ser promovido a canon sem decisao do criador.</p>
 */
public final class LoreEntry {

    private final String id;
    private final String era;
    private final int camada;
    private final String tipo;
    private final String titulo;
    private final String texto;
    private final String status;
    private final String contradiz;

    public LoreEntry(String id, String era, int camada, String tipo, String titulo,
                     String texto, String status, String contradiz) {
        this.id = id;
        this.era = era;
        this.camada = camada;
        this.tipo = tipo;
        this.titulo = titulo;
        this.texto = texto;
        this.status = status;
        this.contradiz = contradiz == null ? "" : contradiz;
    }

    public String getId() {
        return id;
    }

    /** Id da era (deve existir em eras.yml). */
    public String getEra() {
        return era;
    }

    /** 1 historia | 2 inconsistencias | 3 causalidade | 4 cosmologia | 5 ANORMAL. */
    public int getCamada() {
        return camada;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTexto() {
        return texto;
    }

    /** canon | diretriz | proposta | aberto. */
    public String getStatus() {
        return status;
    }

    /** Id de outra entrada que esta contradiz; "" se nao houver. */
    public String getContradiz() {
        return contradiz;
    }

    public boolean temContradiz() {
        return !contradiz.isBlank();
    }
}
