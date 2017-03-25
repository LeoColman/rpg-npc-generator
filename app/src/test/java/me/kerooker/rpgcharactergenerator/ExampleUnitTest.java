package me.kerooker.rpgcharactergenerator;

import org.junit.Test;

import me.kerooker.enums.Sexuality;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void generatingRacesOk() {
        for (int i = 0; i < 100; i++) {
            System.out.println(Sexuality.getRandomSexuality());

        }
    }
}