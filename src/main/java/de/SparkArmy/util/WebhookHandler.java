package de.SparkArmy.util;

import club.minnced.discord.webhook.WebhookCluster;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.util.customTypes.LogChannelType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WebhookHandler {

    private static final JDA jda = Utils.jda;

    public static void createWebhookMessage(@NotNull GuildChannel channel, String webhookName, String webhookAvatarUrl, Message msg) {
        // Add all values from the message to the msgBuilder
        WebhookMessageBuilder msgBuilder = WebhookMessageBuilder.fromJDA(msg);
        msgBuilder.setUsername(webhookName);
        msgBuilder.setAvatarUrl(webhookAvatarUrl);

        // Get webhooks in channel and create one if none exist
        // Returns if more than one webhook exist

        switch (channel.getType()) {

            case TEXT -> {
                TextChannel textChannel = jda.getTextChannelById(channel.getId());
                if (textChannel == null) return;
                textChannel.retrieveWebhooks().queue(webhooks -> {
                    if (webhooks.isEmpty()) {
                        textChannel.createWebhook(jda.getSelfUser().getName()).queue(x -> sendWebhook(x.getUrl(), msgBuilder));
                        return;
                    }
                    if (webhooks.size() > 1) return;
                    sendWebhook(webhooks.get(0).getUrl(), msgBuilder);
                });
            }
            case VOICE -> {
                VoiceChannel voiceChannel = jda.getVoiceChannelById(channel.getId());
                if (voiceChannel == null) return;
                voiceChannel.retrieveWebhooks().queue(webhooks -> {
                    if (webhooks.isEmpty()) {
                        voiceChannel.createWebhook(jda.getSelfUser().getName()).queue(x -> sendWebhook(x.getUrl(), msgBuilder));
                        return;
                    }
                    if (webhooks.size() > 1) return;
                    sendWebhook(webhooks.get(0).getUrl(), msgBuilder);
                });
            }
            case NEWS -> {
                NewsChannel newsChannel = jda.getNewsChannelById(channel.getId());
                if (newsChannel == null) return;
                newsChannel.retrieveWebhooks().queue(webhooks -> {
                    if (webhooks.isEmpty()) {
                        newsChannel.createWebhook(jda.getSelfUser().getName()).queue(x -> sendWebhook(x.getUrl(), msgBuilder));
                        return;
                    }
                    if (webhooks.size() > 1) return;
                    sendWebhook(webhooks.get(0).getUrl(), msgBuilder);
                });
            }
            case STAGE -> {
                StageChannel stageChannel = jda.getStageChannelById(channel.getId());
                if (stageChannel == null) return;
                stageChannel.retrieveWebhooks().queue(webhooks -> {
                    if (webhooks.isEmpty()) {
                        stageChannel.createWebhook(jda.getSelfUser().getName()).queue(x -> sendWebhook(x.getUrl(), msgBuilder));
                        return;
                    }
                    if (webhooks.size() > 1) return;
                    sendWebhook(webhooks.get(0).getUrl(), msgBuilder);
                });
            }
        }
    }

    public static void createLogWebhookMessage(LogChannelType logChannelType, Guild guild, Message msg) {
        JSONObject guildConfig = Utils.controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);
        if (guildConfig.isNull("log-channel")) {
            JSONObject logChannelConfig = new JSONObject();
            guild.createCategory("logchannel").queue(category -> {
                logChannelConfig.put("log-category", category.getId());
                category.createTextChannel(logChannelType.getName()).queue(textChannel -> {
                    logChannelConfig.put(logChannelType.getName(), textChannel.getId());

                    guildConfig.put("log-channel", logChannelConfig);
                    Utils.controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, guildConfig);
                });
            });
        }

    }


    private static void sendWebhook(String webhookUrl, @NotNull WebhookMessageBuilder msgBuilder) {
        JDAWebhookClient client = JDAWebhookClient.withUrl(webhookUrl);
        client.send(msgBuilder.build());
        client.close();
    }

    private static void sendWebhook(@NotNull List<String> webhookUrls, @NotNull WebhookMessageBuilder msgBuilder) {
        WebhookCluster cluster = new WebhookCluster();
        cluster.setDefaultHttpClient(new OkHttpClient());
        cluster.setDefaultDaemon(true);

        webhookUrls.forEach(url -> {
            JDAWebhookClient client = JDAWebhookClient.withUrl(url);
            cluster.addWebhooks(client);
        });

        cluster.broadcast(msgBuilder.build());
        cluster.close();
    }

    private static @NotNull WebhookMessageBuilder webhookMessageBuilder(@NotNull Message msg) {
        WebhookMessageBuilder msgBuilder = new WebhookMessageBuilder();
        if (!msg.getEmbeds().isEmpty()) {
            List<WebhookEmbed> embeds = new ArrayList<>();
            msg.getEmbeds().forEach(embed -> {
                WebhookEmbedBuilder webhookEmbed = new WebhookEmbedBuilder();

                webhookEmbed.setTimestamp(embed.getTimestamp());
                webhookEmbed.setColor(embed.getColorRaw());
                webhookEmbed.setDescription(embed.getDescription());
                if (embed.getThumbnail() != null) {
                    webhookEmbed.setThumbnailUrl(embed.getThumbnail().getUrl());
                }
                if (embed.getImage() != null) {
                    webhookEmbed.setImageUrl(embed.getImage().getUrl());
                }
                if (embed.getFooter() != null) {
                    if (embed.getFooter().getText() != null)
                        webhookEmbed.setFooter(new WebhookEmbed.EmbedFooter(embed.getFooter().getText(), embed.getFooter().getIconUrl()));
                }
                if (embed.getTitle() != null) {
                    webhookEmbed.setTitle(new WebhookEmbed.EmbedTitle(embed.getTitle(), null));
                }
                MessageEmbed.AuthorInfo authorInfo = embed.getAuthor();
                if (authorInfo != null) {
                    webhookEmbed.setAuthor(new WebhookEmbed.EmbedAuthor(authorInfo.getName() != null ? authorInfo.getName() : jda.getSelfUser().getName(), authorInfo.getIconUrl(), authorInfo.getUrl()));
                }
                embed.getFields().forEach(field -> {
                    if (field.getName() != null && field.getValue() != null)
                        webhookEmbed.addField(new WebhookEmbed.EmbedField(field.isInline(), field.getName(), field.getValue()));
                });

                embeds.add(webhookEmbed.build());
            });
            msgBuilder.addEmbeds(embeds);
        }

        if (!msg.getAttachments().isEmpty()) {
            File directory = FileHandler.getDirectoryInUserDirectory("botstuff/webhookStuff/attachments");
            List<File> attachmentFiles = msg.getAttachments().stream().map(x -> {
                File file = FileHandler.getFileInDirectory(directory, x.getFileName());
                try {
                    file = x.getProxy().downloadToFile(file).get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return file;
            }).toList();
            attachmentFiles.forEach(file -> {
                msgBuilder.addFile(file);
                file.delete();
            });
        }

        if (!msg.getContentRaw().isEmpty()) {
            msgBuilder.append(msg.getContentRaw());
        }

        return msgBuilder;
    }
}
