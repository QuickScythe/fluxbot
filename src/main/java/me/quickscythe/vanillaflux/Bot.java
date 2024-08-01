package me.quickscythe.vanillaflux;

import me.quickscythe.vanillaflux.listeners.MessageListener;
import me.quickscythe.vanillaflux.utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Bot {


    public static final String CMD_PREFIX = "!";
    public static final long COMMAND_CHANNEL = 1268045246512758835L;
    public static final long LOG_CHANNEL = 1268006180626628690L;
    public static final long GUILD_ID = 1140468525190877206L;
    public static final long INACTIVE_ROLE = 1226923455648104559L;
    public static final long INACTIVE_DAYS_TIMER = 1;
    private static String BOT_TOKEN;

    public static void main(String[] args) {
        BOT_TOKEN = loadToken();
        JDA api = JDABuilder.createDefault(BOT_TOKEN, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).build();
        Utils.init(api);
        api.addEventListener(new MessageListener());

    }

    private static String loadToken() {
        try {
            File token = new File("token");
            if (!token.exists()) if(token.createNewFile()){
                throw new RuntimeException("Token file generated. Please enter your token before launch.");
            }
            BufferedReader reader = new BufferedReader(new FileReader("token"));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();

            return stringBuilder.toString();
        } catch (IOException ex){
            throw new RuntimeException("Token File couldn't be generated or accessed...");
        }
    }

    public static long getInactiveEpochTime() {
        return TimeUnit.MILLISECONDS.convert(INACTIVE_DAYS_TIMER, TimeUnit.DAYS);
    }
}