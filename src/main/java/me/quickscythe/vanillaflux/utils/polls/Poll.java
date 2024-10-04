package me.quickscythe.vanillaflux.utils.polls;

import json2.JSONArray;
import json2.JSONObject;
import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.polls.charts.ChartGenerator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class Poll {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy, hh:mm a");
    long started = 0;
    private long uid = 0;
    private TextChannel channel = null;
    private long duration = 0;
    private String question = "?";
    private EmbedBuilder pollMessage;
    private LinkedHashMap<Character, PollOption> options = new LinkedHashMap<>();
    private boolean open;

//    private final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public Poll(long uid, JSONObject object) throws Exception {

        this.channel = Utils.getGuild().getTextChannelById(object.getString("channel"));
        this.question = object.getString("question");
        this.duration = object.getLong("duration");
        this.started = object.getLong("started");
        this.open = object.getBoolean("open");
        this.uid = uid;
        JSONArray options = object.getJSONArray("options");
        for (int i = 0; i < options.length(); i++) {
            JSONObject option = options.getJSONObject(i);
            PollOption pollOption = new PollOption(option);
            this.options.put(pollOption.getId(), pollOption);
        }
        channel.retrieveMessageById(uid).queue(message -> {
            for (ActionRow row : message.getActionRows()) {
                for (Button button : row.getButtons()) {
                    for (PollOption option : this.options.values()) {
                        if (button.getId().equals("poll_button-" + this.started + option.getId())) {
                            option.setButton(button);
                        }
                    }
                }
            }
            pollMessage = new EmbedBuilder();

            buildPollMessage();
//            pollMessage.setFooter("Votes: " + total);
            message.editMessageEmbeds(pollMessage.build()).queue();

        });

    }

    public Poll(InteractionHook hook, TextChannel channel, String question, long duration, List<PollOption> options) {
        this.channel = channel;
        this.question = question;
        this.duration = duration;
        for (PollOption option : options) {
            this.options.put(option.getId(), option);
        }
        this.open = false;

        open(hook);

    }

    public void update() {
        channel.retrieveMessageById(uid).queue(message -> {
            pollMessage = new EmbedBuilder();
            buildPollMessage();
            message.editMessageEmbeds(pollMessage.build()).queue();
            for (Button button : message.getButtons()) {
                for (PollOption option : options.values()) {
                    if (button.getId().equals("poll_button-" + this.started + option.getId())) {
                        option.setButton(button.withLabel(option.getAnswer()));
                    }
                }
            }
        });
    }

    private void buildPollMessage() {
        pollMessage.setTitle(question);
        int total = getTotalVotes();
        for (PollOption answer : this.options.values()) {
            int votes = answer.getVotes();
            double percent = Utils.formatDecimal((((double) votes) / ((double) total)) * 100D);
//            pollMessage.addField(":regional_indicator_" + (answer.getId() + "").toLowerCase() + ": " + answer.getAnswer(), answer.getProgressBar(percent) + "  |  **" + percent + "%**  _(" + votes + ")_", false);
            pollMessage.addField(getOptionField(answer));
        }
        pollMessage.setColor(getColor());
        if (open)
            pollMessage.setFooter("Poll open until " + Utils.formatTime(started + duration) + ". Votes: " + total);
        else pollMessage.setFooter("Poll closed " + Utils.formatTime(started + duration) + ". Votes: " + total);

    }

    private void open(InteractionHook hook) {
        open = true;
        this.started = new Date().getTime();
        pollMessage = new EmbedBuilder();
        pollMessage.setColor(getColor());
        pollMessage.setTitle(question);
        List<Button> buttons = new ArrayList<>();
        for (PollOption answer : options.values()) {
            int votes = answer.getVotes();
//            pollMessage.addField( ":regional_indicator_" + (answer.getId() + "").toLowerCase() + ": " + answer.getAnswer(), answer.getProgressBar(0) + "  |  **" + 0 + "%**  _(" + votes + ")_", false);
            pollMessage.addField(getOptionField(answer));
            Button button = Button.of(ButtonStyle.PRIMARY, "poll_button-" + this.started + answer.getId(), answer.getId() + "");
            answer.setButton(button);
            buttons.add(button);
        }
        pollMessage.setFooter("Poll open until " + Utils.formatTime(started + duration) + ". Votes: 0");
        Consumer<Message> success = message -> {
            setUid(message.getIdLong());
            save();
        };
        if (hook == null) channel.sendMessageEmbeds(pollMessage.build()).addActionRow(buttons).queue(success);
        else hook.sendMessageEmbeds(pollMessage.build()).addActionRow(buttons).queue(success);

    }

    public LinkedHashMap<Character, PollOption> getOptions() {
        return options;
    }

    public String getQuestion() {
        return question;
    }

    public Color getColor() {
        return open ? new Color(0x27FAE8) : new Color(0xF43C57);
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
        for (PollOption option : options.values()) {
            option.getVoteList().remove(user.getIdLong());
        }
        pollMessage.clearFields();
        userAnswer.vote(user);
        int total = getTotalVotes();
        for (PollOption answer : options.values()) {
            pollMessage.addField(getOptionField(answer));
        }
        pollMessage.setFooter("Poll open until " + Utils.formatTime(started + duration) + ". Votes: " + total);
        event.editMessageEmbeds(pollMessage.build()).queue();
        save();
//        try {
//            event.reply("You voted for " + userAnswer.getAnswer()).setEphemeral(true).queue();
//        } catch (Exception e) {
//            //ignore
//        }
    }

    private MessageEmbed.Field getOptionField(PollOption answer) {
        double percent = answer.getVotes() == 0 ? 0 : Utils.formatDecimal((((double) answer.getVotes()) / ((double) getTotalVotes())) * 100D);
        return new MessageEmbed.Field(":regional_indicator_" + (answer.getId() + "").toLowerCase() + ": " + answer.getAnswer(), answer.getProgressBar(percent) + "  |  **" + percent + "%**  _(" + answer.getVotes() + ")_", false);

    }


    private int getTotalVotes() {
        int votes = 0;
        for (PollOption option : options.values()) {
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
        long now = new Date().getTime();
        if (started + duration > now) {
            duration = now - started;
        }
        save();

        //Close old poll
        String poll_link = channel.retrieveMessageById(uid).complete().getJumpUrl();
        channel.retrieveMessageById(uid).queue(message -> {
//            poll_link = message.getJumpUrl();
            for (Button button : message.getButtons()) {
                message.editMessageComponents(ActionRow.of(button.asDisabled())).queue();
            }
            pollMessage.setColor(getColor());
            pollMessage.setFooter("Poll closed " + Utils.formatTime(started + duration) + ". Votes: " + getTotalVotes());
            message.editMessageEmbeds(pollMessage.build()).queue();
        });


        //Send new embed with results

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Poll Closed (" + question + ")");
        builder.setColor(getColor());
        for (PollOption option : options.values()) {
            builder.addField(":regional_indicator_" + (option.getId() + "").toLowerCase() + ": " + option.getAnswer() , option.getVotes() + " votes", false);
        }
        Button button = Button.link(poll_link, "Go to poll");
//        builder.addField("poll-" + uid, "This poll has been closed. Results are as follows:", false);
        try {
            InputStream file = URI.create("http://localhost:" + Bot.WEB_PORT() + Bot.API_ENTRY_POINT() + "/polls/" + uid + ".png").toURL().openStream();
            builder.setImage("attachment://" + uid + ".png") // we specify this in sendFile as "cat.png"
                    .setDescription("Poll Results for " + question);
//            channel.sendFiles(FileUpload.fromData(file, uid + ".png"))
//                    .setEmbeds(builder.build()).queue();
            channel.sendMessageEmbeds(builder.build()).addActionRow(button).addFiles(FileUpload.fromData(file, uid + ".png")).queue();
        } catch (IOException e) {
            Utils.getLogger().error("Error", e);
            builder.addField("Error", "Couldn't load image.", false);
            channel.sendMessageEmbeds(builder.build()).addActionRow(button).queue();

        }
    }

    public void save() {
        if (uid == 0) {
            Utils.getLogger().error("Poll has not been initialized yet. Cannot save.");
            return;
        }
        try {
            File file = new File(PollUtils.getPollFolder(), uid + "/data.json");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            JSONObject object = json();
            Map<String, Float> data = new HashMap<>();
            for (PollOption option : options.values()) {
                if (option.getVotes() > 0) data.put(option.getId() + "", (float) option.getVotes());
            }

            ChartGenerator.generateRingChart("Poll Results", data, PollUtils.getPollFolder() + "/" + getUid() + "/results.png");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(object.toString(2));
                Utils.getLogger().log("Poll saved: " + question);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public JSONObject json() {
        JSONObject object = new JSONObject();
        object.put("question", question);
        object.put("duration", duration);
        object.put("started", started);
        object.put("channel", channel.getId());
        JSONArray options = jsonOptions();
        object.put("options", options);
        object.put("open", open);
        object.put("uid", uid);
        return object;
    }

    @NotNull
    private JSONArray jsonOptions() {
        JSONArray options = new JSONArray();
        for (PollOption option : this.options.values()) {
            JSONObject optionObject = new JSONObject();
            optionObject.put("answer", option.getAnswer());
            optionObject.put("id", option.getId());
            JSONArray votes = new JSONArray();
            for (Long vote : option.getVoteList()) {
                votes.put(vote);
            }
            optionObject.put("votes", votes);
            options.put(optionObject);
        }
        return options;
    }

    public TextChannel getChannel() {
        return channel;
    }

    public long getUid() {
        return uid;
    }

    private void setUid(long idLong) {
        this.uid = idLong;
        synchronized (this) {
            notifyAll();
        }
    }

    public PollOption getOption(char id) {
        return options.get(id);
    }
}
