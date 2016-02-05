package com.doctordark.hcf.util;

public class SpigotUtils {

    public static net.md_5.bungee.api.ChatColor toBungee(org.bukkit.ChatColor color) {
        switch (color) {
            case BLACK:
                return net.md_5.bungee.api.ChatColor.BLACK;
            case DARK_BLUE:
                return net.md_5.bungee.api.ChatColor.DARK_BLUE;
            case DARK_GREEN:
                return net.md_5.bungee.api.ChatColor.DARK_GREEN;
            case DARK_AQUA:
                return net.md_5.bungee.api.ChatColor.DARK_AQUA;
            case DARK_RED:
                return net.md_5.bungee.api.ChatColor.DARK_RED;
            case DARK_PURPLE:
                return net.md_5.bungee.api.ChatColor.DARK_PURPLE;
            case GOLD:
                return net.md_5.bungee.api.ChatColor.GOLD;
            case GRAY:
                return net.md_5.bungee.api.ChatColor.GRAY;
            case DARK_GRAY:
                return net.md_5.bungee.api.ChatColor.DARK_GRAY;
            case BLUE:
                return net.md_5.bungee.api.ChatColor.BLUE;
            case GREEN:
                return net.md_5.bungee.api.ChatColor.GREEN;
            case AQUA:
                return net.md_5.bungee.api.ChatColor.AQUA;
            case RED:
                return net.md_5.bungee.api.ChatColor.RED;
            case LIGHT_PURPLE:
                return net.md_5.bungee.api.ChatColor.LIGHT_PURPLE;
            case YELLOW:
                return net.md_5.bungee.api.ChatColor.YELLOW;
            case WHITE:
                return net.md_5.bungee.api.ChatColor.WHITE;
            case MAGIC:
                return net.md_5.bungee.api.ChatColor.MAGIC;
            case BOLD:
                return net.md_5.bungee.api.ChatColor.BOLD;
            case STRIKETHROUGH:
                return net.md_5.bungee.api.ChatColor.STRIKETHROUGH;
            case UNDERLINE:
                return net.md_5.bungee.api.ChatColor.UNDERLINE;
            case ITALIC:
                return net.md_5.bungee.api.ChatColor.ITALIC;
            case RESET:
                return net.md_5.bungee.api.ChatColor.RESET;
            default:
                throw new IllegalArgumentException("Unrecognised Bukkit colour " + color.name() + ".");
        }
    }
}
