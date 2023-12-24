package org.skills.data.managers;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.skills.abilities.Ability;
import org.skills.abilities.ActiveAbility;
import org.skills.abilities.eidolon.EidolonForm;
import org.skills.api.events.*;
import org.skills.data.database.DataContainer;
import org.skills.events.SkillsEvent;
import org.skills.events.SkillsEventType;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.LevelManager;
import org.skills.managers.LevelUp;
import org.skills.masteries.managers.Mastery;
import org.skills.masteries.managers.MasteryManager;
import org.skills.party.PartyRank;
import org.skills.party.SkillsParty;
import org.skills.types.Skill;
import org.skills.types.SkillScaling;
import org.skills.types.Stat;
import org.skills.utils.Cooldown;
import org.skills.utils.FastUUID;
import org.skills.utils.NoEpochDate;

import javax.annotation.Nonnull;
import java.util.*;

public class SkilledPlayer extends DataContainer {
    private transient PlayerSkill skill = new PlayerSkill(PlayerSkill.NONE);
    private Map<String, PlayerSkill> skills = new HashMap<String, PlayerSkill>() {{
        put(PlayerSkill.NONE, skill);
    }};
    private transient UUID id;
    private transient double energyBooster, energy;
    private double healthScaling = SkillsConfig.DEFAULT_HEALTH_SCALING.getDouble();
    private long lastSkillChange;
    private transient EidolonForm form = EidolonForm.LIGHT;
    private transient ActiveAbility lastAbilityUsed, activeReady;
    private boolean showActionBar = SkillsConfig.ACTIONBAR_DEFAULT.getBoolean();
    private Map<String, Integer> masteries = new HashMap<>();
    private Map<SkillsEventType, SkillsEvent> bonuses = new EnumMap<>(SkillsEventType.class);
    private Map<String, Cosmetic> cosmetics = new HashMap<>();
    private Set<UUID> friends = new HashSet<>();
    private Set<UUID> friendRequests = new HashSet<>();
    private UUID party;
    private PartyRank rank;
    /**
     * This is mainly used as a protection for owners that set the ability cooldown lower than the
     * ability duration. It's also useful as some kind of meta data if anyones interested.
     */
    private final transient Set<Ability> activeAbilities = Collections.newSetFromMap(new IdentityHashMap<>());

    public SkilledPlayer(UUID id) {
        this.id = Objects.requireNonNull(id, "Skilled Player's UUID cannot be null");
        SkillsPro.get().getPlayerDataManager().load(this);
    }

    public SkilledPlayer() {
    }

    public static @NonNull
    SkilledPlayer getSkilledPlayer(@NonNull UUID id) {
        Objects.requireNonNull(id, "Cannot get skilled player data with null player UUID");
        SkilledPlayer info = SkillsPro.get().getPlayerDataManager().getData(id);
        if (info == null) {
            info = new SkilledPlayer(id);
            if (SkillsConfig.STARTER_ENABLED.getBoolean()) {
                info.setLevel(SkillsConfig.STARTER_LEVEL.getInt());
                info.setSouls(SkillsConfig.STARTER_SOULS.getLong());

                ConfigurationSection section = SkillsConfig.STARTER_STATS.getSection();
                for (String key : section.getKeys(false)) {
                    Stat stat = Stat.getStat(key);
                    if (stat == null) continue;
                    info.setStat(stat, section.getInt(key));
                }

                int masteryLvl = SkillsConfig.STARTER_MASTERIES.getInt();
                if (masteryLvl > 0) {
                    for (Mastery mastery : MasteryManager.getMasteries()) {
                        info.masteries.put(mastery.getName(), masteryLvl);
                    }
                }
            }
        }
        return info;
    }

    public static @NonNull
    SkilledPlayer getSkilledPlayer(@NonNull OfflinePlayer player) {
        Objects.requireNonNull(player, "Cannot get skilled player data for null player");
        return getSkilledPlayer(player.getUniqueId());
    }

    public boolean hasSkill(Skill skill) {
        return skills.containsKey(skill.getName());
    }

    public Set<UUID> getFriends() {
        return friends;
    }

    public void setFriends(Set<UUID> friends) {
        this.friends = friends;
    }

