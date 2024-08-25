package me.quickscythe.vanillaflux;

import com.sun.net.httpserver.HttpServer;
import me.quickscythe.vanillaflux.listeners.MessageListener;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.sql.SqlUtils;
import me.quickscythe.vanillaflux.webapp.PlayerApiHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Bot {


    public static final String CMD_PREFIX = "?";
    public static final long COMMAND_CHANNEL = 1268045246512758835L;
    public static final long LOG_CHANNEL = 1268006180626628690L;
    public static final long GUILD_ID = 1140468525190877206L;
    public static final long INACTIVE_ROLE = 1226923455648104559L;
    public static final long INACTIVE_DAYS_TIMER = 90;
    private static String BOT_TOKEN;

    public static void main(String[] args) {
        Utils._before_init();
        BOT_TOKEN = loadToken();
        JDA api = JDABuilder.createDefault(BOT_TOKEN, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).build();
        Utils.init(api);
        api.addEventListener(new MessageListener());

        launchApi();

    }

    private static void launchApi() {
        try {
            int port = 8585;
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            ResultSet rs = SqlUtils.getDatabase("core").query("SELECT * FROM users");
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                String player = rs.getString("username");
//                Utils.log("Creating context for " + player);
                server.createContext("/" + uuid, new PlayerApiHandler(uuid));
                server.createContext("/" + player, new PlayerApiHandler(uuid));
            }


            server.setExecutor(null); // creates a default executor
            server.start();
            Utils.getLogger().log("Web Server started on port " + port, true);


        } catch (IOException | SQLException ex) {
            Utils.getLogger().warn("There was an error setting up the Web Server", true);
            Utils.getLogger().error("",ex);
        }
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
        return stringBuilder.toString();
    }

    public static long getInactiveEpochTime() {
        return TimeUnit.MILLISECONDS.convert(INACTIVE_DAYS_TIMER, TimeUnit.DAYS);
    }
}