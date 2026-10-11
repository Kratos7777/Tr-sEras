package br.com.threeeras.player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    private final Map<String, Integer> passosQuests;
    private final Set<String> questsConcluidas;
    private String rotaAtual;
    private boolean rotaConcluida;
    private boolean kitRecebido;
    private String nome;
    private long primeiroJoin;
    private long ultimoVisto;
    private String eraAtual;
    private long tempoJogadoSegundos;

    public PlayerData(UUID uuid, String nome, long primeiroJoin, long ultimoVisto,
                      String eraAtual, long tempoJogadoSegundos, Collection<String> descobertas,
                      Map<String, Integer> passosQuests, Collection<String> questsConcluidas,
                      String rotaAtual, boolean rotaConcluida, boolean kitRecebido) {
        this.uuid = uuid;
        this.nome = nome;
        this.primeiroJoin = primeiroJoin;
        this.ultimoVisto = ultimoVisto;
        this.eraAtual = eraAtual;
        this.tempoJogadoSegundos = tempoJogadoSegundos;
        this.descobertas = new LinkedHashSet<>(descobertas);
        this.passosQuests = new LinkedHashMap<>(passosQuests);
        this.questsConcluidas = new LinkedHashSet<>(questsConcluidas);
        this.rotaAtual = rotaAtual;
        this.rotaConcluida = rotaConcluida;
        this.kitRecebido = kitRecebido;
    }

    /** Cria registro de primeiro acesso. */
    public static PlayerData novo(UUID uuid, String nome, long agora, String eraInicial) {
        return new PlayerData(uuid, nome, agora, agora, eraInicial, 0L,
                List.of(), Map.of(), List.of(), null, false, false);
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

    // ---------- progresso de missoes ----------

    /** Ids das missoes em andamento. */
    public Set<String> getQuestsAtivas() {
        return Collections.unmodifiableSet(passosQuests.keySet());
    }

    /** Passo atual de cada missao em andamento (0 = primeiro objetivo). */
    public Map<String, Integer> getPassosQuests() {
        return Collections.unmodifiableMap(passosQuests);
    }

    /** Ids das missoes concluidas. */
    public Set<String> getQuestsConcluidas() {
        return Collections.unmodifiableSet(questsConcluidas);
    }

    public boolean questAtiva(String id) {
        return passosQuests.containsKey(id);
    }

    public boolean questConcluida(String id) {
        return questsConcluidas.contains(id);
    }

    /** Passo atual da missao; -1 se nao estiver em andamento. */
    public int getPasso(String id) {
        return passosQuests.getOrDefault(id, -1);
    }

    public void iniciarQuest(String id) {
        passosQuests.put(id, 0);
    }

    public void setPasso(String id, int passo) {
        passosQuests.put(id, passo);
    }

    public void marcarQuestConcluida(String id) {
        passosQuests.remove(id);
        questsConcluidas.add(id);
    }

    public void abandonarQuest(String id) {
        passosQuests.remove(id);
    }

    // ---------- rota narrativa ----------

    /** Id da rota atual do jogador; null se nenhuma foi atribuida. */
    public String getRotaAtual() {
        return rotaAtual;
    }

    public boolean isRotaConcluida() {
        return rotaConcluida;
    }

    public void definirRota(String id) {
        this.rotaAtual = id;
    }

    public void concluirRota() {
        this.rotaConcluida = true;
    }

    public void limparRota() {
        this.rotaAtual = null;
        this.rotaConcluida = false;
    }

    // ---------- kit inicial ----------

    public boolean isKitRecebido() {
        return kitRecebido;
    }

    public void marcarKitRecebido() {
        this.kitRecebido = true;
    }
}
