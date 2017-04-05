package me.kerooker.generatednpcs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import me.kerooker.characterinformation.Information;
import me.kerooker.characterinformation.Npc;
import me.kerooker.rpgcharactergenerator.R;

public class OpenNpcActivity extends AppCompatActivity {

    private static final int HIGH_PRIORITY_TEXT_SIZE = 22;
    private static final int NORMAL_PRIORITY_TEXT_SIZE = 22;
    private static final int LAYOUT_BORDERS_TEXT_VIEWS = 8;
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
        switch (inf.getPriority()) {
            case LOWEST:
                break;
            case LOW:
                break;
            case NORMAL:
                /* Falls Thru */
                break;
            case HIGH:
                addHighNormalPriority(inf);
                break;
        }
    }

    private void addHighNormalPriority(Information inf) {
        TextView tv = new TextView(this);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(inf.getInformation());


        tv.setText(builder);
        builder.setSpan(new AbsoluteSizeSpan(HIGH_PRIORITY_TEXT_SIZE, true), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv.setText(builder);


        getHighLinear().addView(tv);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();
        params.setMargins(LAYOUT_BORDERS_TEXT_VIEWS, 0, LAYOUT_BORDERS_TEXT_VIEWS, 0);
        tv.setLayoutParams(params);

    }

    private LinearLayout getHighLinear() {
        return (LinearLayout) findViewById(R.id.open_npc_high_linear);
    }


    private Npc getNpcFromIntent() {
        Intent i = getIntent();
        return (Npc) i.getSerializableExtra(getResources().getString(R.string.npc_key_intent_open_npc_activity));

    }
}
