package org.skills.abilities.juggernaut;

import com.cryptomorin.xseries.XSound;
import com.google.common.base.Enums;
import com.google.common.base.Optional;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.LastHitManager;
import org.skills.utils.versionsupport.VersionSupport;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JuggernautThrow extends ActiveAbility {
    private static final Map<Integer, Integer>
            ACTIVE_CARRY_COUNT = new HashMap<>(),
            THROW_PAIRS = new HashMap<>();

    static {
        addDisposableHandler(ACTIVE_CARRY_COUNT, THROW_PAIRS);
    }

    public JuggernautThrow() {
        super("Juggernaut", "throw", false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJuggernautAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getWorld())) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.activeCheckup(player);
        if (info == null) return;

        boolean pass = false;
        for (String mobs : getExtra(info, "whitelist").getStringList()) {
            if (mobs.equals("*")) {
                pass = true;
                break;
            }

            @SuppressWarnings("Guava")
            Optional<EntityType> type = Enums.getIfPresent(EntityType.class, mobs.toUpperCase(Locale.ENGLISH));
            if (type.isPresent() && type.get() == event.getEntity().getType()) {
                pass = true;
                break;
            }
        }
        if (!pass) return;

        useSkill(player);
        carryEntity(player, info, (LivingEntity) event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityAttemptDamage(EntityDamageByEntityEvent event) {
        if (THROW_PAIRS.containsKey(event.getEntity().getEntityId())) event.setDamage(event.getDamage() / 2);
        if (THROW_PAIRS.containsKey(event.getDamager().getEntityId()) || THROW_PAIRS.containsValue(event.getDamager().getEntityId())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onThrowExit(EntityDismountEvent event) {
        if (THROW_PAIRS.containsKey(event.getDismounted().getEntityId())) event.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    private void carryEntity(Player carrier, SkilledPlayer info, LivingEntity target) {
        THROW_PAIRS.put(carrier.getEntityId(), target.getEntityId());
        carrier.setPassenger(target);
        SkillsLang.Skill_Juggernaut_Active_Activated_Message.sendMessage(carrier);

        new BukkitRunnable() {
            public void run() {
                int i = ACTIVE_CARRY_COUNT.getOrDefault(target.getEntityId(), 0);
                if (target.isValid() && i < 3 && carrier.isValid()) {
                    i++;
                    ACTIVE_CARRY_COUNT.put(target.getEntityId(), i);
                    if (!VersionSupport.isPassenger(carrier, target)) carrier.setPassenger(target);
                    SkillsLang.Skill_Juggernaut_Active_Throw_Message_Countdown.sendMessage(carrier, "%countdown%", 3 - i);
                    carrier.playNote(carrier.getLocation(), Instrument.CHIME, Note.natural(1, Note.Tone.values()[i]));
                } else {
                    THROW_PAIRS.remove(carrier.getEntityId());
                    ACTIVE_CARRY_COUNT.remove(target.getEntityId());
                    SkillsLang.Skill_Juggernaut_Active_Throw_Success.sendMessage(carrier);
                    cancel();
                    if (!VersionSupport.isPassenger(carrier, target)) carrier.setPassenger(target);
                    carrier.eject();
                    target.eject();
                    target.teleport(carrier.getLocation().add(0, 1, 0));

                    Vector vector = carrier.getLocation().getDirection().multiply(2);
                    target.setVelocity(vector);
                    double damage = getScaling(info);
                    LastHitManager.damage(target, carrier, damage);
                    XSound.ENTITY_EGG_THROW.play(carrier, 2, 0);
                }
            }
        }.runTaskTimer(SkillsPro.get(), 0L, 20L);
    }
}
