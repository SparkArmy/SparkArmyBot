package de.SparkArmy.eventListener.globalEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ModmailListener extends ListenerAdapter implements CustomEventListener {

    private final Logger logger = MainUtil.logger;
    private final ConfigController controller = MainUtil.controller;

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        String[] strings = modalId.split(";");
        if (strings[1].equals("modmail")) return;
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/modmail");
        if (directory == null){
            logger.warning("MODMAIL: directory is null");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        List<File> fileList = Arrays.stream(directory.listFiles()).filter(f->f.getName().equals(strings[1] + ".json")).toList();
        if (fileList.isEmpty()){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        SelectMenu.Builder guilds = SelectMenu.create("guilds;" + strings[1]);

        event.getJDA().getGuilds().forEach(g->{
            JSONObject guildConfig = controller.getSpecificConfig(g,"config.json");
            if (guildConfig != null){
                if (!guildConfig.keySet().contains("command-permissions")) return;
                if (guildConfig.getJSONObject("command-permissions").getBoolean("modmail")){
                    guilds.addOption(g.getName(),g.getId());
                }
            }
        });

        event.reply("Please select the target guild").addActionRow(guilds.build()).setEphemeral(true).queue();
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {

    }
}
