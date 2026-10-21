package br.com.threeeras.sociallink;

public class SocialLinkData {
    private final java.util.UUID playerId;
    private final String npcId;
    private int level;

    public SocialLinkData(java.util.UUID playerId, String npcId, int level) {
        this.playerId = playerId;
        this.npcId = npcId;
        this.level = Math.max(1, Math.min(5, level));
    }

    public java.util.UUID getPlayerId() { return playerId; }
    public String getNpcId() { return npcId; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, Math.min(5, level)); }
}
