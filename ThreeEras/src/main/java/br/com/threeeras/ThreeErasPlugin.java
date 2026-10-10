package br.com.threeeras;

import br.com.threeeras.command.ThreeErasCommand;
import br.com.threeeras.config.Messages;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ThreeErasPlugin extends JavaPlugin {

    private Messages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        messages = new Messages(this);
        messages.load();

        PluginCommand command = getCommand("threeeras");
        if (command == null) {
            getLogger().severe("Comando 'threeeras' ausente no plugin.yml. Desabilitando plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ThreeErasCommand executor = new ThreeErasCommand(this, messages);
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        getLogger().info("ThreeEras foi iniciado! Use /threeeras ajuda");
        if (getConfig().getBoolean("geral.debug")) {
            getLogger().info("Modo debug ativo: diagnostico extras habilitado.");
        }
    }

    public Messages getMessages() {
        return messages;
    }

    @Override
    public void onDisable() {
        getLogger().info("ThreeEras foi desligado.");
    }
}
