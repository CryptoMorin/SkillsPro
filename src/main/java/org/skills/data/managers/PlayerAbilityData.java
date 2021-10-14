package org.skills.data.managers;

import org.skills.abilities.KeyBinding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerAbilityData {
    private int level;
    @Nullable private KeyBinding[] keyBinding;
    private boolean disabled;

    public PlayerAbilityData() {
        this(0);
    }

    public PlayerAbilityData(int level) {
        this(level, null);
    }

    public PlayerAbilityData(int level, @Nullable KeyBinding[] keyBinding) {
        this.level = level;
        this.keyBinding = keyBinding;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void addLevel(int level) {
        this.level += level;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public KeyBinding[] getKeyBinding() {
        return keyBinding;
    }

    public void setKeyBinding(@Nullable KeyBinding[] keyBinding) {
        this.keyBinding = keyBinding;
    }

    public void setKeyBinding(@Nonnull String binding) {
        this.keyBinding = KeyBinding.parseBinding(binding);
    }
}
