package me.quickscythe.vanillaflux.listeners;

import json2.JSONArray;
import json2.JSONObject;
import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.UID;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.polls.Poll;
import me.quickscythe.vanillaflux.utils.polls.PollOption;
import me.quickscythe.vanillaflux.utils.polls.PollUtils;
import me.quickscythe.vanillaflux.utils.sql.SqlDatabase;
import me.quickscythe.vanillaflux.utils.sql.SqlUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().equals(Utils.getCommandsChannel()) && !event.getChannel().equals(Utils.getLogsChannel())) return;
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw();
        String cmd = content.toLowerCase().split(" ")[0];
        String[] args = content.split(" ");
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (cmd.equals(Bot.CMD_PREFIX() + "runsearch")) {
            event.getChannel().sendMessage("Running inactive search now.").queue();
            Utils.runInactiveSearch();
        }

        if (cmd.equals(Bot.CMD_PREFIX() + "closepoll")) {
            if (args.length < 2) {
                event.getChannel().sendMessage("Usage: `" + Bot.CMD_PREFIX() + "closepoll <uid>`").queue();
                return;
            }
            String uid = args[1];
            PollUtils.getPoll(Long.parseLong(uid)).close();
        }

        if (cmd.equals(Bot.CMD_PREFIX() + "update")) {
            Utils.getLogger().log("Updating the bot now.", true);
            Utils.update();

        }
        if(cmd.equals(Bot.CMD_PREFIX() + "allow")){
            if(!Bot.getConfig().has("allow")){
                Bot.getConfig().put("allow", new JSONArray());
            }
            if(args.length < 2){
                event.getChannel().sendMessage("Usage: `" + Bot.CMD_PREFIX() + "allow <ip>`").queue();
                return;
            }

            Bot.getConfig().getJSONArray("allow").put(args[1]);
            Bot.saveConfig();
        }

        if (cmd.equals(Bot.CMD_PREFIX() + "linkdiscord")) {
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
