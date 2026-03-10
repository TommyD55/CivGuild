package com.civtale.civguild;

import com.civtale.civguild.util.DataStorage;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.*;
import java.util.logging.Level;

public class GuildManager {
    private static GuildManager instance; //ref to self for other classes to get
    private static HytaleLogger logger;
    private static final Map<UUID, Guild> guilds = new HashMap<>(); //Map of all guilds
    private static final Map<UUID, UUID> players = new HashMap<>(); //Map of player UUID against guild UUID
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

    public Collection<Guild> getGuilds() { return guilds.values(); }


    ///Below guild logic methods require a caller playerRef, Any output is given back to this player and the affected player if a different player

    //Creates a new guild when given a name and a leader
    public void createGuild(PlayerRef callerRef, String guildName, PlayerRef leaderRef) {
        //Check caller
        if (!(PermissionsModule.get().getGroupsForUser(callerRef.getUuid()).contains("OP") || //If caller is OP they can run this
                callerRef == leaderRef)){ //Or if the caller is the leader, ie they are creating their own guild
            callerRef.sendMessage(Message.raw("[CivGuild] You don't have permission for this"));
            return;
        }
        //Check leader
        if (players.containsKey(leaderRef.getUuid())) {
            callerRef.sendMessage(Message.raw("[CivGuild] Cannot create guild: Leader is already in a guild"));
            return;
        }
        //Check guild name
        String checkMessage = guildNameCheck(guildName);
        if (checkMessage != null) {
            callerRef.sendMessage(Message.raw("[CivGuild] Cannot create guild: " + checkMessage));
            return;
        }
        //All OK - Create guild
        Guild guild = new Guild(guildName, leaderRef);
        guilds.put(guild.getUuid(), guild); //save guild against guild uuid
        players.put(leaderRef.getUuid(), guild.getUuid()); //save player uuid against guild uuid
        //Save changes //TODO a better way than rewriting entire files? May need individual files for each player/guild or access only a specific GuildObj
        DataStorage.getInstance().saveData(guilds);
        //Announce & Log
        if (callerRef != leaderRef) { guild.memberMessage(leaderRef.getUuid(), "Created successfully with you as the leader"); }
        callerRef.sendMessage(Message.raw("[CivGuild] " + guild.getName() + " created successfully with " + leaderRef.getUsername() + " as the leader"));
        logger.at(Level.INFO).log(guild.getName() + " created successfully with " + leaderRef.getUsername() + " as the leader");
    }

    //Disbands the given guild
    public void disbandGuild(PlayerRef callerRef, Guild guild) {
        //Check Caller
        PermChecker permChecker = new PermChecker(callerRef, guild);
        if (!(permChecker.isOP() || (permChecker.isInGuild() && permChecker.getRank().canDisband()))){ //caller is OP OR (in this guild AND has 'disband' permission)
            callerRef.sendMessage(Message.raw("[CivGuild] You don't have permission for this"));
            return;
        }
        //All OK - Disband guild
        for (GuildMember member : guild.getMembers()) { //run through members and remove all
            players.remove(member.getPlayerUuid());
            //TODO update player nametags
        }
        String guildName = guild.getName();
        guild.notifyMembers("Has been disbanded");
        guilds.remove(guild.getUuid()); //remove guild object and with it all the members
        //Save changes
        DataStorage.getInstance().saveData(guilds);
        //Announce & Log
        callerRef.sendMessage(Message.raw("[CivGuild] " + guildName + " has been disbanded"));
        logger.at(Level.INFO).log(guildName + " has been disbanded");
    }

