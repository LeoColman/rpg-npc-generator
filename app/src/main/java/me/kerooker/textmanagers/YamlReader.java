package me.kerooker.textmanagers;

import android.content.Context;
import android.content.res.AssetManager;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class YamlReader {


    public static Map loadYamlFromAssets(Context c, String fileName) throws IOException {
        AssetManager assets = c.getAssets();
        String stripped = fileName.replaceAll("\\.yml", "");
        stripped = stripped.replaceAll("\\.yaml", "");

        InputStream file = assets.open("yaml/" + stripped + ".yaml");
        Yaml yaml = new Yaml();
        return (Map) yaml.load(file);
    }


}
