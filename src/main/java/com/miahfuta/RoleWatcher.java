package com.miahfuta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

public class RoleWatcher {

    private static Properties prop;

    private static RoleCompare roleCompare;

    private static GatewayDiscordClient client;

    private static String msgChannel;

    public void init(RoleCompare rc, GatewayDiscordClient gdc, boolean testing, Properties p) {

        roleCompare = rc;
        client = gdc;
        prop = p;

        String announcements = prop.getProperty("bot_announcements_channel_id");
        String spamTesting = prop.getProperty("test_channel_id");

        msgChannel = testing ? spamTesting : announcements;

    }

    public void patreon(MemberUpdateEvent event, ArrayList<String> roles) {

        String memberID = event.getMemberId().asString();

        for (String roleID : roles) {

            boolean isPlus = roleID.equals(prop.getProperty("discord_patron_tier2_role_id"));

            String tier = "Tier " + (isPlus ? "2" : "1");

            String message = "**Welcome to my Newest " + tier + " Patron** <@" + memberID + "> " + randomEmote();

            if (roleCompare.gotRoleID(roleID)) {

                sendMsg(message, true);

                break;

            }

        }

    }

    private String randomEmote() {

        ArrayList<String> emotes = new ArrayList<String>(Arrays.asList(
                ":heart:", ":blush:", ":smiling_face_with_3_hearts:"));

        Random random = new Random();
        int rand = random.nextInt(emotes.size());

        return emotes.get(rand);

    }

    public static void sendMsg(String message, boolean reaction) {

        TextChannel channel = (TextChannel) client.getChannelById(Snowflake.of(msgChannel)).block();

        Message msg = channel.createMessage(message).block();

        if (reaction) {
            msg.addReaction(ReactionEmoji.codepoints("U+1f44b")).block(); // Wave
            msg.addReaction(ReactionEmoji.codepoints("U+1f60a")).block(); // Blush
            msg.addReaction(ReactionEmoji.codepoints("U+1f970")).block(); // Face Hearts
            msg.addReaction(ReactionEmoji.codepoints("U+2764")).block(); // Red Heart
        }

        System.out.println(message);

    }

}