package com.doctordark.hcf.faction.struct;

import com.doctordark.hcf.HCF;
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
        final HCF plugin = HCF.getPlugin();
        String prefix;
        switch (this) {
            case FACTION:
                prefix = plugin.getConfiguration().getRelationColourTeammate().toString();
                break;
            case ALLIANCE:
                prefix = plugin.getConfiguration().getRelationColourAlly().toString();
                break;
            case PUBLIC:
            default:
                prefix = plugin.getConfiguration().getRelationColourEnemy().toString();
                break;
        }

        return prefix + name;
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
                return HCF.getPlugin().getConfiguration().getFactionMaxAllies() > 0 ? ALLIANCE : FACTION;
            case ALLIANCE:
                return FACTION;
            default:
                return PUBLIC;
        }
    }

    public String getRawFormat(Player player) {
        final HCF plugin = HCF.getPlugin();
        ChatColor colour;
        switch (this) {
            case FACTION:
                colour = plugin.getConfiguration().getRelationColourTeammate();
                return colour + "(" + getDisplayName() + colour + ") " + player.getName() + ChatColor.GRAY + ": " + ChatColor.YELLOW + "%2$s";
            case ALLIANCE:
                colour = plugin.getConfiguration().getRelationColourTeammate();
                return colour + "(" + getDisplayName() + colour + ") " + player.getName() + ChatColor.GRAY + ": " + ChatColor.YELLOW + "%2$s";
            default:
                throw new IllegalArgumentException("Cannot get the raw format for public chat channel");
        }
    }
}
