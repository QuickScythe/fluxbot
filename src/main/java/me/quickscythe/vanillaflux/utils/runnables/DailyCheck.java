package me.quickscythe.vanillaflux.utils.runnables;

import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.Utils;

import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class DailyCheck extends TimerTask {

    private static long last_search = 0L;


    public DailyCheck() {
    }

    @Override
    public void run() {

        Utils.getLogger().attemptQueue();
        long now = new Date().getTime();
        if (now - last_search >= Utils.convertTime(12, TimeUnit.HOURS)) {
            last_search = now;
            Utils.getLogger().log("Checking for inactive users...", !Bot.isDebug());
            Utils.runInactiveSearch();
        }
    }
}
