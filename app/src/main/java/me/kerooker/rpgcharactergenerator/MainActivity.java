package me.kerooker.rpgcharactergenerator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import me.kerooker.advertiser.Advertiser;
import me.kerooker.characterinformation.Npc;
import me.kerooker.characterinformation.Race;
import me.kerooker.enums.Gender;
import me.kerooker.generatednpcs.GeneratedNpcsActivity;

public class MainActivity extends AppCompatActivity implements Serializable {

    public static final String NPCS_LIST_INTENT_NAME = "npcslist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start();
        setEventHandlers();
        setSpinners();
    }

    private void start() {
        proccessAdvertisement(this);
    }

    private void setSpinners() {
        setGenderSpinner();
        setRaceSpinner();

    }

    private void setRaceSpinner() {
        ArrayList<String> racesChoice = new ArrayList<>();
        racesChoice.add("Random");
        racesChoice.addAll(Race.getRaceList(this));

        Spinner spinner = (Spinner) findViewById(R.id.race_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, racesChoice);
        spinner.setAdapter(adapter);
    }

    private void setGenderSpinner() {
        ArrayList<String> genderChoices = new ArrayList<>();
        Gender[] possibleGenders = Gender.values();
        genderChoices.add("Random");
        for (Gender g : possibleGenders) {
            genderChoices.add(g.toString());
        }
        Spinner spinner = (Spinner) findViewById(R.id.gender_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genderChoices);
        spinner.setAdapter(adapter);

    }

    private void setEventHandlers() {
        setDiscreeteBarEvent();
        setGenerateButtonHandler();
        setAdvancedTextHandler();
        setOnSpinSelectForRace();
    }

    private void setOnSpinSelectForRace() {
        Spinner race = (Spinner) findViewById(R.id.race_spinner);
        race.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getSelectedItem();
                selected = selected.toLowerCase();
                if (selected.contains("random")) {
                    //Disable subrace
                    disableSubraceSelector();
                } else {
                    //Enable subrace
                    enableSubraceSelector(selected);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //DoNothing
            }
        });
    }

    private void disableSubraceSelector() {
        View label = findViewById(R.id.sub_race_text_field);
        View spinner = findViewById(R.id.subrace_spinner);
        label.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
    }

    private void enableSubraceSelector(String selected) {
        View label = findViewById(R.id.sub_race_text_field);
        Spinner spinner = (Spinner) findViewById(R.id.subrace_spinner);
        label.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);

        List<String> subraces = new ArrayList<>();
        subraces.add("Random");
        List<String> subracesFromClass = Race.getSubraceList(selected, this);
        if (subracesFromClass.isEmpty()) {
            disableSubraceSelector();
            return;
        }
        subraces.addAll(subracesFromClass);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subraces);
        spinner.setAdapter(adapter);
    }

    private void setAdvancedTextHandler() {
        final View constraint = findViewById(R.id.advancedContent);
        final TextView text = (TextView) findViewById(R.id.showAdvanced);

        text.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String currentText = text.getText().toString().toLowerCase();
                if (isShowText(currentText)) {
                    //Show
                    Log.d("Eita", "E");
                    constraint.setVisibility(View.VISIBLE);
                    text.setText(getHideText());
                } else {
                    //Hide
                    Log.d("Eita2", "EE");
                    constraint.setVisibility(View.GONE);
                    text.setText(getShowText());
                }

            }
        });

    }


    private boolean isShowText(String text) {
        String showText = getShowText().toLowerCase();
        return text.toLowerCase().contains(showText);
    }

    private String getShowText() {
        return getResources().getString(R.string.show_advanced_filters);
    }

    private String getHideText() {
        return getResources().getString(R.string.hide_advanced_filters);
    }

    private void setGenerateButtonHandler() {
        final DiscreteSeekBar bar = (DiscreteSeekBar) findViewById(R.id.main_screen_random_selector_bar);
        final Button generateButton = (Button) findViewById(R.id.main_screen_generate_button);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int amountToGenerate = bar.getProgress();
                ArrayList<Npc> generatedNpcs = generateNpcs(amountToGenerate);
                openNpcActivity(generatedNpcs);
            }
        });
    }

    private void openNpcActivity(ArrayList<Npc> npcsToShow) {
        Intent i = new Intent(this, GeneratedNpcsActivity.class);
        i.putExtra(MainActivity.NPCS_LIST_INTENT_NAME, npcsToShow);
        startActivity(i);

    }

    private ArrayList<Npc> generateNpcs(int amountToGenerate) {
        ArrayList<Npc> npcList = new ArrayList<>();
        for (int i = 0; i < amountToGenerate; i++) {
            npcList.add(
                    new Npc.Builder(this)
                            .random()
                            .withInformation(getCurrentRace())
                            .withInformation(getCurrentGender())
                            .getNpcInstance());
        }
        return npcList;
    }

    private me.kerooker.characterinformation.Gender getCurrentGender() {
        Spinner genderSpinner = (Spinner) findViewById(R.id.gender_spinner);
        String selected = (String) genderSpinner.getSelectedItem();
        me.kerooker.characterinformation.Gender gender;
        if (selected.toLowerCase().contains("random")) {
            gender = new me.kerooker.characterinformation.Gender();
        } else {
            Gender g = Gender.valueOf(selected.toUpperCase());
            gender = new me.kerooker.characterinformation.Gender(g);
        }
        return gender;
    }

    private Race getCurrentRace() {
        Spinner raceSpinner = (Spinner) findViewById(R.id.race_spinner);
        String selected = (String) raceSpinner.getSelectedItem();
        if (!selected.toLowerCase().contains("random")) {
            Race r = new Race(selected);
            String sub = getCurrentSubrace();
            if (sub != null && !sub.toLowerCase().contains("random")) {
                r.setSubrace(sub);
            }
            return r;
        }
        return new Race(this);
    }

    private String getCurrentSubrace() {
        Spinner raceSpinner = (Spinner) findViewById(R.id.subrace_spinner);
        return (String) raceSpinner.getSelectedItem();
    }

    /**
     * Sets the event for the discreetebar to change the textview on it's progerss
     */
    private void setDiscreeteBarEvent() {
        final DiscreteSeekBar bar = (DiscreteSeekBar) findViewById(R.id.main_screen_random_selector_bar);
        final TextView view = (TextView) findViewById(R.id.main_screen_textViewAmountOfNpcs);

        /* Setting onProgressChange AKA when bar moves*/
        bar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                String textNow = view.getText().toString();
                textNow = textNow.replaceAll("\\d", "");
                String newText = textNow + value;
                view.setText(newText);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                //IGNORE
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                //IGNORE
            }
        });
    }


    private void proccessAdvertisement(MainActivity activity) {
        Advertiser.attemptAdvertisement(activity);
    }
}
