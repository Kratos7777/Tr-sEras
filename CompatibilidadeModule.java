package br.com.threeeras.modulo;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Modulo de compatibilidade Java/Bedrock.
 *
 * <p>Detecta Geyser e Floodgate e expõe um relatorio de ambiente. Regras
 * honestas: o plugin NAO promete recursos que o Geyser nao suporta — ele
 * informa o que esta disponivel para o admin decidir.</p>
 */
public final class CompatibilidadeModule {

    private static final String[] NOMES_GEYSER = {"Geyser-Spigot", "Geyser", "geyser"};
    private static final String[] NOMES_FLOODGATE = {"Floodgate-Spigot", "floodgate", "Floodgate"};

    private final ThreeErasPlugin plugin;
    private final Messages messages;

    private boolean geyser;
    private boolean floodgate;

    public CompatibilidadeModule(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    /** Verifica a presenca de Geyser/Floodgate. Chamado no onEnable. */
    public void detectar() {
        geyser = procurarPlugin(NOMES_GEYSER);
        floodgate = procurarPlugin(NOMES_FLOODGATE);
        if (geyser) {
            plugin.getLogger().info("Geyser detectado: jogadores Bedrock podem entrar.");
        } else {
            plugin.getLogger().info("Geyser ausente: servidor aceita apenas Java (por enquanto).");
        }
        if (floodgate) {
            plugin.getLogger().info("Floodgate detectado: autenticacao Bedrock OK.");
        }
    }

    private boolean procurarPlugin(String[] nomes) {
        for (String nome : nomes) {
            Plugin encontrado = Bukkit.getPluginManager().getPlugin(nome);
            if (encontrado != null && encontrado.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public boolean temGeyser() {
        return geyser;
    }

    public boolean temFloodgate() {
        return floodgate;
    }

    /** Envia o relatorio de ambiente. */
    public void relatorio(CommandSender sender) {
        sender.sendMessage(messages.plain("comando.compat.titulo"));
        sender.sendMessage(messages.message("comando.compat.java",
                "{java}", System.getProperty("java.version")));
        sender.sendMessage(messages.message("comando.compat.servidor",
                "{servidor}", Bukkit.getName() + " " + Bukkit.getVersion()));
        sender.sendMessage(messages.message("comando.compat.geyser",
                "{estado}", geyser ? "&apresente (Bedrock pode entrar)" : "&causente (so Java)"));
        sender.sendMessage(messages.message("comando.compat.floodgate",
                "{estado}", floodgate ? "&apresente" : "&causente"));
        sender.sendMessage(messages.message("comando.compat.mundos",
                "{mundos}", String.valueOf(Bukkit.getWorlds().size())));
        int entidades = 0;
        for (World world : Bukkit.getWorlds()) {
            entidades += world.getEntities().size();
        }
        sender.sendMessage(messages.message("comando.compat.entidades",
                "{entidades}", String.valueOf(entidades)));
        sender.sendMessage(messages.message("comando.compat.online",
                "{online}", String.valueOf(Bukkit.getOnlinePlayers().size())));
    }
}
