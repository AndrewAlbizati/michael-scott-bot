package com.github.AndrewAlbizati;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.user.UserStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot {
    public static void main(String[] args) {
        // Get token from token.txt
        // Raise exception if not found
        String token = "";
        try {
            File f = new File("token.txt");
            if (f.createNewFile())
                throw new NullPointerException("Please enter your bot token in token.txt");

            Scanner s = new Scanner(f);
            token = s.next();
            s.close();

            if (token.length() == 0)
                throw new NullPointerException("Token not found!");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        DiscordApi api = new DiscordApiBuilder().setToken(token).setAllIntents().login().join();
        System.out.println("Logged in as " + api.getYourself().getDiscriminatedName());

        // Set bot status to online
        api.updateStatus(UserStatus.ONLINE);

        // Create runnable that runs every 22 minutes
        // Changes bot activity
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            // Set bot activity to watching a random episode
            // (Season #) (Episode #): (Title)
            api.updateActivity(ActivityType.WATCHING, getRandEpisode());
        }, 0, 22, TimeUnit.MINUTES);

        // Add message create listener to process commands
        // And respond to certain messages
        api.addMessageCreateListener(new OnMessage());
    }

    private static String getRandEpisode() {
        try {
            // Episode information stored in episodes.json
            URL url = ClassLoader.getSystemClassLoader().getResource("episodes.json");
            File f = new File(url.getFile());
            InputStream jsonStream = new FileInputStream(f);

            JSONParser parser = new JSONParser();
            JSONObject episodes = (JSONObject) parser.parse(new InputStreamReader(jsonStream, "UTF-8"));

            jsonStream.close();

            Random rand = new Random();
            // episodes object has the seasons as keys
            // Get random key from episodes object
            String season = (String) episodes.keySet().toArray()[rand.nextInt(episodes.keySet().size())];

            // seasonObj contains all episodes in a season
            JSONObject seasonObj = (JSONObject) episodes.get(season);

            List<Map.Entry> episodesList = new ArrayList<>();
            episodesList.addAll(seasonObj.entrySet());

            // episodeEntry contains all information for one episode
            // Formatted as Title: info (JSONObject)
            Map.Entry episodeEntry = episodesList.get(rand.nextInt(episodesList.size()));

            String episodeNumber = (String) episodeEntry.getKey();
            JSONObject episodeDetails = (JSONObject) episodeEntry.getValue();

            return season + " " + episodeNumber + ": \"" + episodeDetails.get("Title") + "\"";
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
