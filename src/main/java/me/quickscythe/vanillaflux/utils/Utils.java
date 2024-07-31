package me.quickscythe.vanillaflux.utils;

import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.runnables.DailyCheck;
import me.quickscythe.vanillaflux.utils.sql.SqlDatabase;
import me.quickscythe.vanillaflux.utils.sql.SqlUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class Utils {

        private static SqlDatabase core;
    private static JDA api;

    public static void init(JDA api) {
        Utils.api = api;
        SqlUtils.createDatabase("core", new SqlDatabase(SqlUtils.SQLDriver.MYSQL, "sql.vanillaflux.com", "vanillaflux", 3306, "sys", "9gGKGqthQJ&!#DGd"));
        core = SqlUtils.getDatabase("core");
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new DailyCheck(), convertTime(10,TimeUnit.SECONDS), convertTime(10, TimeUnit.SECONDS));
//        timer.schedule(new DailyCheck(timer), convertTime(10, TimeUnit.SECONDS));
    }

    public static JDA getApi() {
        return api;
    }

    public static Guild getGuild() {
        return api.getGuildById(Bot.GUILD_ID);
    }

    public static TextChannel getLogsChannel() {
        return getGuild().getChannelById(TextChannel.class, Bot.LOG_CHANNEL);
    }

    public static TextChannel getCommandsChannel() {
        return getGuild().getChannelById(TextChannel.class, Bot.COMMAND_CHANNEL);
    }

    public static long convertTime(int duration, TimeUnit timeUnit) {
        return TimeUnit.MILLISECONDS.convert(duration, timeUnit);
    }

    public static Role getInactiveRole() {
        return getGuild().getRoleById(Bot.INACTIVE_ROLE);
    }

    public static void runInactiveSearch() {
        ResultSet rs = SqlUtils.getDatabase("core").query("SELECT * FROM users WHERE discord_id <> 'null';");
        try {
            long now = new Date().getTime();
            Role role = Utils.getInactiveRole();
            if (role == null) {
                Utils.getLogsChannel().sendMessage("There was an error finding the `inactive` role.").queue();
                return;
            }
            while (rs.next()) {
                long userId;
                try {
                    userId = Long.parseLong(rs.getString("discord_id"));
                } catch (NumberFormatException ex) {
                    userId = -1;
                }
                if (userId == -1) {
                    Utils.getLogsChannel().sendMessage("Error finding user. (" + rs.getString("discord_id") + ")").queue();
                    return;
                }
                Member member = getGuild().retrieveMemberById(userId).complete();
                if (now - Long.parseLong(rs.getString("last_seen")) >= Bot.getInactiveEpochTime()) {
                    if (member != null) {
                        getGuild().addRoleToMember(member, role).complete();
                        Utils.getLogsChannel().sendMessage(member.getEffectiveName() + " has been made inactive.").queue();
                    } else getCommandsChannel().sendMessage("Error..").queue();
                } else {

                    if (member.getRoles().contains(role)) {
                        getGuild().removeRoleFromMember(member, role).complete();
                        Utils.getLogsChannel().sendMessage(member.getEffectiveName() + " has been made active again").queue();
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
