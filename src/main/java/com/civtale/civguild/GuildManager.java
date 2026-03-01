package com.civtale.civguild;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildManager {
    private static GuildManager instance; //ref to self for other classes to get
    private final Map<UUID, Guild> guilds = new HashMap<>(); //Map of all guilds
    
    private GuildManager() {
        //TODO load()
    }
    
    //Initialise GuildManager which loads data from save
    public static void initialize() {
        if (instance == null) {
            instance = new GuildManager();
        }
    }
    
    public static GuildManager getInstance() {
        //TODO throw exception if not instanced yet
        return instance;
    }
    
    
    //TODO below are all placeholders, need to determine exaclt what they do, what params the need
    public void createGuild(PlayerRef leaderRef, String guildName) {
        //TODO ensure playerRef is actually a player? & not part of another guild
        Guild guild = new Guild(guildName, leaderRef.getUuid());
        guilds.put(guild.getUuid(), guild);
        //TEMP
        leaderRef.sendMessage(Message.raw("[CivGuild]: Guild Created!\n" + guild));
    }

    public void disbandGuild(String arg) {
    }

    public void joinGuild(Player player, String arg) {
    }

    public void acceptJoin(Player player, String arg) {
    }

    public void rejectJoin(Player player, String arg) {
    }

    public void kick(Player player, String arg) {
    }

    public void assignRank(Player player, String arg, String arg1) {
    }

    public void renameGuild(String arg) {
    }

    public void setSpawn(Player player) {
    }
}