    public Set<UUID> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(Set<UUID> friendRequests) {
        this.friendRequests = friendRequests;
    }

    @NonNull
    public Map<SkillsEventType, SkillsEvent> getBonuses() {
        bonuses.values().removeIf(event -> !event.isActive());
        return bonuses;
    }

    public void setBonuses(Map<SkillsEventType, SkillsEvent> bonuses) {
        this.bonuses = bonuses;
    }

    public Set<Ability> getActiveAbilities() {
        return activeAbilities;
    }

    public void setActiveAbilitiy(Ability ability, boolean active) {
        if (active) activeAbilities.add(ability);
        else activeAbilities.remove(ability);
    }

    public Map<String, PlayerSkill> getSkills() {
        return skills;
    }

    public void setSkills(Map<String, PlayerSkill> skills) {
        this.skills = skills;
    }

    public double getLevelXP(int level) {
        return LevelUp.getLevel(this, level);
    }

    public double getLevelXP() {
        return getLevelXP(this.skill.getLevel());
    }

    public Cosmetic setCosmetic(Cosmetic cosmetic) {
        Objects.requireNonNull(cosmetic, "Cannot set null cosmetic");
        return cosmetics.put(cosmetic.getCategory().getName(), cosmetic);
    }

    public BukkitTask setEnergy(double amount) {
        Player player = getPlayer();
        if (player == null)
            throw new IllegalStateException("Cannot set energy of offline player: " + getOfflinePlayer().getName() + " - " + getOfflinePlayer().getLastPlayed());

        return new BukkitRunnable() {
            @Override
            public void run() {
                SkillEnergyChangeEvent event = new SkillEnergyChangeEvent(player, SkilledPlayer.this, amount);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;

                SkilledPlayer.this.energy = event.getAmount();
                if (skill.getSkillName().equalsIgnoreCase("eidolon")) {
                    if (SkilledPlayer.this.energy >= getScaling(SkillScaling.MAX_ENERGY)) {
                        EidolonForm newForm = form == EidolonForm.DARK ? EidolonForm.LIGHT : EidolonForm.DARK;
                        EidolonImbalanceChangeEvent eidolonFormChange = new EidolonImbalanceChangeEvent(player, newForm);
                        Bukkit.getPluginManager().callEvent(eidolonFormChange);

                        SkilledPlayer.this.form = eidolonFormChange.getNewForm();
                        SkilledPlayer.this.energy = 0;
                    }
                }
            }
        }.runTaskAsynchronously(SkillsPro.get());
    }

    public void setCooldown(long amount) {
        new Cooldown(id, "CD", amount);
    }

    public long getCooldownTimeLeft() {
        return Cooldown.getTimeLeft(id, "CD");
    }

    public boolean isInCooldown() {
        return Cooldown.isInCooldown(id, "CD");
    }

    public boolean showReadyMessage() {
        return skill.showReadyMessage();
    }

    public void setShowReadyMessage(boolean showReadyMessage) {
        skill.setShowReadyMessage(showReadyMessage);
    }

    public @NonNull
    UUID getId() {
        return this.id;
    }

    public void setId(@NonNull UUID id) {
        Objects.requireNonNull(id, "Cannot set Skilled player's UUID to null");
        this.id = id;
    }

    @Override
    @NotNull
    public String getCompressedData() {
        return skill + compressUUID(party) + (rank == null ? "" : rank.ordinal()) + (healthScaling == 0 ? "" : healthScaling)
                + compressCollecton(bonuses.values(), x -> x.start + x.time)
                + compressCollecton(masteries.values(), x -> x)
                + compressCollecton(friends, DataContainer::compressUUID)
                + compressCollecton(friendRequests, DataContainer::compressUUID)
                + compressCollecton(skills.values(), PlayerSkill::getCompressedData);
    }

    @Override
    public @NonNull
    String getKey() {
        return FastUUID.toString(this.id);
    }

    @Override
    public void setIdentifier(@NonNull String identifier) {
        this.id = FastUUID.fromString(identifier);
    }

