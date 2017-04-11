package me.kerooker.generatednpcs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.kerooker.characterinformation.Information;
import me.kerooker.characterinformation.Npc;
import me.kerooker.enums.Priority;
import me.kerooker.rpgcharactergenerator.R;
import me.kerooker.util.FileManager;

public class OpenNpcActivity extends AppCompatActivity {

    private static final int HIGH_PRIORITY_TEXT_SIZE = 22;
    private static final int NORMAL_PRIORITY_TEXT_SIZE = 20;
    private static final int LOW_PRIORITY_TEXT_SIZE = 20;
    private static final int LOWEST_PRIORITY_TEXT_SIZE = LOW_PRIORITY_TEXT_SIZE;
    private static final int LAYOUT_BORDERS_TEXT_VIEWS = 8;
    private static final int EDITABLE_TEXT_COLOR = R.color.grey_editable_text;
    private static final int NON_EDITABLE_TEXT_COLOR = R.color.common_google_signin_btn_text_light_default;
    List<Information> npcInformation;
    private Menu menu;
    private Npc npc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_npc);
        npc = getNpcFromIntent();

        loadNpc();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_edit:
                //Allow Editting
                allowEdditing();
                break;
            case R.id.menu_cancel:
                disallowEdditing(false);
                break;
            case R.id.menu_save:
                disallowEdditing(true);
                break;
            case R.id.menu_save_to_file:
                handleSaveToFile();
                break;
            case R.id.menu_export:
                handleExport();
                break;

        }
        return true;
    }

    private void handleSaveToFile() {
        FileManager.saveNpcToFile(npc, this);
        Npc n = FileManager.getNpcFromFile(npc.getUUID(), this);
        Log.d("Aff", n.getCharacter());
    }

    private void handleExport() {

    }

    private void allowEdditing() {
        showCancelAndSave();
        hideMenuEdit();

        setEditable(getEditTextList());

    }

    private void saveInformation() {
        List<Information> listToSave = new ArrayList<>();


        EditText title = (EditText) findViewById(R.id.open_npc_title);
        final String titleString = title.getText().toString();
        listToSave.add(new Information() {
            @Override
            public Priority getPriority() {
                return Priority.TOP;
            }

            @Override
            public String getInformation() {
                return titleString;
            }
        });

        LinearLayout highLinear = (LinearLayout) findViewById(R.id.open_npc_high_linear);

        for (int i = 0; i < highLinear.getChildCount(); i++) {
            TextView child = (TextView) highLinear.getChildAt(i);
            final String childText = child.getText().toString();

            listToSave.add(new Information() {
                @Override
                public Priority getPriority() {
                    return Priority.HIGH;
                }

                @Override
                public String getInformation() {
                    return childText;
                }
            });
        }

        LinearLayout normalLinear = (LinearLayout) findViewById(R.id.open_npc_normal_linear);

        for (int i = 0; i < normalLinear.getChildCount(); i++) {
            TextView child = (TextView) normalLinear.getChildAt(i);
            final String childText = child.getText().toString();

            listToSave.add(new Information() {
                @Override
                public Priority getPriority() {
                    return Priority.NORMAL;
                }

                @Override
                public String getInformation() {
                    return childText;
                }
            });
        }


        LinearLayout lowLinear = (LinearLayout) findViewById(R.id.open_npc_low_lowest_linear);

        for (int i = 0; i < lowLinear.getChildCount(); i++) {
            TextView child = (TextView) lowLinear.getChildAt(i);
            final String childText = child.getText().toString();

            listToSave.add(new Information() {
                @Override
                public Priority getPriority() {
                    return Priority.LOW;
                }

                @Override
                public String getInformation() {
                    return childText;
                }
            });
        }

        npc.setInformation(listToSave);
    }

    private List<EditText> getEditTextList() {
        ArrayList<EditText> texts = new ArrayList<>();


        EditText title = (EditText) findViewById(R.id.open_npc_title);
        texts.add(title);


        LinearLayout highLinear = (LinearLayout) findViewById(R.id.open_npc_high_linear);
        for (int i = 0; i < highLinear.getChildCount(); i++) {
            EditText child = (EditText) highLinear.getChildAt(i);
            texts.add(child);
        }

        LinearLayout normalLinear = (LinearLayout) findViewById(R.id.open_npc_normal_linear);
        for (int i = 0; i < normalLinear.getChildCount(); i++) {
            EditText child = (EditText) normalLinear.getChildAt(i);
            texts.add(child);
        }

        LinearLayout lowLinear = (LinearLayout) findViewById(R.id.open_npc_low_lowest_linear);
        for (int i = 0; i < lowLinear.getChildCount(); i++) {
            EditText child = (EditText) lowLinear.getChildAt(i);
            texts.add(child);
        }

        return texts;
    }

    private void disallowEdditing(boolean save) {
        hideCancelAndSave();
        showMenuEdit();
        List<EditText> textList = getEditTextList();

        setUneditable(textList);

        if (save) {
            saveInformation();
        } else {
            returnOriginalText(textList);
        }
    }

    private void returnOriginalText(List<EditText> texts) {
        for (EditText text : texts) {
            text.setText((CharSequence) text.getTag());
        }
    }

    private void setUneditable(EditText t) {

        t.setEnabled(false);
        t.setTextColor(getResources().getColor(OpenNpcActivity.NON_EDITABLE_TEXT_COLOR));
        //t.setInputType(InputType.TYPE_NULL);
    }

    private void setEditable(List<EditText> t) {
        for (EditText text : t) {
            setEditable(text);
        }
    }

    private void setUneditable(List<EditText> t) {
        for (EditText text : t) {
            setUneditable(text);
        }
    }

    private void setEditable(EditText t) {
        t.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        t.setSingleLine(false);
        t.setEnabled(true);
        t.setTextColor(getResources().getColor(EDITABLE_TEXT_COLOR));

        t.setTag(t.getText().toString());
    }

    private void showCancelAndSave() {
        menu.findItem(R.id.menu_cancel).setVisible(true);
        menu.findItem(R.id.menu_save).setVisible(true);
    }

    private void hideCancelAndSave() {
        menu.findItem(R.id.menu_cancel).setVisible(false);
        menu.findItem(R.id.menu_save).setVisible(false);
    }

    private void showMenuEdit() {
        menu.findItem(R.id.menu_edit).setVisible(true);
    }

    private void hideMenuEdit() {
        menu.findItem(R.id.menu_edit).setVisible(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.open_npc_action_bar, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private void loadNpc() {
        npcInformation = npc.getNpcInformation();

        TextView title = (TextView) findViewById(R.id.open_npc_title);
        title.setText(npc.getTopInformation().getInformation());

        for (Information inf : npcInformation) {
            addInformation(inf);
        }

    }

    private void addInformation(Information inf) {
        switch (inf.getPriority()) {
            case LOWEST:
               /*Falls thru*/
            case LOW:
                addLowAndLowestInformation(inf);
                break;
            case NORMAL:
                addNormalInformation(inf);
                break;
            case HIGH:
                addHighInformation(inf);
                break;
        }
    }

    private void addLowAndLowestInformation(Information inf) {
        int textSize;
        switch (inf.getPriority()) {
            case LOWEST:
                textSize = LOWEST_PRIORITY_TEXT_SIZE;
                break;
            case LOW:
                textSize = LOW_PRIORITY_TEXT_SIZE;
                break;
            default:
                throw new RuntimeException("Invalid text size");
        }

        TextView tv = getInformationTextView(inf, textSize);
        getLowAndLowestLinearLayout().addView(tv);
        setLowAndLowestMargins(tv);
    }

    private LinearLayout getLowAndLowestLinearLayout() {
        return (LinearLayout) findViewById(R.id.open_npc_low_lowest_linear);
    }

    private TextView getInformationTextView(Information inf, int size) {
        EditText tv = new EditText(this);

        tv.setEllipsize(TextUtils.TruncateAt.START);
        tv.setHorizontallyScrolling(false);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(inf.getInformation());

        tv.setTextSize(size);
        tv.setText(builder);
        tv.setBackgroundDrawable(null);
        tv.setEnabled(false);
        tv.setTextColor(getResources().getColor(OpenNpcActivity.NON_EDITABLE_TEXT_COLOR));
        return tv;
    }

    private void setMargins(TextView tv) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();
        params.setMargins(LAYOUT_BORDERS_TEXT_VIEWS, 0, LAYOUT_BORDERS_TEXT_VIEWS, 0);
        tv.setLayoutParams(params);
    }

    private void setLowAndLowestMargins(TextView tv) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();
        params.setMargins(LAYOUT_BORDERS_TEXT_VIEWS, LAYOUT_BORDERS_TEXT_VIEWS, LAYOUT_BORDERS_TEXT_VIEWS, LAYOUT_BORDERS_TEXT_VIEWS);
        tv.setLayoutParams(params);
    }


    private void addNormalInformation(Information inf) {
        TextView tv = getInformationTextView(inf, NORMAL_PRIORITY_TEXT_SIZE);
        getNormalLinear().addView(tv);
        setMargins(tv);

    }

    private void addHighInformation(Information inf) {

        TextView tv = getInformationTextView(inf, HIGH_PRIORITY_TEXT_SIZE);
        getHighLinear().addView(tv);
        setMargins(tv);
    }

    private LinearLayout getHighLinear() {
        return (LinearLayout) findViewById(R.id.open_npc_high_linear);
    }

    private LinearLayout getNormalLinear() {
        return (LinearLayout) findViewById(R.id.open_npc_normal_linear);
    }


    private Npc getNpcFromIntent() {
        Intent i = getIntent();
        int index = i.getIntExtra(getResources().getString(R.string.npc_key_intent_open_npc_activity), 0);
        return GeneratedNpcsActivity.getNpc(index);

    }
}
