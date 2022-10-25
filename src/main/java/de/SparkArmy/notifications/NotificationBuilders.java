package de.SparkArmy.notifications;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class NotificationBuilders {

    private static final Color color = new Color(0x1C7B9B);

    private static final String idTemplate = "%s;%s";

    protected static @NotNull MessageEmbed overviewEmbed(NotificationType type){
        return new EmbedBuilder(){{
            setTitle("Notifications");
            if (type != null) setTitle(type.name());
            setDescription("Select with the buttons below a action");
            setColor(color);
            addField("Add","Adds a new notification",true);
            addField("Edit","Edit a notification",true);
            addField("Remove","Remove a notification",true);
        }}.build();
    }

    protected static @NotNull MessageEmbed notificationSelectEmbed(String actionString){
        return new EmbedBuilder(){{
            setTitle(actionString + " Notification");
            setDescription("Please select an notification-type below to continue");
            setColor(color);
        }}.build();
    }

    protected static @NotNull MessageEmbed selectNotificationEmbed(@NotNull List<MessageEmbed.Field> fields){
        EmbedBuilder embed = new EmbedBuilder(){{
            setTitle("Notification Selection");
            setDescription("Select a notification below to edit/delete this.");
        }};
        fields.forEach(embed::addField);
        return embed.build();
    }


    protected static @NotNull Collection<Button> notificationSelectButtonCollection(String suffix){
        return new ArrayList<>(){{
            add(Button.primary(String.format(idTemplate,"twitterNotification",suffix),"Twitter"));
            add(Button.primary(String.format(idTemplate,"twitchNotification",suffix),"Twitch"));
            add(Button.primary(String.format(idTemplate,"youtubeNotification",suffix),"YouTube"));
        }};
    }

    @Contract("_, _ -> new")
    protected static @NotNull Collection<Button> actionSelectButtonCollection(@NotNull User user, NotificationType type){
        String userId = user.getId();
        String suffix;
        if (type != null) {
            String typeString = type.name();
            suffix = String.format("%s,%s",userId,typeString);
        }else {
            suffix = String.format("%s",userId);
        }

        return new ArrayList<>(){{
            add(Button.primary(String.format(idTemplate,"addNotification", suffix),"Add"));
            add(Button.primary(String.format(idTemplate,"editNotification", suffix),"Edit"));
            add(Button.danger(String.format(idTemplate,"removeNotification", suffix),"Remove"));
        }};
    }

    protected static @NotNull Collection<ActionRow> actionRowsForEditModal(int fieldsBefore, int fieldsRemain, JSONObject content, @NotNull User user, @NotNull NotificationType type, String action){
        Button nextButton = Button.primary(String.format("nextNotificationEmbed;%s,%s,%d,%s",user.getId(),type.getTypeName(),fieldsRemain,action),"Next");
        Button beforeButton = Button.primary(String.format("beforeNotificationEmbed;%s,%s,%d,%s",user.getId(),type.getTypeName(),fieldsBefore,action),"Before");

        Collection<ActionRow> actionRows = new ArrayList<>();
        if (fieldsRemain > 0 && fieldsBefore > 0) actionRows.add(ActionRow.of(nextButton,beforeButton));
        else if (fieldsBefore > 0)actionRows.add(ActionRow.of(beforeButton));
        else if (fieldsRemain > 0) actionRows.add(ActionRow.of(nextButton));

        StringSelectMenu.Builder menu = StringSelectMenu.create("notificationSelectMenu;" + user.getId() + "," + type.getTypeName() + "," + action);
        var ref = new Object() {
            int i = 0;
        };
        content.keySet().forEach(x->{
            ref.i = ref.i + 1;
            if (ref.i <24) {
                JSONObject c = content.getJSONObject(x);
                menu.addOption(c.getString("userName"), x);
            }
        });

        actionRows.add(ActionRow.of(menu.build()));
        return actionRows;
    }

    protected static @NotNull Modal addNotificationModal(@NotNull NotificationType type, @NotNull User user){
        TextInput userName = TextInput.create("userName","User Name", TextInputStyle.SHORT)
                .setRequired(true)
                .build();

        TextInput channel = TextInput.create("channel","Notification Channel",TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setMaxLength(1024)
                .setPlaceholder("The channel id in this format: 1234(,5678)")
                .build();

        TextInput roles = TextInput.create("roles","Mentioned Roles",TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setMaxLength(1024)
                .setPlaceholder("The role-id in this format : 1234(,5678)")
                .build();

        TextInput message = TextInput.create("message","Message String",TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setMaxLength(1024)
                .setPlaceholder("f.e: [@Role1,@Role2] New notification from user")
                .build();

        return Modal.create(String.format("%sNotification;%s",type.getTypeName(),user.getId()),String.format("New %s Notification",type.name())).addActionRows(
                ActionRow.of(userName),
                ActionRow.of(channel),
                ActionRow.of(roles),
                ActionRow.of(message)
        ).build();
    }

    protected static @NotNull Modal editNotificationModal(@NotNull NotificationType type, @NotNull JSONObject content, @NotNull User user){
        TextInput userName = TextInput.create("userName","User Name", TextInputStyle.SHORT)
                .setRequired(true)
                .setValue(content.getString("userName"))
                .build();

        StringBuilder channelString = new StringBuilder();
        content.getJSONArray("channel").forEach(x->channelString.append(x).append(","));
        channelString.deleteCharAt(channelString.length()-1);

        TextInput channel = TextInput.create("channel","Notification Channel",TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setPlaceholder("The channel id in this format: 1234,5678")
                .setMaxLength(1024)
                .setValue(channelString.toString())
                .build();

        StringBuilder rolesString = new StringBuilder();
        content.getJSONArray("roles").forEach(x->rolesString.append(x).append(","));
        rolesString.deleteCharAt(rolesString.length()-1);

        TextInput roles = TextInput.create("roles","Mentioned Roles",TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setMaxLength(1024)
                .setPlaceholder("The role-id in this format : 1234(,5678)")
                .setValue(rolesString.toString())
                .build();

        TextInput message = TextInput.create("message","Message String",TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setMaxLength(1024)
                .setPlaceholder("f.e: [@Role1,@Role2 (Mentioned Roles)] (Message:) New notification from user xyz")
                .setValue(content.getString("message"))
                .build();

        return Modal.create(String.format("%sNotification;%s",type.getTypeName(),user.getId()),String.format("Edit %s Notification",type.name())).addActionRows(
                ActionRow.of(userName),
                ActionRow.of(channel),
                ActionRow.of(roles),
                ActionRow.of(message)
        ).build();
    }

    protected static @NotNull MessageEmbed showContent(@NotNull NotificationType type, @NotNull JSONObject content, @NotNull ModalMapping channel, @NotNull ModalMapping roles){
        return new EmbedBuilder(){{
            setTitle(type.name() + " Content");
            setDescription("The content from " + content.getString("userName"));
            setColor(color);
            addField("Channel-Id's",channel.getAsString(),false);
            addField("Role-Id's", roles.getAsString(), false);
            addField("Message",content.getString("message"),false);
        }}.build();
    }

    protected static @NotNull MessageCreateBuilder twitchNotification(@NotNull JSONObject streamData, @NotNull JSONObject userData, @NotNull JSONObject content, Guild guild){

        StringBuilder roleString = new StringBuilder();
        content.getJSONArray("roles").forEach(x->{
            Role role = guild.getRoleById(x.toString());
            if (role == null) return;
            roleString.append(role.getAsMention()).append(",");
        });
        roleString.deleteCharAt(roleString.length()-1);
        roleString.append(" ");

        String messageString = String.format("%s %s",roleString,content.getString("message"));

        String profilePicture = userData.getString("profile_image_url");
        String picture = streamData.getString("thumbnail_url").replace("{width}","1600").replace("{height}","900") +
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));

        String link = "https://www.twitch.tv/" + streamData.getString("user_login");

        EmbedBuilder twitchEmbed = new EmbedBuilder();
        twitchEmbed.setAuthor(userData.getString("display_name"),null,profilePicture);
        twitchEmbed.setTitle(streamData.getString("title"),link);
        twitchEmbed.setColor(new Color(0x431282));
        twitchEmbed.setImage(picture);
        twitchEmbed.setTimestamp(OffsetDateTime.now());

        return new MessageCreateBuilder().addContent(messageString).addEmbeds(twitchEmbed.build());
    }
}
