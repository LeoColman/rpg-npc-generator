package me.kerooker.generatednpcs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
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

public class OpenNpcActivity extends AppCompatActivity {

    private static final int HIGH_PRIORITY_TEXT_SIZE = 22;
    private static final int NORMAL_PRIORITY_TEXT_SIZE = 20;
    private static final int LOW_PRIORITY_TEXT_SIZE = 20;
    private static final int LOWEST_PRIORITY_TEXT_SIZE = LOW_PRIORITY_TEXT_SIZE;
    private static final int LAYOUT_BORDERS_TEXT_VIEWS = 8;
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
                showCancelAndSave();
                hideMenuEdit();
                allowEdditing();

                break;
            case R.id.menu_cancel:
                disallowEdditing();
                recreate();
                break;
            case R.id.menu_save:
                disallowEdditing();
                saveInformation();
                break;

        }
        return true;
    }

    @Override
    public void recreate() {
        Intent i = this.getIntent();
        finish();
        startActivity(i);
    }

    private void allowEdditing() {
        EditText title = (EditText) findViewById(R.id.open_npc_title);

        setEditable(title);

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

        for(int i = 0; i < highLinear.getChildCount(); i++) {
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

    private void disallowEdditing() {
        EditText title = (EditText) findViewById(R.id.open_npc_title);


        setUneditable(title);
    }

    private void setUneditable(EditText t) {

        t.setEnabled(false);
        t.setInputType(InputType.TYPE_NULL);
    }

    private void setEditable(EditText ... t) {
        for (EditText text : t) {
            setEditable(text);
        }
    }

    private void setUneditable(EditText ... t) {
        for (EditText text : t) {
            setUneditable(text);
        }
    }

    private void setEditable(EditText t) {
        t.setEnabled(true);
        t.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
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
        TextView tv = new TextView(this);

        tv.setEllipsize(TextUtils.TruncateAt.START);
        tv.setHorizontallyScrolling(false);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(inf.getInformation());

        builder.setSpan(new AbsoluteSizeSpan(size, true), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(builder);
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
        int index =  i.getIntExtra(getResources().getString(R.string.npc_key_intent_open_npc_activity), 0);
        return GeneratedNpcsActivity.getNpc(index);

    }
}
