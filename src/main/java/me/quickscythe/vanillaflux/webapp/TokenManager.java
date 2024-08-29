package me.quickscythe.vanillaflux.webapp;

import json2.JSONArray;
import json2.JSONObject;
import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TokenManager {

    private static final Map<String, String[]> TOKENS = new HashMap<>();

    public static String requestNewToken(String ip) {
        if (!Bot.getConfig().has("allow")) {
            Bot.getConfig().put("allow", new JSONArray());
        }
        Utils.getLogger().log("Requesting token for " + ip);
        boolean allowed = false;
        JSONObject data = Bot.getConfig();
        for (int i = 0; i != data.getJSONArray("allow").length(); i++) {
            if (data.getJSONArray("allow").getString(i).equals(ip)) {
                allowed = true;
                break;
            }
        }
        if (allowed) {
            String token = UUID.randomUUID().toString();
            while (TOKENS.containsKey(token)) token = UUID.randomUUID().toString();
            TOKENS.put(token, new String[]{ip.equals("0:0:0:0:0:0:0:1") ? "*" : ""});
            return token;
        }
        return null;
    }

    public static String[] getToken(String token) {
        return TOKENS.getOrDefault(token, null);
    }
}
