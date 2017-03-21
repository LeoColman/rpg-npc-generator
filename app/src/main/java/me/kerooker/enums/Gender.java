package me.kerooker.enums;

import java.util.Arrays;
import java.util.List;

public enum Gender {
    MALE(49.30), FEMALE(49.30), AGENDER(0.70), BIGENDER(0.70);

    private double chanceToHappen;

    Gender(double chanceToHappen) {
        this.chanceToHappen = chanceToHappen;
    }

    public static Gender getRandomGender() {
        List<Gender> genders = Arrays.asList(Gender.values());
        return chooseOnWeight(genders);

    }

    private static Gender chooseOnWeight(List<Gender> genders) {
        double completeWeight = 0.0;
        for (Gender item : genders)
            completeWeight += item.getChanceToHappen();
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (Gender item : genders) {
            countWeight += item.getChanceToHappen();
            if (countWeight >= r)
                return item;
        }
        throw new RuntimeException("Should never be shown.");
    }

    private double getChanceToHappen() {
        return chanceToHappen;
    }

    @Override
    public String toString() {
        String name = this.name();
        name = name.toLowerCase();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }
}
