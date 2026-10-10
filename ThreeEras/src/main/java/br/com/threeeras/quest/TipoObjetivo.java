package br.com.threeeras.quest;

/** Tipo de objetivo de uma missao. */
public enum TipoObjetivo {

    /** Jogador descobriu a entrada de lore indicada. */
    DESCOBERTA,

    /** Jogador possui o item no inventario (nao consome). */
    ITEM,

    /** Jogador entrega (consome) uma quantidade do item. */
    ENTREGAR
}
