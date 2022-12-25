package de.SparkArmy.utils.jda;

import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


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

    public static String discordTimestamp(OffsetDateTime time,String formatting){
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
        return String.format("<t:%s:%s>",timeToEpochSecond(time),formatting);
    }

    public static @NotNull JSONArray storeDataOnStorageServer(Message msg){
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/attachments");
        if (directory == null) return new JSONArray();

        List<Message.Attachment> attachments = msg.getAttachments();
        if (attachments.isEmpty()) return new JSONArray();
        // Get all attachments from message
        List<File> files = attachments.stream().map(x->{
            File file = FileHandler.getFileInDirectory(directory,x.getFileName());
            try {
                file = x.getProxy().downloadToFile(file).get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return file;
        }).toList();

        // Get the storage channel and if non exists create one
        List<TextChannel> storageChannels = MainUtil.storageServer.getTextChannelsByName("attachment-storage",true);
        TextChannel storageChannel;
        if (storageChannels.isEmpty()){
           storageChannel = ChannelUtil.createTextChannel(MainUtil.storageServer,"attachment-storage");
        }else {
            storageChannel = storageChannels.get(0);
        }

        // Send the attachments in this channel and get the urls
        JSONArray attachmentUrls = new JSONArray();
        List<FileUpload> fileUploads = new ArrayList<>();
        if (files.isEmpty()) return new JSONArray();
        files.forEach(x->fileUploads.add(FileUpload.fromData(x)));
        storageChannel.sendFiles(fileUploads).complete().getAttachments().forEach(y->attachmentUrls.put(y.getUrl()));

        files.forEach(File::delete);

        return attachmentUrls;
    }
}
