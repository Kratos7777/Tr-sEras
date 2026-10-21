package br.com.threeeras.sociallink;

import br.com.threeeras.ThreeErasPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SocialLinkManager {
    private final ThreeErasPlugin plugin;
    private final Map<String, SocialLinkData> links = new HashMap<>();

    public SocialLinkManager(ThreeErasPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() { }

    public SocialLinkData get(UUID playerId, String npcId) {
        String key = playerId + ":" + npcId;
        return links.computeIfAbsent(key, k -> new SocialLinkData(playerId, npcId, 1));
    }

    public void setLevel(UUID playerId, String npcId, int level) {
        get(playerId, npcId).setLevel(level);
    }
}
