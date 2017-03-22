package me.kerooker.characterinformation;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.kerooker.enums.Priority;

public class Npc {

    List<Information> information = new ArrayList<Information>();

    public Npc(Information ... informations) {
        for (Information inf : informations) {
            information.add(inf);
        }
    }

    public Npc(List<Information> info) {
        this.information = info;
    }

    public String getCharacter() {
        Collections.sort(information, getComparator());
        String informationBuilder = "";

        for (Information inf : information) {
           informationBuilder += inf.getInformation() + "\n";
        }
        return informationBuilder;
    }

    private Comparator<Information> getComparator() {
        Comparator<Information> comparator = new Comparator<Information>() {
            @Override
            public int compare(Information o1, Information o2) {
                Priority o1P = o1.getPriority();
                Priority o2P = o2.getPriority();
                return - (o1P.compareTo(o2P));
            }
        };

        return comparator;
    }
}
