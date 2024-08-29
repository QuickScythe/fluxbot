package me.quickscythe.vanillaflux.utils.runnables;

import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.Utils;

import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Heartbeat extends TimerTask {

    private static long daily_check = 0L;
    private static long config_check = 0L;
    private static long token_check = 0L;


    public Heartbeat() {
    }

    @Override
    public void run() {

        Utils.getLogger().attemptQueue();
        long now = new Date().getTime();
        if (now - daily_check >= Utils.convertTime(12, TimeUnit.HOURS)) {
            daily_check = now;
            Utils.getLogger().log("Checking for inactive users...", !Bot.isDebug());
            Utils.runInactiveSearch();
        }
        if (now - config_check >= Utils.convertTime(5, TimeUnit.MINUTES)) {
            config_check = now;
            Bot.saveConfig();
        }
        if (now - token_check >= Utils.convertTime(Bot.TOKEN_VALID_TIME(), TimeUnit.HOURS)) {
            token_check = now;
            Utils.getLogger().log("Checking for expired tokens...", !Bot.isDebug());
            Utils.runTokenCheck();
        }
    }
}
