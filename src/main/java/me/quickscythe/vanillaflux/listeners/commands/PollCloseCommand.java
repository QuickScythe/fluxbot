package me.quickscythe.vanillaflux.listeners.commands;

import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.polls.Poll;
import me.quickscythe.vanillaflux.utils.polls.PollOption;
import me.quickscythe.vanillaflux.utils.polls.PollUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PollCloseCommand extends CustomCommand {

    private final char[] ALPHABET = "0ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public PollCloseCommand(Guild guild, String label, String desc, OptionData... options) {

        super(guild, label, desc, options);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(getLabel())) {
            if(event.getUser().)
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

            List<String> ids = new ArrayList<>();
            for(Poll poll : PollUtils.getPolls()){
                if(poll.isOpen()) ids.add(poll.getUid() + "");
            }

            List<Command.Choice> options = Stream.of(ids.toArray(new String[0]))
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                    .map(word -> new Command.Choice(word, word)) // map the words to choices
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }

}
