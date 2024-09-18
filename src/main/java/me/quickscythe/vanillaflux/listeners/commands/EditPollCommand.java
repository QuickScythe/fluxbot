package me.quickscythe.vanillaflux.listeners.commands;

import me.quickscythe.vanillaflux.utils.polls.Poll;
import me.quickscythe.vanillaflux.utils.polls.PollUtils;
import me.quickscythe.vanillaflux.utils.sql.SqlDatabase;
import me.quickscythe.vanillaflux.utils.sql.SqlUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EditPollCommand extends CustomCommand {
    public EditPollCommand(Guild guild, String label, String desc, OptionData... options) {
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
            char optionKey = event.getOption("option").getAsString().charAt(0);
            String newOption = event.getOption("newoption").getAsString();
            poll.getOptions().get(optionKey).setAnswer(newOption);
            poll.update();
            event.reply("Option changed").setEphemeral(true).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if(event.getName().equals(getLabel())){
            List<Command.Choice> completions = new ArrayList<>();
            if(event.getFocusedOption().getName().equals("option")) {
                for (Poll poll : PollUtils.getPolls()) {
                    if (poll.isOpen()) {
                        for (char key : poll.getOptions().keySet()) {
                            Command.Choice choice = new Command.Choice(poll.getOptions().get(key).getAnswer(), key + "");
                            if ((key + "").startsWith(event.getFocusedOption().getValue()))
                                completions.add(choice);
                            if (poll.getOptions().get(key).getAnswer().startsWith(event.getFocusedOption().getValue()))
                                if (!completions.contains(choice))
                                    completions.add(new Command.Choice(poll.getOptions().get(key).getAnswer(), key + ""));
                        }
                    }
                }
            }
            if(event.getFocusedOption().getName().equals("id")) {
                for (Poll poll : PollUtils.getPolls()) {
                    if (poll.isOpen()) {
                        Command.Choice choice = new Command.Choice(poll.getQuestion(), poll.getUid() + "");
                        if ((poll.getUid() + "").startsWith(event.getFocusedOption().getValue()))
                            completions.add(choice);
                        if (poll.getQuestion().startsWith(event.getFocusedOption().getValue()))
                            if (!completions.contains(choice))
                                completions.add(new Command.Choice(poll.getQuestion(), poll.getUid() + ""));
                    }
                }

            }
            event.replyChoices(completions).queue();
        }
    }
}
