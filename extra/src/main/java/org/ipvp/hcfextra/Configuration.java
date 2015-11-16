package org.ipvp.hcfextra;

import com.google.common.collect.Iterables;
import lombok.Getter;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Configuration {

    @Getter
    private String[] coordsMessage;

    @Getter
    private String[] helpMessage;

    private final JavaPlugin plugin;

    public Configuration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        File folder = plugin.getDataFolder();
        if (!folder.exists()) {
            this.quietlyCreateFile(folder);
        }

        try {
            this.coordsMessage = this.convertLines(new File(folder, "coords.txt"));
            this.helpMessage = this.convertLines(new File(folder, "help.txt"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String[] convertLines(File file) throws IOException {
        if (!file.exists()) {
            this.quietlyCreateFile(file);
        }

        plugin.getLogger().log(Level.INFO, "Reading lines of file " + file.getName() + ".");

        String[] lines = Iterables.toArray(FileUtils.readLines(file), String.class);
        int count = 0;
        for (String line : lines) {
            lines[count++] = ChatColor.translateAlternateColorCodes('&', line);
        }

        return lines;
    }

    private void quietlyCreateFile(File file) {
        try {
            HCFExtra.getPlugin().getLogger().log(Level.INFO, (file.createNewFile() ? "Failed to create" : "Created") + " file " + file.getName() + ".");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
