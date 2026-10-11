package br.com.threeeras.quest;

/**
 * Recompensa de uma missao: um item do ThreeEras com quantidade.
 *
 * <p>Formato no quests.yml: {@code <item-id>} ou {@code <item-id>:<quantidade>}.</p>
 */
public final class Recompensa {

    private final String itemId;
    private final int quantidade;

    public Recompensa(String itemId, int quantidade) {
        this.itemId = itemId;
        this.quantidade = Math.max(1, quantidade);
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
