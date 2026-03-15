package com.civtale.civguild.util;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildMember;
import com.civtale.civguild.GuildRank;
import com.google.gson.*;
import com.hypixel.hytale.logger.HytaleLogger;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataStorage {
    private static DataStorage instance;
    private final Path dataDirectory;
    private final Gson gson;
    private static HytaleLogger logger;

    private DataStorage(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.gson = (new GsonBuilder()).setPrettyPrinting().create();
    }

    public static void initialize(Path dataDirectory, HytaleLogger pluginLogger) {
        instance = new DataStorage(dataDirectory);
        logger = pluginLogger;
    }

    public static DataStorage getInstance() { return instance; }


    //Loads all CivGuild Data
    public void loadAll(Map<UUID, Guild> guilds, Map<UUID, UUID> players, Map<String, UUID> usernames) {
        if (!guilds.isEmpty() || !players.isEmpty() || !usernames.isEmpty()) {
            logger.at(Level.WARNING).log("Data already loaded, DataStorage.loadAll() should not be getting called");
            return;
        }
        //Load Guild & Player Data
        File[] guildDirectories = new File(dataDirectory.resolve("guilds").toString()).listFiles(); //get list of files under "guilds" directory
        if (guildDirectories == null || guildDirectories.length == 0) { //exit if nothing found
            logger.at(Level.INFO).log("No data files found");
            loadUsernames(usernames); //try load usernames
            return;
        }
        for (File file : guildDirectories) { //run through file list
            Guild guild = loadGuild(file.toPath(), players); //load guild from JSON file, also saves to players Map
            assert guild != null; //should only be null if loadGuild() throws exception
            guilds.put(guild.getUuid(), guild); //save to Map
        }
        logger.at(Level.INFO).log("Guild Data loaded successfully");

        //Load Username Data into Map
        loadUsernames(usernames);
    }

    //saves the username Map
    public void saveUsernames(Map<String, UUID> usernames) {
        try {
            Path trackingDataDirectory = dataDirectory.resolve("tracking");
            Files.createDirectories(trackingDataDirectory.getParent()); //ensure tracking file exists

            JsonArray playersArray = new JsonArray(); //Array to store player data against
            for (String username : usernames.keySet()) { // add the member's data to a JSON OBJECT
                JsonObject playerObj = new JsonObject(); //object to add fields to
                playerObj.addProperty("username", username);
                playerObj.addProperty("uuid", String.valueOf(usernames.get(username)));
                playersArray.add(playerObj); //add to array
            }
            JsonObject rootObj = new JsonObject();
            rootObj.add("players", playersArray); //save the array to root obj

            try (Writer writer = Files.newBufferedWriter(trackingDataDirectory)) { //create a writer
                gson.toJson(rootObj, writer); //write the root JSON object to file
            }

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Could not save tracking data: " + e.getMessage());
        }
    }

    //loads the username Map
    private void loadUsernames(Map<String, UUID> usernames) {
        try (Reader reader = Files.newBufferedReader(dataDirectory.resolve("tracking"))) { //init reader for the usernames/tracking file
            JsonObject root = gson.fromJson(reader, JsonObject.class); //read file to JSON object
            if (root == null || !root.has("players")) { //check the file & field exists
                logger.at(Level.INFO).log("No tracking file found");
                return;
            }
            //Guild Data
            for (JsonElement playerElement : root.getAsJsonArray("players")) { //get "players" object as set of elements & run through it
                JsonObject playerObj = playerElement.getAsJsonObject(); //json element to json object
                String username = playerObj.get("username").getAsString(); //retrieve fields from the object
                UUID uuid = UUID.fromString(playerObj.get("uuid").getAsString());
                usernames.put(username, uuid); //save to Map
            }
            } catch (Exception e) {
                logger.at(Level.SEVERE).log("Could not load guild data: " + e.getMessage());
            }
            logger.at(Level.INFO).log("Tracking Data loaded successfully");
    }

    //saves a single guild
    public void saveGuild(Guild guild) {
        try {

            Path guildDataDirectory = dataDirectory.resolve("guilds").resolve(guild.getUuid().toString()); //ensure path exists plus additional 'guilds' folder plus guild's name
            Files.createDirectories(guildDataDirectory.getParent()); //NOTE this creates or overrides a file of the UUID name

            JsonObject guildObj = new JsonObject(); //create a JSON object and write all guild info to it
            guildObj.addProperty("guildUuid", guild.getUuid().toString());
            guildObj.addProperty("createdTimestamp", guild.getCreatedTimestamp());
            guildObj.addProperty("name", guild.getName());
            guildObj.addProperty("nameTimestamp", guild.getNameTimestamp());
            guildObj.addProperty("r", guild.getColour().getRed()); //colour must be decoded into separate ints
            guildObj.addProperty("g", guild.getColour().getGreen());
            guildObj.addProperty("b", guild.getColour().getBlue());
            guildObj.addProperty("colourTimestamp", guild.getColourTimestamp());
            guildObj.addProperty("spx", guild.getSpawnpoint().x); //Hytale Vector3d can't decode from string so save as doubles
            guildObj.addProperty("spy", guild.getSpawnpoint().y);
            guildObj.addProperty("spz", guild.getSpawnpoint().z);
            guildObj.addProperty("spawnTimestamp", guild.getSpawnTimestamp());
            //TODO add any other guild variables

            JsonArray membersArray = new JsonArray(); //create an array to write member objects to
            for (GuildMember member : guild.getMembers().values()) { // add the member's data to a JSON OBJECT
                JsonObject memberObj = new JsonObject();
                memberObj.addProperty("playerUuid", member.getPlayerUuid().toString());
                memberObj.addProperty("rank", member.getRank().toString()); //NOTE toString used since loadData() uses valueOf()
                //TODO add any other member variables
                membersArray.add(memberObj);
            }
            guildObj.add("member", membersArray); //add the members array to the guild object

            try (Writer writer = Files.newBufferedWriter(guildDataDirectory)) { //create a writer
                gson.toJson(guildObj, writer); //write the guild JSON object to file
            }

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Could not save guild data: " + e.getMessage());
        }
    }

    //Removes a guild file
    public void deleteGuild(UUID uuid) {
        try {
            boolean result = Files.deleteIfExists(dataDirectory.resolve("guilds").resolve(uuid.toString()));
            if (!result) {
                logger.at(Level.WARNING).log("Could not delete guild data");
            }
        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Could not delete guild data: " + e.getMessage());
        }
    }

    //returns a single guild from file
    private Guild loadGuild(Path guildDataFile, Map<UUID, UUID> players) {
        try (Reader reader = Files.newBufferedReader(guildDataFile)) { //init reader for this file
            JsonObject guildObj = gson.fromJson(reader, JsonObject.class); //read file to JSON object
            //retrieve Guild fields from JSON object
            UUID guildUuid = UUID.fromString(guildObj.get("guildUuid").getAsString()); //Guild UUID
            long createdTimestamp = guildObj.get("createdTimestamp").getAsLong();
            String name = guildObj.get("name").getAsString(); //Guild name
            long nameTimestamp = guildObj.get("nameTimestamp").getAsLong();
            com.hypixel.hytale.math.vector.Vector3d spawnpoint = new com.hypixel.hytale.math.vector.Vector3d(guildObj.get("spx").getAsDouble(), guildObj.get("spy").getAsDouble(), guildObj.get("spz").getAsDouble());
            long spawnTimestamp = guildObj.get("spawnTimestamp").getAsLong();
            Color colour = new Color(guildObj.get("r").getAsInt(), guildObj.get("g").getAsInt(), guildObj.get("b").getAsInt());
            long colorTimestamp = guildObj.get("colourTimestamp").getAsLong();
            //TODO any other guild variables

            //Member Data
            Map<UUID, GuildMember> members = new HashMap<>(); //temp for storing members
            for (JsonElement memberElement : guildObj.getAsJsonArray("member")) { //run through the JSON elements under the "member" field
                JsonObject memberObj = memberElement.getAsJsonObject(); //json element to json object

                UUID playerUuid = UUID.fromString(memberObj.get("playerUuid").getAsString()); //Member UUID
                GuildRank rank = GuildRank.valueOf(memberObj.get("rank").getAsString()); //Member rank
                //TODO any other member variables

                //Create Member object and save to member map
                GuildMember member = new GuildMember(playerUuid, rank);
                members.put(playerUuid, member);
            }
            //Create guild object & load in all above retrieved Data
            Guild guild = new Guild(guildUuid, members, name, spawnpoint, colour, createdTimestamp, nameTimestamp, spawnTimestamp, colorTimestamp);
            //Add each member to the players Map
            for (UUID playerUuid : members.keySet()) {
                players.put(playerUuid, guildUuid);
            }
            return guild; //return the guild object

        } catch (Exception e) {
        logger.at(Level.SEVERE).log("Could not load guild data: " + e.getMessage());
        }
        return null;
    }

}
