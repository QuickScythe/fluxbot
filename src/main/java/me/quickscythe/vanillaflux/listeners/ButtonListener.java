package me.quickscythe.vanillaflux.listeners;

import me.quickscythe.vanillaflux.utils.polls.Poll;
import me.quickscythe.vanillaflux.utils.polls.PollOption;
import me.quickscythe.vanillaflux.utils.polls.PollUtils;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(event.getComponentId().startsWith("poll_button-")){
            for(Poll poll : PollUtils.getPolls()){
                for(PollOption option : poll.getOptions().values()){
                    if(option.getButton() == null) continue;
                    if(option.getButton().getId().equals(event.getComponentId())){
                        poll.vote(event, option, event.getUser());
                    }
                }
            }
        }
    }
}
