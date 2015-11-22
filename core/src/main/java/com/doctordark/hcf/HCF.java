package com.doctordark.hcf;

import com.doctordark.hcf.combatlog.CombatLogListener;
import com.doctordark.hcf.command.AngleCommand;
import com.doctordark.hcf.command.GoppleCommand;
import com.doctordark.hcf.command.LocationCommand;
import com.doctordark.hcf.command.LogoutCommand;
import com.doctordark.hcf.command.MapKitCommand;
import com.doctordark.hcf.command.PvpTimerCommand;
import com.doctordark.hcf.command.RegenCommand;
import com.doctordark.hcf.command.ServerTimeCommand;
import com.doctordark.hcf.command.SpawnCannonCommand;
import com.doctordark.hcf.command.ToggleCapzoneEntryCommand;
import com.doctordark.hcf.command.ToggleLightningCommand;
import com.doctordark.hcf.command.ToggleSidebarCommand;
import com.doctordark.hcf.deathban.Deathban;
import com.doctordark.hcf.deathban.DeathbanListener;
import com.doctordark.hcf.deathban.DeathbanManager;
import com.doctordark.hcf.deathban.FlatFileDeathbanManager;
import com.doctordark.hcf.deathban.StaffReviveCommand;
import com.doctordark.hcf.deathban.lives.LivesExecutor;
import com.doctordark.hcf.economy.EconomyCommand;
import com.doctordark.hcf.economy.EconomyManager;
import com.doctordark.hcf.economy.FlatFileEconomyManager;
import com.doctordark.hcf.economy.PayCommand;
import com.doctordark.hcf.economy.ShopSignListener;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventExecutor;
import com.doctordark.hcf.eventgame.EventScheduler;
import com.doctordark.hcf.eventgame.conquest.ConquestExecutor;
import com.doctordark.hcf.eventgame.crate.KeyListener;
import com.doctordark.hcf.eventgame.crate.KeyManager;
import com.doctordark.hcf.eventgame.eotw.EotwCommand;
import com.doctordark.hcf.eventgame.eotw.EotwHandler;
import com.doctordark.hcf.eventgame.eotw.EotwListener;
import com.doctordark.hcf.eventgame.faction.CapturableFaction;
import com.doctordark.hcf.eventgame.faction.ConquestFaction;
import com.doctordark.hcf.eventgame.faction.KothFaction;
import com.doctordark.hcf.eventgame.koth.KothExecutor;
import com.doctordark.hcf.faction.FactionExecutor;
import com.doctordark.hcf.faction.FactionManager;
import com.doctordark.hcf.faction.FactionMember;
import com.doctordark.hcf.faction.FlatFileFactionManager;
import com.doctordark.hcf.faction.claim.Claim;
import com.doctordark.hcf.faction.claim.ClaimHandler;
import com.doctordark.hcf.faction.claim.ClaimWandListener;
import com.doctordark.hcf.faction.claim.Subclaim;
import com.doctordark.hcf.faction.claim.SubclaimWandListener;
import com.doctordark.hcf.faction.type.ClaimableFaction;
import com.doctordark.hcf.faction.type.EndPortalFaction;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.hcf.faction.type.RoadFaction;
import com.doctordark.hcf.faction.type.SpawnFaction;
import com.doctordark.hcf.listener.BookDeenchantListener;
import com.doctordark.hcf.listener.BottledExpListener;
import com.doctordark.hcf.listener.ChatListener;
import com.doctordark.hcf.listener.CoreListener;
import com.doctordark.hcf.listener.CrowbarListener;
import com.doctordark.hcf.listener.DeathListener;
import com.doctordark.hcf.listener.DeathMessageListener;
import com.doctordark.hcf.listener.DeathSignListener;
import com.doctordark.hcf.listener.EnchantLimitListener;
import com.doctordark.hcf.listener.EntityLimitListener;
import com.doctordark.hcf.listener.EventSignListener;
import com.doctordark.hcf.listener.ExpMultiplierListener;
import com.doctordark.hcf.listener.FactionListener;
import com.doctordark.hcf.listener.FurnaceSmeltSpeederListener;
import com.doctordark.hcf.listener.KitMapListener;
import com.doctordark.hcf.listener.PortalListener;
import com.doctordark.hcf.listener.PotionLimitListener;
import com.doctordark.hcf.listener.ProtectionListener;
import com.doctordark.hcf.listener.SignSubclaimListener;
import com.doctordark.hcf.listener.SkullListener;
import com.doctordark.hcf.listener.WorldListener;
import com.doctordark.hcf.listener.fixes.BeaconStrengthFixListener;
import com.doctordark.hcf.listener.fixes.BlockHitFixListener;
import com.doctordark.hcf.listener.fixes.BlockJumpGlitchFixListener;
import com.doctordark.hcf.listener.fixes.BoatGlitchFixListener;
import com.doctordark.hcf.listener.fixes.EnderChestRemovalListener;
import com.doctordark.hcf.listener.fixes.InfinityArrowFixListener;
import com.doctordark.hcf.listener.fixes.PearlGlitchListener;
import com.doctordark.hcf.listener.fixes.VoidGlitchFixListener;
import com.doctordark.hcf.pvpclass.PvpClassManager;
import com.doctordark.hcf.pvpclass.bard.EffectRestorer;
import com.doctordark.hcf.scoreboard.ScoreboardHandler;
import com.doctordark.hcf.sotw.SotwCommand;
import com.doctordark.hcf.sotw.SotwListener;
import com.doctordark.hcf.sotw.SotwTimer;
import com.doctordark.hcf.timer.TimerExecutor;
import com.doctordark.hcf.timer.TimerManager;
import com.doctordark.hcf.user.FactionUser;
import com.doctordark.hcf.user.UserManager;
import com.doctordark.hcf.visualise.ProtocolLibHook;
import com.doctordark.hcf.visualise.VisualiseHandler;
import com.doctordark.hcf.visualise.WallBorderListener;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HCF extends JavaPlugin {

    @Getter
    private static HCF plugin;

    @Getter
    private Random random = new Random();

    @Getter
    private ClaimHandler claimHandler;

    @Getter
    private CombatLogListener combatLogListener;

    @Getter
    private DeathbanManager deathbanManager;

    @Getter
    private EconomyManager economyManager;

    @Getter
    private EffectRestorer effectRestorer;

    @Getter
    private EotwHandler eotwHandler;

    @Getter
    private EventScheduler eventScheduler;

    @Getter
    private FactionManager factionManager;

    @Getter
    private KeyManager keyManager;

    @Getter
    private PvpClassManager pvpClassManager;

    @Getter
    private ScoreboardHandler scoreboardHandler;

    @Getter
    private SotwTimer sotwTimer;

    @Getter
    private TimerManager timerManager;

    @Getter
    private UserManager userManager;

    @Getter
    private VisualiseHandler visualiseHandler;

    @Getter
    private WorldEditPlugin worldEdit;

    @Override
    public void onEnable() {
        HCF.plugin = this;
        ProtocolLibHook.hook(this);

        Plugin wep = getServer().getPluginManager().getPlugin("WorldEdit");
        this.worldEdit = wep instanceof WorldEditPlugin && wep.isEnabled() ? (WorldEditPlugin) wep : null;

        ConfigurationService.init(this);
        this.effectRestorer = new EffectRestorer(this);
        this.registerConfiguration();
        this.registerCommands();
        this.registerManagers();
        this.registerListeners();

        new BukkitRunnable() {
            @Override
            public void run() {
                saveData();
            }
        }.runTaskTimerAsynchronously(plugin, TimeUnit.MINUTES.toMillis(20L), TimeUnit.MINUTES.toMillis(20L));
    }

    private void saveData() {
        this.deathbanManager.saveDeathbanData();
        this.economyManager.saveEconomyData();
        this.factionManager.saveFactionData();
        this.keyManager.saveKeyData();
        this.timerManager.saveTimerData();
        this.userManager.saveUserData();
    }

    @Override
    public void onDisable() {
        this.combatLogListener.removeCombatLoggers();
        this.pvpClassManager.onDisable();
        this.scoreboardHandler.clearBoards();
        this.saveData();

        HCF.plugin = null; // always initialise last
    }

    private void registerConfiguration() {
        ConfigurationSerialization.registerClass(CaptureZone.class);
        ConfigurationSerialization.registerClass(Deathban.class);
        ConfigurationSerialization.registerClass(Claim.class);
        ConfigurationSerialization.registerClass(Subclaim.class);
        ConfigurationSerialization.registerClass(Deathban.class);
        ConfigurationSerialization.registerClass(FactionUser.class);
        ConfigurationSerialization.registerClass(ClaimableFaction.class);
        ConfigurationSerialization.registerClass(ConquestFaction.class);
        ConfigurationSerialization.registerClass(CapturableFaction.class);
        ConfigurationSerialization.registerClass(KothFaction.class);
        ConfigurationSerialization.registerClass(EndPortalFaction.class);
        ConfigurationSerialization.registerClass(Faction.class);
        ConfigurationSerialization.registerClass(FactionMember.class);
        ConfigurationSerialization.registerClass(PlayerFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.class);
        ConfigurationSerialization.registerClass(SpawnFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.NorthRoadFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.EastRoadFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.SouthRoadFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.WestRoadFaction.class);
    }

    private void registerListeners() {
        PluginManager manager = this.getServer().getPluginManager();
        manager.registerEvents(new BlockHitFixListener(), this);
        manager.registerEvents(new BlockJumpGlitchFixListener(), this);
        manager.registerEvents(new BoatGlitchFixListener(), this);
        manager.registerEvents(new BookDeenchantListener(), this);
        manager.registerEvents(new BottledExpListener(), this);
        manager.registerEvents(new ChatListener(this), this);
        manager.registerEvents(new ClaimWandListener(this), this);
        manager.registerEvents(this.combatLogListener = new CombatLogListener(this), this);
        manager.registerEvents(new CoreListener(this), this);
        //manager.registerEvents(new CreativeClickListener(), this);
        manager.registerEvents(new CrowbarListener(this), this);
        manager.registerEvents(new DeathListener(this), this);
        manager.registerEvents(new DeathMessageListener(this), this);
        manager.registerEvents(new DeathSignListener(this), this);
        manager.registerEvents(new DeathbanListener(this), this);
        manager.registerEvents(new EnchantLimitListener(), this);
        manager.registerEvents(new EnderChestRemovalListener(), this);
        manager.registerEvents(new EntityLimitListener(), this);
        manager.registerEvents(new EotwListener(this), this);
        manager.registerEvents(new EventSignListener(), this);
        manager.registerEvents(new ExpMultiplierListener(), this);
        manager.registerEvents(new FactionListener(this), this);
        manager.registerEvents(new FurnaceSmeltSpeederListener(), this);
        manager.registerEvents(new InfinityArrowFixListener(), this);
        manager.registerEvents(new KeyListener(this), this);
        manager.registerEvents(new KitMapListener(this), this);
        manager.registerEvents(new PearlGlitchListener(this), this);
        manager.registerEvents(new PortalListener(this), this);
        manager.registerEvents(new PotionLimitListener(), this);
        manager.registerEvents(new ProtectionListener(this), this);
        manager.registerEvents(new SubclaimWandListener(this), this);
        manager.registerEvents(new SignSubclaimListener(this), this);
        manager.registerEvents(new ShopSignListener(this), this);
        manager.registerEvents(new SkullListener(), this);
        manager.registerEvents(new SotwListener(this), this);
        manager.registerEvents(new BeaconStrengthFixListener(), this);
        manager.registerEvents(new VoidGlitchFixListener(), this);
        manager.registerEvents(new WallBorderListener(this), this);
        manager.registerEvents(new WorldListener(), this);
    }

    private void registerCommands() {
        getCommand("angle").setExecutor(new AngleCommand());
        getCommand("conquest").setExecutor(new ConquestExecutor(this));
        getCommand("economy").setExecutor(new EconomyCommand(this));
        getCommand("eotw").setExecutor(new EotwCommand(this));
        getCommand("event").setExecutor(new EventExecutor(this));
        getCommand("faction").setExecutor(new FactionExecutor(this));
        getCommand("gopple").setExecutor(new GoppleCommand(this));
        getCommand("koth").setExecutor(new KothExecutor(this));
        getCommand("lives").setExecutor(new LivesExecutor(this));
        getCommand("location").setExecutor(new LocationCommand(this));
        getCommand("logout").setExecutor(new LogoutCommand(this));
        getCommand("mapkit").setExecutor(new MapKitCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("pvptimer").setExecutor(new PvpTimerCommand(this));
        getCommand("regen").setExecutor(new RegenCommand(this));
        getCommand("servertime").setExecutor(new ServerTimeCommand());
        getCommand("sotw").setExecutor(new SotwCommand(this));
        getCommand("spawncannon").setExecutor(new SpawnCannonCommand(this));
        getCommand("staffrevive").setExecutor(new StaffReviveCommand(this));
        getCommand("timer").setExecutor(new TimerExecutor(this));
        getCommand("togglecapzoneentry").setExecutor(new ToggleCapzoneEntryCommand(this));
        getCommand("togglelightning").setExecutor(new ToggleLightningCommand(this));
        getCommand("togglesidebar").setExecutor(new ToggleSidebarCommand(this));

        Map<String, Map<String, Object>> map = getDescription().getCommands();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            PluginCommand command = getCommand(entry.getKey());
            command.setPermission("hcf.command." + entry.getKey());
            command.setPermissionMessage(ChatColor.RED + "You do not have permission for this command.");
        }
    }

    private void registerManagers() {
        this.claimHandler = new ClaimHandler(this);
        this.deathbanManager = new FlatFileDeathbanManager(this);
        this.economyManager = new FlatFileEconomyManager(this);
        this.eotwHandler = new EotwHandler(this);
        this.eventScheduler = new EventScheduler(this);
        this.factionManager = new FlatFileFactionManager(this);
        this.keyManager = new KeyManager(this);
        this.pvpClassManager = new PvpClassManager(this);
        this.sotwTimer = new SotwTimer();
        this.timerManager = new TimerManager(this); // needs to be registered before ScoreboardHandler
        this.scoreboardHandler = new ScoreboardHandler(this);
        this.userManager = new UserManager(this);
        this.visualiseHandler = new VisualiseHandler();
    }
}
