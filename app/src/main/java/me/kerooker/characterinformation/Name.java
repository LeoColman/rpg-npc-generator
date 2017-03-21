package me.kerooker.characterinformation;

import android.content.Context;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import me.kerooker.textmanagers.TxtReader;

public class Name implements Information {

    private static final double CHANCE_TO_HAVE_SURNAME = 0.97;
    private static final double CHANCE_TO_HAVE_A_NICKNAME = 0.15;
    private static Priority namePriority = Priority.HIGHEST;
    private static List<String> names;
    private static Random random = new Random();
    private String name;
    private Context context;

    public Name(Context context) {
        this.context = context;
        loadNames();
        generateRandomName();
    }

    private void loadNames() {
        try {
            names = TxtReader.readTextFile(context, "nameList");
        } catch (IOException e) {
            //Shouldn't ever happen
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void generateRandomName() {
        String name1 = getRandomName();
        String name2 = getRandomName();
        boolean shouldSurname = shouldSurname();
        boolean shouldNickname = shouldNickname();

        setName(name1);
        if (shouldSurname)setName(getName() + " " + name2);
        if (shouldNickname) {
            //TODO
        }
    }

    private String getRandomName() {
        int maxSize = names.size();
        int randomNickIndex = random.nextInt(maxSize);
        return names.get(randomNickIndex);

    }

    private boolean shouldSurname() {
        double randomico = random.nextDouble();
        return randomico <= CHANCE_TO_HAVE_SURNAME;

    }

    private boolean shouldNickname() {
        double randomico = random.nextDouble();
        return randomico <= CHANCE_TO_HAVE_A_NICKNAME;
    }


    @Override
    public Priority getPriority() {
        return namePriority;
    }

    @Override
    public String getInformation() {
        return getName();
    }
}