    //Adds the given player to the given guild
    public void addMember(PlayerRef callerRef, Guild guild, PlayerRef playerRef) {
        //Check Caller
        PermChecker permChecker = new PermChecker(callerRef, guild);
        if (!(permChecker.isOP() || (permChecker.isInGuild() && permChecker.getRank().canAddMember()))){ //caller is OP OR (in this guild AND has add-member permission)
            callerRef.sendMessage(Message.raw("[CivGuild] You don't have permission for this"));
            return;
        }
        //Check adding player
        if (players.containsKey(playerRef.getUuid())) {
            callerRef.sendMessage(Message.raw("[CivGuild] Cannot add member: Player is already in a guild"));
            return;
        }
        //All OK - Add player
        guild.notifyMembers(playerRef.getUsername() + " has joined the guild"); // before adding so the joining player doesn't get the message
        invites.remove(playerRef.getUuid()); //removes the player's pending invite if it exists
        guild.addMember(playerRef);
        players.put(playerRef.getUuid(), guild.getUuid());
        //Save changes
        DataStorage.getInstance().saveData(guilds);
        //Announce & Log
        guild.memberMessage(playerRef.getUuid(), "Welcome, you are now a member"); //message the joiner, note if the caller is OP and not in the guild they don't receive a message, but they can check the server logs
        logger.at(Level.INFO).log(playerRef.getUsername() + " is now a member of " + guild.getName());
    }

    //Removes the given player from their current guild
    public void removeMember(PlayerRef callerRef, PlayerRef memberRef, String kickReason) {
        //Check Member (only performing this before the perms since the guild is unknown and may be null)
        Guild guild = guilds.get(players.get(memberRef.getUuid())); //lookup guild from the member's UUID
        if (guild == null) {
            callerRef.sendMessage(Message.raw("[CivGuild] This player is not in a guild"));
            return;
        }
        //Check Caller
        PermChecker permChecker = new PermChecker(callerRef, guild);
        if (callerRef != memberRef && !permChecker.isOP()) { //if caller is leaving themselves, no permission required OR is OP
            if (!(permChecker.isInGuild() && permChecker.getRank().canKickMember())) { //caller is in this guild AND has kick-member permission
                callerRef.sendMessage(Message.raw("[CivGuild] You don't have permission for this"));
                return;
            }
            if (permChecker.isRankedBelow(memberRef)) { //Ensure the caller is ranked above the member, ie co-leaders can't kick each other or the leader
                callerRef.sendMessage(Message.raw("[CivGuild] Cannot remove member: Your rank isn't high enough"));
                return;
            }
        }
        //Check Member
        if (guild.getLeaderUuids().contains(memberRef.getUuid()) && guild.getLeaderUuids().size() == 1) { //cannot kick a leader if there is only one
            callerRef.sendMessage(Message.raw("[CivGuild] Cannot remove member: Member is the only guild leader, assign a new leader first"));
            return;
        }
        //All OK - Remove player
        guild.removeMember(memberRef.getUuid());
        players.remove(memberRef.getUuid());
        //Save changes
        DataStorage.getInstance().saveData(guilds);
        //Announce & Log
        memberRef.sendMessage(Message.raw("[CivGuild] You have been removed from " + guild.getName() + " for reason: " + kickReason));  //message the joiner, note if the caller is OP and not in the guild they don't receive a message, but they can check the server logs
        guild.notifyMembers(memberRef.getUsername() + " has left");
        logger.at(Level.INFO).log(memberRef.getUsername() + " has been removed from " + guild.getName());
    }

    //Assigns the given player the given rank
    public void assignRank(PlayerRef callerRef, PlayerRef memberRef, GuildRank rank) {
        Guild guild = guilds.get(players.get(memberRef.getUuid()));
        //Check Caller
        PermChecker permChecker = new PermChecker(callerRef, guild);
        if (!permChecker.isOP()) { //caller is OP
            if (!(permChecker.isInGuild() && permChecker.getRank().canAssignRank())) { //caller is in this guild AND has assign-rank permission
                callerRef.sendMessage(Message.raw("[CivGuild] You don't have permission for this"));
                return;
            }
            if (permChecker.isRankedBelow(memberRef)) { //Ensure the caller is ranked above the member, ie co-leaders can't assign each other or the leader
                callerRef.sendMessage(Message.raw("[CivGuild] Cannot assign rank: Your own rank isn't high enough"));
                return;
            }
        //Check Rank
            if (permChecker.getRank().getPermissionLevel() < rank.getPermissionLevel()) { //rank must be equal or lower than caller's
                callerRef.sendMessage(Message.raw("[CivGuild] Cannot assign rank: Your own rank isn't high enough"));
                return;
            }
        }
        //Check Member
        if (guild.getMember(memberRef.getUuid()).getRank() == rank) { //assigning to rank already held
            callerRef.sendMessage(Message.raw("[CivGuild] Member is already " + rank.getDisplayName()));
            return;
        }
        if (guild.getLeaderUuids().contains(memberRef.getUuid()) && guild.getLeaderUuids().size() == 1) { // if the only leader is being assigned a different rank
            callerRef.sendMessage(Message.raw("[CivGuild] Cannot assign rank: Member is the only guild leader, assign a new leader first"));
            return;
        }
        //All OK - Assign rank
        guild.assignRank(memberRef.getUuid(), rank);
        //Save Changes
        DataStorage.getInstance().saveData(guilds);
        //Announce & Log
        guild.notifyMembersByRank(GuildRank.COLEADER, memberRef.getUsername() + " has been assigned rank " + rank.getDisplayName());
        guild.memberMessage(memberRef.getUuid(), "You have been assigned rank " + rank.getDisplayName()); //message the assignee, note if the caller is OP and not in the guild they don't receive a message, but they can check the server logs
        logger.at(Level.INFO).log(memberRef.getUsername() + " has been assigned rank " + rank.getDisplayName());
    }

