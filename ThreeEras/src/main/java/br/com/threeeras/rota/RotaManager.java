package br.com.threeeras.rota;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import br.com.threeeras.era.EraManager;
import br.com.threeeras.lore.LoreManager;
import br.com.threeeras.player.PlayerData;
import br.com.threeeras.player.PlayerManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Rotas narrativas por conjunto de descobertas.
 *
 * <p>Regras:</p>
 * <ul>
 *   <li>A rota e atribuída na primeira vez que o conjunto de requisitos
 *       esta satisfeito (avaliacao na ordem do arquivo; a primeira que
 *       bate ganha).</li>
 *   <li>Uma vez atribuida, a rota e permanente (fica salva no jogador).</li>
 *   <li>A rota e concluida quando todas as descobertas de fechamento
 *       tambem estao presentes.</li>
 *   <li>Rotas sem requisitos nunca sao atribuidas automaticamente.</li>
 * </ul>
 *
 * <p>Chamado na thread principal (eventos, comandos, pos-descoberta).</p>
 */
public final class RotaManager implements Listener {

    private static final String FILE_NAME = "rotas.yml";

    private final ThreeErasPlugin plugin;
    private final Messages messages;
    private final LoreManager loreManager;
    private final EraManager eraManager;
    private final PlayerManager playerManager;
    private final File file;
    private final Map<String, RotaDefinition> rotas = new LinkedHashMap<>();

    public RotaManager(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.loreManager = plugin.getLoreManager();
        this.eraManager = plugin.getEraManager();
        this.playerManager = plugin.getPlayerManager();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
    }

    /** Registra os ouvintes deste manager. */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** (Re)carrega rotas.yml do disco. */
    public void load() {
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, RotaDefinition> carregadas = new LinkedHashMap<>();
        ConfigurationSection secao = config.getConfigurationSection("rotas");
        if (secao == null) {
            plugin.getLogger().warning("Nenhuma rota definida em " + FILE_NAME + " (secao 'rotas' ausente).");
        } else {
            for (String id : secao.getKeys(false)) {
                String base = "rotas." + id;
                String era = config.getString(base + ".era", "");
                List<String> requisitos = config.getStringList(base + ".requisitos");
                List<String> fechamento = config.getStringList(base + ".fechamento");

                if (!eraManager.existe(era)) {
                    plugin.getLogger().warning("Rota '" + id + "': era '" + era
                            + "' nao existe em eras.yml.");
                }
                for (String loreId : requisitos) {
                    if (!loreManager.existe(loreId)) {
                        plugin.getLogger().warning("Rota '" + id + "': requisito '"
                                + loreId + "' nao existe em lore.yml.");
                    }
                }
                for (String loreId : fechamento) {
                    if (!loreManager.existe(loreId)) {
                        plugin.getLogger().warning("Rota '" + id + "': fechamento '"
                                + loreId + "' nao existe em lore.yml.");
                    }
                }
                if (requisitos.isEmpty()) {
                    plugin.getLogger().warning("Rota '" + id
                            + "' sem requisitos: nunca sera atribuida automaticamente.");
                }

                carregadas.put(id.toLowerCase(Locale.ROOT), new RotaDefinition(
                        id,
                        config.getString(base + ".nome", id),
                        era,
                        config.getString(base + ".descricao", ""),
                        requisitos,
                        fechamento));
            }
        }

        rotas.clear();
        rotas.putAll(carregadas);
        plugin.getLogger().info("Rotas carregadas (" + rotas.size() + ").");
    }

    public RotaDefinition porId(String id) {
        if (id == null) {
            return null;
        }
        return rotas.get(id.toLowerCase(Locale.ROOT));
    }

    public List<RotaDefinition> listar() {
        return List.copyOf(rotas.values());
    }

    public int total() {
        return rotas.size();
    }

    /**
     * Reavalia o jogador: atribui rota se o conjunto de requisitos de
     * alguma rota (na ordem do arquivo) esta satisfeito; marca fechamento
     * quando as descobertas de fechamento estao completas.
     */
    public void avaliar(Player player) {
        PlayerData data = playerManager.get(player);
        boolean mudou = false;

        if (data.getRotaAtual() == null) {
            for (RotaDefinition rota : rotas.values()) {
                if (rota.getRequisitos().isEmpty()) {
                    continue;
                }
                if (possuiTodas(data, rota.getRequisitos())) {
                    data.definirRota(rota.getId());
                    mudou = true;
                    player.sendMessage(messages.message("comando.rota.atribuida",
                            "{nome}", rota.getNome(), "{descricao}", rota.getDescricao()));
                    break;
                }
            }
        }

        RotaDefinition atual = data.getRotaAtual() != null ? porId(data.getRotaAtual()) : null;
        if (atual != null && !data.isRotaConcluida() && atual.temFechamento()
                && possuiTodas(data, atual.getFechamento())) {
            data.concluirRota();
            mudou = true;
            player.sendMessage(messages.message("comando.rota.fechada", "{nome}", atual.getNome()));
        }

        if (mudou) {
            playerManager.save(data);
        }
    }

    private boolean possuiTodas(PlayerData data, List<String> ids) {
        for (String id : ids) {
            if (!data.temDescoberta(id)) {
                return false;
            }
        }
        return true;
    }

    /** Progresso do fechamento: quantas descobertas de fechamento o jogador tem. */
    public int progressoFechamento(PlayerData data) {
        RotaDefinition rota = porId(data.getRotaAtual());
        if (rota == null || !rota.temFechamento()) {
            return 0;
        }
        int feitas = 0;
        for (String id : rota.getFechamento()) {
            if (data.temDescoberta(id)) {
                feitas++;
            }
        }
        return feitas;
    }

    public int totalFechamento(PlayerData data) {
        RotaDefinition rota = porId(data.getRotaAtual());
        return rota != null ? rota.getFechamento().size() : 0;
    }

    /** Rotas cujos requisitos ja estao satisfeitos (para exibicao). */
    public List<RotaDefinition> disponiveis(PlayerData data) {
        List<RotaDefinition> livres = new ArrayList<>();
        for (RotaDefinition rota : rotas.values()) {
            if (!rota.getRequisitos().isEmpty() && possuiTodas(data, rota.getRequisitos())) {
                livres.add(rota);
            }
        }
        return livres;
    }

    /** Forca a rota do jogador (admin); rescrita direta. */
    public boolean definir(Player player, String id) {
        RotaDefinition rota = porId(id);
        if (rota == null) {
            return false;
        }
        PlayerData data = playerManager.get(player);
        data.definirRota(rota.getId());
        playerManager.save(data);
        return true;
    }

    /** Limpa a rota do jogador (admin). */
    public boolean limpar(Player player) {
        PlayerData data = playerManager.get(player);
        if (data.getRotaAtual() == null && !data.isRotaConcluida()) {
            return false;
        }
        data.limparRota();
        playerManager.save(data);
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        avaliar(event.getPlayer());
    }
}
