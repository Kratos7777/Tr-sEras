package br.com.threeeras.config;

import br.com.threeeras.ThreeErasPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Carrega e fornece as mensagens do plugin (messages.yml).
 *
 * <p>Responsabilidade unica: leitura de textos. Nao guarda estado de jogo.</p>
 */
public final class Messages {

    private static final String FILE_NAME = "messages.yml";
    private static final String DEFAULT_PREFIX = "&8[&bThreeEras&8]&r";

    private final ThreeErasPlugin plugin;
    private YamlConfiguration config;

    public Messages(ThreeErasPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * (Re)carrega o arquivo do disco. Cria o arquivo padrao se nao existir.
     * Se o YAML estiver corrompido, o Bukkit registra o erro e mantem os
     * valores padrao; o estado anterior em memoria e descartado.
     */
    public void load() {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /** Texto cru, sem prefixo e sem formatar. */
    public String raw(String path) {
        String value = config.getString(path);
        if (value == null) {
            plugin.getLogger().warning("Mensagem ausente em messages.yml: " + path);
            return "&c<mensagem ausente: " + path + ">";
        }
        return value;
    }

    /** Mensagem com o prefixo do plugin. */
    public Component prefixed(String path) {
        return message(path);
    }

    /**
     * Mensagem com prefixo e substituicao de placeholders.
     * Os replacements vao em pares: {"{de}", "para", "{outro}", "valor"}.
     */
    public Component message(String path, String... replacements) {
        String text = raw(path);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace(replacements[i], replacements[i + 1]);
        }
        return component(prefix() + " " + text);
    }

    /** Mensagem sem prefixo (para listas e informativos). */
    public Component plain(String path) {
        return component(raw(path));
    }

    /** Converte texto cru (com códigos &) em Component. */
    public Component component(String rawText) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(rawText);
    }

    private String prefix() {
        String value = config.getString("prefix", DEFAULT_PREFIX);
        return value == null ? DEFAULT_PREFIX : value;
    }
}
