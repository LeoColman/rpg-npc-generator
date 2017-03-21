package me.kerooker.characterinformation;

import android.content.Context;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import me.kerooker.enums.Age;
import me.kerooker.enums.Priority;
import me.kerooker.textmanagers.TxtReader;


public class Profession implements Information {

    private static final Priority professionPriority = Priority.NORMAL;
    private static List<String> professions;
    private static List<String> childProfessions;
    private static Random random = new Random();
    private me.kerooker.enums.Age age;
    private String profession;
    private Context context;

    public Profession(Age age, Context context) {
        this.age = age;
        this.context = context;
        setupProfessions();
        generateRandomProfession();
    }

    public Profession(String profession, Context context) {
        this.profession = profession;
        this.context = context;
        setupProfessions();
    }

    public Profession(Context context) {
        this.age = Age.getRandomAge();
        this.context = context;
        setupProfessions();
        generateRandomProfession();
    }

    private void setupProfessions() {
        try {
            if (professions == null) professions = TxtReader.readTextFile(context, "professions");
            if (childProfessions == null)
                childProfessions = TxtReader.readTextFile(context, "childprofessions.txt");
        } catch (IOException e) {
            //Shouldn't happen
            e.printStackTrace();
        }
    }


    private void generateRandomProfession() {
        if (age.isChild()) {
            profession = getRandomChildProfession();
        } else {
            profession = getRandomProfession();
        }
    }

    private String getRandomProfession() {
        return getRandomFromList(professions);
    }

    private String getRandomChildProfession() {
        return getRandomFromList(childProfessions);
    }

    private String getRandomFromList(List<String> list) {
        int max = list.size();
        int randomico = random.nextInt(max);
        return list.get(randomico);
    }


    @Override
    public Priority getPriority() {
        return professionPriority;
    }

    @Override
    public String getInformation() {
        return "Profession: " + profession;
    }
}