    public @NonNull
    Skill getSkill() {
        Skill skill = this.skill.getSkill();
        if (skill == null) {
            MessageHandler.sendConsolePluginMessage("&4Unable to find skill (class) with name &e" + this.skill.skill
                    + " &4for player&8: &e" + getOfflinePlayer().getName() + " &8(&e" + this.id + "&8)");
            MessageHandler.sendConsolePluginMessage("&4Internal skill names might've been changed in the Skills folder. Defaulting back to 'none' skill");

            this.skill = new PlayerSkill(PlayerSkill.NONE);
            skill = this.skill.getSkill();
            if (skill == null) throw new AssertionError("Cannot find the default skill: " + PlayerSkill.NONE);
        }

        return skill;
    }

    public void setAbsoluteActiveSkill(@NonNull PlayerSkill skill) {
        this.skill = Objects.requireNonNull(skill, "Active player skill cannot be null");
    }

    public int getLevel() {
        return skill.level;
    }

    public void setLevel(int level) {
        skill.setLevel(level);
    }

    public void addLevel(int level) {
        setLevel(skill.level + level);
        skill.xp = Math.min(getXP(), getLevelXP());
    }

    public double getXP() {
        return skill.getXP();
    }

    /**
     * Silent is false as default.
     *
     * @see #setXP(double, boolean)
     */
    public void setXP(double xp) {
        setXP(xp, false);
    }

    /**
     * This method will run a level check as well.<br>
     * This should not be used to decrease XP, use {@link #decreaseXP(double)}
     *
     * @param xp     the amount of XP to give.
     * @param silent if true, level will manually increase instead of using {@link #levelUp(int)}
     */
    public void setXP(double xp, boolean silent) {
        if (xp == 0) {
            setAbsoluteXP(0);
            return;
        } else if (xp < 0) {
            decreaseXP(Math.abs(xp));
            return;
        }

        int addedLevels = 0;

        double needed;
        int lvl = 0;
        while (xp >= (needed = getLevelXP(getLevel() + lvl++))) {
            xp -= needed;
            addedLevels++;
        }

        if (silent) addLevel(addedLevels);
        else levelUp(addedLevels);
        setAbsoluteXP(xp);
    }

    public void setAbsoluteXP(double xp) {
        skill.setAbsoluteXP(xp);
    }

    public void chargeEnergy() {
        chargeEnergy(0);
    }

    public void chargeEnergy(double extra) {
        Skill skill = getSkill();

        if (!Cooldown.isInCooldown(this.id, "ENERGY_BOOSTER")) energyBooster = 0;
        double maxEnergy = skill.getScaling(this, SkillScaling.MAX_ENERGY);
        double energyRegen = skill.getScaling(this, SkillScaling.ENERGY_REGEN) + extra + (extra <= 0 ? 0 : energyBooster);
        double finale = energy;

        if (finale >= maxEnergy) return;
        if (energy + energyRegen >= maxEnergy) finale = maxEnergy;
        else finale += energyRegen;

        setEnergy(finale);
        if (finale >= maxEnergy) XSound.play(getPlayer(), getSkill().getEnergy().getSoundFull());
    }

    public void addXP(double xp) {
        addXP(xp, false);
    }

    public void addSouls(long souls) {
        this.skill.souls += souls;
    }

    public boolean hasSounds(long souls) {
        return this.skill.souls >= souls;
    }

    public void addXP(double xp, boolean silent) {
        if (xp == 0) return;
        if (xp < 0) decreaseXP(-xp);
        else setXP(this.skill.xp + xp, silent);
    }

    public String getSkillName() {
        return this.skill.skill;
    }

    /**
     * Decreases your XP and levels down if needed.
     * Doesn't call {@link SkillLevelUpEvent}.
     *
     * @param xp the amount of XP to remove.
     */
    private void decreaseXP(double xp) {
        if (xp == 0) return;
        xp = Math.abs(xp);
        while (true) {
            if (getXP() < xp) {
                if (getLevel() <= 0) {
                    setLevel(0);
                    setAbsoluteXP(0);
                    return;
                }

                xp -= getXP();
                addLevel(-1);
                setAbsoluteXP(getLevelXP(getLevel()));
            } else {
                setAbsoluteXP(getXP() - xp);
                return;
            }
        }
    }

    /**
     * Sets the XP from zero, and it will still levelup.
     *
     * @param xp     the raw amount of XP to set.
     * @param silent if true, level will manually increase instead of using {@link #levelUp(int)}
     */
    public void setRawXP(double xp, boolean silent) {
        setLevel(0);
        setXP(xp, silent);
    }

