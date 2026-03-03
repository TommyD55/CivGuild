package com.civtale.civguild.util;

import com.civtale.civguild.Guild;
import com.google.gson.Gson;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.util.LoggerPrintStream;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataStorage {
    private static DataStorage instance;
    private final Path dataDirectory;
    private static HytaleLogger logger;

    private static final String guildFilename = "guild_data.json";
    private static final String playerFilename = "player_data.json";

    private DataStorage(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public static void initialize(Path dataDirectory, HytaleLogger pluginLogger) {
        instance = new DataStorage(dataDirectory);
        logger = pluginLogger;
    }

    public static DataStorage getInstance() { return instance; }

    //returns null if file not found
    public Map<UUID, Guild> loadGuilds() {
        File file = new File(dataDirectory.toFile(), guildFilename);
        if (!file.exists()) { return new HashMap<>(); }
        // Read data
        try  (FileReader reader = new FileReader(file)) {
            return new Gson().fromJson(reader, Map.class);
        } catch (Exception e) {
            logger.at(Level.SEVERE).log("[CivGuild] Failed to load guild data %s", e.getMessage());
            return new HashMap<>();
        }
    }
    //returns null if file not found
    public Map<UUID, UUID>loadPlayers() {
        File file = new File(dataDirectory.toFile(), playerFilename);
        if (!file.exists()) { return new HashMap<>();  }
        // Read data
        try  (FileReader reader = new FileReader(file)) {
            return new Gson().fromJson(reader, Map.class);
        } catch (Exception e) {
            logger.at(Level.SEVERE).log("[CivGuild] Failed to load player data %s", e.getMessage());
            return new HashMap<>();
        }
    }

    //
    public void saveGuilds(Map<UUID, Guild>  guilds) {
      File file = new File(dataDirectory.toFile(), guildFilename);
      //Write data
      try  (FileWriter writer = new FileWriter(file)) {
          new Gson().toJson(guilds, writer);
      } catch (Exception e) {
          logger.at(Level.SEVERE).log("[CivGuild] Failed to save guild data %s", e.getMessage());
      }
    }

    public void savePlayers(Map<UUID, UUID> players) {
        File file = new File(dataDirectory.toFile(), playerFilename);
        //Write data
        try  (FileWriter writer = new FileWriter(file)) {
            new Gson().toJson(players, writer);
        } catch (Exception e) {
            logger.at(Level.SEVERE).log("[CivGuild] Failed to save player data %s", e.getMessage());
        }
    }
}
