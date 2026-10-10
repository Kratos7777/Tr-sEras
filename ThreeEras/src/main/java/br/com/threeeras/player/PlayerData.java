package br.com.threeeras.player;

import java.util.UUID;

/**
 * Dados persistentes de um jogador.
 *
 * <p>POJO puro: sem Bukkit, sem I/O. Quem le/escreve no disco e o
 * {@link PlayerManager}.</p>
 */
public final class PlayerData {

    private final UUID uuid;
    private String nome;
    private long primeiroJoin;
    private long ultimoVisto;
    private String eraAtual;
    private long tempoJogadoSegundos;

    public PlayerData(UUID uuid, String nome, long primeiroJoin, long ultimoVisto,
                      String eraAtual, long tempoJogadoSegundos) {
        this.uuid = uuid;
        this.nome = nome;
        this.primeiroJoin = primeiroJoin;
        this.ultimoVisto = ultimoVisto;
        this.eraAtual = eraAtual;
        this.tempoJogadoSegundos = tempoJogadoSegundos;
    }

    /** Cria registro de primeiro acesso. */
    public static PlayerData novo(UUID uuid, String nome, long agora, String eraInicial) {
        return new PlayerData(uuid, nome, agora, agora, eraInicial, 0L);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public long getPrimeiroJoin() {
        return primeiroJoin;
    }

    public void setPrimeiroJoin(long primeiroJoin) {
        this.primeiroJoin = primeiroJoin;
    }

    public long getUltimoVisto() {
        return ultimoVisto;
    }

    public void setUltimoVisto(long ultimoVisto) {
        this.ultimoVisto = ultimoVisto;
    }

    public String getEraAtual() {
        return eraAtual;
    }

    public void setEraAtual(String eraAtual) {
        this.eraAtual = eraAtual;
    }

    public long getTempoJogadoSegundos() {
        return tempoJogadoSegundos;
    }

    public void setTempoJogadoSegundos(long tempoJogadoSegundos) {
        this.tempoJogadoSegundos = tempoJogadoSegundos;
    }

    public void adicionarTempo(long segundos) {
        if (segundos > 0) {
            this.tempoJogadoSegundos += segundos;
        }
    }
}
