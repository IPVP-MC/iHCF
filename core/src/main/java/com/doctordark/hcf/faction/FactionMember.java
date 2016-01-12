package com.doctordark.hcf.faction;

import com.doctordark.base.GuavaCompat;
import com.doctordark.hcf.faction.struct.ChatChannel;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.Faction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Stores data about members in a {@link Faction}.
 */
public class FactionMember implements ConfigurationSerializable {

    private final UUID uniqueID;
    private ChatChannel chatChannel;
    private Role role;

    public FactionMember(Player player, ChatChannel chatChannel, Role role) {
        this.uniqueID = player.getUniqueId();
        this.chatChannel = chatChannel;
        this.role = role;
    }

    /**
     * Constructs a new {@link FactionMember} from a map.
     *
     * @param map the map to construct from
     */
    public FactionMember(Map<String, Object> map) {
        this.uniqueID = UUID.fromString((String) map.get("uniqueID"));
        this.chatChannel = GuavaCompat.getIfPresent(ChatChannel.class, (String) map.get("chatChannel")).or(ChatChannel.PUBLIC);
        this.role = GuavaCompat.getIfPresent(Role.class, (String) map.get("role")).or(Role.MEMBER);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uniqueID", uniqueID.toString());
        map.put("chatChannel", chatChannel.name());
        map.put("role", role.name());
        return map;
    }

    /**
     * Gets the name of this {@link FactionMember}.
     *
     * @return the name of this {@link FactionMember}
     */
    public String getName() {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uniqueID);
        return offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline() ? offlinePlayer.getName() : null;
    }

    /**
     * Gets the {@link UUID} of this {@link FactionMember}.
     *
     * @return the {@link UUID}
     */
    public UUID getUniqueId() {
        return uniqueID;
    }

    /**
     * Gets the {@link ChatChannel} of this {@link FactionMember}.
     *
     * @return the {@link ChatChannel}
     */
    public ChatChannel getChatChannel() {
        return chatChannel;
    }

    /**
     * Sets the {@link ChatChannel} of this {@link FactionMember}.
     *
     * @param chatChannel the {@link ChatChannel} to set
     */
    public void setChatChannel(ChatChannel chatChannel) {
        Objects.requireNonNull(chatChannel, "ChatChannel cannot be null");
        this.chatChannel = chatChannel;
    }

    /**
     * Gets the {@link Role} of this {@link FactionMember}.
     *
     * @return the {@link Role}
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the {@link Role} of this {@link FactionMember}.
     *
     * @param role the {@link Role} to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Converts this {@link Player} to a {@link Player}.
     *
     * @return the {@link Player} or null if not found
     */
    public Player toOnlinePlayer() {
        return Bukkit.getPlayer(uniqueID);
    }
}