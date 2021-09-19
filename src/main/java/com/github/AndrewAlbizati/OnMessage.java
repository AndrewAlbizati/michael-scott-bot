package com.github.AndrewAlbizati;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
import java.net.URL;
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
            eb.setDescription("Type: \"" + COMMAND_PREFIX + "mike\" for help.\n" +
                    "Type: \"" + COMMAND_PREFIX + "quote\" to receive a random quote from either Dwight,Stanley,Kevin,Jim, Michael, Phyllis, Angela, Oscar, or Creed.\n" +
                    "Type \"" + COMMAND_PREFIX + "join\" to hear The Office theme song.\n" +
                    "Type: \"office\" to receive DUNDER MIFFLIN");
            eb.setColor(new Color(26, 188, 156));
            eb.setThumbnail("https://cdn.discordapp.com/attachments/798681420603981837/812212702767218698/Screen_Shot_2021-02-18_at_10.43.10_PM.png");
            eb.setFooter("Requested by: " + author.getDisplayName());
            channel.sendMessage(eb);
        }

        // User sends join command
        // Bot joins the voice channel and plays The Office's theme song
        if (message.getContent().equalsIgnoreCase(COMMAND_PREFIX + "join")) {
            if (!messageCreateEvent.getMessageAuthor().getConnectedVoiceChannel().isPresent()) {
                channel.sendMessage("Please join a voice channel.");
                return;
            }

            ServerVoiceChannel voiceChannel = messageCreateEvent.getMessageAuthor().getConnectedVoiceChannel().get();

            voiceChannel.connect().thenAccept(audioConnection -> {
                // Create a player manager
                AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
                playerManager.registerSourceManager(new YoutubeAudioSourceManager());
                AudioPlayer player = playerManager.createPlayer();

                // Create an audio source and add it to the audio connection's queue
                AudioSource source = new LavaplayerAudioSource(api, player);
                audioConnection.setAudioSource(source);

                // Load The Office theme song and play it
                playerManager.loadItem("https://www.youtube.com/watch?v=uyIVAm9PVrI", new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        player.playTrack(track);
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        for (AudioTrack track : playlist.getTracks()) {
                            player.playTrack(track);
                        }
                    }

                    @Override
                    public void noMatches() {
                        // Notify the user that we've got nothing
                    }

                    @Override
                    public void loadFailed(FriendlyException throwable) {
                        // Notify the user that everything exploded
                    }
                });

                // Add a track end event listener
                player.addListener(new AudioEventAdapter() {
                    @Override
                    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                        // Leave the channel when the audio finishes playing
                        audioConnection.close();
                    }
                });
            }).exceptionally(e -> {
                // Failed to connect to voice channel (no permissions?)
                e.printStackTrace();
                return null;
            });
        }
    }

    private static String getRandQuote() {
        try {
            URL url = ClassLoader.getSystemClassLoader().getResource("quotes.json");
            File f = new File(url.getFile());
            InputStream jsonStream = new FileInputStream(f);

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
