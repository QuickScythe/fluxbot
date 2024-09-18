package me.quickscythe.vanillaflux.utils.polls;

import json2.JSONObject;
import me.quickscythe.vanillaflux.utils.UID;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

import static me.quickscythe.vanillaflux.utils.Utils.newUID;

public class PollOption {
    private String answer;
    private UID id;
    private Button button;
    private List<Long> votes = new ArrayList<>();

    public PollOption(JSONObject data){
        answer = data.getString("answer");
        for(int i=0; i<data.getJSONArray("votes").length(); i++){
            votes.add(data.getJSONArray("votes").getLong(i));
        }
        id = newUID();
    }

    public PollOption(String answer) {
        this.answer = answer;
        id = newUID();
    }

    public String getAnswer() {
        return answer;
    }

    public UID getUid() {
        return id;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public String getProgressBar(double percent) {
        if(percent == 0) percent = 0.01;
        int totalBars = 20; // Total number of bars in the progress bar
        StringBuilder progressBar = new StringBuilder();
        progressBar.append("`");
        for(int i=0; i<totalBars; i++){
            double barPercent = ((double) i /totalBars)*100;
            if(barPercent >= percent) {
                progressBar.append(" ");
            } else {
                progressBar.append("█");
            }
        }
        progressBar.append("`");
        return progressBar.toString();
    }

    public int getVotes() {
        return votes.size();
    }

    public List<Long> getVoteList() {
        return votes;
    }

    public void vote(User user) {
        System.out.println("Voting for " + user.getName() + " on " + answer);
        if(votes.contains(user.getIdLong())) return;
        votes.add(user.getIdLong());
    }
}
