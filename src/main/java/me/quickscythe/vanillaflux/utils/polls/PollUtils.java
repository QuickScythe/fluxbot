package me.quickscythe.vanillaflux.utils.polls;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.List;

public class PollUtils {

    static List<Poll> polls = new ArrayList<>();

    public static Poll createPoll(TextChannel channel, String question, long duration, List<PollOption> options){
        Poll poll = new Poll(channel, question, duration, options);
        polls.add(poll);
        return poll;
    }

    public static List<Poll> getPolls(){
        return polls;
    }


}
