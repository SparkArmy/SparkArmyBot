package de.SparkArmy.eventListener.globalEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ModmailListener extends CustomEventListener {

    private final Logger logger = MainUtil.logger;
    private final ConfigController controller = MainUtil.controller;

    private final File directory = FileHandler.getDirectoryInUserDirectory("botstuff/modmail");

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (!modalId.contains(";")) return;
        String[] strings = modalId.split(";");
        if (Arrays.stream(strings).filter("modmail"::equals).toList().isEmpty()) return;
        @NonNls String idExtension = strings[1];
        if (null == directory){
            this.logger.warning("MODMAIL-ModalInteraction: directory is null");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        List<File> fileList = new ArrayList<>();
        if (null != directory.listFiles())
             fileList = Arrays.stream(Objects.requireNonNull(directory.listFiles())).filter(f->f.getName().equals(idExtension + ".json")).toList();
        if (fileList.isEmpty()){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        JSONObject modalData = new JSONObject(){{
           put("topic", Objects.requireNonNull(event.getValue("topic;" + idExtension)).getAsString());
           put("body", Objects.requireNonNull(event.getValue("body;" + idExtension)).getAsString());
        }};
        if (!FileHandler.writeValuesInFile(fileList.get(0).getAbsolutePath(),modalData)){
            this.logger.warning("MODMAIL-ModalInteraction: can#t write values in file");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }


        SelectMenu.Builder guilds = SelectMenu.create("modmailGuildPicker;" + idExtension);

        event.getJDA().getGuilds().forEach(g->{
            JSONObject guildConfig = this.controller.getSpecificConfig(g,"config.json");
            if (null != guildConfig){
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
        String menuName = event.getComponentId();
        logger.info(menuName);
        if (!menuName.contains(";")) return;
        String[] strings = menuName.split(";");
        if (Arrays.stream(strings).filter("modmailGuildPicker"::equals).toList().isEmpty()) return;
        if (null == directory){
            this.logger.warning("MODMAIL-SelectMenuInteraction: directory is null");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        List<File> fileList = new ArrayList<>();
        if (null != directory.listFiles())
            fileList = Arrays.stream(Objects.requireNonNull(directory.listFiles())).filter(f->f.getName().equals(strings[1] + ".json")).toList();
        if (fileList.isEmpty()){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        JSONObject modalData = new JSONObject(Objects.requireNonNull(FileHandler.getFileContent(fileList.get(0).getAbsolutePath())));

        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(event.getUser().getAsTag(),null,event.getUser().getAvatarUrl());
        builder.setTitle(modalData.getString("topic"));
        builder.setDescription(modalData.getString("body"));

        fileList.get(0).deleteOnExit();

        event.reply("Will you sent attachments to this server?").setEphemeral(true)
                .addActionRow(Button.primary("modmailYes","Yes"))
                .addActionRow(Button.primary("modmailNo","No")).queue(x->
                        MainUtil.waiter.waitForEvent(ButtonInteractionEvent.class,f->f.getUser().getId().equals(event.getUser().getId()),f->{
                            logger.info("test");
                            if (f.getComponentId().equals("modmailNo")){
                                x.editOriginal("Your message was sent to the server").queue();
                                x.editOriginalComponents().queue();
                            }
                        },30, TimeUnit.SECONDS,()-> x.editOriginal("Your time is over, the message will be send to the server").queue()));

    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final File staticDirectory = FileHandler.getDirectoryInUserDirectory("botstuff/modmail");
    public static void deleteOldFiles(){
        if (Objects.requireNonNull(staticDirectory).listFiles() == null) return;
        Objects.requireNonNull(FileHandler.getFilesInDirectory(staticDirectory)).forEach(file -> {
           String name = file.getName().split(",")[1].split("\\.")[0];
           if (LocalDateTime.parse(name,formatter).plusMinutes(10).isBefore(LocalDateTime.parse(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis())),formatter))){
               //noinspection ResultOfMethodCallIgnored
               file.delete();
           }
       });
    }
}
