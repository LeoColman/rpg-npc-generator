package me.kerooker.characterinformation;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import me.kerooker.enums.Priority;
import me.kerooker.util.Randomizer;

public class Language implements Information, Serializable {
    private static final Priority LANGUAGE_PRIORITY = Priority.LOW;
    private static final Random RANDOM = Randomizer.getRandom();
    private List<me.kerooker.enums.Language> spoken = new ArrayList<>();
    private Race race;

    public Language(me.kerooker.enums.Language... language) {
        List<me.kerooker.enums.Language> languages = Arrays.asList(language);
        spoken.addAll(languages);
    }

    public Language(Race race) {
        this.race = race;
        addRaceAndCommonLanguages();
        generateRandomLanguages();
    }

    private void addRaceAndCommonLanguages() {
        verifyAndAddCommon();
        spoken.addAll(race.getRacialLanguages());
    }

    private void generateRandomLanguages() {
        double currentChance = 0.7;
        List<me.kerooker.enums.Language> languages = me.kerooker.enums.Language.languagesWithoutCommon();
        for (int i = 0; i < me.kerooker.enums.Language.amountOfLanguages(); i++) {
            double generated = RANDOM.nextDouble();
            if (generated < currentChance) {
                me.kerooker.enums.Language randomLanguage = getRandomFromList(languages);
                if (!addLanguage(randomLanguage))continue;
            }
            currentChance = currentChance / 2.5;
        }
    }

    /**
     * Returns true if and only if the language could be added
     */
    private boolean addLanguage(me.kerooker.enums.Language l) {
        if (!spoken.contains(l)) {
            spoken.add(l);
            return true;
        }
        return false;
    }

    private me.kerooker.enums.Language getRandomFromList(List<me.kerooker.enums.Language> list) {
        int max = list.size();
        int randomico = RANDOM.nextInt(max);
        return list.get(randomico);
    }

    private void verifyAndAddCommon() {
        double randomDoubleForCommon = RANDOM.nextDouble();
        if (randomDoubleForCommon < 0.95) spoken.add(me.kerooker.enums.Language.COMMON);
    }


    @Override
    public Priority getPriority() {
        return LANGUAGE_PRIORITY;
    }

    @Override
    public String getInformation() {
        String builder = "Languages: ";
        if (spoken.isEmpty()) {
            builder += "(none)";
            return builder;
        }
        for (me.kerooker.enums.Language lang : spoken) {
            builder += lang.toString() + ", ";
        }
        return builder.substring(0, builder.length() - 2);    //Removing last ", "
    }
}
