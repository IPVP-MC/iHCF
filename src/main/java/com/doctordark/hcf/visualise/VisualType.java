package com.doctordark.hcf.visualise;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.struct.Relation;
import com.doctordark.hcf.faction.type.Faction;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public enum VisualType {

    //TODO: Figure out a better way for filling blocks than this

    /**
     * Represents the wall approaching claims when Spawn Tagged.
     */
    SPAWN_BORDER() {
        private final BlockFiller blockFiller = new BlockFiller() {
            @Override
            VisualBlockData generate(Player player, Location location) {
                return new VisualBlockData(Material.STAINED_GLASS, DyeColor.PURPLE.getData());
            }
        };

        @Override
        BlockFiller blockFiller() {
            return blockFiller;
        }
    },
    /**
     * Represents the wall approaching claims when PVP Protected.
     */
    CLAIM_BORDER() {
        private final BlockFiller blockFiller = new BlockFiller() {
            @Override
            VisualBlockData generate(Player player, Location location) {
                return new VisualBlockData(Material.STAINED_GLASS, DyeColor.PINK.getData());
            }
        };

        @Override
        BlockFiller blockFiller() {
            return blockFiller;
        }
    },
    /**
     * Represents claims shown using /faction map.
     */
    SUBCLAIM_MAP() {
        private final BlockFiller blockFiller = new BlockFiller() {
            @Override
            VisualBlockData generate(Player player, Location location) {
                return new VisualBlockData(Material.LOG, (byte) 1);
            }
        };

        @Override
        BlockFiller blockFiller() {
            return blockFiller;
        }
    },
    /**
     * Represents claims shown using /faction map.
     */
    CLAIM_MAP() {
        private final BlockFiller blockFiller = new BlockFiller() {
            private final Material[] types = new Material[]{
                    Material.SNOW_BLOCK,
                    Material.SANDSTONE,
                    Material.FURNACE,
                    Material.NETHERRACK,
                    Material.GLOWSTONE,
                    Material.LAPIS_BLOCK,
                    Material.NETHER_BRICK,
                    Material.DIAMOND_ORE,
                    Material.COAL_ORE,
                    Material.IRON_ORE,
                    Material.GOLD_ORE,
                    Material.LAPIS_ORE,
                    Material.REDSTONE_ORE
            };

            private int materialCounter = 0;

            @Override
            VisualBlockData generate(Player player, Location location) {
                int y = location.getBlockY();
                if (y == 0 || y % 3 == 0) {
                    return new VisualBlockData(types[materialCounter]);
                }

                Faction faction = HCF.getPlugin().getFactionManager().getFactionAt(location);
                return new VisualBlockData(Material.STAINED_GLASS, (faction != null ? faction.getRelation(player) : Relation.ENEMY).toDyeColour().getData());
            }

            @Override
            ArrayList<VisualBlockData> bulkGenerate(Player player, Iterable<Location> locations) {
                ArrayList<VisualBlockData> result = super.bulkGenerate(player, locations);
                if (++materialCounter == types.length) materialCounter = 0;
                return result;
            }
        };

        @Override
        BlockFiller blockFiller() {
            return blockFiller;
        }
    },

    CREATE_CLAIM_SELECTION() {
        private final BlockFiller blockFiller = new BlockFiller() {
            @Override
            VisualBlockData generate(Player player, Location location) {
                return new VisualBlockData(location.getBlockY() % 3 != 0 ? Material.GLASS : Material.GOLD_BLOCK);
            }
        };

        @Override
        BlockFiller blockFiller() {
            return blockFiller;
        }
    },;

    /**
     * Gets the {@link BlockFiller} instance.
     *
     * @return the filler
     */
    abstract BlockFiller blockFiller();
}
