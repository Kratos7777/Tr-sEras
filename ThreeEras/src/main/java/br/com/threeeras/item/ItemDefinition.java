package br.com.threeeras.item;

import java.util.List;

/**
 * Definicao de um item personalizado.
 *
 * <p>POJO puro; a construcao do ItemStack e responsabilidade do
 * {@link ItemManager}.</p>
 */
public final class ItemDefinition {

    private final String id;
    private final String era;
    private final String material;
    private final String nome;
    private final int quantidade;
    private final List<String> lore;
    private final List<String> paginas;

    public ItemDefinition(String id, String era, String material, String nome,
                          int quantidade, List<String> lore, List<String> paginas) {
        this.id = id;
        this.era = era;
        this.material = material;
        this.nome = nome;
        this.quantidade = quantidade;
        this.lore = lore;
        this.paginas = paginas;
    }

    public String getId() {
        return id;
    }

    public String getEra() {
        return era;
    }

    /** Nome do Material do Bukkit (ex.: WRITTEN_BOOK). */
    public String getMaterial() {
        return material;
    }

    /** Nome de exibicao (aceita codigos & de cor). */
    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public List<String> getLore() {
        return lore;
    }

    /** Paginas para itens do tipo livro; vazio para itens comuns. */
    public List<String> getPaginas() {
        return paginas;
    }
}
