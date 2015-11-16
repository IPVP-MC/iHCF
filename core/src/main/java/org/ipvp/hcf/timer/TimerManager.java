package org.ipvp.hcf.timer;

import com.doctordark.util.Config;
import lombok.Data;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.eventgame.EventTimer;
import org.ipvp.hcf.timer.type.CombatTimer;
import org.ipvp.hcf.timer.type.EnderPearlTimer;
import org.ipvp.hcf.timer.type.GappleTimer;
import org.ipvp.hcf.timer.type.InvincibilityTimer;
import org.ipvp.hcf.timer.type.LogoutTimer;
import org.ipvp.hcf.timer.type.PvpClassWarmupTimer;
import org.ipvp.hcf.timer.type.StuckTimer;
import org.ipvp.hcf.timer.type.TeleportTimer;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class TimerManager implements Listener {

    @Getter
    private final CombatTimer combatTimer;

    @Getter
    private final LogoutTimer logoutTimer;

    @Getter
    private final EnderPearlTimer enderPearlTimer;

    @Getter
    private final EventTimer eventTimer;

    @Getter
    private final GappleTimer gappleTimer;

    @Getter
    private final InvincibilityTimer invincibilityTimer;

    @Getter
    private final PvpClassWarmupTimer pvpClassWarmupTimer;

    @Getter
    private final StuckTimer stuckTimer;

    @Getter
    private final TeleportTimer teleportTimer;

    @Getter
    private final Set<Timer> timers = new LinkedHashSet<>();

    private final JavaPlugin plugin;
    private Config config;

    public TimerManager(HCF plugin) {
        (this.plugin = plugin).getServer().getPluginManager().registerEvents(this, plugin);
        this.registerTimer(this.enderPearlTimer = new EnderPearlTimer(plugin));
        this.registerTimer(this.logoutTimer = new LogoutTimer());
        this.registerTimer(this.gappleTimer = new GappleTimer(plugin));
        this.registerTimer(this.stuckTimer = new StuckTimer());
        this.registerTimer(this.invincibilityTimer = new InvincibilityTimer(plugin));
        this.registerTimer(this.combatTimer = new CombatTimer(plugin));
        this.registerTimer(this.teleportTimer = new TeleportTimer(plugin));
        this.registerTimer(this.eventTimer = new EventTimer(plugin));
        this.registerTimer(this.pvpClassWarmupTimer = new PvpClassWarmupTimer(plugin));
        this.reloadTimerData();
    }

    public void registerTimer(Timer timer) {
        this.timers.add(timer);
        if (timer instanceof Listener) {
            this.plugin.getServer().getPluginManager().registerEvents((Listener) timer, this.plugin);
        }
    }

    public void unregisterTimer(Timer timer) {
        this.timers.remove(timer);
    }

    /**
     * Reloads the {@link Timer} data from storage.
     */
    public void reloadTimerData() {
        this.config = new Config(plugin, "timers");
        for (Timer timer : this.timers) {
            timer.load(this.config);
        }
    }

    /**
     * Saves the {@link Timer} data to storage.
     */
    public void saveTimerData() {
        for (Timer timer : this.timers) {
            timer.onDisable(this.config);
        }

        this.config.save();
    }
}
