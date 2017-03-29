package me.kerooker.enums;

import java.util.Arrays;
import java.util.List;

public enum Sexuality {
    HETEROSEXUAL(80.0), HOMOSEXUAL(10.0), BISEXUAL(9.0), ASEXUAL(1.0);

    private double chanceToHappen;

    Sexuality(double chanceToHappen) {
        this.chanceToHappen = chanceToHappen;
    }

    public static Sexuality getRandomSexuality() {
        List<Sexuality> sexualities = Arrays.asList(Sexuality.values());
        return chooseOnWeight(sexualities);

    }

    private static Sexuality chooseOnWeight(List<Sexuality> sexualities) {
        double completeWeight = 0.0;
        for (Sexuality item : sexualities)
            completeWeight += item.getChanceToHappen();
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (Sexuality item : sexualities) {
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
