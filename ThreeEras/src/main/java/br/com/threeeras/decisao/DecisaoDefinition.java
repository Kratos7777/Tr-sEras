package br.com.threeeras.decisao;

import java.util.List;

/**
 * Definicao de uma decisao narrativa persistente.
 *
 * <p>POJO puro. Cada opcao exige itens (entregues pelo jogador) e produz
 * consequencias: descobertas de lore (registros do que foi escolhido) e
 * itens concedidos.</p>
 */
public final class DecisaoDefinition {

    private final String id;
    private final String nome;
    private final String descricao;
    private final List<OpcaoDecisao> opcoes;

    public DecisaoDefinition(String id, String nome, String descricao, List<OpcaoDecisao> opcoes) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.opcoes = opcoes;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public List<OpcaoDecisao> getOpcoes() { return opcoes; }

    public OpcaoDecisao getOpcao(String id) {
        if (id == null) {
            return null;
        }
        for (OpcaoDecisao opcao : opcoes) {
            if (opcao.getId().equalsIgnoreCase(id)) {
                return opcao;
            }
        }
        return null;
    }
}
