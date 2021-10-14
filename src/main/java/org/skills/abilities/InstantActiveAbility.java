package org.skills.abilities;

public abstract class InstantActiveAbility extends ActiveAbility {
    public InstantActiveAbility(String skill, String name) {
        super(skill, name);
    }

    public abstract void useSkill(AbilityContext context);
}
