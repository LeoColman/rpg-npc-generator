package me.kerooker.characterinformation;

import android.content.Context;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.kerooker.enums.Language;
import me.kerooker.enums.Priority;
import me.kerooker.textmanagers.YamlReader;
import me.kerooker.util.Randomizer;

public class Race implements Information, Serializable {

    private static final Priority racePriority = Priority.HIGH;
    private static Random random = Randomizer.getRandom();
    private static HashMap<String, Object> races;
    private transient Context context;
    private String race;
    private String subrace;


    public Race(Context c) {
        this.context = c;
        setYml(c);
        generateRandomRaceAndSubrace();

    }

// --Commented out by Inspection START (12/04/2017 18:41):
//    public Race(String race, String subrace) {
//        this.setRace(race);
//        this.setSubrace(subrace);
//
//    }
// --Commented out by Inspection STOP (12/04/2017 18:41)

    public Race(String race) {
        this.setRace(race);
        this.generateRandomSubrace(race);
    }

    @SuppressWarnings("unchecked")
    private static void setYml(Context c) {
        try {
            if (races == null) races = YamlReader.loadYamlFromAssets(c, "racesAndSubraces");
        } catch (IOException e) {
            //Shouldn't ever happen
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getSubraceList(String race, Context c) {
        setYml(c);
        String raceKey = "";
        for (String s : races.keySet()) {
            Map<String, Object> raceInfo = (Map<String, Object>) races.get(s);
            String raceName = (String) raceInfo.get("name");
            if (raceName.equalsIgnoreCase(race)) {
                raceKey = s;
            }
        }


        Map<String, Object> raceInfo = (Map<String, Object>) races.get(raceKey);
        return (List<String>) raceInfo.get("subraces");
    }

    @SuppressWarnings("unchecked")
    public static List<String> getRaceList(Context context) {
        setYml(context);
        Set<String> racesSet = races.keySet();

        List<String> racesList = new ArrayList<>();

        for (String race : racesSet) {
            Map<String, Object> raceInfo = (Map<String, Object>) races.get(race);

            String raceName = (String) raceInfo.get("name");
            racesList.add(raceName);
        }
        return racesList;
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
    private void generateRandomRaceAndSubrace() {

        List<String> raceList = getRaceList();
        String randomRace = getRandomString(raceList);

        Map<String, Object> raceInfo = (Map<String, Object>) races.get(randomRace);

        String randomRaceName = (String) raceInfo.get("name");
        setRace(randomRaceName);                                    //Setting race name

        List<String> possibleSubraces = (List<String>) raceInfo.get("subraces");

        String randomSubraceName = getRandomString(possibleSubraces);
        setSubrace(randomSubraceName);                              //Setting subrace name

    }

    private void generateRandomSubrace(String raceName) {
        subrace = getRandomString(getSubraceList(raceName, context ));
    }

    private List<String> getRaceList() {
        Set<String> racesSet = races.keySet();
        return new ArrayList<>(racesSet);
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

    private String getRace() {
        return race;
    }

    private void setRace(String race) {
        this.race = race;
    }

    private String getSubrace() {
        return subrace;
    }

    public void setSubrace(String subrace) {
        this.subrace = subrace;
    }
}
