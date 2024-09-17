package me.quickscythe.vanillaflux.utils.polls;

import me.quickscythe.vanillaflux.utils.UID;
import me.quickscythe.vanillaflux.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static me.quickscythe.vanillaflux.utils.Utils.newUID;

public class Poll {

    UID uid;
    Message pollMessage;
    TextChannel channel;
    private long duration;
    private String question;
    private String[] answers;
    private int[] votes;
    private boolean open;

    public Poll(TextChannel channel, String question, long duration, String[] answers){
        this.channel = channel;
        this.question = question;
        this.duration = duration;
        this.answers = answers;
        this.votes = new int[answers.length];
        this.open = false;
        uid = newUID();
    }

    public void open(InteractionHook hook){
        open = true;
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.CYAN);
        builder.setTitle(question);
        List<Button> buttons = new ArrayList<>();
        for(int i = 0; i < answers.length; i++){
            //use this unicode character to make the poll look better (█)
            builder.addField(answers[i], "`               ` | 0% (0)", false);
            buttons.add(Button.of(ButtonStyle.PRIMARY, String.valueOf(i), answers[i]));
        }
//        Button btn = Button.of(ButtonStyle.SUCCESS, "1", answers[1]);
        hook.sendMessageEmbeds(builder.build()).addActionRow(buttons).queue();

    }

    class Option {
        String option;
        UID id;

    }

    class Vote {
        UID uid;
        Option option;
        User voter;
    }

}
