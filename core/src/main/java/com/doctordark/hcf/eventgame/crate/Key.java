package com.doctordark.hcf.eventgame.crate;

import com.doctordark.util.Config;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an {@link Key} that can be used to unlock crates.
 */
public abstract class Key {

    private String name;

    /**
     * Constructs a new {@link Key} with a given name.
     *
     * @param name the name to construct with
     */
    public Key(String name) {
        this.name = name;
    }

    /**
     * Gets the name of this {@link Key}.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the {@link Key}.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the {@link ChatColor} of this {@link Key}.
     *
     * @return the {@link ChatColor}
     */
    public abstract ChatColor getColour();

    /**
     * Gets the display name of this {@link Key}.
     * <p>This will return the prefix, followed by the display name.</p>
     *
     * @return the {@link Key} display name
     */
    public String getDisplayName() {
        return getColour() + name;
    }

    /**
     * Gets the {@link ItemStack} of this {@link Key}
     *
     * @return the {@link ItemStack}
     */
    public abstract ItemStack getItemStack();

    /**
     * Loads data from the {@link Key} {@link Config}.
     *
     * @param config the {@link Config} to load from
     */
    public void load(Config config) {
    }

    /**
     * Saves data to the {@link Key} {@link Config}.
     *
     * @param config the {@link Config} to save to
     */
    public void save(Config config) {
    }
}