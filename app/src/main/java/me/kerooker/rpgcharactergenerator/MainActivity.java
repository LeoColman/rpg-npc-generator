package me.kerooker.rpgcharactergenerator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.Serializable;
import java.util.ArrayList;

import me.kerooker.advertiser.Advertiser;
import me.kerooker.characterinformation.Npc;

public class MainActivity extends AppCompatActivity implements Serializable {

    public static final String NPCS_LIST_INTENT_NAME = "npcslist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start();
        setEventHandlers();
    }

    private void start() {
        proccessAdvertisement(this);    /* Calls finishLoadingAd later */
    }

    private void setEventHandlers() {
        setDiscreeteBarEvent();
        setGenerateButtonHandler();
    }

    private void setGenerateButtonHandler() {
        final DiscreteSeekBar bar = (DiscreteSeekBar) findViewById(R.id.main_screen_random_selector_bar);
        final Button generateButton = (Button) findViewById(R.id.main_screen_generate_button);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int amountToGenerate = bar.getProgress();
                Log.d("Start", "Start Generating");
                ArrayList<Npc> generatedNpcs = generateNpcs(amountToGenerate);
                Log.d("Open", "Openning next activity");
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
