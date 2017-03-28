package me.kerooker.characterinformation;

import android.content.Context;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.kerooker.enums.Language;
import me.kerooker.enums.Priority;
import me.kerooker.textmanagers.YamlReader;

public class Race implements Information, Serializable {

    private static final Priority racePriority = Priority.HIGH;
    private static Random random = new Random();
    private static Map<String, Object> races;
    private String race;
    private String subrace;
    private Context c;


    public Race(Context c) {
        this.c = c;
        setYml();
        generateRandomRaceAndSubrace();

    }

    public Race(Context c, String race, String subrace) {
        this.c = c;
        this.setRace(race);
        this.setSubrace(subrace);

    }

    @SuppressWarnings("unchecked")
    public List<me.kerooker.enums.Language> getRacialLanguages() {
        List<Language> languages = new ArrayList<>();

        Map<String, Object> raceInfo = (Map<String, Object>) races.get(getLowerCaseRace());
        List<String> racialLanguages = (List<String>) raceInfo.get("racialLanguages");

        double currentChance = 0.95;
        for (String language : racialLanguages) {
            double generated = random.nextDouble();
            if (generated < currentChance) {
                languages.add(Language.valueOf(language.toUpperCase()));
            }
            currentChance = currentChance / 2;
        }

        return languages;
    }

    private String getLowerCaseRace() {
        return race.toLowerCase().replaceAll("-","");
    }

    @SuppressWarnings("unchecked")
    private void setYml() {
        try {
            if (races == null)races = YamlReader.loadYamlFromAssets(c, "racesAndSubraces");
        } catch (IOException e) {
            //Shouldn't ever happen
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void generateRandomRaceAndSubrace() {

        Set<String> raceList = races.keySet();
        String randomRace = getRandomString(raceList);

        Map<String, Object> raceInfo = (Map<String, Object>) races.get(randomRace);

        String randomRaceName = (String) raceInfo.get("name");
        setRace(randomRaceName);                                    //Setting race name

        List<String> possibleSubraces = (List<String>) raceInfo.get("subraces");

        String randomSubraceName = getRandomString(possibleSubraces);
        setSubrace(randomSubraceName);                              //Setting subrace name


    }

    private String getRandomString(Collection<String> strings) {
        if (strings.size() == 0) return "";
        int randomico = random.nextInt(strings.size());
        List<String> list = new ArrayList<>(strings);
        return list.get(randomico);
    }


    @Override
    public Priority getPriority() {
        return racePriority;
    }

    @Override
    public String getInformation() {
        String race = "Race: " + getRace();
        String subrace = getSubrace();

        if (subrace != null && !subrace.isEmpty()) {
            return race + "\n" + "Subrace: " + subrace;
        } else {
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
