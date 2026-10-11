package br.com.threeeras.decisao;

import java.util.List;

/**
 * Uma opcao de decisao: o que ela exige e o que produz.
 */
public final class OpcaoDecisao {

    private final String id;
    private final String texto;
    /** Itens exigidos (id ou id:quantidade) — sao consumidos ao escolher. */
    private final List<String> exige;
    /** Descobertas de lore concedidas (registro da consequencia). */
    private final List<String> concedeLore;
    /** Itens concedidos (id ou id:quantidade). */
    private final List<String> concedeItens;

    public OpcaoDecisao(String id, String texto, List<String> exige,
                        List<String> concedeLore, List<String> concedeItens) {
        this.id = id;
        this.texto = texto;
        this.exige = exige;
        this.concedeLore = concedeLore;
        this.concedeItens = concedeItens;
    }

    public String getId() { return id; }
    public String getTexto() { return texto; }
    public List<String> getExige() { return exige; }
    public List<String> getConcedeLore() { return concedeLore; }
    public List<String> getConcedeItens() { return concedeItens; }
}
