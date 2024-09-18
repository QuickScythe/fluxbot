package me.quickscythe.vanillaflux;

import json2.JSONObject;
import me.quickscythe.vanillaflux.listeners.ButtonListener;
import me.quickscythe.vanillaflux.listeners.MessageListener;
import me.quickscythe.vanillaflux.listeners.commands.LinkCommand;
import me.quickscythe.vanillaflux.listeners.commands.PollCommand;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.polls.PollUtils;
import me.quickscythe.vanillaflux.webapp.TokenManager;
import me.quickscythe.vanillaflux.webapp.WebApp;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class Bot {


    private static final long COMMAND_CHANNEL = 1268045246512758835L;
    private static final long LOG_CHANNEL = 1268006180626628690L;
    private static final long GUILD_ID = 1140468525190877206L;
    private static final long INACTIVE_ROLE = 1226923455648104559L;
    private static final long ONLINE_ROLE = 1278524752348315689L;
    private static final String API_ENTRY_POINT = "/api";
    private static final String APP_ENTRY_POINT = "/app";
    private static final int INACTIVE_DAYS_TIMER = 90;
    private static String CMD_PREFIX = "!";
    private static String BOT_TOKEN;
    private static String APP_TOKEN;
    private static int WEB_PORT = 8585;
    private static int TOKEN_VALID_TIME = 1;
    private static boolean DEBUG = false;
    private static JSONObject CONFIG;

    public static void main(String[] args) throws InterruptedException {
        Utils._before_init();

        CONFIG = loadConfig();
        if (!CONFIG.has("command_prefix"))
            CONFIG.put("command_prefix", CMD_PREFIX);
        if (!CONFIG.has("command_channel"))
            CONFIG.put("command_channel", COMMAND_CHANNEL);
        if (!CONFIG.has("log_channel"))
            CONFIG.put("log_channel", LOG_CHANNEL);
        if (!CONFIG.has("inactive_role"))
            CONFIG.put("inactive_role", INACTIVE_ROLE);
        if (!CONFIG.has("online_role"))
            CONFIG.put("online_role", ONLINE_ROLE);
        if (!CONFIG.has("api_entry_point"))
            CONFIG.put("api_entry_point", API_ENTRY_POINT);
        if (!CONFIG.has("app_entry_point"))
            CONFIG.put("app_entry_point", APP_ENTRY_POINT);
        if (!CONFIG.has("inactive_days_timer"))
            CONFIG.put("inactive_days_timer", INACTIVE_DAYS_TIMER);
        if (!CONFIG.has("guild_id"))
            CONFIG.put("guild_id", GUILD_ID);
        if (!CONFIG.has("web_port"))
            CONFIG.put("web_port", WEB_PORT);
        if (!CONFIG.has("token_valid_time"))
            CONFIG.put("token_valid_time", TOKEN_VALID_TIME);
        if (!CONFIG.has("bot_token")) {
            Utils.getLogger().error("Bot token not found in config file. Please enter your bot token in the config file.", "=");
            saveConfig();
            throw new RuntimeException("Bot token not found in config file.");
        }
        BOT_TOKEN = CONFIG.getString("bot_token");
        if (BOT_TOKEN.startsWith("ODg1Mz") && BOT_TOKEN.endsWith("kfCA0")) DEBUG = true;
        JDA api = JDABuilder.createDefault(BOT_TOKEN, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).build();
        api.awaitReady();
        Utils.init(api);
        APP_TOKEN = TokenManager.requestNewToken("0:0:0:0:0:0:0:1");
        api.addEventListener(new MessageListener());
        saveConfig();
        new WebApp();
        Utils.getGuild().retrieveCommands().complete().forEach(command -> {
            if (command.getName().contains("apoll") || command.getName().contains("epoll"))
                Utils.getGuild().deleteCommandById(command.getId()).queue();
        });


        api.addEventListener(new PollCommand(Utils.getGuild(), "poll", "Create a poll",
                new OptionData(
                        OptionType.STRING,
                        "question",
                        "The question of the poll",
                        true,
                        false),
                new OptionData(
                        OptionType.STRING,
                        "duration",
                        "How long should the poll last? (Example: 3d12h = 3 days and 12 hours)",
                        true,
                        false),
                new OptionData(
                        OptionType.STRING,
                        "answer1",
                        "First answer to the question",
                        true,
                        false),
                new OptionData(
                        OptionType.STRING,
                        "answer2",
                        "Second answer to the question",
                        true,
                        false),
                new OptionData(
                        OptionType.STRING,
                        "answer3",
                        "Third answer to the question",
                        false,
                        false),
                new OptionData(
                        OptionType.STRING,
                        "answer4",
                        "Fourth answer to the question",
                        false,
                        false),
                new OptionData(
                        OptionType.STRING,
                        "answer5",
                        "Fifth answer to the question",
                        false,
                        false),
                new OptionData(
                        OptionType.STRING,
                        "answer6",
                        "Sixth answer to the question",
                        false,
                        false),
                new OptionData(
                        OptionType.STRING,
                        "answer7",
                        "Seventh answer to the question",
                        false,
                        false),
                new OptionData(
                        OptionType.STRING,
                        "answer8",
                        "Eighth answer to the question",
                        false,
                        false),
                new OptionData(
                        OptionType.STRING,
                        "answer9",
                        "Ninth answer to the question",
                        false,
                        false),
                new OptionData(
                        OptionType.STRING,
                        "answer10",
                        "Tenth answer to the question",
                        false,
                        false)

        ));
        api.addEventListener(new LinkCommand(Utils.getGuild(), "link", "Link your Discord account to the server.",
                new OptionData(
                        OptionType.STRING,
                        "key",
                        "The key you received from the server",
                        true,
                        false)
        ));
        api.addEventListener(new ButtonListener());
        PollUtils.init();

    }

    private static JSONObject loadConfig() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            File config = new File("config.json");
            if (!config.exists()) if (config.createNewFile()) {
                Utils.getLogger().error("Config file generated.", "=");
            }
            BufferedReader reader = new BufferedReader(new FileReader("config.json"));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();


        } catch (IOException ex) {
            Utils.getLogger().error("Config File couldn't be generated or accessed. Please check console for more details.", ex);
        }
        String config = stringBuilder.toString();
        return config.isEmpty() ? new JSONObject() : new JSONObject(config);
    }

    public static JSONObject getConfig() {
        return CONFIG;
    }

    public static void saveConfig() {
        try {
            FileWriter f2 = new FileWriter("config.json", false);
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
        return TimeUnit.MILLISECONDS.convert(INACTIVE_DAYS_TIMER(), TimeUnit.DAYS);
    }

    public static String APP_TOKEN() {
        return APP_TOKEN;
    }

    public static String BOT_TOKEN() {
        return BOT_TOKEN;
    }

    public static String CMD_PREFIX() {
        return CONFIG.getString("command_prefix");
    }

    public static long COMMAND_CHANNEL() {
        return CONFIG.getLong("command_channel");
    }

    public static long LOG_CHANNEL() {
        return CONFIG.getLong("log_channel");
    }

    public static long GUILD_ID() {
        return GUILD_ID;
    }

    public static long INACTIVE_ROLE() {
        return CONFIG.getLong("inactive_role");
    }

    public static long ONLINE_ROLE() {
        return CONFIG.getLong("online_role");
    }

    public static long INACTIVE_DAYS_TIMER() {
        return CONFIG.getLong("inactive_days_timer");
    }

    public static String API_ENTRY_POINT() {
        return CONFIG.getString("api_entry_point");
    }

    public static String APP_ENTRY_POINT() {
        return CONFIG.getString("app_entry_point");
    }

    public static int WEB_PORT() {
        return CONFIG.getInt("web_port");
    }

    public static int TOKEN_VALID_TIME() {
        return CONFIG.getInt("token_valid_time");
    }

}