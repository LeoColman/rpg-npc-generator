package me.kerooker.characterinformation;

import android.content.Context;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import me.kerooker.enums.Priority;
import me.kerooker.textmanagers.TxtReader;

public class Name implements Information {

    private static final double CHANCE_TO_HAVE_SURNAME = 0.97;
    private static final double CHANCE_TO_HAVE_A_NICKNAME = 0.15;
    private static Priority namePriority = Priority.HIGHEST;
    private static List<String> names;
    private static List<String> nicknames;
    private static Random random = new Random();
    private String name;
    private Context context;

    public Name(Context context) {
        this.context = context;
        loadNamesAndNicknames();
        generateRandomName();
    }

    private void loadNamesAndNicknames() {
        try {
            if (names == null) names = TxtReader.readTextFile(context, "nameList");
            if (nicknames == null) nicknames = TxtReader.readTextFile(context, "nicknames");
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

        setName(name1);
        if (shouldSurname()) setName(getName() + " " + name2);
        if (shouldNickname()) {
            setName(getName() + ", the " + getRandomNickname());
        }
    }

    private String toUpperCase(String s) {
        return (s.substring(0,1).toUpperCase() + s.substring(1));
    }

    private String getRandomNickname() {
        int maxSize = nicknames.size();
        int randomNickIndex = random.nextInt(maxSize);
        return toUpperCase(nicknames.get(randomNickIndex));
    }

    private String getRandomName() {
        int maxSize = names.size();
        int randomNameIndex = random.nextInt(maxSize);
        return names.get(randomNameIndex);

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
