package me.kerooker.generatednpcs;

import android.content.Intent;
import android.graphics.Color;
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

    private static final int NORMAL_PRIORITY_TEXT_SIZE = 14;
    private static final int HIGH_PRIORITY_TEXT_SIZE = 18;
    private static final int LOW_PRIORITY_TEXT_SIZE = 12;
    private static final int LOWEST_PRIORITY_TEXT_SIZE = 10;
    private static final int MAIN_CONSTRAINT_ID = R.id.generated_main_constraint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generated_npcs);

        showLoadedNpcs();
    }



    @SuppressWarnings("unchecked")
    private void showLoadedNpcs() {
        Intent thisIntent = getIntent();
        ArrayList<Npc> npcs = (ArrayList<Npc>) thisIntent.getSerializableExtra(MainActivity.NPCS_LIST_INTENT_NAME);
        for (Npc n : npcs) {
            addNpc(n);
        }
    }

    private void addNpc(Npc n) {
        ConstraintLayout mainConstraint = (ConstraintLayout) findViewById(MAIN_CONSTRAINT_ID);
        int lastChildPos = mainConstraint.getChildCount() - 1;

        ConstraintLayout newBox = getNewBoxLayout(mainConstraint);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            newBox.setBackground(getBoxBorder());
        }else {
            newBox.setBackgroundDrawable(getBoxBorder());
        }
        newBox.setId(ViewIdGenerator.generateViewId());

        Information topInformation = n.popTopInformation();
        TextView title = (TextView) newBox.findViewById(R.id.top_priority);
        title.setText(topInformation.getInformation());

        List<Information> info = n.npcInformation();
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
    }

    private GradientDrawable getBoxBorder() {
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFFFFFFF); //white background
        border.setStroke(1, 0xFF000000); //black border with full opacity
        return border;
    }

    private void addInformation(ConstraintLayout newBox, Information inf) {
        LinearLayout layout = (LinearLayout) newBox.findViewById(R.id.linearLayout_high_highest_normal);
        Priority p = inf.getPriority();

        TextView toAdd = new TextView(this);
        toAdd.setEllipsize(TextUtils.TruncateAt.START);
        toAdd.setHorizontallyScrolling(false);
        //TODO setTextLayout

        toAdd.setText(inf.getInformation());
        int sizeToSet = 0;
        switch (p) {
            case LOWEST:
                sizeToSet = LOWEST_PRIORITY_TEXT_SIZE;
                break;
            case LOW:
                sizeToSet = LOW_PRIORITY_TEXT_SIZE;
                break;
            case NORMAL:
                sizeToSet = NORMAL_PRIORITY_TEXT_SIZE;
                break;
            case HIGH:
                sizeToSet = HIGH_PRIORITY_TEXT_SIZE;
                break;
        }
        if (sizeToSet == 0) throw new RuntimeException("Size not set");
        toAdd.setTextSize(sizeToSet);

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
