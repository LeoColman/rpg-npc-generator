package me.kerooker.generatednpcs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.kerooker.characterinformation.Information;
import me.kerooker.characterinformation.Npc;
import me.kerooker.enums.Priority;
import me.kerooker.rpgcharactergenerator.MainActivity;
import me.kerooker.rpgcharactergenerator.R;
import me.kerooker.util.FileManager;
import me.kerooker.util.ImageFormatter;
import me.kerooker.util.ViewIdGenerator;

public class GeneratedNpcsActivity extends AppCompatActivity {

    private static final int HIGH_PRIORITY_TEXT_SIZE = 18;
    private static final int MAIN_LINEAR = R.id.generated_main_linear;
    /*
     * List representing the NPCs on this Activity List.
      * Used to pass reference of the Npcs from one class to another class
      * Enabling the edition of NPCs.
      * USED BECAUSE ANDROID WON'T LET ME PASS A REFERENCE
     */
    private static List<Npc> generatedNpcList;
    private boolean menuSelected = false;
    private transient Menu menu;
    private transient List<Integer> indexesToDelete = new ArrayList<>();

    /**
     * Returns the NPC object of index index
     *
     * @param index The index of the npc
     * @return The npc of index index on the static npc list
     */
    public static Npc getNpc(int index) {
        return generatedNpcList.get(index);
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (menuSelected) {
            deleteSelected();
        } else {
            selectMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectMenu() {
        MenuItem item = menu.getItem(0);
        item.setIcon(R.drawable.delete_icon_red);
        menuSelected = true;
    }

    private void deleteSelected() {
        int toDelete = indexesToDelete.size();
        if (toDelete == 0) {
            unselectMenu();
            return;
        }

        popupDeleteAlert();

    }

    private void popupDeleteAlert() {
        int toDelete = indexesToDelete.size();
        String confirmDelete = getResources().getString(R.string.confirm_delete);
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(String.format(confirmDelete, toDelete))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    //Delete
                    proceedWithDeletion();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    //Dismiss
                    dialog.dismiss();
                })
                .create();
        d.show();
    }

    private void proceedWithDeletion() {
        unselectMenu();

        List<Npc> npcsToRemove = new ArrayList<>();
        List<View> viewsToRemove = new ArrayList<>();
        LinearLayout mainLinear = (LinearLayout) findViewById(R.id.generated_main_linear);//TODO

        Iterator<Integer> iterator = indexesToDelete.iterator();
        while (iterator.hasNext()) {
            int indextoRemove = iterator.next();
            npcsToRemove.add(generatedNpcList.get(indextoRemove));
            viewsToRemove.add(mainLinear.getChildAt(indextoRemove));
            iterator.remove();
        }

        deleteNpcs(npcsToRemove);
        deleteViews(mainLinear, viewsToRemove);
        resetListeners(mainLinear);
    }

    private void resetListeners(LinearLayout mainLinear) {
        int childCount = mainLinear.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mainLinear.getChildAt(i);
            Npc npc = generatedNpcList.get(i);
            setClickToOpenNpc((ConstraintLayout) view, npc);
        }
    }

    private void deleteViews(LinearLayout mainLinear, List<View> toRemove) {
        for (View v : toRemove) {
            mainLinear.removeView(v);
        }
    }

    private void deleteNpcs(List<Npc> toDelete) {
        generatedNpcList.removeAll(toDelete);
        FileManager.deleteNpcs(this, toDelete);
    }

    private void unselectMenu() {
        MenuItem item = menu.getItem(0);
        item.setIcon(R.drawable.delete_icon);
        menuSelected = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.generated_activity_action_bar, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }


    @SuppressWarnings("unchecked")
    private void loadNpcs() {
        Intent thisIntent = getIntent();
        boolean isSaved = thisIntent.getBooleanExtra(MainActivity.NPCS_LIST_INTENT_IS_SAVED_BOOLEAN_NAME, false);
        if (!isSaved) {
            ArrayList<Npc> npcs = (ArrayList<Npc>) thisIntent.getSerializableExtra(MainActivity.NPCS_LIST_INTENT_NAME);
            generatedNpcList = npcs;
        } else {
            ArrayList<String> npcsUuids = (ArrayList<String>) thisIntent.getSerializableExtra(MainActivity.NPCS_LIST_INTENT_NAME);
            generatedNpcList = new ArrayList<>();
            for (String uuid : npcsUuids) {
                Npc n = FileManager.getNpcFromFile(uuid, this);
                generatedNpcList.add(n);
            }
        }
    }

    private void showLoadedNpcs() {
        setContentView(R.layout.activity_generated_npcs);
        for (Npc n : generatedNpcList) {
            addNpc(n);
        }
    }

    private void addNpc(Npc npc) {
        LinearLayout mainLinear = (LinearLayout) findViewById(MAIN_LINEAR);
        int lastChildPos = mainLinear.getChildCount() - 1;

        ConstraintLayout newBox = getNewBoxLayout(mainLinear);

        if (npc.hasImage()) {
            String imageBits = npc.getImageBits();
            ImageView v = (ImageView) newBox.findViewById(R.id.npcImage);
            Bitmap bmp = ImageFormatter.getBitmap(imageBits);
            BitmapDrawable d = new BitmapDrawable(getResources(), bmp);
            v.setImageDrawable(d);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            newBox.setBackground(getBoxBorder());
        } else {
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
            addChildToLast(mainLinear, newBox);
        } else {
            addChild(mainLinear, newBox);
        }

        setClickToOpenNpc(newBox, npc);

    }

    /**
     * Sets OnClickEvent to open another activity with the Npc.
     * Passes only the reference to the NPC (via index of static list)
     * Should be got by getNpc static method
     *
     * @deprecated ANDROID SHOULD FIX THIS SHIT
     */
    private void setClickToOpenNpc(ConstraintLayout box, Npc toOpen) {
        final int toOpenIndex = generatedNpcList.indexOf(toOpen);

        View.OnClickListener listener = v -> {
            if (menuSelected) {

                if (isSelected(box)) {
                    box.setBackgroundDrawable(getBoxBorder());
                    indexesToDelete.remove(Integer.valueOf(toOpenIndex));
                    box.setTag(false);

                } else {
                    box.setBackgroundDrawable(getSelectedBoxBackground());
                    box.setTag(true);
                    indexesToDelete.add(toOpenIndex);
                }
            } else {
                openActivityForNpc(toOpenIndex);
            }
        };
        box.setOnClickListener(listener);

    }

    private boolean isSelected(ConstraintLayout box) {
        Object tag = box.getTag();
        if (tag == null) return false;
        Boolean b = (Boolean) tag;
        return b.booleanValue();
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

    private GradientDrawable getSelectedBoxBackground() {
        GradientDrawable border = new GradientDrawable();
        final int BLACK = 0xFF000000;
        final int COLOR_BACK = getResources().getColor(R.color.red_color_selected_box);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            border.setColor(COLOR_BACK); //white background
        } else {
            border.setColor(COLOR_BACK);
        }
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

    private void addChild(LinearLayout mainLinear, ConstraintLayout newBox) {
        mainLinear.addView(newBox);
    }


    private void addChildToLast(LinearLayout mainLinear, ConstraintLayout newBox) {
        mainLinear.addView(newBox);
    }

    private ConstraintLayout getNewBoxLayout(ViewGroup parent) {
        return (ConstraintLayout) getLayoutInflater().inflate(R.layout.box_npcs, parent, false);
    }


}
