package br.com.threeeras.dungeon;

import java.util.List;

/**
 * Definicao de uma dungeon (expedicao).
 *
 * <p>POJO puro. A dungeon e gerada por codigo no mundo; ao ser concluida,
 * paga recompensas em lore/itens e pode concluir missoes e rotas.</p>
 */
public final class DungeonDefinition {

    private final String id;
    private final String nome;
    private final String era;
    /** Rota exigida para entrar; "" = livre. */
    private final String rota;
    /** Id do chefe que precisa ser derrotado para concluir; "" = sem chefe. */
    private final String chefe;
    /** Descobertas de lore concedidas ao concluir. */
    private final List<String> recompensasLore;
    /** Itens concedidos ao concluir (id ou id:quantidade). */
    private final List<String> recompensasItens;
    private final int larguraSalas;
    private final int numeroSalas;

    public DungeonDefinition(String id, String nome, String era, String rota, String chefe,
                             List<String> recompensasLore, List<String> recompensasItens,
                             int larguraSalas, int numeroSalas) {
        this.id = id;
        this.nome = nome;
        this.era = era;
        this.rota = rota == null ? "" : rota;
        this.chefe = chefe == null ? "" : chefe;
        this.recompensasLore = recompensasLore;
        this.recompensasItens = recompensasItens;
        this.larguraSalas = Math.max(5, larguraSalas);
        this.numeroSalas = Math.max(1, numeroSalas);
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEra() { return era; }

    /** Rota exigida; "" = livre para qualquer jogador. */
    public String getRota() { return rota; }
    public boolean temRota() { return !rota.isBlank(); }

    /** Id do chefe da dungeon; "" = conclui por comando/exploracao. */
    public String getChefe() { return chefe; }
    public boolean temChefe() { return !chefe.isBlank(); }

    public List<String> getRecompensasLore() { return recompensasLore; }
    public List<String> getRecompensasItens() { return recompensasItens; }
    public int getLarguraSalas() { return larguraSalas; }
    public int getNumeroSalas() { return numeroSalas; }
}
