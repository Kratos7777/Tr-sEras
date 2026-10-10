package br.com.threeeras.storage;

import br.com.threeeras.ThreeErasPlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Persistencia simples em arquivo local (plugins/ThreeEras/dados.yml).
 *
 * <p>Regras de thread observadas aqui:</p>
 * <ul>
 *   <li>O objeto {@code dados} so e modificado na thread principal.</li>
 *   <li>Salvamentos em runtime sao assincronos: a thread principal tira uma
 *       copia (snapshot em texto) e passa a escrita para uma task async.</li>
 *   <li>No desligamento a gravacao e sincrona porque nao ha scheduler
 *       confiavel nesse momento; o arquivo e pequeno por design.</li>
 * </ul>
 */
public final class StorageManager {

    private final ThreeErasPlugin plugin;
    private final File file;
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final AtomicBoolean salvando = new AtomicBoolean(false);

    private YamlConfiguration dados;
    private BukkitTask autosaveTask;
    private int autosaveSegundos;

    public StorageManager(ThreeErasPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "dados.yml");
    }

    /** Carrega o arquivo do disco. Se nao existir, comeca vazio (sera criado ao salvar). */
    public void load() {
        if (file.exists()) {
            dados = YamlConfiguration.loadConfiguration(file);
        } else {
            dados = new YamlConfiguration();
            dirty.set(true);
        }
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public File getFile() {
        return file;
    }

    public int getAutosaveSegundos() {
        return autosaveSegundos;
    }

    public boolean isAutosaveAtivo() {
        return autosaveTask != null && !autosaveTask.isCancelled();
    }

    public Set<String> getRootKeys() {
        return dados.getKeys(false);
    }

    /** Chaves filhas de uma secao (ex.: os UUIDs em "jogadores"). */
    public Set<String> getKeys(String path, boolean deep) {
        if (!dados.isConfigurationSection(path)) {
            return Set.of();
        }
        return dados.getConfigurationSection(path).getKeys(deep);
    }

    public boolean contains(String path) {
        return dados.contains(path);
    }

    public String getString(String path, String def) {
        return dados.getString(path, def);
    }

    public int getInt(String path, int def) {
        return dados.getInt(path, def);
    }

    public long getLong(String path, long def) {
        return dados.getLong(path, def);
    }

    public List<String> getStringList(String path) {
        return dados.getStringList(path);
    }

    public void set(String path, Object value) {
        dados.set(path, value);
        dirty.set(true);
    }

    /**
     * Gravacao assincrona; chamar sempre da thread principal.
     * Se outra gravacao ja estiver em andamento, esta e reagendada para o
     * proximo ciclo (autosave ou desligamento) para evitar escrita fora de ordem.
     */
    public void saveAsync() {
        if (!dirty.get() || !dirty.compareAndSet(true, false)) {
            return;
        }
        if (!salvando.compareAndSet(false, true)) {
            dirty.set(true);
            return;
        }
        final String snapshot;
        try {
            snapshot = dados.saveToString();
        } catch (RuntimeException e) {
            plugin.getLogger().severe("Falha ao serializar dados.yml: " + e.getMessage());
            dirty.set(true);
            salvando.set(false);
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> write(snapshot));
    }

    private void write(String snapshot) {
        try {
            YamlConfiguration copia = new YamlConfiguration();
            copia.loadFromString(snapshot);
            copia.save(file);
            plugin.getLogger().info("dados.yml salvo em disco (" + file.length() + " bytes).");
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Falha ao salvar dados.yml: " + e.getMessage());
            dirty.set(true);
        } finally {
            salvando.set(false);
        }
    }

    public void startAutosave(int seconds) {
        this.autosaveSegundos = seconds;
        stopAutosave();
        if (seconds <= 0) {
            plugin.getLogger().info("Autosave do armazenamento desligado (intervalo 0).");
            return;
        }
        long ticks = seconds * 20L;
        autosaveTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::saveAsync, ticks, ticks);
        plugin.getLogger().info("Autosave do armazenamento a cada " + seconds + "s.");
    }

    public void stopAutosave() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
            autosaveTask = null;
        }
    }

    /** Gravacao sincronamente no desligamento. */
    public void shutdown() {
        stopAutosave();
        if (dirty.compareAndSet(true, false)) {
            try {
                dados.save(file);
                plugin.getLogger().info("dados.yml salvo no desligamento.");
            } catch (IOException e) {
                plugin.getLogger().severe("Falha ao salvar dados.yml no desligamento: " + e.getMessage());
            }
        }
    }
}
