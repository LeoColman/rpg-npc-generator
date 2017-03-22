package me.kerooker.rpgcharactergenerator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.kerooker.advertiser.Advertiser;
import me.kerooker.characterinformation.Gender;
import me.kerooker.characterinformation.Information;
import me.kerooker.characterinformation.Name;
import me.kerooker.characterinformation.Profession;
import me.kerooker.characterinformation.Race;
import me.kerooker.characterinformation.Sexuality;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);
        start();
    }

    private void start() {
        proccessAdvertisement(this);
        //TODO
        TESTE();
    }

    private void TESTE() {
        for (int i = 0; i < 100; i++) {
            List<Information> info = new ArrayList<>();
            info.add(new Name(this));
            me.kerooker.characterinformation.Age a = new me.kerooker.characterinformation.Age();
            info.add(a);
            info.add(new Gender());
            info.add(new Profession(a.getAge(), this));
            info.add(new Sexuality());
            info.add(new Race(this));
            Log.d("Char " + i, new me.kerooker.characterinformation.Character(info).getCharacter());
        }
    }

    public void finishLoadingAd() {
        setContentView(R.layout.activity_main);
    }

    private void proccessAdvertisement(MainActivity activity) {
        Advertiser.attemptAdvertisement(activity);
    }
}
