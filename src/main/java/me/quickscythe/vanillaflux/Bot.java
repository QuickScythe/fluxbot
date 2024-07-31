package me.quickscythe.vanillaflux;

import me.quickscythe.vanillaflux.listeners.MessageListener;
import me.quickscythe.vanillaflux.utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

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
    private static final String BOT_TOKEN = "NzQ3NjcyNjU1OTUxNDI5Njcy.Gx4qa-.vR4pIbBzV0u3tFmNOE8fIei2iqJZqnIBns7Yg8";

    public static void main(String[] args) {
        JDA api = JDABuilder.createDefault(BOT_TOKEN, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).build();
        Utils.init(api);
        api.addEventListener(new MessageListener());

    }

    public static long getInactiveEpochTime() {
        return TimeUnit.MILLISECONDS.convert(INACTIVE_DAYS_TIMER, TimeUnit.DAYS);
    }
}