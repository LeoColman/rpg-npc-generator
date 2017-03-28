package me.kerooker.characterinformation;


import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.kerooker.enums.Priority;

public class Npc implements Serializable {


    List<Information> information = new ArrayList<Information>();

    public Npc(Information ... informations) {
        for (Information inf : informations) {
            information.add(inf);
        }

        sortInformation();
    }

    public Npc(List<Information> info) {
        this.information = info;
    }

    /**
     * Generates a Random NPC with the following information:
     * Name, race, gender, age, sexuality, profession, motivation, personality traits and language
     *
     * @param context The context to get information from
     * @return A new random npc
     */
    public static Npc generateRandomNpc(Context context) {
        Age age = new Age();
        Gender gender = new Gender();
        Race race = new Race(context);
        Language language = new Language(race);
        Name name = new Name(context);
        Motivation motivation = new Motivation(context);
        PersonalityTraits traits = new PersonalityTraits(context);
        Profession profession = new Profession(age, context);
        Sexuality sexuality = new Sexuality();
        return new Npc(name, race, gender, age, profession, sexuality, motivation, traits, language);
    }

    public Information popTopInformation() {
        Information topInformation = null;
        for (Information i : information) {
            if (i.getPriority().equals(Priority.TOP)) {
                topInformation = i;
            }
        }
        if (topInformation == null) throw new RuntimeException("No top Priority!");
        information.remove(topInformation);
        return topInformation;

    }

    public List<Information> npcInformation() {
        return information;
    }

    private void sortInformation() {
        Collections.sort(information, getInformationComparator());
    }

    public String getCharacter() {

        String informationBuilder = "";

        for (Information inf : information) {
           informationBuilder += inf.getInformation() + "\n";
        }
        return informationBuilder;
    }

    private Comparator<Information> getInformationComparator() {
        return new Comparator<Information>() {
            @Override
            public int compare(Information o1, Information o2) {
                Priority o1P = o1.getPriority();
                Priority o2P = o2.getPriority();
                return - (o1P.compareTo(o2P));
            }
        };

    }
}
