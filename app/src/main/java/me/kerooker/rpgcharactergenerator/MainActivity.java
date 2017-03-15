package me.kerooker.rpgcharactergenerator;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import me.kerooker.advertiser.Advertiser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void start() {
        proccessAdvertisement(this);
        //TODO
    }

    private void proccessAdvertisement(Activity activity) {
        Advertiser.attemptAdvertisement(activity);
        //TODO
    }
}
