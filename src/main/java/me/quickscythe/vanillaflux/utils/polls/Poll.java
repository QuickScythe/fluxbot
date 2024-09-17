package me.quickscythe.vanillaflux.utils.polls;

import me.quickscythe.vanillaflux.utils.UID;
import me.quickscythe.vanillaflux.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;

import static me.quickscythe.vanillaflux.utils.Utils.newUID;

public class Poll {

    UID uid;
    Message pollMessage;
    TextChannel channel;
    private String question;
    private String[] answers;
    private int[] votes;
    private boolean open;

    public Poll(TextChannel channel, String question, String[] answers){
        this.channel = channel;
        this.question = question;
        this.answers = answers;
        this.votes = new int[answers.length];
        this.open = false;
        uid = newUID();
    }

    public void open(){
        open = true;
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.CYAN);
        builder.setTitle(question);
        for(int i = 0; i < answers.length; i++){
            //use this unicode character to make the poll look better (█)
            builder.addField(answers[i], "`               ` | 0% (0)", false);

        }
        channel.sendMessageEmbeds(builder.build()).queue();

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
