package com.civtale.civguild;

import com.civtale.civguild.util.DataStorage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;

import static java.lang.Math.round;

public class GuildManager {
    private static GuildManager instance; //ref to self for other classes to get
    private static HytaleLogger logger;
    private static final Map<UUID, Guild> guilds = new HashMap<>(); //Map of all guilds
    private static final Map<UUID, UUID> players = new HashMap<>(); //Map of in-guild players UUID against guild UUID
    private static final Map<String, UUID> usernames = new HashMap<>(); //Map of all players UUID against usernames
    private static final Map<UUID, UUID> invites = new HashMap<>(); //Map of pending invites player UUID against guild UUID
    
    private GuildManager() {}
    
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
        if (instance == null) {
            logger.at(Level.SEVERE).log("GuildManager was not instanced before getting called");
        }
        return instance;
    }

    //Returns a guild from name, or null if it doesn't exist
    public Guild getGuildByName(String guildName) {
        //stream of Guild objects -> if lowercase name matches guildName
        for (Guild guild : guilds.values()) {
            if (guild.getName().equalsIgnoreCase(guildName)) {
                return guild;
            }
        }
        return null;
    }

    //Returns guild when given a member uuid
    public Guild getGuildByMember(UUID member) {
        return guilds.get(players.get(member));
    }

    public Collection<Guild> getGuilds() { return guilds.values(); }

    //Returns a player's UUID from their username, null if it doesn't exist
    public UUID getUUIDByName(String name) {
        return usernames.get(name);
    }

    //returns a name by UUID
    public String getNameByUUID(UUID uuid) {
        for (Map.Entry<String, UUID> entry : usernames.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                return entry.getKey();
            }
        }
        return "Error: invalid player UUID";
    }

    //Run on player Join to ensure username is up to date
    public void updateUsername(UUID uuid, String username) {
        if (usernames.get(username) != null ) {
            if (usernames.get(username).equals(uuid)) return; //nothing to change if saved details match
            usernames.remove(username); //in the case player UUIDs have swapped name, remove the entry to redo it
        }
        // either renamed player or new player, search for their UUID
        for (Map.Entry<String, UUID> entry : usernames.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                usernames.remove(entry.getKey()); //UUID match found, remove it
                break; //break the loop
            }
        }
        usernames.put(username, uuid); //save new entry
    }

    private void messageUUID(UUID uuid, String message) {
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if (playerRef == null) return; //skip offline players
        playerRef.sendMessage(Message.raw(message));
    }

    ///Below guild logic methods require a caller playerRef, Any output is given back to this player and the affected player if a different player

    //Creates a new guild when given a name and a leader
    public void createGuild(UUID callerUUID, String guildName, UUID leaderUUID) {
        //Check caller
        if (!(new PermChecker(callerUUID, null).isOP() || callerUUID.equals(leaderUUID))){ //OK if caller is OP OR they are the leader (can't make guild for another player)
            messageUUID(callerUUID, "[CivGuild] You don't have permission for this");
            return;
        }
        //Check leader
        if (players.containsKey(leaderUUID)) {
            messageUUID(callerUUID, "[CivGuild] Cannot create guild: Leader is already in a guild");
            return;
        }
        //Check guild name
        String checkMessage = guildNameCheck(guildName);
        if (checkMessage != null) {
            messageUUID(callerUUID, "[CivGuild] Cannot create guild: " + checkMessage);
            return;
        }
        //All OK - Create guild
        Guild guild = new Guild(guildName, leaderUUID);
        guilds.put(guild.getUuid(), guild); //save guild against guild uuid
        players.put(leaderUUID, guild.getUuid()); //save player uuid against guild uuid
        updatePlayerNameplate(guild, leaderUUID); //Player status has changed so update their nametag
        //Save changes
        DataStorage.getInstance().saveData(guilds); //TODO save new guild data
        //Announce & Log
        String username = getNameByUUID(leaderUUID);
        guild.memberMessage(leaderUUID, "Created with you as the leader");
        if (!callerUUID.equals(leaderUUID)) { messageUUID(callerUUID, "[CivGuild] " + guild.getName() + " created successfully with " + username + " as the leader"); } //different caller
        logger.at(Level.INFO).log(guild.getName() + " created successfully with " + username + " as the leader");
    }

    //Disbands the given guild
    public void disbandGuild(UUID callerUUID, Guild guild) {
        //Check Caller
        if (!new PermChecker(callerUUID, guild).canDisband()){
            messageUUID(callerUUID, "[CivGuild] You don't have permission for this");
            return;
        }
        //All OK - Disband guild
        for (GuildMember member : guild.getMembers().values()) { //run through members and remove all
            players.remove(member.getPlayerUuid());
            updatePlayerNameplate(null, member.getPlayerUuid()); //Player status has changed so update their nametag
            removeGuildRespawn(member.getPlayerUuid()); //remove spawnpoint
        }
        String guildName = guild.getName();
        guild.notifyMembers("Has been disbanded");
        guilds.remove(guild.getUuid()); //remove guild object and with it all the members
        //Save changes
        DataStorage.getInstance().saveData(guilds); //TODO delete guild data
        //Announce & Log
        messageUUID(callerUUID, "[CivGuild] " + guildName + " has been disbanded");
        logger.at(Level.INFO).log(guildName + " has been disbanded");
    }

    //Adds the given player to the given guild
    public void addMember(UUID callerUUID, Guild guild, UUID playerUUID) {
        //Check Caller
        if (!new PermChecker(callerUUID, guild).canAddMember()) {
            messageUUID(callerUUID, "[CivGuild] You don't have permission for this");
            return;
        }
        //Check adding player
        if (players.containsKey(playerUUID)) {
            messageUUID(callerUUID, "[CivGuild] Cannot add member: Player is already in a guild");
            return;
        }
        //All OK - Add player
        String username = getNameByUUID(playerUUID);
        guild.notifyMembers(username + " has joined the guild"); // before adding so the joining player doesn't get the message
        invites.remove(playerUUID); //removes the player's pending invite if it exists
        guild.addMember(playerUUID);
        players.put(playerUUID, guild.getUuid());
        updatePlayerNameplate(guild, playerUUID); //Player status has changed so update their nametag
        //Save changes
        DataStorage.getInstance().saveData(guilds); //TODO edit this guild only
        //Announce & Log
        guild.memberMessage(playerUUID, "Welcome, you are now a member");
        messageUUID(callerUUID, username + " is now a member of " + guild.getName());
        logger.at(Level.INFO).log(username + " is now a member of " + guild.getName());
    }

    //Removes the given player from their current guild
    public void removeMember(UUID callerUUID, UUID memberUUID, String kickReason) {
        //Check Member (only performing this before the perms since the guild is unknown and may be null)
        Guild guild = getGuildByMember(memberUUID); //lookup guild from the member's UUID
        if (guild == null) {
            messageUUID(callerUUID, "[CivGuild] This player is not in a guild");
            return;
        }
        //Check Caller
        if (!new PermChecker(callerUUID, guild).canKickMember(memberUUID)) {
            messageUUID(callerUUID, "[CivGuild] You don't have permission for this");
            return;
        }
        //Check Member
        if (guild.getLeaderUuids().contains(memberUUID) && guild.getLeaderUuids().size() == 1) { //cannot kick a leader if there is only one
            messageUUID(callerUUID,"[CivGuild] Cannot remove member: Member is the only guild leader, assign a new leader first");
            return;
        }
        //All OK - Remove player
        guild.removeMember(memberUUID);
        players.remove(memberUUID);
        updatePlayerNameplate(null, memberUUID); //Player status has changed so update their nametag
        removeGuildRespawn(memberUUID); //remove respawn point
        //Save changes
        DataStorage.getInstance().saveData(guilds); //TODO edit this guild only
        //Announce & Log
        String username = getNameByUUID(memberUUID);
        messageUUID(memberUUID, "[CivGuild] You have been removed from " + guild.getName() + " for reason: " + kickReason);
        if (!callerUUID.equals(memberUUID)) {messageUUID(callerUUID, username + " has been removed from " + guild.getName());}
        guild.notifyMembers(username + " has left the guild");
        logger.at(Level.INFO).log(username + " has been removed from " + guild.getName());
    }

    //Assigns the given player the given rank
    public void assignRank(UUID callerUUID, UUID memberUUID, GuildRank rank) {
        Guild guild = getGuildByMember(memberUUID);
        if (guild == null) {
            messageUUID(callerUUID, "[CivGuild] This player is not in a guild");
            return;
        }
        //Check Caller
        if (!new PermChecker(callerUUID, guild).canAssignRank(memberUUID, rank)) {
            messageUUID(callerUUID, "[CivGuild] You don't have permission for this");
            return;
        }
        //Check Member
        if (guild.getMember(memberUUID).getRank() == rank) { //assigning to rank already held
            messageUUID(callerUUID, "[CivGuild] Member is already " + rank.getDisplayName());
            return;
        }
        if (guild.getLeaderUuids().contains(memberUUID) && guild.getLeaderUuids().size() == 1) { // if the only leader is being assigned a different rank
            messageUUID(callerUUID, "[CivGuild] Cannot assign rank: Member is the only guild leader, assign a new leader first");
            return;
        }
        //All OK - Assign rank
        guild.assignRank(memberUUID, rank);
        updatePlayerNameplate(guild, memberUUID); //Player status has changed so update their nametag
        //Save Changes
        DataStorage.getInstance().saveData(guilds); //TODO edit this guild only
        //Announce & Log
        String username = getNameByUUID(memberUUID);
        messageUUID(callerUUID, username + " has been assigned to " + rank.getDisplayName());
        guild.notifyMembers( username + " has been assigned rank " + rank.getDisplayName());
        logger.at(Level.INFO).log(username + " has been assigned rank " + rank.getDisplayName());
    }

    //Renames the given guild with the given name
    public void renameGuild(UUID callerUUID, Guild guild, String newName) {
        //Check Caller
        if (!new PermChecker(callerUUID, guild).canRename()) {
            messageUUID(callerUUID, "[CivGuild] You don't have permission for this");
            return;
        }
        //Check new name
        String checkMessage = guildNameCheck(newName);
        if (checkMessage != null) {
            messageUUID(callerUUID, "[CivGuild] Cannot rename guild: " + checkMessage);
            return;
        }
        //All OK - rename if cooldown permits
        String oldName = guild.getName();
        long cooldown = guild.setName(newName);
        if (cooldown > 0) { //name won't be changed if bigger than 0
        messageUUID(callerUUID, "[CivGuild] Cannot set name: " + Duration.ofSeconds(cooldown) + " cooldown remaining");
        return;
        }
        //Save changes
        DataStorage.getInstance().saveData(guilds); //TODO update guild only
        for(GuildMember member : guild.getMembers().values()) { //Player status has changed so update their nametag
            updatePlayerNameplate(guild, member.getPlayerUuid());
        }
        //Announce & Log
        messageUUID(callerUUID, "Guild " + oldName + " has been renamed to " + newName);
        guild.notifyMembers("Guild " + oldName + " has been renamed to " + newName);
        logger.at(Level.INFO).log("Guild" + oldName + " has been renamed to " + newName);
    }

    //Sets the spawn of the given guild to the given coords
    public void setSpawn(UUID callerUUID, Guild guild, com.hypixel.hytale.math.vector.Vector3d coords) {
        //Check Caller
        if (!new PermChecker(callerUUID, guild).canSetSpawn()) {
            messageUUID(callerUUID, "[CivGuild] You don't have permission for this");
            return;
        }
        //TODO check if coords are valid
        //All OK - set spawn change if cooldown permits
        long cooldown = guild.setSpawnpoint(coords);
        if (cooldown > 0) { //spawn won't be changed if bigger than 0
            messageUUID(callerUUID, "[CivGuild] Cannot set spawn: " + Duration.ofSeconds(cooldown) + " cooldown remaining");
            return;
        }
        for (UUID uuid : guild.getMembers().keySet()){ //remove the spawnpoint from all members, NOTE if they respawn they will receive it
            removeGuildRespawn(uuid);
        }
        //Save changes
        DataStorage.getInstance().saveData(guilds); //TODO update guild only
        //Announce & Log
        String coordsStr = (int)round(coords.x) + ", " + (int)round(coords.y) + ", " + (int)round(coords.z);
        messageUUID(callerUUID, guild.getName() + " spawn has been set to (" + coordsStr + ")");
        guild.notifyMembers("Default guild spawnpoint has been set to (" + coordsStr + ")");
        logger.at(Level.INFO).log(guild.getName() + " spawn has been set to (" + coordsStr + ")");
    }
    //Sets the colour of a guild
    public void setColour(UUID callerUUID, Guild guild, Color colour) {
        //Check Caller
        if (!new PermChecker(callerUUID, guild).canSetColour()) {
            messageUUID(callerUUID, "[CivGuild] You don't have permission for this");
            return;
        }
        //All OK - change colour if cooldown permits
        long cooldown = guild.setColour(colour);
        if (cooldown > 0) { //Colour won't be changed if bigger than 0
            messageUUID(callerUUID, "[CivGuild] Cannot set colour: " + Duration.ofSeconds(cooldown) + " cooldown remaining");
            return;
        }
        //Save & announce
        DataStorage.getInstance().saveData(guilds); //TODO update guild only
        messageUUID(callerUUID, guild.getName() + " colour has been set to " + colour.toString());
        guild.notifyMembers("Guild colour changed");
        logger.at(Level.INFO).log(guild.getName() + " colour has been set to " + colour);
    }

    ///Invite System Methods, the caller is the player being affected
    //Sends request from caller to the given guild
    public void joinRequest(UUID callerUUID, Guild guild) {
        //Check Caller
        if (players.containsKey(callerUUID)) { //can't join if already in a guild
            messageUUID(callerUUID, "[CivGuild] Cannot join guild: Already in a guild, leave it to join another guild");
            return;
        }
        if (invites.containsKey(callerUUID)) { //player already has a pending invite
            if (invites.get(callerUUID) == guild.getUuid()) { //already sent a request to this guild
                messageUUID(callerUUID,"[CivGuild] Join request already sent");
                return;
            } else { //otherwise the pending request is for a different guild, so cancel it before continuing
                cancelRequest(callerUUID);
            }
        }
        //Send Invite
        invites.put(callerUUID, guild.getUuid());
        guild.notifyMembersByRank(GuildRank.COLEADER, getNameByUUID(callerUUID) + " has requested to join");
        messageUUID(callerUUID,"[CivGuild] Request to join " + guild.getName() + " sent");
    }
    //Cancels the current request for the given player
    public void cancelRequest(UUID callerUUID) {
        if (!invites.containsKey(callerUUID)) {
            messageUUID(callerUUID, "[CivGuild] No pending join request found");
            return;
        }
        Guild guild = guilds.get(invites.get(callerUUID));
        invites.remove(callerUUID);
        guild.notifyMembersByRank(GuildRank.COLEADER, getNameByUUID(callerUUID) + "'s join request has been cancelled");
        messageUUID(callerUUID, "[CivGuild] Join request for " + guild.getName() + " cancelled");
    }
    //Accepts the request for the given player
    public void acceptJoin(UUID callerUUID, UUID playerUUID) {
        //Check invite
        if (!invites.containsKey(playerUUID)) {
            messageUUID(callerUUID,"[CivGuild] This player hasn't requested to join a guild");
            return;
        }
        // Run add Member - this does perm checks, cancels the invite & notifies
        Guild guild = guilds.get(invites.get(playerUUID)); //Note must be joining player's guild not the caller's
        addMember(callerUUID, guild, playerUUID);
    }
    //Rejects the request for the given player
    public void rejectJoin(UUID callerUUID, UUID playerUUID) {
        //Check invite
        if (!invites.containsKey(playerUUID)) {
            messageUUID(callerUUID,"[CivGuild] This player hasn't requested to join a guild");
            return;
        }
        //Check caller
        Guild guild = guilds.get(invites.get(playerUUID)); //Note must be joining player's guild not the caller's
        if (!new PermChecker(callerUUID, guild).canManageJoinRequests()) {
            messageUUID(callerUUID, "[CivGuild] You don't have permission for this");
            return;
        }
        //Remove invite & notify
        invites.remove(playerUUID);
        messageUUID(playerUUID,"[CivGuild] Join request for " + guild.getName() + " was rejected");
        guild.notifyMembersByRank(GuildRank.COLEADER, getNameByUUID(playerUUID) + "'s join request rejected");
    }
    //Returns current join requests for the caller's guild
    public Set<String> getInvitesByGuild(UUID callerUUID) {
        Set<String> guildRequests = new HashSet<>();
        Guild guild = getGuildByMember(callerUUID);
        //Check Caller
        if (guild == null || !guild.getMember(callerUUID).getRank().canManageJoinRequests()) {
            messageUUID(callerUUID, "[CivGuild] You don't have permission for this");
            return guildRequests;
        }
        //Get invites
        for (UUID uuid : invites.keySet()) {
            if (invites.get(uuid).equals(guild.getUuid())) {
                guildRequests.add(getNameByUUID(uuid)); //save player's name
            }
        }
        return guildRequests;
    }


    // Compact permission checks
    private record PermChecker(UUID callerUuid, Guild guild) {

        public boolean isOP() {
            return PermissionsModule.get().getGroupsForUser(callerUuid).contains("OP");
        }

        public GuildRank getRank() {
            return guild.getMember(callerUuid).getRank();
        }

        public boolean isInGuild() {
            return players.get(callerUuid) == guild.getUuid();
        } //NOTE checks if the caller is in the guild provided to constructor

        public boolean isRankedAbove(UUID memberUUID) { //caller is ranked above the given member //NOTE both must be in same guild
            return getRank().getPermissionLevel() >= guild.getMember(memberUUID).getRank().getPermissionLevel();
        }

        public boolean canDisband() { return isOP() || (isInGuild() && getRank().canDisband()); } //caller is OP OR (in this guild AND has 'disband' permission)
        public boolean canAddMember() { return isOP() || (isInGuild() && getRank().canAddMember()); } //caller is OP OR (in this guild AND has add-member permission)
        public boolean canKickMember(UUID memberUUID) { //Kicked member must have a lower rank
            if (callerUuid.equals(memberUUID) || isOP()) { return true; }//if caller is leaving themselves, no permission required OR is OP
            return isInGuild() && getRank().canKickMember() && isRankedAbove(memberUUID); //caller is in this guild AND has kick-member permission AND ranks above/equal to the member
        }
        public boolean canAssignRank(UUID memberUuid, GuildRank rank) { //pro/demoted member must have a lower rank
            if (isOP()) {return true;} //OP or in the same guild AND has assign perms AND caller is above/equalv member's rank AND caller's rank is above/equal to the new rank
            return isInGuild() && getRank().canAssignRank() && isRankedAbove(memberUuid) && getRank().getPermissionLevel() >= rank.getPermissionLevel();
        }
        public boolean canRename() { return isOP() || (isInGuild() && getRank().canRename()); }
        public boolean canSetSpawn() { return isOP() || (isInGuild() && getRank().canSetSpawn()); }
        public boolean canSetColour() { return isOP() || (isInGuild() && getRank().canSetColour()); }
        public boolean canManageJoinRequests() { return isOP() || (isInGuild() && getRank().canManageJoinRequests()); }
    }

    //Checks if a string is a valid guild name & returns a message if not
    private String guildNameCheck(String guildName) {
        if (guildName.isEmpty()) {
            return "Name cannot be empty";
        }
        if (2 >= guildName.length() || guildName.length() >= 24) {
            return "Name must be between 2 and 24 characters";
        }
        if (!guildName.matches("[a-zA-z]+")) { //TODO allow spaces - also accepts _ currently..change _ to space? or wait for "string " support
            return "Name must only include letters";
        }
        if (getGuildByName(guildName) != null) {
            return "This guild already exists";
        }
        return null; // all checks passed
    }

    //Updates the given player's nameplate
    public void updatePlayerNameplate(Guild guild, UUID uuid) {
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if (playerRef == null) { //player is most likely offline so don't need to update
            return;
        }
        String displayText = playerRef.getUsername();
        if (guild != null) { //Update player nameplate if they are in a guild
            GuildRank rank = guild.getMember(uuid).getRank();
            if (rank.getPermissionLevel() > 1) { //add [guild][rank] if higher than member
                displayText = "[" + rank.getDisplayName() + "][" + guild.getName()+ "]\n" + displayText; //TODO multiline support \n doesnt work currently
            } else { //otherwise just [guild]
                displayText = "[" + guild.getName() + "]\n" + displayText;
            }
        }

        //Thread safe edit component
        Ref<EntityStore> ref = playerRef.getReference();
        assert ref != null;
        Store<EntityStore> store = ref.getStore();
        assert playerRef.getWorldUuid() != null;
        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        assert world != null;
        String finalDisplayText = displayText;
        world.execute(() -> {
            Objects.requireNonNull(store.getComponent(ref, Nameplate.getComponentType())).setText(finalDisplayText); //TODO colour support?
        });
    }

    //Removes a guild spawn point from a player's respawn data
    private void removeGuildRespawn(UUID uuid) {
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if (playerRef == null) { //TODO edit offline playerdata?
            return;
        }
        Ref<EntityStore> ref = playerRef.getReference();
        Store<EntityStore> store = Objects.requireNonNull(ref).getStore();
        Player player = store.getComponent(ref, Player.getComponentType());
        assert player != null;
        PlayerConfigData config = player.getPlayerConfigData();
        World world = player.getWorld();
        assert world != null;
        PlayerWorldData worldData = config.getPerWorldData(world.getName()); //player's config data in this world
        if (worldData.getRespawnPoints() == null) { return;} //nothing to remove
        //Editing component so be thread-safe:
        world.execute(() -> {
            //Find the guild point
            PlayerRespawnPointData[] respawnPoints = worldData.getRespawnPoints();
            int marker = 10; //must be a high int to not run in case guild spawnpoint doesn't exist
            for (int i = 0; i < respawnPoints.length; i++) { //run through respawn point objects
                if (respawnPoints[i].getName().equals("Guild Spawnpoint")){ //check if this is the guild spawnpoint
                    marker = i;
                }
            }
            //make a new array excluding the guild point
            PlayerRespawnPointData[] newRespawnPoints = new PlayerRespawnPointData[respawnPoints.length-1];
            int tracker = 0;
            for (PlayerRespawnPointData respawnPoint : worldData.getRespawnPoints()) {
                if (tracker != marker){
                    newRespawnPoints[tracker] = respawnPoint;
                    tracker++;
                }
            } //update player spawn points
            worldData.setRespawnPoints(newRespawnPoints);
            config.markChanged();
        });
    }
}
