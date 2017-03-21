package me.kerooker.rpgcharactergenerator;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import me.kerooker.characterinformation.Race;

import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void generateRaceOk() {
        Context c = InstrumentationRegistry.getContext();

        List<String> racesAndSubraces = new ArrayList<>();
        for (int i = 0; i<100; i++) {
            racesAndSubraces.add(new Race(c).getInformation());
            Log.d("Random Race " + i, racesAndSubraces.get(i));
        }
        assertTrue(racesAndSubraces.size() == 100);


    }
}
