package me.quickscythe.vanillaflux.listeners.commands;

import me.quickscythe.vanillaflux.utils.sql.SqlDatabase;
import me.quickscythe.vanillaflux.utils.sql.SqlUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LinkCommand extends CustomCommand {
    public LinkCommand(Guild guild, String label, String desc, OptionData... options) {
        super(guild, label, desc, options);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(getLabel())) {
            event.deferReply().queue();
            String key = event.getOption("key").getAsString();
            SqlDatabase core = SqlUtils.getDatabase("core");
            try {
                ResultSet rs = core.query("SELECT * FROM users WHERE discord_key='" + key + "';");
                if (!rs.next()) {
                    event.getHook().sendMessage("That key couldn't be found. These are case sensitive, please check your key and try again.").setEphemeral(true).queue();
                } else {
                    if (rs.getString("discord_id").equalsIgnoreCase("null")) {
                        event.getHook().sendMessage(event.getUser().getAsMention() + " linked to " + rs.getString("username") + ".").queue();
                        core.update("UPDATE users SET discord_id='" + event.getUser().getId() + "' WHERE discord_key='" + key + "';");

                    } else event.getHook().sendMessage("Your account has already been linked.").setEphemeral(true).queue();

                }
            } catch (SQLException e) {
                event.getHook().sendMessage("There was an error. Please check your key as it is case-sensitive. If you continue to run into an error please reach out to QuickScythe.").setEphemeral(true).queue();
                throw new RuntimeException(e);
            }
        }
    }
}
