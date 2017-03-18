package me.kerooker.characterinformation;

import android.content.Context;

import me.kerooker.rpgcharactergenerator.R;

public class Race implements Information {

    private String race;
    private String subrace;
    private Context c;

    public Race(Context c) {
        this.c = c;
    }

    private String generateRandomRaceAndSubrace() {
    return null;
    }


    public Race(Context c, String race, String subrace) {
        this.c = c;
        this.setRace(race);
        this.setSubrace(subrace);
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public String getInformation() {
        String race = "Race: " + getRace();
        String subrace = getSubrace();

        if (subrace != null && !subrace.isEmpty()) {
            return race + "\n" + subrace;
        }else {
            return race;
        }

    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getSubrace() {
        return subrace;
    }

    public void setSubrace(String subrace) {
        this.subrace = subrace;
    }
}
