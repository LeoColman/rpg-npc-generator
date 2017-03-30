package me.kerooker.enums;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Language {

    AARAKOCRA, ABYSSAL, AQUAN, AURAN, CELESTIAL, COMMON, DRACONIC, DRUIDIC, DWARVEN, ELVEN, GIANT, GNOME, GOBLIN, GNOLL, HALFLING, IGNAN, INFERNAL, ORC, PRIMORDIAL, SYLVAN, TERRAN, UNDERCOMMON;

    public static List<Language> languagesWithoutCommon() {
        List<Language> languages = new ArrayList<>(Arrays.asList(values()));
        languages.remove(COMMON);
        return languages;
    }


    public static int amountOfLanguages() {
        return values().length;
    }


    @Override
    public String toString() {
        return WordUtils.capitalizeFully(name());
    }
}
