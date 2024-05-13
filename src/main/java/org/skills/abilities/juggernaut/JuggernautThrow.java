package org.skills.abilities.juggernaut;

import com.cryptomorin.xseries.XSound;
import com.google.common.base.Enums;
import com.google.common.base.Optional;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.DamageManager;
import org.skills.utils.MathUtils;
import org.skills.utils.versionsupport.VersionSupport;

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
        super("Juggernaut", "throw");
    }

    @EventHandler(ignoreCancelled = true)
    public void onJuggernautAttack(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        boolean pass = false;
        for (String mobs : getOptions(info, "whitelist").getStringList()) {
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

        carryEntity(player, info, (LivingEntity) event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityAttemptDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        if (victim instanceof Player && THROW_PAIRS.containsKey(victim.getEntityId())) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer((Player) victim);
            double damage = event.getDamage();
            event.setDamage(damage - MathUtils.percentOfAmount(getScaling(info, "shield-percent"), damage));
        }
        if (THROW_PAIRS.containsKey(damager.getEntityId()) || THROW_PAIRS.containsValue(damager.getEntityId()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onThrowExit(EntityDismountEvent event) {
        if (THROW_PAIRS.containsKey(event.getDismounted().getEntityId())) event.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    private void carryEntity(Player carrier, SkilledPlayer info, LivingEntity target) {
        THROW_PAIRS.put(carrier.getEntityId(), target.getEntityId());
        carrier.setPassenger(target); // IllegalArgumentException: Entity cannot ride itself (fixed in commonDamageCheckup)
        SkillsLang.Skill_Juggernaut_Active_Activated_Message.sendMessage(carrier);

        new BukkitRunnable() {
            final int carrySeconds = (int) getScaling(info, "carry-time");

            public void run() {
                int i = ACTIVE_CARRY_COUNT.getOrDefault(target.getEntityId(), 0);
                if (target.isValid() && i < carrySeconds && carrier.isValid()) {
                    i++;
                    ACTIVE_CARRY_COUNT.put(target.getEntityId(), i);
                    if (!VersionSupport.isPassenger(carrier, target)) carrier.setPassenger(target);
                    SkillsLang.Skill_Juggernaut_Active_Throw_Message_Countdown.sendMessage(carrier, "%countdown%", carrySeconds - i);
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
                    double damage = getScaling(info, "damage");
                    DamageManager.damage(target, carrier, damage);
                    XSound.ENTITY_EGG_THROW.play(carrier.getLocation());
                }
            }
        }.runTaskTimer(SkillsPro.get(), 0L, 20L);
    }
}
