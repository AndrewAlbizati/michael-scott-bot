package com.github.AndrewAlbizati;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.user.UserStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

public class Bot {
    public static void main(String[] args) {
        String token = "";
        try {
            File f = new File("token.txt");
            Scanner s = new Scanner(f);
            token = s.next();
            s.close();

            if (token.length() == 0)
                throw new NullPointerException();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        DiscordApi api = new DiscordApiBuilder().setToken(token).setAllIntents().login().join();
        System.out.println("Logged in as " + api.getYourself().getDiscriminatedName());

        api.updateStatus(UserStatus.ONLINE);
        api.updateActivity(ActivityType.WATCHING, getRandEpisode());

        api.addMessageCreateListener(new OnMessage());
    }

    private static String getRandEpisode() {
        try {
            InputStream jsonStream = Bot.class.getResourceAsStream("resources/episodes.json");
            JSONParser parser = new JSONParser();
            JSONObject episodes = (JSONObject) parser.parse(new InputStreamReader(jsonStream, "UTF-8"));

            Random rand = new Random();
            String season = (String) episodes.keySet().toArray()[rand.nextInt(episodes.keySet().size())];

            JSONObject seasonObj = (JSONObject) episodes.get(season);

            List<Map.Entry> episodesList = new ArrayList<>();
            episodesList.addAll(seasonObj.entrySet());

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
