package gg.uhc.uhc;

import gg.uhc.uhc.command.ShowIconsCommand;
import gg.uhc.uhc.modules.ModuleRegistry;
import gg.uhc.uhc.modules.border.WorldBorderCommand;
import gg.uhc.uhc.modules.commands.ModuleCommands;
import gg.uhc.uhc.modules.difficulty.DifficultyModule;
import gg.uhc.uhc.modules.enderpearls.EnderpearlsModule;
import gg.uhc.uhc.modules.food.ExtendedSaturationModule;
import gg.uhc.uhc.modules.food.FeedCommand;
import gg.uhc.uhc.modules.heads.GoldenHeadsHealthCommand;
import gg.uhc.uhc.modules.heads.GoldenHeadsModule;
import gg.uhc.uhc.modules.heads.HeadDropsModule;
import gg.uhc.uhc.modules.heads.PlayerHeadProvider;
import gg.uhc.uhc.modules.health.GhastTearDropsModule;
import gg.uhc.uhc.modules.health.HealCommand;
import gg.uhc.uhc.modules.health.HealthRegenerationModule;
import gg.uhc.uhc.modules.health.PlayerListHealthCommand;
import gg.uhc.uhc.modules.inventory.ClearInventoryCommand;
import gg.uhc.uhc.modules.inventory.ClearXPCommand;
import gg.uhc.uhc.modules.inventory.ResetPlayerCommand;
import gg.uhc.uhc.modules.portals.NetherModule;
import gg.uhc.uhc.modules.potions.*;
import gg.uhc.uhc.modules.pvp.GlobalPVPModule;
import gg.uhc.uhc.modules.recipes.GlisteringMelonRecipeModule;
import gg.uhc.uhc.modules.recipes.GoldenCarrotRecipeModule;
import gg.uhc.uhc.modules.recipes.NotchApplesModule;
import gg.uhc.uhc.modules.team.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;

public class UHC extends JavaPlugin {

    protected ModuleRegistry registry;
    protected DebouncedRunnable configSaver;

    @Override
    public void onEnable() {
        // setup to save the config with a debounce of 2 seconds
        configSaver = new DebouncedRunnable(this, new Runnable() {
            @Override
            public void run() {
                saveConfigNow();
            }
        }, 40);

        registry = new ModuleRegistry(this, getConfig());


        // TODO configuration to stop modules loading at all
        registry.register(new DifficultyModule(), "HardDifficulty");
        registry.register(new HealthRegenerationModule(), "HealthRegen");
        registry.register(new GhastTearDropsModule(), "GhastTears");
        registry.register(new GoldenCarrotRecipeModule(), "GoldenCarrotRecipe");
        registry.register(new GlisteringMelonRecipeModule(), "GlisteringMelonRecipe");
        registry.register(new NotchApplesModule(), "NotchApples");
        registry.register(new AbsorptionModule(), "Absoption");
        registry.register(new ExtendedSaturationModule(), "ExtendedSaturation");
        registry.register(new GlobalPVPModule(), "PVP");
        registry.register(new EnderpearlsModule(), "EnderpearlDamage");
        registry.register(new WitchesModule(), "WitchSpawns");
        registry.register(new NetherModule(), "Nether");

        PotionFuelsListener fuelsListener = new PotionFuelsListener();
        registry.registerEvents(fuelsListener);
        registry.register(new Tier2PotionsModule(fuelsListener), "Tier2Potions");
        registry.register(new SplashPotionsModule(fuelsListener), "SplashPotions");

        PlayerHeadProvider headProvider = new PlayerHeadProvider();
        GoldenHeadsModule gheadModule = new GoldenHeadsModule(headProvider);
        getCommand("ghead").setExecutor(new GoldenHeadsHealthCommand(gheadModule));
        registry.register(new HeadDropsModule(headProvider), "HeadDrops");
        registry.register(gheadModule, "GoldenHeads");

        TeamModule teamModule = new TeamModule();
        registry.register(teamModule, "TeamManager");

        getCommand("teams").setExecutor(new ListTeamsCommand(teamModule));
        getCommand("team").setExecutor(new TeamCommands(teamModule));
        getCommand("noteam").setExecutor(new NoTeamCommand(teamModule));
        getCommand("pmt").setExecutor(new TeamPMCommand(teamModule));
        getCommand("randomteams").setExecutor(new RandomTeamsCommand(teamModule));
        getCommand("clearteams").setExecutor(new ClearTeamsCommand(teamModule));

        // TODO team requests?
        // TODO timer
        // TODO add freeze/scatter to this repo?
        // TODO horse armour + horses
        // TODO death message removal/coords
        // TODO death bans?
        // TODO death items?
        // TODO tpp?
        // TODO figure out hardcore hearts 1.8

        getCommand("border").setExecutor(new WorldBorderCommand());
        getCommand("addons").setExecutor(new ShowIconsCommand(registry.getInventory()));
        getCommand("uhc").setExecutor(new ModuleCommands(registry));
        getCommand("showhealth").setExecutor(new PlayerListHealthCommand(
                Bukkit.getScoreboardManager().getMainScoreboard(),
                DisplaySlot.PLAYER_LIST,
                "UHCHealth",
                "Health"
        ));

        PlayerResetter resetter = new PlayerResetter();
        getCommand("heal").setExecutor(new HealCommand(resetter));
        getCommand("feed").setExecutor(new FeedCommand(resetter));
        getCommand("clearxp").setExecutor(new ClearXPCommand(resetter));
        getCommand("ci").setExecutor(new ClearInventoryCommand(resetter));
        getCommand("reset").setExecutor(new ResetPlayerCommand(resetter));
        getCommand("cleareffects").setExecutor(new ClearPotionsCommand(resetter));

        // save config just to make sure at the end
        saveConfig();
    }

    @Override
    public void saveConfig() {
        configSaver.trigger();
    }

    public void saveConfigNow() {
        super.saveConfig();
        getLogger().info("Saved configuration changes");
    }
}
