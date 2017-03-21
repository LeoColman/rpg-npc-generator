package me.kerooker.rpgcharactergenerator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import me.kerooker.advertiser.Advertiser;
import me.kerooker.characterinformation.Name;

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
        for (int i = 0; i < 500; i++) {
            Name n = new Name(this);
            Log.d("Name " + i, n.getInformation());
        }
    }

    public void finishLoadingAd() {
        setContentView(R.layout.activity_main);
    }

    private void proccessAdvertisement(MainActivity activity) {
        Advertiser.attemptAdvertisement(activity);
    }
}