    /**
     * Gets the current XP and the total XP gained to levelup for each level.
     *
     * @return raw total XP.
     */
    public double getRawXP() {
        if (getLevel() < 1) return getXP();
        return getXP() + getLevelXP(getLevel() - 1);
    }

    public boolean willLevelUp(double xp) {
        return getXP() + xp >= getLevelXP(getLevel());
    }

    public long getSouls() {
        return skill.getSouls();
    }

    public void setSouls(long souls) {
        skill.setSouls(souls);
    }

    public boolean hasSkill() {
        return !skill.skill.equals(PlayerSkill.NONE);
    }

    public int getMasteryLevel(Mastery mastery) {
        return masteries.getOrDefault(mastery.getName().toLowerCase(), 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SkilledPlayer)) return false;
        SkilledPlayer info = (SkilledPlayer) obj;
        return this.id.equals(info.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public void setScaledHealth() {
        Player player = Objects.requireNonNull(getPlayer(), "Cannot scale an offline players health");
        if (this.healthScaling < 0) {
            MessageHandler.sendConsolePluginMessage("&cInvalid health scaling for " + player.getName() + ": " + this.healthScaling + ". Resetting...");
            this.healthScaling = 0;
            return;
        }

        if (this.healthScaling == 0) {
            player.setHealthScaled(false);
            return;
        }

        if (!player.isHealthScaled()) player.setHealthScaled(true);
        player.setHealthScale(this.healthScaling);
    }

    public boolean hasAbility(Ability ability) {
        return getSkill().hasAbility(ability);
    }

    public boolean isActiveReady(ActiveAbility ability) {
        return ability == this.activeReady;
    }

    public boolean setActiveReady(ActiveAbility ability, boolean isReady) {
        if (isReady) {
            if (isActiveReady(ability)) return false;
        } else if (!isActiveReady()) return false;

        Player player = getPlayer();
        if (player == null) throw new IllegalArgumentException("Cannot change an offline player ability state");

        SkillActiveStateChangeEvent event = new SkillActiveStateChangeEvent(player, ability, isReady);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        this.activeReady = isReady ? ability : null;
        return true;
    }

    public void deactivateReady() {
        this.activeReady = null;
    }

    public ActiveAbility getActiveReady() {
        return activeReady;
    }

    public boolean isActiveReady() {
        return this.activeReady != null;
    }

    public double getScaling(SkillScaling type) {
        return getSkill().getScaling(this, type);
    }

    /**
     * Increases the level and calls {@link SkillLevelUpEvent} for each increase.
     *
     * @param levels the amount to increase the level.
     * @see #setXP(double, boolean)
     * @see #setRawXP(double, boolean)
     * @see #setLevel(int)
     */
    public void levelUp(int levels) {
        if (levels == 0) return;
        Player player = getPlayer();

        if (player != null) {
            int maxLvl = (int) getScaling(SkillScaling.MAX_LEVEL);
            int newLvl = getLevel() + levels;

            if (this.skill.level >= maxLvl) {
                SkillsLang.MAX_LEVEL.sendMessage(player);
                return;
            } else if (newLvl > maxLvl) levels = newLvl - maxLvl;

            SkillLevelUpEvent event = new SkillLevelUpEvent(this, player, levels);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            LevelManager.onLevelUp(event);
            levels = event.getAddedLevel();
        }

        setLevel(getLevel() + levels);
        this.skill.xp = 0;
    }

    public boolean canChangeSkill() {
        if (lastSkillChange < 1) return true;
        long cd = SkillsConfig.SKILL_CHANGE_COOLDOWN.getTimeMillis();
        if (cd < 0) return false;
        if (cd == 0) return true;

        return System.currentTimeMillis() - lastSkillChange > cd;
    }

    public long getTimeLeftToChangeSkill() {
        long cd = SkillsConfig.SKILL_CHANGE_COOLDOWN.getTimeMillis();
        if (cd < 1) return 0;
        long diff = System.currentTimeMillis() - lastSkillChange;
        if (diff < 1) return 0;

        return Math.abs(cd - diff);
    }

    public String getTimeLeftToChangeSkillString() {
        return new NoEpochDate(getTimeLeftToChangeSkill()).format(SkillsConfig.TIME_FORMAT.getString());
    }

    public void changedSkill() {
        lastSkillChange = System.currentTimeMillis();
    }

    public @NonNull
    OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(this.id);
    }

