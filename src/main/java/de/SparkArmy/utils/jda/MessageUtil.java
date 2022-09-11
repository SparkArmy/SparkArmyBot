package de.SparkArmy.utils.jda;

import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("unused")
public class MessageUtil {
    private static @NotNull String timeToEpochSecond(@NotNull OffsetDateTime parsedTime){
        return String.valueOf(parsedTime.toEpochSecond());
    }

    public static String discordTimestamp(OffsetDateTime time){
        /*
        * t	16:20	Short Time
        * T	16:20:30	Long Time
        * d	20/04/2021	Short Date
        * D	20 April 2021	Long Date
        * f* 20 April 2021 16:20	Short Date/Time
        * F	Tuesday, 20 April 2021 16:20	Long Date/Time
        * R	2 months ago	Relative Time
        * *default
        * */
        return String.format("<t:%s>",timeToEpochSecond(time));
    }

    public static String discordTimestamp(OffsetDateTime time,String formating){
        /*
         * t	16:20	Short Time
         * T	16:20:30	Long Time
         * d	20/04/2021	Short Date
         * D	20 April 2021	Long Date
         * f* 20 April 2021 16:20	Short Date/Time
         * F	Tuesday, 20 April 2021 16:20	Long Date/Time
         * R	2 months ago	Relative Time
         * *default
         * */
        return String.format("<t:%s:%s>",timeToEpochSecond(time),formating);
    }

    public static @NotNull String logAttachmentsOnStorageServer(@NotNull Message.Attachment attachment, @NotNull Guild guild){
        Guild storage = MainUtil.storageServer;
        Category category;
        if (storage.getCategoriesByName(guild.getId(),false).isEmpty()){
           category = ChannelUtil.createCategory(storage,guild.getId());
        }else {
            category = storage.getCategoriesByName(guild.getId(),false).get(0);
        }

        if (category == null) {
            category = ChannelUtil.createCategory(storage,guild.getId());
        }

        TextChannel channel;
        if (category.getTextChannels().stream().filter(x->x.getName().equals("message-attachments")).toList().isEmpty()){
            channel = ChannelUtil.createTextChannel(category,"message-attachments");
        }else {
           channel = category.getTextChannels().stream().filter(x->x.getName().equals("message-attachments")).toList().get(0);
        }

        if (channel == null){
            channel = ChannelUtil.createTextChannel(category,"message-attachments");
        }

        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/attachments");
        assert directory != null;
        File file = FileHandler.getFileInDirectory(directory,attachment.getFileName());
        try {
            file = attachment.getProxy().downloadToFile(file).get();
        } catch (InterruptedException | ExecutionException e) {
            MainUtil.logger.error(e.getMessage());
        }

        File finalFile = file;
        var ref = new Object() {
            String messageId;
        };
        String attachmentUrl = channel.sendFiles(new ArrayList<>(){{add(FileUpload.fromData(finalFile));}}).complete().getAttachments().get(0).getUrl();
        //noinspection ResultOfMethodCallIgnored
        file.delete();
        return attachmentUrl;
    }
}
