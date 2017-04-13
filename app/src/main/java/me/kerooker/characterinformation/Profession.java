package me.kerooker.characterinformation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

import me.kerooker.enums.Age;
import me.kerooker.enums.Priority;
import me.kerooker.textmanagers.TxtReader;
import me.kerooker.util.Randomizer;


public class Profession implements Information, Serializable {

    private static final Priority professionPriority = Priority.NORMAL;
    private static final Random random = Randomizer.getRandom();
    private static List<String> professions;
    private static List<String> childProfessions;
    private me.kerooker.enums.Age age;
    private String profession;
    private transient Context context;

    private Profession(@Nullable Age age, Context context) {
        this.age = age;
        this.context = context;
        setupProfessions();
        generateRandomProfession();
    }

// --Commented out by Inspection START (12/04/2017 18:41):
//    public Profession(String profession, Context context) {
//        this.profession = profession;
//        this.context = context;
//        setupProfessions();
//    }
// --Commented out by Inspection STOP (12/04/2017 18:41)

// --Commented out by Inspection START (12/04/2017 18:41):
//    public Profession(Context context) {
//        this.age = Age.getRandomAge();
//        this.context = context;
//        setupProfessions();
//        generateRandomProfession();
//    }
// --Commented out by Inspection STOP (12/04/2017 18:41)

    public Profession(@NonNull me.kerooker.characterinformation.Age age, Context context) {
        this(age.getAge(), context);
    }

// --Commented out by Inspection START (12/04/2017 18:41):
//    public Profession() {
//
//    }
// --Commented out by Inspection STOP (12/04/2017 18:41)

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
        if (age == null)age = Age.getRandomAge();
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
        return "Profession: " + WordUtils.capitalizeFully(profession);
    }
}
