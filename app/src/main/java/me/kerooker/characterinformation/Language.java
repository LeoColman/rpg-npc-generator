package me.kerooker.characterinformation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import me.kerooker.enums.Priority;

public class Language implements Information {
    private static final Priority languagePriority = Priority.LOW;
    private static final Random random = new Random();
    private List<me.kerooker.enums.Language> spoken = new ArrayList<>();

    public Language(me.kerooker.enums.Language... language) {
        List<me.kerooker.enums.Language> languages = Arrays.asList(language);
        spoken.addAll(languages);
    }

    public Language() {
        generateRandomLanguages();
    }

    private void generateRandomLanguages() {
        verifyAndAddCommon();
        double currentChance = 0.7;
        List<me.kerooker.enums.Language> languages = me.kerooker.enums.Language.languagesWithoutCommon();
        for (int i = 0; i < me.kerooker.enums.Language.amountOfLanguages(); i++) {
            double generated = random.nextDouble();
            if (generated < currentChance) {
                me.kerooker.enums.Language randomLanguage = getRandomFromList(languages);
                if (spoken.contains(randomLanguage)) continue;
                spoken.add(randomLanguage);
            }
            currentChance = currentChance / 2.5;
        }
    }

    private me.kerooker.enums.Language getRandomFromList(List<me.kerooker.enums.Language> list) {
        int max = list.size();
        int randomico = random.nextInt(max);
        return list.get(randomico);
    }

    private void verifyAndAddCommon() {
        double randomDoubleForCommon = random.nextDouble();
        if (randomDoubleForCommon < 0.95) spoken.add(me.kerooker.enums.Language.COMMON);
    }


    @Override
    public Priority getPriority() {
        return languagePriority;
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
