package dev.jcsoftware.playtestingplugin;

import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigurationFile {

    private final YamlConfiguration yamlConfiguration;
    private final File file;

    /**
     * Create a new ConfigurationFile
     * @param plugin The plugin which should own this file.
     * @param name The name (without extension) of the file
     */
    @SneakyThrows
    public ConfigurationFile(Plugin plugin, String name) {
        this.file = new File(plugin.getDataFolder(), name + ".yml");
        this.yamlConfiguration = new YamlConfiguration();
        if (!this.file.exists()) {
            this.file.createNewFile();
        }
        this.yamlConfiguration.load(this.file);
    }

    public YamlConfiguration getConfiguration() {
        return yamlConfiguration;
    }

    @SneakyThrows
    public void save() {
        yamlConfiguration.save(file);
    }

}
