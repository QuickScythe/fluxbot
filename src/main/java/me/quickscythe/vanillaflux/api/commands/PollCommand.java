package me.quickscythe.vanillaflux.api.commands;

import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.polls.Poll;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class PollCommand extends CustomCommand {
    public PollCommand(Guild guild, String label, String desc, OptionData... options) {

        super(guild, label, desc, options);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(getLabel())) {
            String question = event.getOption("question").getAsString();
            long dur = Utils.parseDuration(event.getOption("duration").getAsString());
            List<String> answers = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                if (event.getOption("answer" + i) != null) {
                    answers.add(event.getOption("answer" + i).getAsString());
                }
            }

            Poll poll = new Poll(event.getChannel().asTextChannel(), question, dur, answers.toArray(new String[0]));
            event.deferReply().queue();
            poll.open(event.getHook());
//            event.getOption("question").getAsString();
//            event.reply("Hello").complete();
        }
    }

}
