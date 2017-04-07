package me.kerooker.generatednpcs;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.kerooker.characterinformation.Information;
import me.kerooker.characterinformation.Npc;
import me.kerooker.enums.Priority;
import me.kerooker.rpgcharactergenerator.MainActivity;
import me.kerooker.rpgcharactergenerator.R;
import me.kerooker.util.ViewIdGenerator;

public class GeneratedNpcsActivity extends AppCompatActivity {

    private static final int HIGH_PRIORITY_TEXT_SIZE = 18;
    private static final int MAIN_CONSTRAINT_ID = R.id.generated_main_constraint;

    /*
     * List representing the NPCs on this Activity List.
      * Used to pass reference of the Npcs from one class to another class
      * Enabling the edition of NPCs.
      * USED BECAUSE ANDROID WON'T LET ME PASS A REFERENCE
     */
    private static List<Npc> generatedNpcList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadNpcs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadedNpcs();
    }

    @SuppressWarnings("unchecked")
    private void loadNpcs() {
        Intent thisIntent = getIntent();
        ArrayList<Npc> npcs = (ArrayList<Npc>) thisIntent.getSerializableExtra(MainActivity.NPCS_LIST_INTENT_NAME);
        generatedNpcList = npcs;
    }

    private void showLoadedNpcs() {
        setContentView(R.layout.activity_generated_npcs);
        for (Npc n : generatedNpcList) {
            addNpc(n);
        }
    }

    private void addNpc(Npc npc) {
        ConstraintLayout mainConstraint = (ConstraintLayout) findViewById(MAIN_CONSTRAINT_ID);
        int lastChildPos = mainConstraint.getChildCount() - 1;

        ConstraintLayout newBox = getNewBoxLayout(mainConstraint);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            newBox.setBackground(getBoxBorder());
        }else {
            //noinspection deprecation
            newBox.setBackgroundDrawable(getBoxBorder());
        }
        newBox.setId(ViewIdGenerator.generateViewId());

        Information topInformation = npc.getTopInformation();
        TextView title = (TextView) newBox.findViewById(R.id.top_priority);
        title.setText(topInformation.getInformation());

        List<Information> info = npc.getNpcInformation();
        for (Information inf : info) {
            if (inf.getPriority() == Priority.HIGH) {
                addInformation(newBox, inf);
            }
        }

        if (lastChildPos >= 0) {
            addChildToLast(mainConstraint, newBox);
        } else {
            addChild(mainConstraint, newBox);
        }

        setClickToOpenNpc(newBox, npc);

    }

    /**
     * Returns the NPC object of index index
     * @param index The index of the npc
     * @return The npc of index index on the static npc list
     */
    public static Npc getNpc(int index) {
        return generatedNpcList.get(index);
    }

    /**
     * Sets OnClickEvent to open another activity with the Npc.
     * Passes only the reference to the NPC (via index of static list)
     * Should be get by getNpc static method
     * @deprecated ANDROID SHOULD FIX THIS SHIT
     */
    private void setClickToOpenNpc(ConstraintLayout box, Npc toOpen) {
        final int toOpenIndex = generatedNpcList.indexOf(toOpen);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityForNpc(toOpenIndex);
            }
        };
        box.setOnClickListener(listener);

    }

    private void openActivityForNpc(int toOpen) {
        Intent i = new Intent(this, OpenNpcActivity.class);
        i.putExtra(getResources().getString(R.string.npc_key_intent_open_npc_activity), toOpen);
        startActivity(i);
    }




    private GradientDrawable getBoxBorder() {
        GradientDrawable border = new GradientDrawable();
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;
        border.setColor(WHITE); //white background
        border.setStroke(1, BLACK); //black border with full opacity
        return border;
    }

    private void addInformation(ConstraintLayout newBox, Information inf) {
        LinearLayout layout = (LinearLayout) newBox.findViewById(R.id.linearLayout_high_highest_normal);

        TextView toAdd = new TextView(this);
        toAdd.setEllipsize(TextUtils.TruncateAt.START);
        toAdd.setHorizontallyScrolling(false);
        //TODO setTextLayout

        toAdd.setText(inf.getInformation());
        toAdd.setTextSize(HIGH_PRIORITY_TEXT_SIZE);

        layout.addView(toAdd);

    }

    private void addChild(ConstraintLayout mainConstraint, ConstraintLayout newBox) {
        mainConstraint.addView(newBox);
    }


    private void addChildToLast(ConstraintLayout mainConstraint, ConstraintLayout newBox) {
        View lastChild = mainConstraint.getChildAt(mainConstraint.getChildCount() - 1);
        mainConstraint.addView(newBox);

        ConstraintSet set = new ConstraintSet();
        set.clone(mainConstraint);

        set.connect(newBox.getId(), ConstraintSet.TOP, lastChild.getId(), ConstraintSet.BOTTOM, 0);
        set.applyTo(mainConstraint);

    }

    private ConstraintLayout getNewBoxLayout(ViewGroup parent) {
        return (ConstraintLayout) getLayoutInflater().inflate(R.layout.box_npcs, parent, false);
    }


}
