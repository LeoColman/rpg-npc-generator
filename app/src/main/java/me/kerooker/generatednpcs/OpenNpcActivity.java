package me.kerooker.generatednpcs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.List;

import me.kerooker.characterinformation.Information;
import me.kerooker.characterinformation.Npc;
import me.kerooker.rpgcharactergenerator.R;

public class OpenNpcActivity extends AppCompatActivity {

    List<Information> npcInformation;
    private Npc npc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_npc);
        npc = getNpcFromIntent();
        loadNpc();
    }

    private void loadNpc() {
        npcInformation = npc.npcInformation();

        TextView title = (TextView) findViewById(R.id.open_npc_title);
        title.setText(npc.getTopInformation().getInformation());

        for (Information inf : npcInformation) {
            addInformation(inf);
        }

    }

    private void addInformation(Information inf) {
        
    }


    private Npc getNpcFromIntent() {
        Intent i = getIntent();
        return (Npc) i.getSerializableExtra(getResources().getString(R.string.npc_key_intent_open_npc_activity));

    }
}
