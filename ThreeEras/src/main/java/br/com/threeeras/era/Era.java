package br.com.threeeras.era;

/**
 * Definicao de uma era. POJO puro, sem Bukkit.
 */
public final class Era {

    private final String id;
    private final String nome;
    private final String mundo;
    private final int ordem;
    private final String descricao;

    public Era(String id, String nome, String mundo, int ordem, String descricao) {
        this.id = id;
        this.nome = nome;
        this.mundo = mundo == null ? "" : mundo;
        this.ordem = ordem;
        this.descricao = descricao;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    /** Nome do mundo Bukkit destino; "" se ainda nao configurado. */
    public String getMundo() {
        return mundo;
    }

    public int getOrdem() {
        return ordem;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean temMundo() {
        return !mundo.isBlank();
    }
}
