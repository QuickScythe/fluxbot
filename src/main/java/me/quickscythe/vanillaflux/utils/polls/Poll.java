package me.quickscythe.vanillaflux.utils.polls;

import json2.JSONArray;
import json2.JSONObject;
import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.polls.charts.PieChartGenerator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.*;

public class Poll {

    long started = 0;
    private long uid = 0;
    private TextChannel channel = null;
    private long duration = 0;
    private String question = "?";
    private EmbedBuilder pollMessage;
    private List<PollOption> options = new ArrayList<>();
    private boolean open;

    public Poll(long uid, JSONObject object) throws Exception {

        this.channel = Utils.getGuild().getTextChannelById(object.getString("channel"));
        this.question = object.getString("question");
        this.duration = object.getLong("duration");
        this.started = object.getLong("started");
        this.open = object.getBoolean("open");
        JSONArray options = object.getJSONArray("options");
        for (int i = 0; i < options.length(); i++) {
            JSONObject option = options.getJSONObject(i);
            PollOption pollOption = new PollOption(option);
            this.options.add(pollOption);
        }
        channel.retrieveMessageById(uid).queue(message -> {
            for (ActionRow row : message.getActionRows()) {
                for (Button button : row.getButtons()) {
                    for (PollOption option : this.options) {
                        if (button.getId().equals("poll_button-" + this.started + option.getAnswer())) {
                            option.setButton(button);
                        }
                    }
                }
            }
            pollMessage = new EmbedBuilder();
            pollMessage.setColor(Color.RED);
            pollMessage.setTitle(question);
            int total = getTotalVotes();
            for (PollOption answer : this.options) {
                int votes = answer.getVotes();
                double percent = ((((double) votes) / ((double) total)) * 100D);
                pollMessage.addField(answer.getAnswer(), answer.getProgressBar(percent) + "  |  **" + percent + "%**  _(" + votes + ")_", false);

            }
            pollMessage.setFooter("Votes: " + total);
            message.editMessageEmbeds(pollMessage.build()).queue();

        });

    }

    public Poll(InteractionHook hook, TextChannel channel, String question, long duration, PollOption... options) {
        this.channel = channel;
        this.question = question;
        this.duration = duration;
        this.options.addAll(Arrays.asList(options));
        this.open = false;
        open(hook);
    }

    public Poll(InteractionHook hook, TextChannel channel, String question, long duration, List<PollOption> options) {
        this.channel = channel;
        this.question = question;
        this.duration = duration;
        this.options = options;
        this.open = false;

        open(hook);

    }

    private void open(InteractionHook hook) {
        open = true;
        this.started = new Date().getTime();
        pollMessage = new EmbedBuilder();
        pollMessage.setColor(Color.CYAN);
        pollMessage.setTitle(question);
        List<Button> buttons = new ArrayList<>();
        for (PollOption answer : options) {
            int votes = answer.getVotes();
            double percent = ((((double) votes) / ((double) getTotalVotes())) * 100D);
            pollMessage.addField(answer.getAnswer(), answer.getProgressBar(0) + "  |  **" + 0 + "%**  _(" + votes + ")_", false);
            Button button = Button.of(ButtonStyle.SUCCESS, "poll_button-" + this.started + answer.getAnswer(), answer.getAnswer());
            answer.setButton(button);
            buttons.add(button);
        }
        uid = hook.sendMessageEmbeds(pollMessage.build()).addActionRow(buttons).complete().getIdLong();
        save();
    }


    public List<PollOption> getOptions() {
        return options;
    }

    public String getQuestion() {
        return question;
    }

    public void vote(ButtonInteractionEvent event, PollOption userAnswer, User user) {
        if (!open) {
            event.reply("This poll is closed.").setEphemeral(true).queue();
            return;
        }
        if (Utils.getGuild().getMemberById(user.getId()).getRoles().contains(Utils.getGuild().getRoleById(Bot.INACTIVE_ROLE()))) {
            event.reply("You cannot vote in polls while inactive. Please join the server to get your active role back.").setEphemeral(true).queue();
            return;
        }
        for(PollOption option : options){
            if(option.getVoteList().contains(user.getIdLong())){
                option.getVoteList().remove(user.getIdLong());
            }
        }
        pollMessage.clearFields();
        userAnswer.vote(user);
        int total = getTotalVotes();
        for (PollOption answer : options) {
            System.out.println("Checking option: " + answer.getAnswer());
            int votes = answer.getVotes();
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


    private int getTotalVotes() {
        int votes = 0;
        for (PollOption option : options) {
            votes += option.getVotes();
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
        save();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Poll Closed (" + question + ")");
//        builder.addField("poll-" + uid, "This poll has been closed. Results are as follows:", false);
        try {
            InputStream file = URI.create("http://localhost:" + Bot.WEB_PORT() + Bot.API_ENTRY_POINT() + "/polls/" + uid + ".png").toURL().openStream();
            builder.setImage("attachment://" + uid + ".png") // we specify this in sendFile as "cat.png"
                    .setDescription("Poll Results");
            channel.sendFiles(FileUpload.fromData(file, uid + ".png")).setEmbeds(builder.build()).queue();
//            builder.setImage("http://localhost:8585/api/polls/1285785724523909203.png");
        } catch (IOException e) {
            Utils.getLogger().error("Error", e);
            builder.addField("Error", "Results could not be found", false);
            channel.sendMessageEmbeds(builder.build()).queue();
        }


//        Utils.getLogger().log("Poll closed: " + question + "", true);

    }

    public void save() {
        File file = new File(PollUtils.getPollFolder() + "/" + uid + ".json");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        JSONObject object = json();
        Map<String, Integer> data = new HashMap<>();
        for (PollOption option : options) {
            data.put(option.getAnswer(), option.getVotes());
        }

        PieChartGenerator.generatePieChart("Poll Results", data, PollUtils.getPollFolder() + "/" + getUid() + ".png");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(object.toString(2));
            Utils.getLogger().log("Poll saved: " + question);
            System.out.println("File written successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private JSONObject json() {
        JSONObject object = new JSONObject();
        object.put("question", question);
        object.put("duration", duration);
        object.put("started", started);
        object.put("channel", channel.getId());
        JSONArray options = new JSONArray();
        for (PollOption option : this.options) {
            JSONObject optionObject = new JSONObject();
            optionObject.put("answer", option.getAnswer());
            JSONArray votes = new JSONArray();
            for (Long vote : option.getVoteList()) {
                votes.put(vote);
            }
            optionObject.put("votes", votes);
            options.put(optionObject);
        }
        object.put("options", options);
        object.put("open", open);
        return object;
    }

    public TextChannel getChannel() {
        return channel;
    }

    public long getUid() {
        return uid;
    }
}