    //Renames the given guild with the given name
    public void renameGuild(PlayerRef callerRef, Guild guild, String newName) {
        //Check Caller
        PermChecker permChecker = new PermChecker(callerRef, guild);
        if (!(permChecker.isOP() || (permChecker.isInGuild() && permChecker.getRank().canRename()))){ //caller is OP OR (in this guild AND has 'Rename' permission)
            callerRef.sendMessage(Message.raw("[CivGuild] You don't have permission for this"));
            return;
        }
        //Check cooldown
        //TODO check cooldown
        //Check new name
        String checkMessage = guildNameCheck(newName);
        if (checkMessage != null) {
            callerRef.sendMessage(Message.raw("[CivGuild] Cannot create guild: " + checkMessage));
            return;
        }
        //All OK - rename
        String oldName = guild.getName();
        guild.setName(newName);
        //Save changes
        DataStorage.getInstance().saveData(guilds);
        //Announce & Log
        guild.notifyMembers("Guild " + oldName + " has been renamed to " + guild.getName());
        logger.at(Level.INFO).log("Guild" + oldName + " has been renamed to " + guild.getName());
    }

    //Sets the spawn of the given guild to the given coords
    public void setSpawn(PlayerRef callerRef, Guild guild, Vector3d coords) {
        //Check Caller
        PermChecker permChecker = new PermChecker(callerRef, guild);
        if (!(permChecker.isOP() || (permChecker.isInGuild() && permChecker.getRank().canSetSpawn()))){ //caller is OP OR (in this guild AND has setSpawn permission)
            callerRef.sendMessage(Message.raw("[CivGuild] You don't have permission for this"));
            return;
        }
        //Check cooldown
        //TODO check cooldown + check if spawn is a viable location?
        //All OK - set spawn
        guild.setSpawn(coords);
        //Save changes
        DataStorage.getInstance().saveData(guilds);
        //Announce & Log
        guild.notifyMembers("Default guild spawnpoint has been set to (" + coords.x + ", " + coords.y + ", " + coords.z + ")"); //TODO round these values?
        logger.at(Level.INFO).log(guild.getName() + " spawn has been set to (" + coords.x + ", " + coords.y + ", " + coords.z + ")");
    }


