package me.kerooker.rpgcharactergenerator;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import me.kerooker.advertiser.Advertiser;
import me.kerooker.characterinformation.Race;

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
    }

    public void finishLoadingAd() {
        setContentView(R.layout.activity_main);
    }

    private void proccessAdvertisement(MainActivity activity) {
        Advertiser.attemptAdvertisement(activity);
    }
}