    public @Nullable
    Player getPlayer() {
        return Bukkit.getPlayer(this.id);
    }

    public EidolonForm getForm() {
        return form;
    }

    public void setForm(@NonNull EidolonForm form) {
        this.form = form;
    }

    public void toggleAbility(Ability ability) {
        PlayerAbilityData data = this.skill.getAbilityData(ability);
        boolean disabled = !data.isDisabled();
        data.setDisabled(disabled);

        SkillToggleAbilityEvent event = new SkillToggleAbilityEvent(getPlayer(), ability, disabled);
        Bukkit.getPluginManager().callEvent(event);
    }

    public boolean isAbilityDisabled(Ability ability) {
        return this.skill.getAbilityData(ability).isDisabled();
    }

    public @Nullable
    SkillsEvent getBonus(SkillsEventType type) {
        return getBonuses().get(type);
    }

    public void addBonus(SkillsEvent bonus) {
        this.bonuses.put(bonus.getType(), bonus);
    }

    public boolean hasParty() {
        return this.party != null;
    }

    public SkillsEvent removeBonus(SkillsEventType type) {
        this.bonuses.values().forEach(SkillsEvent::stop);
        return this.bonuses.remove(type);
    }

    public void upgradeAbility(@NonNull Ability ability) {
        addAbilityLevel(ability, 1);
        SkillImproveEvent event = new SkillImproveEvent(this.getPlayer(), ability);
        Bukkit.getPluginManager().callEvent(event);
    }

    public int getStat(@NonNull String type) {
        return this.skill.getStats().getOrDefault(type.toUpperCase(Locale.ENGLISH), 0);
    }

    public int getStat(@Nonnull Stat stat) {
        return getStat(stat.getDataNode());
    }

    public int getPoints() {
        return getStat(Stat.POINTS.getDataNode());
    }

    public void addStat(@NonNull String type, int amount) {
        setStat(type, getStat(type) + amount);
    }

    public void addStat(Stat type, int amount) {
        addStat(type.getDataNode(), amount);
    }

    public void setStat(@NonNull String type, int amount) {
        this.skill.getStats().put(type, amount);
    }

    public void setStat(@Nonnull Stat stat, int amount) {
        this.skill.getStats().put(stat.getDataNode(), amount);
    }

    public int resetStats() {
        return skill.resetStats();
    }

    public Map<String, Integer> getStats() {
        return this.skill.getStats();
    }

    public void setMasteryLevel(@NonNull Mastery mastery, int level) {
        masteries.put(mastery.getName().toLowerCase(), level);
    }

    public void addMasteryLevel(@NonNull Mastery mastery, int level) {
        if (level != 0) setMasteryLevel(mastery, getMasteryLevel(mastery) + level);
    }

    public @NonNull
    Map<String, Integer> getMasteries() {
        return masteries;
    }

    public void setMasteries(@NonNull HashMap<String, Integer> masteries) {
        this.masteries = masteries;
    }

    public Map<String, PlayerAbilityData> getAbilities() {
        return this.skill.abilities;
    }

    public void setAbilityLevel(@NonNull Ability ability, int level) {
        skill.setAbilityLevel(ability, level);
    }

    public void addAbilityLevel(@NonNull Ability ability, int level) {
        skill.addAbilityLevel(ability, level);
    }

    public int getAbilityLevel(@NonNull Ability ability) {
        return skill.getAbilityLevel(ability);
    }

    public PlayerAbilityData getAbilityData(Ability ability) {
        return skill.getAbilityData(ability);
    }

    public long getLastSkillChange() {
        return lastSkillChange;
    }

    public void setLastSkillChange(long lastSkillChange) {
        this.lastSkillChange = lastSkillChange;
    }

    public List<OfflinePlayer> getPlayerFriends() {
        List<OfflinePlayer> friends = new ArrayList<>(this.friends.size());
        for (UUID member : this.friends) {
            friends.add(Bukkit.getOfflinePlayer(member));
        }
        return friends;
    }

