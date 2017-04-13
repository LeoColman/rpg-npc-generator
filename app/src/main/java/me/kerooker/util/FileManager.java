package me.kerooker.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        editor.apply();

    }

    public static Npc getNpcFromFile(String uuidKey, Context context) {
        String saveKey = context.getResources().getString(R.string.preferences_saved_npcs);
        SharedPreferences prefs = context.getSharedPreferences(saveKey, Context.MODE_PRIVATE);

        String json = prefs.getString(uuidKey, null);
        return Npc.fromJson(json);

    }

    public static ArrayList<Npc> getSavedNpcList(Context context) {
        ArrayList<Npc> npcList = new ArrayList<>();

        String saveKey = context.getResources().getString(R.string.preferences_saved_npcs);
        SharedPreferences prefs = context.getSharedPreferences(saveKey, Context.MODE_PRIVATE);
        Map<String, ?> npcs = prefs.getAll();
        for (String uuidKey : npcs.keySet()) {
            String npcJson = (String) npcs.get(uuidKey);
            npcList.add(Npc.fromJson(npcJson));
        }

        return npcList;

    }

// --Commented out by Inspection START (12/04/2017 18:40):
//    public static Npc getNpcFromFile(UUID uuidKey, Context context) {
//        return getNpcFromFile(String.valueOf(uuidKey), context);
//
//    }
// --Commented out by Inspection STOP (12/04/2017 18:40)

    private static void deleteNpc(Context c, Npc n) {
        if (!n.hasUUID()) return;

        String saveKey = c.getResources().getString(R.string.preferences_saved_npcs);
        SharedPreferences prefs = c.getSharedPreferences(saveKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(String.valueOf(n.getUUID()));
        editor.apply();

    }

    public static void deleteNpcs(Context context, List<Npc> toDelete) {
        for (Npc npc : toDelete) {
            deleteNpc(context, npc);
        }
    }
}
