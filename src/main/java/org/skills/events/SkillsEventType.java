package org.skills.events;

import org.skills.main.locale.SkillsLang;
import org.skills.utils.StringUtils;

import java.util.Locale;

public enum SkillsEventType {
    XP, SOUL;

    public static SkillsEventType fromString(String str) {
        str = StringUtils.deleteWhitespace(str.toLowerCase(Locale.ENGLISH));
        if (str.equals("exp") || str.equals("xp")) return SkillsEventType.XP;
        if (str.equals("soul") || str.equals("souls")) return SkillsEventType.SOUL;
        return null;
    }

    @Override
    public String toString() {
        return this == SOUL ? SkillsLang.EVENTS_NAME_SOULS.parse() : SkillsLang.EVENTS_NAME_XP.parse();
    }
}