package br.com.threeeras.evento;

/** Tipo de evento agendado. */
public enum TipoEvento {

    /** Aviso narrativo: mensagem + efeito temporario nos jogadores. */
    AVISO,

    /** Invasao: criaturas surgem perto de um jogador aleatorio. */
    INVASAO,

    /** Descoberta: um item do ThreeEras aparece perto de um jogador. */
    ITEM
}
