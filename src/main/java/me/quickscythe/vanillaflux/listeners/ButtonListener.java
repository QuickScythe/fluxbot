package me.quickscythe.vanillaflux.listeners;

import me.quickscythe.vanillaflux.utils.polls.Poll;
import me.quickscythe.vanillaflux.utils.polls.PollOption;
import me.quickscythe.vanillaflux.utils.polls.PollUtils;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        System.out.println("Button clicked");
        if(event.getComponentId().startsWith("poll_button-")){
            System.out.println("Poll button clicked");
            for(Poll poll : PollUtils.getPolls()){
                System.out.println("Checking poll: " + poll.getQuestion() + " (" + poll.getUid() + ")");
                for(PollOption option : poll.getOptions()){
                    System.out.println("Checking option: " + option.getAnswer() + " (" + option.getButton().getId() + ")");
                    if(option.getButton().getId().equals(event.getComponentId())){
                        System.out.println("Voting for option: " + option.getAnswer());
                        poll.vote(event, option, event.getUser());
                    }
                }
//                if(poll.getMessageId().equals(event.getMessageId())){
//                    poll.vote(event);
//                }
            }
        }
        if (event.getComponentId().equals("hello")) {
            event.reply("Hello :)").queue(); // send a message in the channel
        } else if (event.getComponentId().equals("emoji")) {
            event.editMessage("That button didn't say click me").queue(); // update the message
        }
    }
}
