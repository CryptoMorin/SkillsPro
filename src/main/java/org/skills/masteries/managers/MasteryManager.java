package org.skills.masteries.managers;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.masteries.brutality.MasteryFortune;
import org.skills.masteries.brutality.MasteryGlory;
import org.skills.masteries.brutality.MasteryPower;
import org.skills.masteries.brutality.MasteryThickSkin;
import org.skills.masteries.efficiency.*;
import org.skills.masteries.finesse.MasteryAcrobatics;
import org.skills.masteries.finesse.MasteryPrecision;
import org.skills.masteries.finesse.MasteryReap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MasteryManager implements Listener {
    protected static final String PLACED = "PLACED";
    private static final List<Mastery> MASTERIES = new ArrayList<>();

    public MasteryManager() {
        MessageHandler.sendConsolePluginMessage("&3Setting up masteries...");
        MASTERIES.clear();

        new MasteryLogging();
        new MasteryHarvesting();
        new MasteryMining();
        new MasteryPacifist();
        new MasteryRegeneration();

        new MasteryReap();
        new MasteryPrecision();
        new MasteryAcrobatics();
        new MasteryFortune();

        new MasteryGlory();
        new MasteryPower();
        new MasteryThickSkin();

        MASTERIES.sort(Comparator.comparing(Mastery::getName));
        Bukkit.getPluginManager().registerEvents(this, SkillsPro.get());
    }

    protected static void registerDefault(Mastery mastery) {
        MASTERIES.add(mastery);
        Bukkit.getPluginManager().registerEvents(mastery, SkillsPro.get());
    }

    protected static void registerMastery(JavaPlugin plugin, Mastery mastery) {
        MASTERIES.add(mastery);
        Bukkit.getPluginManager().registerEvents(mastery, plugin);
        MASTERIES.sort(Comparator.comparing(Mastery::getName));
    }

    public static void unregisterMastery(String name) {
        if (!isMasteryRegistered(name)) return;
        HandlerList.unregisterAll(getMastery(name));
        MASTERIES.removeIf(x -> x.getName().equalsIgnoreCase(name));
    }

    public static List<Mastery> getMasteries() {
        return MASTERIES;
    }

    public static Mastery getMastery(String name) {
        return MASTERIES.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static boolean isMasteryRegistered(String name) {
        return MASTERIES.stream().anyMatch(x -> x.getName().equalsIgnoreCase(name));
    }

    public boolean isEnabled(Mastery mastery) {
        return SkillsConfig.valueOf("MASTERY_ENABLED_" + mastery.getName()).getBoolean();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent event) {
        event.getBlock().setMetadata(PLACED, new FixedMetadataValue(SkillsPro.get(), null));
    }
}
