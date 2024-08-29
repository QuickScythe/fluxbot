package me.quickscythe.vanillaflux.webapp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TokenManager {

    private static final Map<String, String[]> TOKENS = new HashMap<>();

    public static String requestNewToken(boolean root) {
        String token = UUID.randomUUID().toString();
        while(TOKENS.containsKey(token))
            token = UUID.randomUUID().toString();
        TOKENS.put(token, new String[]{root ? "*" : ""});
        return token;
    }

    public static String[] getToken(String token) {
        return TOKENS.getOrDefault(token, null);
    }
}
