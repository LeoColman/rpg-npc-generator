package me.kerooker.enums;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.List;

public enum Age {

    CHILD(5.0), TEENAGER(10.0), YOUNG_ADULT(35.0), ADULT(35.0), OLD(10.0), VERY_OLD(5.0);

    private double chanceToHappen;

    Age(double chanceToHappen) {
        this.chanceToHappen = chanceToHappen;
    }

    public static Age getRandomAge() {
        List<Age> ages = Arrays.asList(Age.values());
        return chooseOnWeight(ages);
    }


    private static Age chooseOnWeight(List<Age> ages) {
        double completeWeight = 0.0;
        for (Age item : ages)
            completeWeight += item.getChanceToHappen();
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (Age item : ages) {
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
        String name = name();
        name = name.replaceAll("_", " ");
        return WordUtils.capitalizeFully(name);
    }
}
