package me.kerooker.rpgcharactergenerator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.kerooker.advertiser.Advertiser;
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
    }

    private Npc generateRandomNpc() {
        Age npcAge = new Age();
        Gender npcGender = new Gender();
        Race npcRace = new Race(this);
        Language npcLanguage = new Language(npcRace);
        Motivation npcMotivation = new Motivation(this);
        Name npcName = new Name(this);
        PersonalityTraits npcTraits = new PersonalityTraits(this);
        Profession npcProfession = new Profession(npcAge, this);
        Sexuality npcSexuality = new Sexuality();

        return new Npc(npcAge, npcGender, npcRace, npcLanguage, npcMotivation, npcName, npcTraits, npcProfession, npcSexuality);


    }


    public void finishLoadingAd() {
        setContentView(R.layout.activity_main);
    }

    private void proccessAdvertisement(MainActivity activity) {
        Advertiser.attemptAdvertisement(activity);
    }
}
