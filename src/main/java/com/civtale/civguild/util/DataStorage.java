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

    // saves data from the provided maps
    public void saveData(Map<UUID, Guild> guilds) { //NOTE the 'players' Map in GM isn't required to save as all it's info is inside the guild Map
        try {
            Files.createDirectories(dataDirectory.getParent());
            JsonObject root = new JsonObject();
            JsonArray guildsArray = new JsonArray();

            //Guild Data
            for (Guild guild : guilds.values()) { //run through each guild
                JsonObject guildObj = new JsonObject(); //create a JSON element and add the guild's data to it
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

                //Member Data
                JsonArray membersArray = new JsonArray(); //run through each member
                for (GuildMember member : guild.getMembers().values()) { //build a JSON element and add the member's data to it
                    JsonObject memberObj = new JsonObject();
                    memberObj.addProperty("playerUuid", member.getPlayerUuid().toString());
                    memberObj.addProperty("rank", member.getRank().toString()); //NOTE toString used since loadData() uses valueOf()
                    memberObj.addProperty("username", member.getUsername());
                    //TODO add any other member variables
                    membersArray.add(memberObj);
                }

                //Add members to the guild, then the guild to the JSON array
                guildObj.add("member", membersArray);
                guildsArray.add(guildObj);
            }
            // add all above data to the root, then write it to file
            root.add("guild", guildsArray);
            try (Writer writer = Files.newBufferedWriter(dataDirectory)) {
                gson.toJson(root, writer);
                logger.at(Level.INFO).log("Data saved successfully");
            }

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Could not save data");
        }
    }

    // loads save data into the provided maps
    public void loadData(Map<UUID, Guild> guilds, Map<UUID, UUID> players) {
        if (!guilds.isEmpty() || !players.isEmpty()) {
            logger.at(Level.WARNING).log("Guild data already loaded, DataStorage.loadData() should not be getting called");
            return;
        }
        if (!Files.exists(dataDirectory)) {
            logger.at(Level.INFO).log("No data files found");
            return;
        }
        try (Reader reader = Files.newBufferedReader(dataDirectory)) { //setup json reader
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            if (root != null && root.has("guild")) { //guild data file exists
                //Guild Data
                for (JsonElement guildElement : root.getAsJsonArray("guild")) { //run through JSON elements in the guild data file
                    JsonObject guildObj = guildElement.getAsJsonObject(); //json element to json object

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
                    for (JsonElement memberElement : guildObj.getAsJsonArray("member")) { //run through JSON sub-elements in the guild data file
                        JsonObject memberObj = memberElement.getAsJsonObject(); //json element to json object

                        UUID playerUuid = UUID.fromString(memberObj.get("playerUuid").getAsString()); //Member UUID
                        GuildRank rank = GuildRank.valueOf(memberObj.get("rank").getAsString()); //Member rank
                        String username = memberObj.get("username").getAsString(); //Member name
                        //TODO any other member variables

                        //Create Member object and save to member map
                        GuildMember member = new GuildMember(playerUuid, username, rank);
                        members.put(playerUuid, member);
                    }
                    //Create guild object & load in all above retrieved Data
                    Guild guild = new Guild(guildUuid, members, name, spawnpoint, colour, createdTimestamp, nameTimestamp,  spawnTimestamp, colorTimestamp);
                    //Finally add this guild & it's members to the Maps
                    guilds.put(guild.getUuid(), guild);
                    for (UUID playerUuid : members.keySet()) {
                        players.put(playerUuid, guildUuid);
                    }
                }
                logger.at(Level.INFO).log("Data loaded successfully");
            }

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Could not load data: " + e.getMessage());
        }
    }

}
