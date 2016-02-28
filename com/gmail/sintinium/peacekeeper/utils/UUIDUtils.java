package com.gmail.sintinium.peacekeeper.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class UUIDUtils {

    public static String getChangedUsernameFromUUID(UUID uuid) {
        String trimUUID = uuid.toString().replaceAll("-", "");
        String resultName = null;
        try {
            URL fetcher = new URL("https://api.mojang.com/user/profiles/" + trimUUID + "/names");
            JSONParser parser = new JSONParser();
            Object o = parser.parse(new InputStreamReader(fetcher.openStream()));
            JSONArray object = (JSONArray) o;
            if (object.isEmpty()) return null;
            Object name = ((JSONArray) o).get(object.size() - 1);
            resultName = ((JSONObject) name).get("name").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultName;
    }

}
