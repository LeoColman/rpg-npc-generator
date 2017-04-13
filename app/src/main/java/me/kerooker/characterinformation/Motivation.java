package me.kerooker.characterinformation;


import android.content.Context;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

import me.kerooker.enums.Priority;
import me.kerooker.textmanagers.TxtReader;
import me.kerooker.util.Randomizer;

public class Motivation implements Information, Serializable {

    private static final Priority motivationPriority = Priority.LOW;
    private static final Random random = Randomizer.getRandom();
    private static List<String> motivations;
    private transient Context context;
    private String motivation;

    public Motivation(Context context) {
        this.context = context;
        loadMotivations();
        this.motivation = getRandomMotivation();
    }

// --Commented out by Inspection START (12/04/2017 18:41):
//    public Motivation(String motivation) {
//        this.motivation = motivation;
//    }
// --Commented out by Inspection STOP (12/04/2017 18:41)


    private void loadMotivations() {
        if (motivations == null) try {
            motivations = TxtReader.readTextFile(context, "motivations");
        } catch (IOException e) {
            //Shouldn't ever happen
            e.printStackTrace();
        }
    }

    private String getRandomMotivation() {
        return getRandomFromList(motivations);
    }

    private String getRandomFromList(List<String> list) {
        int max = list.size();
        int randomico = random.nextInt(max);
        return list.get(randomico);
    }


    @Override
    public Priority getPriority() {
        return motivationPriority;
    }

    @Override
    public String getInformation() {
        return "Motivated by: " + getMotivation();
    }

    private String getMotivation() {
        return motivation;
    }
}
