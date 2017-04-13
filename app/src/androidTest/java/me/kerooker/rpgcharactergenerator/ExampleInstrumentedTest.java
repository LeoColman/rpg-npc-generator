package me.kerooker.rpgcharactergenerator;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import me.kerooker.characterinformation.Age;
import me.kerooker.characterinformation.Gender;
import me.kerooker.characterinformation.Language;
import me.kerooker.characterinformation.Motivation;
import me.kerooker.characterinformation.Name;
import me.kerooker.characterinformation.Npc;
import me.kerooker.characterinformation.PersonalityTraits;
import me.kerooker.characterinformation.Profession;
import me.kerooker.characterinformation.Race;
import me.kerooker.characterinformation.Sexuality;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void generate100Characters() throws Exception {

        Context c = InstrumentationRegistry.getContext();
        for (int i = 0; i < 100; i++) {
            Race r = new Race(c);
                Age a = new Age();
                Gender g = new Gender();

                Motivation m = new Motivation(c);
                Name n = new Name(c);
                PersonalityTraits traits = new PersonalityTraits(c);
                Profession p = new Profession(a, c);
                Language l = new Language(r);
                Sexuality s = new Sexuality();

                Npc npc = new Npc(a, g, l, m, n, traits, p, r, s);
                Log.d("N: " + i, npc.getCharacter());
        }


    }
}
