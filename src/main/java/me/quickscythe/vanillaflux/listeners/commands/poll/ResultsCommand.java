package me.quickscythe.vanillaflux.listeners.commands.poll;

import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.listeners.commands.CustomCommand;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.polls.Poll;
import me.quickscythe.vanillaflux.utils.polls.PollOption;
import me.quickscythe.vanillaflux.utils.polls.PollUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ResultsCommand extends CustomCommand {


    public ResultsCommand(Guild guild, String label, String desc, OptionData... options) {
        super(guild, label, desc, options);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(getLabel())) {
            long id = event.getOption("id").getAsLong();
            Poll poll = PollUtils.getPoll(id);
            if (poll == null) {
                event.reply("Poll not found").queue();
                return;
            }
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Current votes for poll " + poll.getUid());
            builder.setColor(poll.getColor());
            for (PollOption option : poll.getOptions().values()) {
                builder.addField("(" + option.getId() + ") " + option.getAnswer(), option.getVotes() + " votes", false);
            }
            try {
                InputStream file = URI.create("http://localhost:" + Bot.WEB_PORT() + Bot.API_ENTRY_POINT() + "/polls/" + poll.getUid() + ".png").toURL().openStream();
                builder.setImage("attachment://" + poll.getUid() + ".png") // we specify this in sendFile as "cat.png"
                        .setDescription("Poll Results for " + poll.getQuestion());
//            channel.sendFiles(FileUpload.fromData(file, uid + ".png"))
//                    .setEmbeds(builder.build()).queue();
                event.replyEmbeds(builder.build()).addFiles(FileUpload.fromData(file, poll.getUid() + ".png")).setEphemeral(true).queue();
            } catch (IOException e) {
                Utils.getLogger().error("Error", e);
                builder.addField("Error", "Couldn't load image.", false);
                event.replyEmbeds(builder.build()).setEphemeral(true).queue();

            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals(getLabel()) && event.getFocusedOption().getName().equals("id")) {

            List<Command.Choice> completions = new ArrayList<>();
            for (Poll poll : PollUtils.getPolls()) {
                if(completions.size() > 24) break;
                Command.Choice choice = new Command.Choice(poll.getQuestion(), poll.getUid() + "");
                if ((poll.getUid() + "").startsWith(event.getFocusedOption().getValue()))
                    completions.add(choice);
                if (poll.getQuestion().startsWith(event.getFocusedOption().getValue()))
                    if (!completions.contains(choice))
                        completions.add(new Command.Choice(poll.getQuestion(), poll.getUid() + ""));
            }
            event.replyChoices(completions).queue();
        }
    }
}
