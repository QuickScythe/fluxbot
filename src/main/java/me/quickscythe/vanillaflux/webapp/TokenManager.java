package me.quickscythe.vanillaflux.webapp;

import json2.JSONArray;
import json2.JSONObject;
import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.Utils;
import spark.Request;

import java.util.*;

public class TokenManager {

    private static final Map<String, Token> TOKENS = new HashMap<>();

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
            TOKENS.put(token, new Token(token, ip));
            return token;
        }
        return null;
    }

    public static void removeToken(String token) {
        TOKENS.remove(token);
    }

    public static Collection<Token> getTokens() {
        return TOKENS.values();
    }

    public static String[] getTokens(String ip) {
        List<String> tokens = new ArrayList<>();
        for (Map.Entry<String, Token> entry : TOKENS.entrySet()) {
            if (entry.getValue().getIp().equals(ip)) {
                tokens.add(entry.getKey());
            }
        }
        return tokens.toArray(String[]::new);
    }

    public static boolean validToken(Token token, Request request){
        return token != null && token.getIp().equals(request.ip()) && !token.isExpired();
    }


    public static Token getToken(String token) {
        return TOKENS.getOrDefault(token,null);
    }
}
