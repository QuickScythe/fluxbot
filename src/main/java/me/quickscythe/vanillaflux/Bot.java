package me.quickscythe.vanillaflux;

import me.quickscythe.vanillaflux.listeners.MessageListener;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.webapp.WebApp;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Bot {


    public static final String CMD_PREFIX = "!";
    public static final long COMMAND_CHANNEL = 1268045246512758835L;
    public static final long LOG_CHANNEL = 1268006180626628690L;
    public static final long GUILD_ID = 1140468525190877206L;
    public static final long INACTIVE_ROLE = 1226923455648104559L;
    public static final long INACTIVE_DAYS_TIMER = 90;
    public static final String API_ENTRY_POINT = "/api";
    public static final String APP_ENTRY_POINT = "/app";
    private static String BOT_TOKEN;
    private static boolean DEBUG = false;

    public static void main(String[] args) {
        Utils._before_init();
        BOT_TOKEN = loadToken();
        JDA api = JDABuilder.createDefault(BOT_TOKEN, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).build();
        Utils.init(api);
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

    public static boolean isDebug() {
        return DEBUG;
    }

    public static long getInactiveEpochTime() {
        return TimeUnit.MILLISECONDS.convert(INACTIVE_DAYS_TIMER, TimeUnit.DAYS);
    }
}