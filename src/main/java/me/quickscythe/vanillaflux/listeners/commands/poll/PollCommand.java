package me.quickscythe.vanillaflux.listeners.commands.poll;

import me.quickscythe.vanillaflux.listeners.commands.CustomCommand;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.polls.PollOption;
import me.quickscythe.vanillaflux.utils.polls.PollUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class PollCommand extends CustomCommand {

    private final char[] ALPHABET = "0ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public PollCommand(Guild guild, String label, String desc, OptionData... options) {

        super(guild, label, desc, options);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(getLabel())) {
            String question = event.getOption("question").getAsString();
            long dur = Utils.parseDuration(event.getOption("duration").getAsString());
            List<PollOption> answers = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                if (event.getOption("answer" + i) != null) {
                    answers.add(new PollOption(ALPHABET[i], event.getOption("answer" + i).getAsString()));
                }
            }
            event.deferReply().queue();
            PollUtils.createPoll(event.getHook(), event.getChannel().asTextChannel(), question, dur, answers);
        }
    }

}
