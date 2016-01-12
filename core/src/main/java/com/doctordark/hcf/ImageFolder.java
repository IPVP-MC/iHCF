package com.doctordark.hcf;

import com.google.common.io.ByteStreams;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

public class ImageFolder {

    @Getter
    private final File directory;
    private final HCF plugin;

    @Getter
    private BufferedImage gopple;

    public ImageFolder(HCF plugin) {
        this.plugin = plugin;

        directory = new File(plugin.getDataFolder(), "imageMessages");
        if (!directory.exists() && directory.mkdir()) {
            plugin.getLogger().log(Level.INFO, "Created image directory");
        }

        gopple = load("gapple.png");
    }

    public BufferedImage load(String imageName) {
        File file = new File(directory, imageName);
        if (file.exists()) {
            try {
                return ImageIO.read(file);
            } catch (IOException ignored) {
                // continue to next block
            }
        }

        plugin.getLogger().info("Attempting to copy resource '" + imageName + "' to plugin folder");
        try (InputStream input = plugin.getResource(directory.getName() + "/" + file.getName()); OutputStream output = new FileOutputStream(file)) {
            ByteStreams.copy(input, output);
            return ImageIO.read(input);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to get resource for file '" + imageName + "'", ex);
            return null;
        }
    }
}
