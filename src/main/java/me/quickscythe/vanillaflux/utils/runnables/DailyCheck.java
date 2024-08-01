package me.quickscythe.vanillaflux.utils.runnables;

import me.quickscythe.vanillaflux.utils.Utils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class DailyCheck extends TimerTask {

    private static long last_search = 0L;


    public DailyCheck() {
    }

    @Override
    public void run() {

        Utils.log("Ba-dump");
        long now = new Date().getTime();
        if (now - last_search >= Utils.convertTime(24, TimeUnit.HOURS)) {
            last_search = now;
            Utils.log("Searching");
            Utils.runInactiveSearch();
        }
    }
}
