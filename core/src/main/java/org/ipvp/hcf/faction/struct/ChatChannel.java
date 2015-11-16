package org.ipvp.hcf.faction.struct;

import org.ipvp.hcf.ConfigurationService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Locale;


public enum ChatChannel {

    FACTION("Faction"), ALLIANCE("Alliance"), PUBLIC("Public");

    private final String name;

    ChatChannel(String name) {
        this.name = name;
    }

    /**
     * Gets the name of this {@link ChatChannel}.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    public String getDisplayName() {
        final String prefix;
        switch (this) {
            case FACTION:
                prefix = ConfigurationService.TEAMMATE_COLOUR.toString();
                break;
            case ALLIANCE:
                prefix = ConfigurationService.ALLY_COLOUR.toString();
                break;
            case PUBLIC:
            default:
                prefix = ConfigurationService.ENEMY_COLOUR.toString();
                break;
        }

        return prefix + name;
    }

    /**
     * Gets the short name of this {@link ChatChannel}.
     *
     * @return the short name
     */
    public String getShortName() {
        switch (this) {
            case FACTION:
                return "FC";
            case ALLIANCE:
                return "AC";
            case PUBLIC:
            default:
                return "PC";
        }
    }

    /**
     * Parse an {@link ChatChannel} from an id.
     *
     * @param id the id to search
     * @return the parsed {@link ChatChannel} or {@link ChatChannel#PUBLIC} if not found
     */
    public static ChatChannel parse(String id) {
        return parse(id, PUBLIC);
    }

    /**
     * Parse an {@link ChatChannel} from a String.
     *
     * @param id  the id to search
     * @param def the default {@link ChatChannel} if null
     * @return the {@link ChatChannel} by name
     */
    public static ChatChannel parse(String id, ChatChannel def) {
        id = id.toLowerCase(Locale.ENGLISH);
        switch (id) {
            case "f":
            case "faction":
            case "fc":
            case "fac":
            case "fact":
                return ChatChannel.FACTION;
            case "a":
            case "alliance":
            case "ally":
            case "ac":
                return ChatChannel.ALLIANCE;
            case "p":
            case "pc":
            case "g":
            case "gc":
            case "global":
            case "pub":
            case "publi":
            case "public":
                return ChatChannel.PUBLIC;
            default:
                return def == null ? null : def.getRotation();
        }
    }

    /**
     * Gets the next {@link ChatChannel} from the current.
     *
     * @return the next rotation value
     */
    public ChatChannel getRotation() {
        switch (this) {
            case FACTION:
                return PUBLIC;
            case PUBLIC:
                return ConfigurationService.MAX_ALLIES_PER_FACTION > 0 ? ALLIANCE : FACTION;
            case ALLIANCE:
                return FACTION;
            default:
                return PUBLIC;
        }
    }

    public String getRawFormat(Player player) {
        switch (this) {
            case FACTION:
                return ConfigurationService.TEAMMATE_COLOUR + "(" + getDisplayName() + ConfigurationService.TEAMMATE_COLOUR + ") " + player.getName() + ChatColor.GRAY + ": " + ChatColor.YELLOW + "%2$s";
            case ALLIANCE:
                return ConfigurationService.ALLY_COLOUR + "(" + getDisplayName() + ConfigurationService.ALLY_COLOUR + ") " + player.getName() + ChatColor.GRAY + ": " + ChatColor.YELLOW + "%2$s";
            default:
                throw new IllegalArgumentException("Cannot get the raw format for public chat channel");
        }
    }
}
