package org.skills.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;

public final class FireworkUtil {
    public static FireworkMeta generateFireworkMeta(FireworkMeta meta, int colorCount) {
        List<Color> colors = new ArrayList<>(colorCount);
        for (int j = 0; j < colorCount; j++) {
            colors.add(Color.fromRGB(
                    MathUtils.randInt(0, 255),
                    MathUtils.randInt(0, 255),
                    MathUtils.randInt(0, 255)));
        }

        FireworkEffect effect =
                FireworkEffect.builder().withColor(colors)
                        .with(FireworkEffect.Type.values()[MathUtils.randInt(0, FireworkEffect.Type.values().length - 1)])
                        .withTrail().build();

        meta.addEffect(effect);
        return meta;
    }
}
