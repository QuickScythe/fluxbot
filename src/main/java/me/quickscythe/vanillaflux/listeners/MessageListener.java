package me.quickscythe.vanillaflux.listeners;

import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.sql.SqlDatabase;
import me.quickscythe.vanillaflux.utils.sql.SqlUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().equals(Utils.getCommandsChannel())) return;
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw();
        String cmd = content.toLowerCase().split(" ")[0];
        String[] args = content.split(" ");
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (cmd.equals(Bot.CMD_PREFIX + "runsearch")) {
            event.getChannel().sendMessage("Running inactive search now.").queue();
            Utils.runInactiveSearch();
        }

        if (cmd.equals(Bot.CMD_PREFIX + "linkdiscord")) {
            MessageChannel channel = event.getChannel();
            if (args.length >= 2) {
                String key = args[1];
                SqlDatabase core = SqlUtils.getDatabase("core");
                try {
                    ResultSet rs = core.query("SELECT * FROM users WHERE discord_key='" + key + "';");
                    if (!rs.next()) {
                        channel.sendMessage("That key couldn't be found. These are case sensitive, please check your key and try again.").queue();
                    } else {
                        if (rs.getString("discord_id").equalsIgnoreCase("null")) {
                            channel.sendMessage(event.getAuthor().getAsMention() + " linked to " + rs.getString("username") + ".").queue();
                            core.update("UPDATE users SET discord_id='" + event.getAuthor().getId() + "' WHERE discord_key='" + key + "';");

                        } else channel.sendMessage("Your account has already been linked.").queue();
                        channel.deleteMessageById(event.getMessageId()).complete();
                    }
                } catch (SQLException e) {
                    channel.sendMessage("There was an error. Please check your key as it is case-sensitive. If you continue to run into an error please reach out to QuickScythe.").queue();
                    throw new RuntimeException(e);
                }
            } else {
                channel.sendMessage("**Usage**: `!linkdiscord <key>`").queue();
                channel.sendMessage("If you don't have a key, please join the server and type `/discord`.").queue();
            }
        }
    }
}
