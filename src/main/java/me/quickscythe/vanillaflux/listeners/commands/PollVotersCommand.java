package me.quickscythe.vanillaflux.listeners.commands;

import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.polls.Poll;
import me.quickscythe.vanillaflux.utils.polls.PollOption;
import me.quickscythe.vanillaflux.utils.polls.PollUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Current votes for poll " + poll.getUid());
            for (PollOption opt : poll.getOptions().values()) {
                builder.addField(opt.getAnswer() + "(" + opt.getVotes() + ")", opt.getVoteList().stream().map(Objects::requireNonNull).map(Utils.getGuild()::getMemberById).map(Member::getAsMention).collect(Collectors.joining(", ")), false);
            }
            event.replyEmbeds(builder.build()).setEphemeral(true).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals(getLabel()) && event.getFocusedOption().getName().equals("id")) {

            List<Command.Choice> completions = new ArrayList<>();
            for (Poll poll : PollUtils.getPolls()) {
                Command.Choice choice = new Command.Choice(poll.getQuestion(), poll.getUid() + "");
                if ((poll.getUid() + "").startsWith(event.getFocusedOption().getValue()))
                    completions.add(choice);
                if (poll.getQuestion().startsWith(event.getFocusedOption().getValue()))
                    if (!completions.contains(choice))
                        completions.add(new Command.Choice(poll.getQuestion(), poll.getUid() + ""));

//                completions.add(new Command.Choice(poll.getUid() + "", poll.getUid() + ""));
            }
            event.replyChoices(completions).queue();
        }
    }

}