    ///Invite System Methods, the caller is the player being affected
    //Sends request from caller to the given guild
    public void joinRequest(PlayerRef callerRef, Guild guild) {
        UUID callerUuid = callerRef.getUuid();
        //Check Caller
        if (players.containsKey(callerUuid)) { //can't join if already in a guild
            callerRef.sendMessage(Message.raw("[CivGuild] Cannot join guild: Already in a guild, leave it to join another guild"));
            return;
        }
        if (invites.containsKey(callerUuid)) { //player already has a pending invite
            if (invites.get(callerUuid) == guild.getUuid()) { //already sent a request to this guild
                callerRef.sendMessage(Message.raw("[CivGuild] Join request already sent"));
                return;
            } else { //otherwise the pending request is for a different guild, so cancel it before continuing
                cancelRequest(callerRef);
            }
        }
        //Send Invite
        invites.put(callerUuid, guild.getUuid());
        guild.notifyMembersByRank(GuildRank.COLEADER, callerRef.getUsername() + " has requested to join");
        callerRef.sendMessage(Message.raw("[CivGuild] Request to join " + guild.getName() + " sent"));
    }
    //Cancels the current request for the given player
    public void cancelRequest(PlayerRef callerRef) {
        UUID callerUuid = callerRef.getUuid();
        if (!invites.containsKey(callerUuid)) {
            callerRef.sendMessage(Message.raw("[CivGuild] No pending join request found"));
            return;
        }
        Guild guild = guilds.get(invites.get(callerUuid));
        invites.remove(callerUuid);
        guild.notifyMembersByRank(GuildRank.COLEADER, callerRef.getUsername() + "'s join request has been cancelled");
        callerRef.sendMessage(Message.raw("[CivGuild] Join request for " + guild.getName() + " cancelled"));
    }
    //Accepts the request for the given player
    public void acceptJoin(PlayerRef callerRef, PlayerRef joiningPlayerRef) {
        //Check invite
        UUID joiningPlayerUuid = joiningPlayerRef.getUuid();
        if (!invites.containsKey(joiningPlayerUuid)) {
            callerRef.sendMessage(Message.raw("[CivGuild] This player hasn't requested to join a guild"));
            return;
        }
        // Run add Member - this does perm checks, cancels the invite & notifies
        Guild guild = guilds.get(invites.get(joiningPlayerUuid));
        addMember(callerRef, guild, joiningPlayerRef);
    }
    //Rejects the request for the given player
    public void rejectJoin(PlayerRef callerRef, PlayerRef joiningPlayerRef) {
        //Check invite
        UUID joiningPlayerUuid = joiningPlayerRef.getUuid();
        if (!invites.containsKey(joiningPlayerUuid)) {
            callerRef.sendMessage(Message.raw("[CivGuild] This player hasn't requested to join a guild"));
            return;
        }
        //Check caller
        Guild guild = guilds.get(invites.get(joiningPlayerUuid));
        PermChecker permChecker = new PermChecker(callerRef, guild);
        if (!(permChecker.isOP() || (permChecker.isInGuild() && permChecker.getRank().canManageJoinRequests()))){ //caller is OP OR (in this guild AND has join-request permission)
            callerRef.sendMessage(Message.raw("[CivGuild] You don't have permission for this"));
            return;
        }
        //Remove invite & notify
        invites.remove(joiningPlayerUuid);
        joiningPlayerRef.sendMessage(Message.raw("[CivGuild] Join request for " + guild.getName() + " was rejected"));
        guild.notifyMembersByRank(GuildRank.COLEADER, joiningPlayerRef.getUsername() + "'s join request rejected");
    }
    //Returns current join requests for the caller's guild
    public Set<String> getInvitesByGuild(PlayerRef callerRef) {
        Set<String> guildRequests = new HashSet<>();
        Guild guild = guilds.get(players.get(callerRef.getUuid()));
        //Check Caller
        if (guild == null || !guild.getMember(callerRef.getUuid()).getRank().canManageJoinRequests()) {
            callerRef.sendMessage(Message.raw("[CivGuild] You don't have permission for this"));
            return guildRequests;
        }
        //Get invites
        for (UUID uuid : invites.keySet()) {
            if (invites.get(uuid) == guild.getUuid()) {
                String username = guild.getMember(uuid).getUsername();
                guildRequests.add(username); //save player's name
            }
        }
        return guildRequests;
    }


    // Compact permission checks
    private record PermChecker(UUID callerUuid, Guild guild) {
        private PermChecker(PlayerRef callerUuid, Guild guild) {
            this(callerUuid.getUuid(), guild);
        }

        public boolean isOP() {
            return PermissionsModule.get().getGroupsForUser(callerUuid).contains("OP");
        }

        public GuildRank getRank() {
            return guild.getMember(callerUuid).getRank();
        }

        public boolean isInGuild() {
            return players.get(callerUuid) == guild.getUuid();
        }

        public boolean isRankedBelow(PlayerRef memberRef) { //caller is ranked above the given member //NOTE both must be in same guild
            return guild.getMember(callerUuid).getRank().getPermissionLevel() < guild.getMember(memberRef.getUuid()).getRank().getPermissionLevel();
        }
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
}
