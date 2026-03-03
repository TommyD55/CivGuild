package com.civtale.civguild.util;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildMember;
import com.civtale.civguild.GuildRank;
import com.google.gson.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.util.LoggerPrintStream;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.io.*;
import java.lang.reflect.Member;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
    public void saveData(Map<UUID, Guild> guilds, Map<UUID, UUID> players) {
        try {
            Files.createDirectories(dataDirectory.getParent());
            JsonObject root = new JsonObject();
            JsonArray guildsArray = new JsonArray();

            //Guild Data
            for (Guild guild : guilds.values()) { //run through each guild
                JsonObject guildObj = new JsonObject(); //create a json element and add the guild's data to it
                guildObj.addProperty("name", guild.getName());
                guildObj.addProperty("guildUuid", guild.getUuid().toString());
                //TODO add any other guild variables

                //Member Data
                JsonArray membersArray = new JsonArray(); //run through each member
                for (GuildMember member : guild.getMembers()) { //build a json element and add the member's data to it
                    JsonObject memberObj = new JsonObject();
                    memberObj.addProperty("playerName", member.getPlayerName());
                    memberObj.addProperty("playerUuid", member.getPlayerUuid().toString());
                    memberObj.addProperty("rank", member.getRank().toString()); //NOTE toString used since loadData() uses valueOf()
                    //TODO add any other member variables
                    membersArray.add(memberObj);

                }

                //Add members to the guild, then the guild to the json array
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
        if (!Files.exists(dataDirectory, new LinkOption[0])) {
            logger.at(Level.INFO).log("No data files found");
            return;
        }
        try (Reader reader = Files.newBufferedReader(dataDirectory)) { //setup json reader
            JsonObject root = (JsonObject)gson.fromJson(reader, JsonObject.class);
            if (root != null && root.has("guild")) { //guild data file exists
                //Guild Data
                for (JsonElement guildElement : root.getAsJsonArray("guild")) { //run through json elements in the guild data file
                    JsonObject guildObj = guildElement.getAsJsonObject(); //json element to json object

                    String name = guildObj.get("name").getAsString(); //Guild name
                    UUID guildUuid = UUID.fromString(guildObj.get("guildUuid").getAsString()); //Guild UUID
                    //TODO any other guild variables

                    //Member Data
                    Map<UUID, GuildMember> members = new HashMap<>(); //temp for storing members
                    for (JsonElement memberElement : guildObj.getAsJsonArray("member")) { //run through json sub-elements in the guild data file
                        JsonObject memberObj = memberElement.getAsJsonObject(); //json element to json object

                        UUID playerUuid = UUID.fromString(memberObj.get("playerUuid").getAsString()); //Member UUID
                        String playerName = memberObj.get("playerName").getAsString(); //Member name
                        GuildRank rank = GuildRank.valueOf(memberObj.get("rank").getAsString()); //Member rank
                        //TODO any other member variables

                        //Create Member object and save to member map
                        GuildMember member = new GuildMember(playerName, playerUuid, rank);
                        members.put(playerUuid, member);
                    }
                    //Create guild object & load in all above retrieved Data
                    Guild guild = new Guild(name, guildUuid, members);
                    //Finally add this guild & it's members to the Maps
                    guilds.put(guild.getUuid(), guild);
                    for (UUID playerUuid : members.keySet()) {
                        players.put(playerUuid, guildUuid);
                    }
                }
            }
            logger.at(Level.INFO).log("Data loaded successfully");

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Could not load data");
        }
    }

}
