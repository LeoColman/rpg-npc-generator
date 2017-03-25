package me.kerooker.characterinformation;

import android.content.Context;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import me.kerooker.enums.Priority;
import me.kerooker.textmanagers.TxtReader;

public class PersonalityTraits implements Information, Serializable {

    private static final Priority personalityPriority = Priority.LOW;
    private static final int minTraits = 2;
    private static final int maxTraits = 5;
    private static final Random random = new Random();
    private static List<String> traitsList;
    private Context context;
    private List<String> traits;

    public PersonalityTraits(Context context) {
        this.context = context;
        loadTraits();
        generateRandomTraits();
    }

    public PersonalityTraits(String... traits) {
        this.traits = Arrays.asList(traits);
    }

    private void generateRandomTraits() {
        this.traits = getRandomTraits();
    }

    private void loadTraits() {
        if (traitsList == null) try {
            traitsList = TxtReader.readTextFile(context, "traits");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getRandomTraits() {
        int randomAmount = randomIntFromRange(minTraits, maxTraits);
        List<String> traits = new ArrayList<>();
        for (int i = 0; i < randomAmount; i++) {
            String trait = getRandomTrait();
            if (traits.contains(trait)) {
                i--;
                continue;
            }
            traits.add(getRandomTrait());
        }
        return traits;
    }

    private String getRandomTrait() {
        return getRandomFromList(traitsList);
    }

    private String getRandomFromList(List<String> list) {
        int max = list.size();
        int rand = random.nextInt(max);
        return list.get(rand);
    }

    private int randomIntFromRange(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }


    @Override
    public Priority getPriority() {
        return personalityPriority;
    }

    @Override
    public String getInformation() {
        String builder = "Personality Traits: ";
        for (String s : traits) {
            builder = builder + s + ", ";
        }
        builder = builder.substring(0, builder.length()-2);     //Removing the last two spaces
        return builder;
    }
}
