package me.kerooker.rpgcharactergenerator;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setPaypal();
    }

    private void setPaypal() {
        ImageView paypal = (ImageView) findViewById(R.id.paypal_donate);
        // load image
        try {
            // get input stream
            InputStream ims = getAssets().open("img/PayPal-Donate-Button-PNG-Images.png");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            paypal.setImageDrawable(d);
        } catch (IOException ex) {
            return;
        }

        paypal.setOnClickListener(v -> openPaypal());

    }

    private void openPaypal() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=8V5KLKAEAYTL2"));
        startActivity(browserIntent);
    }
}
