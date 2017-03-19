package me.kerooker.characterinformation;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.kerooker.yaml.YamlReader;

public class Race implements Information {

    private static Random random = new Random();

    private String race;
    private String subrace;
    private Context c;

    public Race(Context c) {
        this.c = c;
        generateRandomRaceAndSubrace();
    }

    public Race(Context c, String race, String subrace) {
        this.c = c;
        this.setRace(race);
        this.setSubrace(subrace);
    }

    @SuppressWarnings("unchecked")
    private void generateRandomRaceAndSubrace() {
        Map<String, Object> races;
        try {
            races = YamlReader.loadYamlFromAssets(c, "racesAndSubraces");
        } catch (IOException e) {
            //Shouldn't ever happen
            e.printStackTrace();
            return;
        }
        Set<String> raceList = races.keySet();
        String randomRace = getRandomString(raceList);

        Map<String, Object> raceInfo = (Map<String, Object>) races.get(randomRace);

        String randomRaceName = (String) raceInfo.get("name");
        setRace(randomRaceName);                                    //Setting race name

        List<String> possibleSubraces = (List<String>) raceInfo.get("subraces");

        String randomSubraceName = getRandomString(possibleSubraces);
        setSubrace(randomSubraceName);


    }

    private String getRandomString(Collection<String> strings) {
        if (strings.size() == 0)return "";
        int randomico = random.nextInt(strings.size());
        List<String> list = new ArrayList<>();
        list.addAll(strings);
        return list.get(randomico);
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
            return race + "\n" + "Subrace: " + subrace;
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
