package me.kerooker.generatednpcs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.text.style.AbsoluteSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import me.kerooker.characterinformation.Information;
import me.kerooker.characterinformation.Npc;
import me.kerooker.rpgcharactergenerator.R;

public class OpenNpcActivity extends AppCompatActivity {

    private static final int HIGH_PRIORITY_TEXT_SIZE = 22;
    private static final int NORMAL_PRIORITY_TEXT_SIZE = 20;
    private static final int LOW_PRIORITY_TEXT_SIZE = 20;
    private static final int LOWEST_PRIORITY_TEXT_SIZE = 20;
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

                break;
            case R.id.menu_save:
                break;

        }
        return true;
    }

    private void allowEdditing() {
        TextView title = (TextView) findViewById(R.id.open_npc_title);
        setEditable(title);

    }

    private void setUneditable(TextView t) {
        t.setTag(t.getKeyListener());
        t.setKeyListener(null);
    }

    private void setEditable(TextView t) {
        setUneditable(t);
        t.setKeyListener((KeyListener) t.getTag());
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
        return (Npc) i.getSerializableExtra(getResources().getString(R.string.npc_key_intent_open_npc_activity));

    }
}