    public UUID getPartyId() {
        return party;
    }

    public SkillsParty getParty() {
        if (this.party == null) return null;

        SkillsParty party = SkillsParty.getParty(this.party);
        if (party == null && this.party != null) {
            MessageHandler.sendConsolePluginMessage("&4Invalid party data for &e" + getOfflinePlayer().getName() + "&4. Removing them from party now... " +
                    "Please do not report if any error occured after this message.");
            this.party = null;
            this.rank = null;
        }
        return party;
    }

    public void setParty(SkillsParty party) {
        this.party = party.getId();
    }

    public void setParty(UUID party) {
        this.party = party;
    }

    public void joinParty(SkillsParty party) {
        setParty(party);
        party.getMembers().add(id);
        this.rank = PartyRank.MEMBER;
    }

    public PlayerSkill getActiveSkill() {
        return this.skill;
    }

    public void setActiveSkill(@NonNull String skill) {
        PlayerSkill activeSkill = skills.get(skill);
        if (activeSkill == null) {
            activeSkill = new PlayerSkill(skill);
            skills.put(skill, activeSkill);
        }
        if (SkillsConfig.SKILLS_SHARED_DATA_SOULS.getBoolean()) activeSkill.setSouls(getSouls());
        if (SkillsConfig.SKILLS_SHARED_DATA_STATS.getBoolean()) activeSkill.setStats(getStats());
        if (SkillsConfig.SKILLS_SHARED_DATA_LEVELS.getBoolean()) {
            activeSkill.setLevel(getLevel());
            activeSkill.setAbsoluteXP(getXP());
        }
        skills.put(this.skill.skill, this.skill);
        this.skill = activeSkill;
    }

    public ClassChangeEvent setActiveSkill(@NonNull Skill skill) {
        ClassChangeEvent classChangeEvent = new ClassChangeEvent(this, skill);
        Bukkit.getPluginManager().callEvent(classChangeEvent);
        if (classChangeEvent.isCancelled()) return classChangeEvent;
        skill = classChangeEvent.getNewSkill();

        setActiveSkill(skill.getName());
        changedSkill();
        return classChangeEvent;
    }

    public void leaveParty() {
        getParty().getMembers().remove(this.id);
        this.party = null;
        this.rank = null;
    }

    public void friendRequest(Player player) {
        friendRequests.add(player.getUniqueId());
    }

    public void settleFriendRequest(Player player) {
        friendRequests.remove(player.getUniqueId());
    }

    public PartyRank getRank() {
        return rank;
    }

    public void setRank(PartyRank rank) {
        this.rank = rank;
    }

    public boolean isFrendly(SkilledPlayer info) {
        if (friends.contains(info.id)) return true;
        return party != null && party.equals(info.party);
    }

    public boolean isFrendly(OfflinePlayer player) {
        return isFrendly(getSkilledPlayer(player));
    }

    public double getHealthScaling() {
        return healthScaling;
    }

    public void setHealthScaling(double healthScaling) {
        this.healthScaling = healthScaling;
    }

    public double getEnergy() {
        return energy;
    }

    public double getEnergyBooster() {
        return energyBooster;
    }

    public void setEnergyBooster(double energyBooster) {
        this.energyBooster = energyBooster;
    }

    public ActiveAbility getLastAbilityUsed() {
        return lastAbilityUsed;
    }

    public void setLastAbilityUsed(ActiveAbility lastAbilityUsed) {
        this.lastAbilityUsed = lastAbilityUsed;
    }

    public Map<String, Cosmetic> getCosmetics() {
        return cosmetics;
    }

    public void setCosmetics(Map<String, Cosmetic> cosmetics) {
        this.cosmetics = cosmetics;
    }

    public Cosmetic getCosmetic(String category) {
        Cosmetic cosmetic = cosmetics.get(category);
        if (cosmetic != null) return cosmetic;

        CosmeticCategory categoryObj = CosmeticCategory.CATEGORIES.get(category);
        if (categoryObj != null) return categoryObj.getCosmetics().get("none");
        return null;
    }

    public boolean showActionBar() {
        return showActionBar;
    }

    public void setShowActionBar(boolean showActionBar) {
        this.showActionBar = showActionBar;
    }
}
