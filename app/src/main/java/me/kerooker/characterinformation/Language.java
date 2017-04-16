package me.kerooker.characterinformation;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.kerooker.enums.Priority;
import me.kerooker.util.Randomizer;

public class Language implements Information, Serializable {
    private static final Priority LANGUAGE_PRIORITY = Priority.LOW;
    private static final Random RANDOM = Randomizer.getRandom();
    private List<me.kerooker.enums.Language> spoken = new ArrayList<>();
    private Race race;

// --Commented out by Inspection START (12/04/2017 18:40):
//    public Language(me.kerooker.enums.Language... language) {
//        List<me.kerooker.enums.Language> languages = Arrays.asList(language);
//        spoken.addAll(languages);
//    }
// --Commented out by Inspection STOP (12/04/2017 18:40)

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
        double currentChance = 0.4;
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
        if (randomDoubleForCommon < 0.995) spoken.add(me.kerooker.enums.Language.COMMON);
    }


    @Override
    public Priority getPriority() {
        return LANGUAGE_PRIORITY;
    }

    @Override
    public String getInformation() {
        StringBuilder builder = new StringBuilder("Languages: ");
        if (spoken.isEmpty()) {
            builder.append("(none)");
            return builder.toString();
        }
        for (me.kerooker.enums.Language lang : spoken) {
            builder.append(lang.toString()).append(", ");
        }
        return builder.substring(0, builder.length() - 2);    //Removing last ", "
    }
}
