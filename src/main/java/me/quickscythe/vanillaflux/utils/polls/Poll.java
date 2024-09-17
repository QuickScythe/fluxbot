package me.quickscythe.vanillaflux.utils.polls;

import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.UID;
import me.quickscythe.vanillaflux.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static me.quickscythe.vanillaflux.utils.Utils.newUID;

public class Poll {

    UID uid;
    EmbedBuilder pollMessage;
    TextChannel channel;
    long started = 0;
    private long duration;
    private String question;
    private List<PollOption> options;
    private boolean open;

    public Poll(TextChannel channel, String question, long duration, List<PollOption> options) {
        this.channel = channel;
        this.question = question;
        this.duration = duration;
        this.options = options;
        this.open = false;
        uid = newUID();

    }

    public void open(InteractionHook hook) {
        open = true;
        this.started = new Date().getTime();
        pollMessage = new EmbedBuilder();
        pollMessage.setColor(Color.CYAN);
        pollMessage.setTitle(question);
        List<Button> buttons = new ArrayList<>();
        for (PollOption answer : options) {
            int votes = answer.getVotes("Poll [46]");
            double percent = ((((double) votes) / ((double) getTotalVotes())) * 100D);
            pollMessage.addField(answer.getAnswer(), answer.getProgressBar(0) + "  |  **" + 0 + "%**  _(" + votes + ")_", false);
            Button button = Button.of(ButtonStyle.SUCCESS, "poll_button-" + uid + answer.getUid(), answer.getAnswer());
            answer.setButton(button);
            buttons.add(button);
        }
//        hook.sendMessageEmbeds(builder.build()).addActionRow(buttons).queue();
//        Button btn = Button.of(ButtonStyle.SUCCESS, "1", answers[1]);
        hook.sendMessageEmbeds(pollMessage.build()).addActionRow(buttons).complete().getId();

    }

    public List<PollOption> getOptions() {
        return options;
    }

    public String getQuestion() {
        return question;
    }

    public UID getUid() {
        return uid;
    }

    public void vote(ButtonInteractionEvent event, PollOption userAnswer, User user) {
        if (open) {
            if(Utils.getGuild().getMemberById(user.getId()).getRoles().contains(Utils.getGuild().getRoleById(Bot.INACTIVE_ROLE()))){
                event.reply("You cannot vote in polls while inactive. Please join the server to get your active role back.").setEphemeral(true).queue();
                return;
            }
            pollMessage.clearFields();
            userAnswer.vote(user);
            int total = getTotalVotes();
            for (PollOption answer : options) {
                System.out.println("Checking option: " + answer.getAnswer());
                int votes = answer.getVotes("Poll [75]");
                //use this unicode character to make the poll look better (█)
                double percent = ((((double) votes) / ((double) total)) * 100D);
                pollMessage.addField(answer.getAnswer(), answer.getProgressBar(percent) + "  |  **" + percent + "%**  _(" + votes + ")_", false);
            }
            System.out.println("Total votes: " + total);
            pollMessage.setFooter("Votes: " + total);
            pollMessage.setColor(Color.green);
            event.editMessageEmbeds(pollMessage.build()).queue();
//        hook.editMessageEmbedsById(pollMessageId).applyMessage()
        }
    }

    private int getTotalVotes() {
        int votes = 0;
        for (PollOption option : options) {
            votes += option.getVotes("Poll [90]");
        }
        return votes;
    }


    public boolean isOpen() {
        return open;
    }

    public long getStarted() {
        return started;
    }

    public long getDuration() {
        return duration;
    }

    public void close() {
        open = false;
        Utils.getLogger().log("Poll closed: " + question + " (" + uid + ")", true);
    }
}
