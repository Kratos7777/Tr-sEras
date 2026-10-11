package br.com.threeeras.npc;

import org.bukkit.Location;

import java.util.List;
import java.util.Map;

/**
 * Definicao de um NPC (dialogo, nao combate).
 *
 * <p>POJO puro. A entidade vanilla e criada/removida pelo
 * {@link NpcManager}; o dialogo condicional depende das descobertas de
 * lore do jogador.</p>
 */
public final class NpcDefinition {

    private final String id;
    private final String nome;
    private final String era;
    private final String tipo;
    /** Rota do NPC; "" = fala com qualquer um. */
    private final String rota;
    private final List<String> dialogo;
    private final Map<String, String> porDescoberta;
    private Location local;

    public NpcDefinition(String id, String nome, String era, String tipo,
                         List<String> dialogo, Map<String, String> porDescoberta, Location local,
                         String rota) {
        this.id = id;
        this.nome = nome;
        this.era = era;
        this.tipo = tipo;
        this.dialogo = dialogo;
        this.porDescoberta = porDescoberta;
        this.local = local;
        this.rota = rota == null ? "" : rota;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    /** Id da era (validado contra eras.yml na carga). */
    public String getEra() {
        return era;
    }

    /** Nome do EntityType do Bukkit (ex.: VILLAGER). */
    public String getTipo() {
        return tipo;
    }

    /**
     * Rota do NPC; "" = fala com qualquer jogador. NPCs de rota so
     * conversam com quem esta naquela rota (recusam os demais).
     */
    public String getRota() {
        return rota;
    }

    public boolean temRota() {
        return !rota.isBlank();
    }

    /** Linhas de dialogo, na ordem. */
    public List<String> getDialogo() {
        return dialogo;
    }

    /** Linhas extras por descoberta de lore (id da entrada -> fala). */
    public Map<String, String> getPorDescoberta() {
        return porDescoberta;
    }

    /** Ultima posicao conhecida; null se nunca foi criado ou foi removido. */
    public Location getLocal() {
        return local;
    }

    public void setLocal(Location local) {
        this.local = local;
    }
}
