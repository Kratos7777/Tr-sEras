package br.com.threeeras.modulo;

import br.com.threeeras.ThreeErasPlugin;
import br.com.threeeras.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modulo de armazenamento avancado: backups do dados.yml.
 *
 * <p>Copia o arquivo de dados para backups/dados-AAAA-MM-DD-HH-MM-SS.yml.
 * A copia roda em task assincrona (I/O de arquivo fora da thread principal).</p>
 */
public final class ArmazenamentoModule {

    private final ThreeErasPlugin plugin;
    private final Messages messages;

    public ArmazenamentoModule(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    /** Cria um backup; retorna o arquivo ou null em falha. */
    public File criarBackup() {
        File dados = plugin.getStorage().getFile();
        if (!dados.exists()) {
            return null;
        }
        File pasta = new File(plugin.getDataFolder(), "backups");
        if (!pasta.exists() && !pasta.mkdirs()) {
            return null;
        }
        String carimbo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        File destino = new File(pasta, "dados-" + carimbo + ".yml");
        try {
            Files.copy(dados.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destino;
        } catch (IOException e) {
            plugin.getLogger().severe("Falha ao criar backup: " + e.getMessage());
            return null;
        }
    }

    /** Backups existentes (mais recentes primeiro). */
    public List<File> listar() {
        File pasta = new File(plugin.getDataFolder(), "backups");
        File[] arquivos = pasta.listFiles((dir, nome) -> nome.startsWith("dados-") && nome.endsWith(".yml"));
        if (arquivos == null) {
            return List.of();
        }
        List<File> lista = new java.util.ArrayList<>(List.of(arquivos));
        lista.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return lista;
    }
}
