package com.civtale.civguild;

import com.civtale.civguild.util.DataStorage;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildManager {
    private static GuildManager instance; //ref to self for other classes to get
    private static HytaleLogger logger;
    private static final Map<UUID, Guild> guilds = new HashMap<>(); //Map of all guilds
    private static final Map<UUID, UUID> players = new HashMap<>(); //Map of player UUID against guild UUID
    
    private GuildManager() {
        //TODO load()
    }
    
    //Initialise GuildManager which loads data from save
    public static void initialize(HytaleLogger pluginLogger) {
        if (instance == null) {
            instance = new GuildManager();
        }
        logger = pluginLogger;
        //Load save data - DataStorage must be init before manager
        DataStorage.getInstance().loadData(guilds, players);
    }
    
    public static GuildManager getInstance() {
        //TODO throw exception if not instanced yet
        return instance;
    }

    //Returns guild UUID from String or null if no match
    public UUID getGuildUUID(String guildName) {
        //stream of Guild objects -> if lowercase name matches guildName
        for (Guild guild : guilds.values()) {
            if (guild.getName().equalsIgnoreCase(guildName)) {
                return guild.getUuid();
            }}
        return null;
    }

    public Guild getGuild(UUID uuid) {
        return guilds.get(uuid);
    }

    public Collection<Guild> getGuilds() { return guilds.values(); }


    //TODO below are all placeholders, need to determine exaclt what they do, what params the need
    //TODO currently doing bool outputs for command errors, may need to use error codes for more info (could do an advanced enum errors so they can be printed & translated)

    //Below funcs require a playerRef to who called it, Any output is given back to this player
    //The playerRef isn't used for anything else, since it can be called by someone in a different guild (admins), the guild being managed must be passed in as well
    //Funcs must check that players perms to whether or not they have perms, or if they're OP

    public boolean createGuild(PlayerRef leaderRef, String guildName) {
        //TODO ensure playerRef is actually a player? & not part of another guild, check name for non-letter chars etc etc
        Guild guild = new Guild(guildName, leaderRef);
        guilds.put(guild.getUuid(), guild); //save guild against guild uuid
        players.put(leaderRef.getUuid(), guild.getUuid()); //save player uuid against guild uuid

        //Save changes //TODO a better way than rewriting entire files? May need individual files for each player/guild
        DataStorage.getInstance().saveData(guilds, players);

        return true;
    }

    public boolean disbandGuild(PlayerRef playerRef) {
        //TODO lookup guild by player uuid, check rank perms
        return false;
    }

    public void joinGuild(PlayerRef playerRef, String arg) {
        //TODO implement invite system
    }

    public void acceptJoin(PlayerRef playerRef) {
        //TODO implement invite system, check rank perms
    }

    public void rejectJoin(PlayerRef playerRef) {
        //TODO implement invite system, check rank perms
    }

    public void kick(PlayerRef playerRef, String playerName) {
        //TODO lookup player, check rank perms
    }

    public void assignRank(PlayerRef playerRef, String playerName, GuildRank rank) {
        //TODO lookup player, check rank perms, rank string to enum
    }

    public void renameGuild(PlayerRef playerRef, String arg) {
        //TODO check rank
    }

    public void setSpawn(PlayerRef playerRef) {
        //TODO check rank
    }
}
