package me.quickscythe.vanillaflux.utils;

public class Errors {

    public static String json(String message) {
        return "{\"error\":\"" + message + "\"}";
    }
}
