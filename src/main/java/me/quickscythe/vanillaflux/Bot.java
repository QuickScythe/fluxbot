package me.quickscythe.vanillaflux;

import json2.JSONObject;
import me.quickscythe.vanillaflux.listeners.MessageListener;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.webapp.TokenManager;
import me.quickscythe.vanillaflux.webapp.WebApp;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class Bot {


    public static final long COMMAND_CHANNEL = 1268045246512758835L;
    public static final long LOG_CHANNEL = 1268006180626628690L;
    public static final long GUILD_ID = 1140468525190877206L;
    public static final long INACTIVE_ROLE = 1226923455648104559L;
    public static final long ONLINE_ROLE = 1278524752348315689L;
    public static final long INACTIVE_DAYS_TIMER = 90;
    public static final String API_ENTRY_POINT = "/api";
    public static final String APP_ENTRY_POINT = "/app";
    private static String CMD_PREFIX = "!";
    private static String BOT_TOKEN;
    private static String APP_TOKEN;
    private static boolean DEBUG = false;
    private static JSONObject CONFIG;

    public static void main(String[] args) {
        Utils._before_init();
        BOT_TOKEN = loadToken();

        CONFIG = loadConfig();
        if (!CONFIG.has("command_prefix"))
            CONFIG.put("command_prefix", CMD_PREFIX);
        else CMD_PREFIX = CONFIG.getString("command_prefix");
        JDA api = JDABuilder.createDefault(BOT_TOKEN, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).build();
        Utils.init(api);
        APP_TOKEN = TokenManager.requestNewToken("0:0:0:0:0:0:0:1");
        api.addEventListener(new MessageListener());
        new WebApp();

    }

    private static String loadToken() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            File token = new File("token");
            if (!token.exists()) if (token.createNewFile()) {
                Utils.getLogger().error("Token file generated. Please enter your token before launch.", "=");
            }
            BufferedReader reader = new BufferedReader(new FileReader("token"));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();


        } catch (IOException ex) {
            Utils.getLogger().error("Token File couldn't be generated or accessed. Please check console for more details.", ex);
        }
        String token = stringBuilder.toString();
        if (token.startsWith("ODg1MzM")) DEBUG = true;
        return token;
    }

    private static JSONObject loadConfig() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            File config = new File("config");
            if (!config.exists()) if (config.createNewFile()) {
                Utils.getLogger().error("Config file generated.", "=");
            }
            BufferedReader reader = new BufferedReader(new FileReader("config"));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();


        } catch (IOException ex) {
            Utils.getLogger().error("Token File couldn't be generated or accessed. Please check console for more details.", ex);
        }
        String config = stringBuilder.toString();
        return config.isEmpty() ? new JSONObject() : new JSONObject(config);
    }

    public static JSONObject getConfig() {
        return CONFIG;
    }

    public static void saveConfig() {
        try {
            FileWriter f2 = new FileWriter(new File("config"), false);
            f2.write(CONFIG.toString(2));
            f2.close();
        } catch (IOException e) {
            Utils.getLogger().log("There was an error saving the config file.", true);
            Utils.getLogger().error("Error", e);
        }
    }

    public static boolean isDebug() {
        return DEBUG;
    }

    public static long getInactiveEpochTime() {
        return TimeUnit.MILLISECONDS.convert(INACTIVE_DAYS_TIMER, TimeUnit.DAYS);
    }

    public static String appToken() {
        return APP_TOKEN;
    }

    public static String botToken() {
        return BOT_TOKEN;
    }

    public static String CMD_PREFIX() {
        return CMD_PREFIX;
    }
}