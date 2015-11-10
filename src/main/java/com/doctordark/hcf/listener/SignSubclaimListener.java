package com.doctordark.hcf.listener;

import com.doctordark.hcf.ConfigurationService;
import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SignSubclaimListener implements Listener {

    private static final int MAX_SIGN_LINE_CHARS = 16;
    private static final String SUBCLAIM_CONVERSION_PREFIX = ChatColor.DARK_RED.toString() + ChatColor.BOLD + "[Subclaim]";
    private static final List<String> SUBCLAIM_ALIASES = Arrays.asList("SUBCLAIM", "PRIVATE");
    private static final Pattern SQUARE_PATTERN_REPLACER = Pattern.compile("\\[|\\]");
    private static final BlockFace[] SIGN_FACES = new BlockFace[]{
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.UP
    };

    private final HCF plugin;

    public SignSubclaimListener(HCF plugin) {
        this.plugin = plugin;
    }

    private boolean isSubclaimable(Block block) {
        Material type = block.getType();
        return type == Material.FENCE_GATE || type == Material.TRAP_DOOR || block.getState() instanceof InventoryHolder;
    }

    private boolean isSubclaimed(Block block) {
        if (isSubclaimable(block)) {
            Collection<Sign> attachedSigns = getAttachedSigns(block);
            for (Sign attachedSign : attachedSigns) {
                if (attachedSign.getLine(0).equals(SignSubclaimListener.SUBCLAIM_CONVERSION_PREFIX)) {
                    return false;
                }
            }
        }

        return false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (!SignSubclaimListener.SUBCLAIM_ALIASES.contains(SignSubclaimListener.SQUARE_PATTERN_REPLACER.matcher(lines[0].toUpperCase()).replaceAll(""))) {
            return; // is not a subclaim written sign
        }

        Block block = event.getBlock();
        MaterialData materialData = block.getState().getData();
        if (materialData instanceof org.bukkit.material.Sign) {
            org.bukkit.material.Sign sign = (org.bukkit.material.Sign) materialData;
            Block attachedBlock = block.getRelative(sign.getAttachedFace());

            if (this.isSubclaimable(attachedBlock)) {
                Player player = event.getPlayer();
                PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
                if (playerFaction == null) {
                    return; // only allow officers to create Subclaims
                }

                Faction factionAt = plugin.getFactionManager().getFactionAt(block.getLocation());
                if (playerFaction == factionAt) {
                    if (this.isSubclaimed(attachedBlock)) {
                        player.sendMessage(ChatColor.RED + "There is already a subclaim sign on this " + attachedBlock.getName() + '.');
                        return;
                    }

                    List<String> memberList = new ArrayList<>(3);
                    for (int i = 1; i < lines.length; i++) {
                        String line = lines[i];
                        if (StringUtils.isNotBlank(line)) memberList.add(line);
                    }

                    if (memberList.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "Subclaim signs need to have at least 1 player name inserted.");
                        return;
                    }

                    boolean leaderChest = lines[1].equals(Role.LEADER.getAstrix()) || lines[1].equalsIgnoreCase("LEADER");

                    if (leaderChest) {
                        if (playerFaction.getMember(player).getRole() != Role.LEADER) {
                            player.sendMessage(ChatColor.RED + "Only faction leaders can create leader subclaimed objects.");
                            return;
                        }

                        // Clear the other lines.
                        event.setLine(2, null);
                        event.setLine(3, null);
                    }

                    event.setLine(0, SignSubclaimListener.SUBCLAIM_CONVERSION_PREFIX); // officialise this as a Subclaim chest.

                    // the lines may contain a name of a user not in the faction.
                    List<String> actualMembers = memberList.stream().filter(member -> playerFaction.getMember(member) != null).collect(Collectors.toList());
                    playerFaction.broadcast(ConfigurationService.TEAMMATE_COLOUR + player.getName() + ChatColor.YELLOW + " has created a subclaim on block type " +
                            ChatColor.AQUA + attachedBlock.getName() + ChatColor.YELLOW + " at " + ChatColor.WHITE + '(' + attachedBlock.getX() + ", " + attachedBlock.getZ() + ')' +
                            ChatColor.YELLOW + " for " + (leaderChest ? "leaders" : actualMembers.isEmpty() ? "captains" : "members " + ChatColor.RED + '[' + StringUtils.join(actualMembers, ", ") + ']'));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getEotwHandler().isEndOfTheWorld()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {
            return;
        }

        Block block = event.getBlock();
        BlockState state = block.getState();

        Block subclaimObjectBlock = null;
        if (!(state instanceof Sign)) {
            subclaimObjectBlock = block;
        } else {
            Sign sign = (Sign) state;
            MaterialData signData = sign.getData();
            if (signData instanceof org.bukkit.material.Sign) {
                org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign) signData;
                subclaimObjectBlock = block.getRelative(materialSign.getAttachedFace());
            }
        }

        if (subclaimObjectBlock != null && !this.checkSubclaimIntegrity(player, subclaimObjectBlock)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break this subclaimed " + subclaimObjectBlock.getName() + '.');
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (plugin.getEotwHandler().isEndOfTheWorld()) return;
        InventoryHolder holder = event.getSource().getHolder();

        // Have to do this hackery since Bukkit doesn't
        // provide an API for us to do this
        Collection<Block> sourceBlocks;
        if (holder instanceof Chest) {
            sourceBlocks = Collections.singletonList(((Chest) holder).getBlock());
        } else if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;
            sourceBlocks = Lists.newArrayList(((Chest) doubleChest.getLeftSide()).getBlock(), ((Chest) doubleChest.getRightSide()).getBlock());
        } else {
            return;
        }

        for (Block block : sourceBlocks) {
            if (isSubclaimable(block)) {
                Collection<Sign> attachedSigns = getAttachedSigns(block);
                for (Sign attachedSign : attachedSigns) {
                    String[] lines = attachedSign.getLines();
                    if (lines[0].equals(SignSubclaimListener.SUBCLAIM_CONVERSION_PREFIX)) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    private String getShortenedName(String originalName) {
        if (originalName.length() >= MAX_SIGN_LINE_CHARS) {
            originalName = originalName.substring(0, MAX_SIGN_LINE_CHARS);
        }

        return originalName;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {
            return;
        }

        if (plugin.getEotwHandler().isEndOfTheWorld() || ConfigurationService.KIT_MAP) {
            return;
        }

        Block block = event.getClickedBlock();
        if (!this.isSubclaimable(block)) {
            return;
        }

        if (!this.checkSubclaimIntegrity(player, block)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You do not have access to this subclaimed " + block.getName() + '.');
        }
    }

    /**
     * Checks subclaim integrity of a {@link Block} for a {@link Player}.
     *
     * @param player         the {@link Player} to check
     * @param subclaimObject the {@link Block} to check
     * @return true if allowed to open
     */
    private boolean checkSubclaimIntegrity(Player player, Block subclaimObject) {
        if (!this.isSubclaimable(subclaimObject)) {
            return true;
        }

        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null || playerFaction.isRaidable()) {
            return true; // just let the player open it from the start.
        }

        Role role = playerFaction.getMember(player).getRole();
        if (role == Role.LEADER) {
            return true; // let leaders open regardless.
        }

        if (playerFaction != plugin.getFactionManager().getFactionAt(subclaimObject)) {
            return true;
        }

        Collection<Sign> attachedSigns = this.getAttachedSigns(subclaimObject);
        if (attachedSigns.isEmpty()) {
            return true;
        }

        boolean hasLooped = false;
        String search = this.getShortenedName(player.getName());
        for (Sign attachedSign : attachedSigns) {
            String[] lines = attachedSign.getLines();
            if (!lines[0].equals(SUBCLAIM_CONVERSION_PREFIX)) {
                continue; // It is not a Subclaim sign, ignore
            }

            hasLooped = true;
            if (Role.LEADER.getAstrix().equals(lines[1])) {
                continue; // This is a leader only subclaim, and the player is not a leader.
            }

            if (role == Role.CAPTAIN) {
                return true; // The players' name wasn't found, but allow captains regardless as it isn't leader only.
            }

            for (int i = 1; i < lines.length; i++) {
                if (lines[i].contains(search)) {
                    return true; // Member is allowed to use
                }
            }
        }

        return !hasLooped;
    }

    /**
     * Gets the attached {@link Sign}s on a {@link Block}.
     *
     * @param block the {@link Block} to get for
     * @return collection of attached {@link Sign}s
     */
    public Collection<Sign> getAttachedSigns(Block block) {
        Set<Sign> results = new HashSet<>();
        getSignsAround(block, results);

        BlockState state = block.getState();
        if (state instanceof Chest) {
            Inventory chestInventory = ((Chest) state).getInventory();
            if (chestInventory instanceof DoubleChestInventory) {
                DoubleChest doubleChest = ((DoubleChestInventory) chestInventory).getHolder();
                Block left = ((Chest) doubleChest.getLeftSide()).getBlock();
                Block right = ((Chest) doubleChest.getRightSide()).getBlock();
                getSignsAround(left.equals(block) ? right : left, results);
            }
        }

        return results;
    }

    /**
     * Gets the {@link Sign}s that are attached to a given {@link Block}.
     *
     * @param block   the {@link Block} to get around
     * @param results the input to add to
     * @return the updated set of {@link Sign}s
     */
    private Set<Sign> getSignsAround(Block block, Set<Sign> results) {
        for (BlockFace face : SIGN_FACES) {
            Block relative = block.getRelative(face);
            BlockState relativeState = relative.getState();
            if (relativeState instanceof Sign) {
                org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign) relativeState.getData();
                if (relative.getRelative(materialSign.getAttachedFace()).equals(block)) {
                    results.add((Sign) relative.getState());
                }
            }
        }

        return results;
    }
}
