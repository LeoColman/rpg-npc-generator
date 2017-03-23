package me.kerooker.enums;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum Language {

    ABYSSAL, AQUAN, AURAN, CELESTIAL, COMMON, DRACONIC, DRUIDIC, DWARVEN, ELVEN, GIANT, GNOME, GOBLIN, GNOLL, HALFLING, IGNAN, INFERNAL, ORC, SYLVAN, TERRAN, UNDERCOMMON;

    private static final Random r = new Random();


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
