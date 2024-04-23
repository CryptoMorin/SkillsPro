package org.skills.abilities;

import org.bukkit.Bukkit;
import org.skills.abilities.arbalist.*;
import org.skills.abilities.devourer.*;
import org.skills.abilities.eidolon.*;
import org.skills.abilities.firemage.*;
import org.skills.abilities.juggernaut.*;
import org.skills.abilities.mage.*;
import org.skills.abilities.priest.*;
import org.skills.abilities.swordsman.*;
import org.skills.abilities.vampire.*;
import org.skills.abilities.vergil.*;
import org.skills.main.SkillsPro;

import java.util.*;

public final class AbilityManager {
    private static final Map<String, Ability> ABILITIES = new HashMap<>();
    public static final Set<String> UNSUPPORTED_ABILITIES = new HashSet<>();
    private static final Set<UUID> COOLDOWN_EXCEMPT = new HashSet<>();

    public static Set<UUID> getCooldownExcempts() {
        return COOLDOWN_EXCEMPT;
    }

    /**
     * There's no need to clear the map when re-registering
     * abilities as their original name is static and will replace the previous one.
     */
    public static void registerAll() {
        Ability[] abilities = {
                new SwordsmanPassive(),
                new SwordsmanParry(),
                new SwordsmanPierce(),
                new SwordsmanDodge(),
                new SwordsmanThousandCuts(),
                new SwordsmanDash(),
                new SwordsmanAnnihilation(),

                new ArbalistPassive(),
                new ArbalistExecute(),
                new ArbalistDualArrows(),
                new ArbalistMinions(),
                new ArbalistFireCrossbow(),
                new ArbalistCrossExamination(),

                new DevourerPassive(),
                new DevourerGliders(),
                new DevourerCloak(),
                new DevourerBlink(),
                new DevourerHook(),
                new DevourerDisarm(),
                new DevourerConsume(),
                new DevourerLocate(),

                new MagePassive(),
                new MageReflect(),
                new MageExplosionSpell(),
                new MageHealSpell(),
                new MageEnergyFlux(),
                new MageNeptune(),
                new MageChronoprohiberis(),

                new FireMagePassive(),
                new FireMageAbsorbEnergy(),
                new FireMagePhoenixEssence(),
                new FireMageBlackFire(),
                new FireMageInferno(),
                new FireMageMeteorite(),

                new JuggernautPassive(),
                new JuggernautHeavyStrikes(),
                new JuggernautStoneSkin(),
                new JuggernautAegisProtection(),
                new JuggernautThrow(),
                new JuggernautChainSmash(),

                new VampirePassive(),
                new VampireBloodLust(),
                new VampireBloodWell(),
                new VampireBleed(),
                new VampireEternalDarkness(),
                new VampireBloodCircuit(),

                new EidolonPassive(),
                new EidolonPurify(),
                new EidolonSpiritFire(),
                new EidolonSpectre(),
                new EidolonDefile(),
                new EidolonShapeShifter(),
                new EidolonFangs(),
                new EidolonBlackhole(),

                new PriestPassive(),
                new PriestAsclepius(),
                new PriestKindlingOfLife(),
                new PriestMindPossession(),
                new PriestSealOfLife(),
                new PriestNaturesForce(),
                new PriestNaturesCall(),
                new PriestBarrier(),
                new PriestPurification(),

                new VergilPassive(),
                new VergilJudgementCut(),
                new VergilJudgementCutEnd(),
                new VergilRapidSlash(),
                new VergilVerticalSlash(),
                new VergilTrickAction(),
                new VergilMirageEdgeSlash(),
                new VergilDoppelganger(),
                new VergilPhantomBlade()
        };

        Arrays.stream(abilities)
                .filter(x -> !x.isSupported())
                .forEach(x -> UNSUPPORTED_ABILITIES.add(x.getName()));

        Arrays.stream(abilities)
                .filter(Ability::isSupported)
                .forEach(AbilityManager::register);
    }

    public static Map<String, Ability> getAbilities() {
        return ABILITIES;
    }

    public static void register(Ability ability) {
        ABILITIES.put(ability.getName(), ability);
        Bukkit.getPluginManager().registerEvents(ability, SkillsPro.get());
    }

    public static Ability getAbility(String abiltiy) {
        return ABILITIES.get(abiltiy);
    }
}
