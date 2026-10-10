package br.com.threeeras;

import org.bukkit.plugin.java.JavaPlugin;

public final class ThreeErasPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("ThreeEras foi iniciado!");
        getLogger().info("As Tres Eras estao despertando...");
    }

    @Override
    public void onDisable() {
        getLogger().info("ThreeEras foi desligado.");
    }
}