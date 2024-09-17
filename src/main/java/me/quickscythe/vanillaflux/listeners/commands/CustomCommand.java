package me.quickscythe.vanillaflux.listeners.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CustomCommand extends ListenerAdapter {

    String label;

    public CustomCommand(Guild guild, String label, String desc, OptionData... options) {
        this.label = label;
        guild.updateCommands().addCommands(Commands.slash(label, desc).addOptions(options)).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(label)) {
            event.reply("Hello").complete();
        }
    }


    public String getLabel() {
        return label;

    }
}
