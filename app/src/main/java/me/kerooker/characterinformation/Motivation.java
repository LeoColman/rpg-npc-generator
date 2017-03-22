package me.kerooker.characterinformation;


import android.content.Context;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import me.kerooker.enums.Priority;
import me.kerooker.textmanagers.TxtReader;

public class Motivation implements Information {

    private static final Priority motivationPriority = Priority.LOW;
    private static final Random random = new Random();
    private static List<String> motivations;
    private Context context;
    private String motivation;

    public Motivation(Context context) {
        this.context = context;
        loadMotivations();
        this.motivation = getRandomMotivation();
    }

    public Motivation(String motivation) {
        this.motivation = motivation;
    }


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
