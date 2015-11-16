package org.ipvp.hcf.pvpclass.bard;

import org.bukkit.potion.PotionEffect;

/**
 * Represents a {@link BardClass} effect.
 */
public class BardEffect {

    public final int energyCost;
    public final PotionEffect clickable;
    public final PotionEffect heldable;

    public BardEffect(int energyCost, PotionEffect clickable, PotionEffect heldable) {
        this.energyCost = energyCost;
        this.clickable = clickable;
        this.heldable = heldable;
    }
}