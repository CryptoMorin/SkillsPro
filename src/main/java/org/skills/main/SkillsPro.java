package org.skills.main;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.skills.abilities.Ability;
import org.skills.abilities.AbilityListener;
import org.skills.abilities.devourer.DevourerGliders;
import org.skills.commands.SkillsCommandHandler;
import org.skills.commands.TabCompleteManager;
import org.skills.data.database.json.OldSkillsConverter;
import org.skills.data.managers.CosmeticCategory;
import org.skills.data.managers.DataHandlers;
import org.skills.data.managers.PlayerDataManager;
import org.skills.data.managers.backup.SkillsBackup;
import org.skills.events.SkillsEventManager;
import org.skills.gui.GUIConfig;
import org.skills.gui.InteractiveGUIManager;
import org.skills.main.locale.LanguageManager;
import org.skills.managers.*;
import org.skills.managers.blood.BloodManager;
import org.skills.managers.blood.RedScreenManager;
import org.skills.managers.resurrect.LastBreath;
import org.skills.masteries.managers.MasteryManager;
import org.skills.party.PartyManager;
import org.skills.services.ServiceWorldGuard;
import org.skills.services.manager.ServiceHandler;
import org.skills.types.Energy;
import org.skills.types.SkillManager;
import org.skills.types.Stat;
import org.skills.utils.Hologram;
import org.skills.utils.Metrics;
import org.skills.utils.OfflineNBT;
import org.skills.utils.UpdateChecker;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SkillsPro extends JavaPlugin {
    private static SkillsPro instance;
    private LanguageManager languageManager;
    private PlayerDataManager playerDataManager;
    private PartyManager partyManager;
    private UpdateChecker updater;

    public static SkillsPro get() {
        return instance;
    }

    public static void main(String[] args) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, "An unbelievable unexpected unhandled IndexOutOfBoundsException has occurred:\n" +
                "The level is greater than the max value, resulting in an RException." +
                "\n\nThis is a Minecraft plugin.\nPut it in the plugins folder. Don't just click on it.", "RException", JOptionPane.ERROR_MESSAGE);

    }

    public LanguageManager getLang() {
        return languageManager;
    }

    @Override
    public void onLoad() {
        instance = this;
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) ServiceWorldGuard.init();
    }

    @Override
    public void onEnable() {
        FileManager manager = new FileManager(this);
        manager.createDataFolder();
        manager.loadConfig();

        languageManager = new LanguageManager(this);
        new GUIConfig(this);
        Stat.init(this);
        Energy.init(this);
        SkillManager.init(this);
        manager.setupWatchService();

        ServiceHandler.init(this);
        ServiceHandler.registerPlaceHolders();
        CosmeticCategory.load(this);

        PluginCommand cmd = this.getCommand("skills");
        cmd.setExecutor(new SkillsCommandHandler(this));
        cmd.setTabCompleter(new TabCompleteManager());

        updater = new UpdateChecker(this, 8981);
        partyManager = new PartyManager(this);
        registerAllEvents();

        new SkillsBackup(this);
        new Hologram(this);

        playerDataManager = new PlayerDataManager(this);
        new OldSkillsConverter(new File(getDataFolder(), "players"), this);
        playerDataManager.setTopLevels(this);

        updater.checkForUpdates().thenRun(updater::sendUpdates);
        new Metrics(this, 6224); // https://bstats.org/plugin/bukkit/SkillsPro/6224
        if (SkillsConfig.ARMOR_WEIGHTS_RESET_SPEEDS_ENABLED.getBoolean()) OfflineNBT.perform();
    }

    @Override
    public void onDisable() {
        if (playerDataManager == null) return;
        playerDataManager.saveAll();
        partyManager.saveAll();
        Hologram.onDisable();
        DevourerGliders.onDisable();
        Ability.onDisable();
    }

    public void reload() {
        languageManager = new LanguageManager(this);
    }

    private void registerEvent(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerAllEvents() {
        registerEvent(new HealthAndEnergyManager(this));
        registerEvent(new AbilityListener());
        registerEvent(new InteractiveGUIManager());
        registerEvent(new SkillItemManager());
        registerEvent(new EnchantmentManager());
        registerEvent(new SkillsEventManager());
        registerEvent(new DebugManager());
        registerEvent(new StatManager());
        if (SkillsConfig.LAST_BREATH_ENABLED.getBoolean() && XMaterial.supports(13)) registerEvent(new LastBreath());
        if (SkillsConfig.SMART_DAMAGE.getBoolean()) registerEvent(new DamageManager());
        if (SkillsConfig.RED_SCREEN_ENABLED.getBoolean() || SkillsConfig.PULSE_ENABLED.getBoolean()) registerEvent(new RedScreenManager());
        if (SkillsConfig.BLOOD_ENABLED.getBoolean()) registerEvent(new BloodManager());

        registerEvent(new LevelManager(this));
        if (SkillsMasteryConfig.MASTERIES_ENABLED.getBoolean()) new MasteryManager();
        if (SkillsConfig.ARMOR_WEIGHTS_ENABLED.getBoolean()) registerEvent(new ArmorWeights());
        registerEvent(new DataHandlers(this));
        registerEvent(partyManager);
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public UpdateChecker getUpdater() {
        return updater;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }
}