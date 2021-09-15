package com.github.AndrewAlbizati;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class OnMessage implements MessageCreateListener {
    private static final char COMMAND_PREFIX = '.';

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        Message message = messageCreateEvent.getMessage();
        TextChannel channel = messageCreateEvent.getChannel();
        MessageAuthor author = messageCreateEvent.getMessageAuthor();
        DiscordApi api = messageCreateEvent.getApi();

        // Ignore messages sent by the bot
        if (author.asUser().get().equals(api.getYourself()))
            return;

        // User sends "office"
        // Bot responds with **DUNDER MIFFLIN**
        if (message.getContent().toLowerCase().contains("office"))
            channel.sendMessage("**DUNDER MIFFLIN**");

        // User sends quote command
        // Bot responds with a random quote
        if (message.getContent().equalsIgnoreCase(COMMAND_PREFIX + "quote"))
            channel.sendMessage(getRandQuote());

        // User sends mike command
        // Bot responds with a list of commands
        if (message.getContent().equalsIgnoreCase(COMMAND_PREFIX + "mike")) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Hello Mifflinite!\n*__Here's a list of possible commands:__*");
            eb.setDescription("Type: \".mike\" for help.\n" +
                    "Type: \".quote\" to receive a random quote from either Dwight,Stanley,Kevin,Jim, Michael, Phyllis, Angela, Oscar, or Creed.\n" +
                    "Type: \"office\" to receive DUNDER MIFFLIN");
            eb.setColor(new Color(26, 188, 156));
            eb.setThumbnail("https://cdn.discordapp.com/attachments/798681420603981837/812212702767218698/Screen_Shot_2021-02-18_at_10.43.10_PM.png");
            eb.setFooter("Requested by: " + author.getDisplayName());
            channel.sendMessage(eb);
        }
    }

    private static String getRandQuote() {
        try {
            InputStream jsonStream = Bot.class.getResourceAsStream("resources/quotes.json");
            JSONParser parser = new JSONParser();
            JSONArray quotes = (JSONArray) parser.parse(new InputStreamReader(jsonStream, "UTF-8"));

            Random rand = new Random();
            String quote = (String) quotes.get(rand.nextInt(quotes.size()));

            return quote;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
