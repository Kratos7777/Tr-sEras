package br.com.threeeras.evento;

/**
 * Definicao de um evento agendado.
 *
 * <p>POJO puro; disparo e logica sao do {@link EventManager}.</p>
 */
public final class EventoDefinition {

    private final String id;
    private final String nome;
    private final String descricao;
    private final boolean habilitado;
    private final TipoEvento tipo;
    private final int intervaloMinMinutos;
    private final int intervaloMaxMinutos;
    private final int duracaoSegundos;
    private final String efeito;
    private final int efeitoAmplificador;
    private final String entidade;
    private final int quantidade;
    private final String itemId;
    private final String mensagemInicio;
    private final String mensagemFim;

    public EventoDefinition(String id, String nome, String descricao, boolean habilitado,
                            TipoEvento tipo, int intervaloMinMinutos, int intervaloMaxMinutos,
                            int duracaoSegundos, String efeito, int efeitoAmplificador,
                            String entidade, int quantidade, String itemId,
                            String mensagemInicio, String mensagemFim) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.habilitado = habilitado;
        this.tipo = tipo;
        this.intervaloMinMinutos = Math.max(1, intervaloMinMinutos);
        this.intervaloMaxMinutos = Math.max(this.intervaloMinMinutos, intervaloMaxMinutos);
        this.duracaoSegundos = Math.max(1, duracaoSegundos);
        this.efeito = efeito;
        this.efeitoAmplificador = efeitoAmplificador;
        this.entidade = entidade;
        this.quantidade = Math.max(1, quantidade);
        this.itemId = itemId;
        this.mensagemInicio = mensagemInicio;
        this.mensagemFim = mensagemFim;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public int getIntervaloMinMinutos() {
        return intervaloMinMinutos;
    }

    public int getIntervaloMaxMinutos() {
        return intervaloMaxMinutos;
    }

    public int getDuracaoSegundos() {
        return duracaoSegundos;
    }

    /** Nome do efeito (ver AVISO: cegueira/nausea/lentidao). */
    public String getEfeito() {
        return efeito;
    }

    public int getEfeitoAmplificador() {
        return efeitoAmplificador;
    }

    /** Nome do EntityType (INVASAO). */
    public String getEntidade() {
        return entidade;
    }

    public int getQuantidade() {
        return quantidade;
    }

    /** Id do item (ITEM). */
    public String getItemId() {
        return itemId;
    }

    public String getMensagemInicio() {
        return mensagemInicio;
    }

    public String getMensagemFim() {
        return mensagemFim;
    }
}
