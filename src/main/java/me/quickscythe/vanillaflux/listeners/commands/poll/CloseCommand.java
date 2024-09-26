package me.quickscythe.vanillaflux.listeners.commands.poll;

import me.quickscythe.vanillaflux.listeners.commands.CustomCommand;
import me.quickscythe.vanillaflux.utils.polls.Poll;
import me.quickscythe.vanillaflux.utils.polls.PollUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class CloseCommand extends CustomCommand {

    private final char[] ALPHABET = "0ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public CloseCommand(Guild guild, String label, String desc, OptionData... options) {

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
            poll.close();
            event.reply("Poll closed").setEphemeral(true).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals(getLabel()) && event.getFocusedOption().getName().equals("id")) {

            List<Command.Choice> completions = new ArrayList<>();
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
            event.replyChoices(completions).queue();
        }
    }

}
