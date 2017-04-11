package me.kerooker.rpgcharactergenerator;

import org.junit.Test;

import me.kerooker.characterinformation.Information;
import me.kerooker.characterinformation.Npc;
import me.kerooker.characterinformation.Sexuality;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void generatingRacesOk() {
        Sexuality s = new Sexuality();

        Npc n = new Npc(s);

        for (Information inf : n.npcInformation()) {
            System.out.println(inf.getClass().isInstance(inf.getClass()));
        }
    }
}