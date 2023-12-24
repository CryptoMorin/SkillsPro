package org.skills.abilities.vergil;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.util.Vector;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.main.SkillsPro;
import org.skills.managers.DamageManager;
import org.skills.utils.EntityUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VergilMirageEdgeSlash extends InstantActiveAbility {
    private static final Cache<UUID, Integer> PERFECT_JUDGEMENT_CUTS = CacheBuilder.newBuilder()
            .expireAfterAccess(500, TimeUnit.MILLISECONDS).build();

    public VergilMirageEdgeSlash() {
        super("Vergil", "mirage_edge_slash");
        setPvPBased(true);
    }

    public static final Map<String, double[]> angles = new HashMap<>();
    private static final ParticleDisplay.Axis[] order = {ParticleDisplay.Axis.X, ParticleDisplay.Axis.Y, ParticleDisplay.Axis.Z};

    @EventHandler
    public void onTest(AsyncPlayerChatEvent event) {
        String message = event.getMessage().replace(" ", "").toLowerCase(Locale.ENGLISH);
        if (message.equals("reset")) {
            angles.clear();
            return;
        }
        if (message.startsWith("!")) {
            String[] split = message.substring(1).toUpperCase().split(",");

            try {
                order[0] = ParticleDisplay.Axis.valueOf(split[0]);
                order[1] = ParticleDisplay.Axis.valueOf(split[1]);
                order[2] = ParticleDisplay.Axis.valueOf(split[2]);

            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        } else {
            String[] split = message.split(",");

            try {
                String ns = split[0];
                double[] angles = VergilMirageEdgeSlash.angles.computeIfAbsent(ns, k -> new double[3]);
                angles[0] = Math.toRadians(Double.parseDouble(split[1]));
                angles[1] = Math.toRadians(Double.parseDouble(split[2]));
                angles[2] = Math.toRadians(Double.parseDouble(split[3]));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();

        double[] beforeFacing = VergilMirageEdgeSlash.angles.getOrDefault("beforefacing", new double[3]);
        double[] afterFacing = VergilMirageEdgeSlash.angles.getOrDefault("afterfacing", new double[3]);
        ParticleDisplay display = ParticleDisplay.of(Particle.FLAME)
                .withLocation(player.getEyeLocation()).rotate(beforeFacing[0], beforeFacing[1], beforeFacing[2]).face(player).rotate(afterFacing[0], afterFacing[1], afterFacing[2])
                .onSpawn(loc -> {
                    Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
                            if (EntityUtil.filterEntity(player, entity)) continue;
                            DamageManager.damage((LivingEntity) entity, player, 3);
                        }
                    });
                    return true;
                });

        if (angles.containsKey("add")) {
            double[] angles = VergilMirageEdgeSlash.angles.get("add");
            if (player.isSneaking()) {
                display.onCalculation(loc -> {
                    ParticleDisplay.Quaternion rot = ParticleDisplay.Quaternion.rotation(angles[0], display.getDirection());
                    Vector vec = ParticleDisplay.Quaternion.rotate(loc, rot);
                    loc.setX(vec.getX());
                    loc.setY(vec.getY());
                    loc.setZ(vec.getZ());
                });
            } else {
                display.rotations.stream()
                        .filter(x -> x.value.equals(ParticleDisplay.Axis.X.getVector()))
                        .findFirst().ifPresent(x -> x.key += angles[0]);
                display.rotations.stream()
                        .filter(x -> x.value.equals(ParticleDisplay.Axis.Y.getVector()))
                        .findFirst().ifPresent(x -> x.key += angles[1]);
                display.rotations.stream()
                        .filter(x -> x.value.equals(ParticleDisplay.Axis.Z.getVector()))
                        .findFirst().ifPresent(x -> x.key += angles[2]);
            }
        }

        XParticle.slash(SkillsPro.get(), 30, true, () -> 3.0, () -> 0.1, display);
    }
}
