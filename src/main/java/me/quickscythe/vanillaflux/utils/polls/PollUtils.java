package me.quickscythe.vanillaflux.utils.polls;

import json2.JSONObject;
import me.quickscythe.vanillaflux.utils.UID;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class PollUtils {

    static Map<Long, Poll> polls = new HashMap<>();
    static File pollFolder = new File("polls");

    public static void init(){
        if(!pollFolder.exists()) pollFolder.mkdirs();
        for(File file : pollFolder.listFiles()){
            if(!file.getName().endsWith(".json")) continue;
            try {

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    Poll poll = new Poll(Long.parseLong(file.getName().replaceAll("\\.json", "")), new JSONObject(content.toString()));
                    polls.put(poll.getUid(), poll);
                    System.out.println("File content: " + content.toString());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

//    static List<Poll> polls = new ArrayList<>();

    public static Poll createPoll(InteractionHook hook, TextChannel channel, String question, long duration, List<PollOption> options){
        Poll poll = new Poll(hook, channel, question, duration, options);
        polls.put(poll.getUid(), poll);
        return poll;
    }

    public static Collection<Poll> getPolls(){
        return polls.values();
    }

    public static Poll getPoll(UID uid){
        return polls.getOrDefault(uid, null);
    }

    public static File getPollFolder() {
        return pollFolder;
    }


}
