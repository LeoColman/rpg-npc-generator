package me.kerooker.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

import me.kerooker.characterinformation.Npc;
import me.kerooker.rpgcharactergenerator.R;

public class FileManager {


    public static void saveNpcToFile(Npc npc, Context context) {
        UUID uuid;

        if (!npc.hasUUID()) {
            npc.setRandomUUID();
        }
        uuid = npc.getUUID();

        String saveKey = context.getResources().getString(R.string.preferences_saved_npcs);
        SharedPreferences prefs = context.getSharedPreferences(saveKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(String.valueOf(uuid), npc.toJson());
        editor.commit();

    }

    public static Npc getNpcFromFile(String uuidKey, Context context) {
        String saveKey = context.getResources().getString(R.string.preferences_saved_npcs);
        SharedPreferences prefs = context.getSharedPreferences(saveKey, Context.MODE_PRIVATE);

        String json = prefs.getString(uuidKey, null);
        return Npc.fromJson(json);

    }

    public static Npc getNpcFromFile(UUID uuidKey, Context context) {
        return getNpcFromFile(String.valueOf(uuidKey), context);

    }

}
