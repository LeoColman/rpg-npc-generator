package me.kerooker.characterinformation;


import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import me.kerooker.enums.Priority;

public class Npc implements Serializable {


    private List<Information> information = new ArrayList<>();

    public Npc(Information... informations) {
        Collections.addAll(information, informations);

        sortInformation();
    }

    public Npc(List<Information> info) {

        this.information = info;
        sortInformation();
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

    public void addInformation(Information inf) {
        information.add(inf);
    }

    public Information getTopInformation() {
        Information topInformation = null;
        for (Information i : information) {
            if (i.getPriority().equals(Priority.TOP)) {
                topInformation = i;
                break;
            }
        }
        if (topInformation == null) throw new RuntimeException("No top Priority!");
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
                return -(o1P.compareTo(o2P));
            }
        };

    }

    public static class Builder {
        private Context context;
        private Npc npcInstance;

        public Builder(Context c) {
            this.context = c;
            npcInstance = new Npc();
        }

        public Builder random() {
            npcInstance = generateRandomNpc(context);
            return this;
        }

        public Builder withInformation(Information inf) {
            Iterator<Information> infoIterator = npcInstance.npcInformation().iterator();
            boolean shouldAdd = false;
            while (infoIterator.hasNext()) {
                Information next = infoIterator.next();
                Class actualClass = next.getClass();
                Class original = inf.getClass();
                if (original.equals(actualClass)) {
                    //Classes are the same, remove from iterator
                    infoIterator.remove();
                    //Add information to list
                    shouldAdd = true;
                }

            }
            if (shouldAdd) {
                npcInstance.npcInformation().add(inf);
            }
            return this;
        }

        public Npc getNpcInstance() {
            npcInstance.sortInformation();
            return npcInstance;
        }
    }
}
