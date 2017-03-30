package me.kerooker.rpgcharactergenerator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

import me.kerooker.advertiser.Advertiser;
import me.kerooker.characterinformation.Npc;
import me.kerooker.characterinformation.Race;
import me.kerooker.enums.Gender;

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
                }else {
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
            npcList.add(Npc.generateRandomNpc(this));
        }
        return npcList;
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
