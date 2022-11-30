package org.skills.types;

public enum SkillScaling {
    MAX_LEVEL,
    MAX_HEALTH,
    MAX_ENERGY,
    REQUIRED_LEVEL,
    COST,
    DAMAGE_CAP,
    ENERGY_REGEN;

    @Override
    public String toString() {
        return "SCALING[" + this.name() + ']';
    }
}
