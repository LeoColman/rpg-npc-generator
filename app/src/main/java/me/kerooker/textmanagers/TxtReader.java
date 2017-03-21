package me.kerooker.textmanagers;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TxtReader {

    private static final String charset = "UTF-8";

    /**
     * Reads a text file from /txts/txtName.txt
     * Each line of the text file will be converted to a String object and passed to
     * the list returned to this method.
     *
     * Example:
     * Text file : {
     *     1
     *     2
     *     3
     *     4
     * }
     * returns a List<String> with values ["1","2","3","4"]
     * @param txtName The file name of the text file. With or without extension
     * @param c The context from which the files will be loaded
     * @return A new List containing all the lines from the textfile
     */
    public static List<String> readTextFile(Context c, String txtName) throws IOException {
        AssetManager assets = c.getAssets();
        String stripped = txtName.replaceAll("\\.txt", "");
        List<String> lines = new ArrayList<>();
        InputStream file = assets.open("txts/" + stripped + ".txt");

        InputStreamReader isr = new InputStreamReader(file, charset);
        BufferedReader reader = new BufferedReader(isr);
        String name;
        while ( (name = reader.readLine()) != null) {
            lines.add(name);
        }
        return lines;

    }
}
