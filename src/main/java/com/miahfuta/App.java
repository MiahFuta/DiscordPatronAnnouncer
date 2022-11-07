package com.miahfuta;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.intent.IntentSet;

public class App {

	public static Properties prop;

	public static GatewayDiscordClient client;

	public static RoleCompare roleCompare;
	public static RoleWatcher roleWatcher;

	public static boolean testing = false;

	public static void main(String[] args) {

		prop = new Properties();

		String fileName = "app.config";

		try (FileInputStream fis = new FileInputStream(fileName)) {
			prop.load(fis);
		} catch (FileNotFoundException ex) {
			System.err.println(ex);
			return;
		} catch (IOException ex) {
			System.err.println(ex);
			return;
		}

		roleCompare = new RoleCompare();
		roleWatcher = new RoleWatcher();

		if (args.length != 0)
			if (args[0].equalsIgnoreCase("debug=true"))
				testing = true;

		String botToken = testing ? prop.getProperty("test_token") : prop.getProperty("bot_token");

		client = DiscordClient.create(botToken)
			.gateway()
			.setAwaitConnections(false)
			.setEnabledIntents(IntentSet.all())
			.login()
			.block();

		client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {

			roleCompare.init(client, prop.getProperty("discord_server_id"));
			roleWatcher.init(roleCompare, client, testing, prop);

			User self = event.getSelf();

			System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));

			ClientActivity activity = testing ? ClientActivity.playing(prop.getProperty("test_activity"))
					: ClientActivity.watching(prop.getProperty("bot_watching_activity"));

			client.updatePresence(ClientPresence.online(activity)).block();

		});

		client.getEventDispatcher().on(MemberUpdateEvent.class).subscribe(event -> {

			roleCompare.setOldRoles(event.getMemberId().asLong());
			roleCompare.setNewRoles(event.getCurrentRoles().collectList().block());

			ArrayList<String> patreonRoles = new ArrayList<String>();

			patreonRoles.add(prop.getProperty("discord_patron_tier1_role_id"));
			patreonRoles.add(prop.getProperty("discord_patron_tier2_role_id"));

			roleWatcher.patreon(event, patreonRoles);

			roleCompare.updateServerMembers();

		});

		client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {

			Message message = event.getMessage();

			String channelID = message.getChannelId().asString();

			if (!event.getMember().get().isBot())
				if (message.getContent().equalsIgnoreCase("!ping"))
					sendMsg(channelID, "pong!");

		});

		client.onDisconnect().block();

	}

	public static void sendMsg(String channelID, String message) {

		TextChannel channel = (TextChannel) client.getChannelById(Snowflake.of(channelID)).block();

		channel.createMessage(message).block();

		System.out.println(message);

	}

}