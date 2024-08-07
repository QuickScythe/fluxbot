package me.quickscythe.vanillaflux.utils.logs;

import me.quickscythe.vanillaflux.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BotLogger {
    private final Logger LOG;
    Map<Long, QueuedLog> queue = new HashMap<>();

    public BotLogger(String name) {
        this.LOG = LoggerFactory.getLogger(name);
    }

    public Logger getLog() {
        return LOG;
    }

    public void log(String message) {
        log(message, false);
    }

    public void log(String message, boolean logToDiscord) {
        try {
            getLog().info(message);
            if (logToDiscord)
                Utils.getLogsChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.GREEN).setTitle("INFO").setDescription(message).build()).queue();
        } catch (Exception ex) {
            queue.put(new Date().getTime(), new QueuedLog(LogLevel.INFO, message, logToDiscord));
        }
    }

    public void warn(String message) {
        warn(message, false);
    }

    public void warn(String message, boolean logToDiscord) {
        try {
            getLog().warn(message);
            if (logToDiscord)
                Utils.getLogsChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.ORANGE).setTitle("WARN").setDescription(message).build()).queue();
        } catch (Exception ex) {
            queue.put(new Date().getTime(), new QueuedLog(LogLevel.WARN, message, logToDiscord));
        }
    }

    public void error(String message) {
        error(message, "");
    }

    public void error(String message, String logToDiscord) {
        try {
            getLog().error(message);
            if (!logToDiscord.isEmpty() && !message.isEmpty())
                Utils.getLogsChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle("ERROR").setDescription(logToDiscord.equals("=") ? message : logToDiscord).build()).queue();
        } catch (Exception ex) {
            queue.put(new Date().getTime(), new QueuedLog(LogLevel.ERROR, message, !logToDiscord.isEmpty()));
        }
    }

    public void error(String message, Exception ex) {
        error(message.isEmpty() ? ex.getMessage() : message, "=");
        for (StackTraceElement el : ex.getStackTrace())
            error(el.toString());
//        Utils.getLogsChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle("ERROR").setDescription(message).build()).queue();
    }

    public void attemptQueue() {
        if (!queue.isEmpty()) {
            log("Attempting to dump queued logs", true);
            for (Map.Entry<Long, QueuedLog> entry : queue.entrySet()) {
                long time = entry.getKey();
                String message = entry.getValue().message;
                boolean logToDiscord = entry.getValue().logToDiscord;
                LogLevel level = entry.getValue().level;

                message = "[QUEUED] " + message;

                switch (level) {
                    case INFO -> log(message, logToDiscord);
                    case WARN -> warn(message, logToDiscord);
                    case ERROR -> error(message, logToDiscord ? "=" : "");
                }
            }
            queue.clear();
        }
    }

    private enum LogLevel {
        INFO, WARN, ERROR;
    }

    private class QueuedLog {
        String message;
        LogLevel level;
        boolean logToDiscord;

        private QueuedLog(LogLevel level, String message, boolean logToDiscord) {
            this.level = level;
            this.message = message;
            this.logToDiscord = logToDiscord;
        }

    }

}
