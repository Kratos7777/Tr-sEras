package br.com.threeeras.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.io.File;

public class LoadCellCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || !args[0].equalsIgnoreCase("loadcell")) {
            sender.sendMessage("Uso: /threeeras loadcell <cell_name>");
            return true;
        }
        String cell = (args.length > 1) ? args[1] : "cell_3_9";
        File base = new File("artifacts/consolidated");
        br.com.threeeras.util.NpzBridge.loadManifest(base);
        sender.sendMessage("Carregando célula: " + cell + " (manifest lido)");
        return true;
    }
}
