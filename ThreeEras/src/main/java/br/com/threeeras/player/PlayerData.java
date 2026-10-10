package br.com.threeeras.player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Dados persistentes de um jogador.
 *
 * <p>POJO puro: sem Bukkit, sem I/O. Quem le/escreve no disco e o
 * {@link PlayerManager}.</p>
 */
public final class PlayerData {

    private final UUID uuid;
    private final Set<String> descobertas;
    private String nome;
    private long primeiroJoin;
    private long ultimoVisto;
    private String eraAtual;
    private long tempoJogadoSegundos;

    public PlayerData(UUID uuid, String nome, long primeiroJoin, long ultimoVisto,
                      String eraAtual, long tempoJogadoSegundos, Collection<String> descobertas) {
        this.uuid = uuid;
        this.nome = nome;
        this.primeiroJoin = primeiroJoin;
        this.ultimoVisto = ultimoVisto;
        this.eraAtual = eraAtual;
        this.tempoJogadoSegundos = tempoJogadoSegundos;
        this.descobertas = new LinkedHashSet<>(descobertas);
    }

    /** Cria registro de primeiro acesso. */
    public static PlayerData novo(UUID uuid, String nome, long agora, String eraInicial) {
        return new PlayerData(uuid, nome, agora, agora, eraInicial, 0L, List.of());
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

    /** Ids de lore descobertos (ordem de descoberta preservada). */
    public Set<String> getDescobertas() {
        return Collections.unmodifiableSet(descobertas);
    }

    public boolean temDescoberta(String id) {
        return descobertas.contains(id);
    }

    /** Registra descoberta; true se era nova. */
    public boolean addDescoberta(String id) {
        return descobertas.add(id);
    }
}
