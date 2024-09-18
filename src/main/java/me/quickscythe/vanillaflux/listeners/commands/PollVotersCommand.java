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

public class PollVotersCommand extends CustomCommand {

    private final char[] ALPHABET = "0ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public PollVotersCommand(Guild guild, String label, String desc, OptionData... options) {

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
            StringBuilder sbuilder = new StringBuilder();
            sbuilder.append("Current votes for poll ").append(poll.getUid()).append(":\n");
            for(PollOption opt : poll.getOptions().values()){
                sbuilder.append(opt.getAnswer()).append(":").append("\n");
                for(long uid : opt.getVoteList()){
                    sbuilder.append(" ").append(Objects.requireNonNull(Utils.getGuild().getMemberById(uid)).getAsMention()).append("\n");
                }
            }

            event.reply(sbuilder.toString()).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals(getLabel()) && event.getFocusedOption().getName().equals("id")) {
            String[] words = PollUtils.getPolls().stream()
                    .map(poll -> poll.getUid() + "")
                    .toArray(String[]::new);
            List<Command.Choice> options = Stream.of(words)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                    .map(word -> new Command.Choice(word, word)) // map the words to choices
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }

}
