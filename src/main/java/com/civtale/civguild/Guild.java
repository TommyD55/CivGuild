package com.civtale.civguild;

import com.hypixel.hytale.protocol.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.awt.*;
import java.time.Instant;
import java.util.*;

public class Guild {
    private static final long cooldown = 120; //TWO MINUTES in seconds
    private final UUID uuid; //final uuid never changes
    private final Map<UUID, GuildMember> members;
    private final Set<UUID> leaderUuids;
    //Properties
    private String name; //guild name, can be changed
    private Vector3d spawnpoint;
    private Color colour;
    //Timestamps
    private final long createdTimestamp;
    private long nameTimestamp;
    private long spawnTimestamp;
    private long colourTimestamp;

    //Constructor for new guild
    public Guild(String name, PlayerRef leaderRef) {
        this.name = name;
        this.uuid = UUID.randomUUID(); //get a unique UUID for this guild
        this.members = new HashMap<>();
        this.members.put(leaderRef.getUuid(), new GuildMember(leaderRef, GuildRank.LEADER)); //save the leader as the first member
        this.leaderUuids = new HashSet<>();
        this.leaderUuids.add(leaderRef.getUuid());
        this.spawnpoint = new Vector3d(0, 0, 0);
        this.colour = Color.GRAY;
        this.createdTimestamp = Instant.now().getEpochSecond();
        this.nameTimestamp = 0;
        this.spawnTimestamp = 0;
        this.colourTimestamp = 0;
    }
    //Constructor for guild from save
    public Guild(UUID uuid, Map<UUID, GuildMember> members, String name, Vector3d spawnpoint, Color colour,
                 long createdTimestamp, long nameTimestamp, long spawnTimestamp, long colourTimestamp) {
        this.uuid = uuid; //get a unique UUID for this guild
        this.members = members;
        this.leaderUuids = new HashSet<>();
        for (GuildMember member : members.values()) { //Find the leaders
            if (member.getRank() == GuildRank.LEADER) {
                this.leaderUuids.add(member.getPlayerUuid());
            }
        }
        this.name = name;
        this.spawnpoint = spawnpoint;
        this.colour = colour;
        this.createdTimestamp = createdTimestamp;
        this.nameTimestamp = nameTimestamp;
        this.spawnTimestamp = spawnTimestamp;
        this.colourTimestamp = colourTimestamp;
    }

    ///

    public UUID getUuid() {
        return uuid;
    }

    public Collection<GuildMember> getMembers() { return members.values(); }

    public GuildMember getMember(UUID uuid) { return members.get(uuid); }

    public boolean hasMember(UUID uuid) { return members.containsKey(uuid); }

    public Set<UUID> getLeaderUuids() {
        return leaderUuids;
    }

    public void addMember(PlayerRef playerRef) {
        this.members.put(playerRef.getUuid(), new GuildMember(playerRef, GuildRank.MEMBER));
    }

    public void removeMember(UUID playerUuid) { //GuildManager ensures this isn't the leader
        if (members.get(playerUuid).getRank() == GuildRank.LEADER) { //remove from leader set if this member is a leader
            this.leaderUuids.remove(playerUuid);
        }
        this.members.remove(playerUuid);
    }

    public void assignRank(UUID uuid, GuildRank rank) {
        GuildMember member = this.members.get(uuid);
        if (member.getRank() == GuildRank.LEADER && rank != GuildRank.LEADER) { //if a leader is being demoted, remove them from the leader set
            this.leaderUuids.remove(uuid);
        }
        if (rank == GuildRank.LEADER) { //if member is being promoted to a leader, add them to the set
            this.leaderUuids.add(uuid);
        }
        member.setRank(rank); //set their rank
    }

    /// Property setters/getters
    public String getName() {
        return name;
    }

    public long setName(String name) {
        long delta = nameTimestamp - Instant.now().getEpochSecond() + cooldown;
        if (delta < 0) {
            this.name = name;
            nameTimestamp = Instant.now().getEpochSecond();
            return 0;
        }
        return delta;
    }

    public Vector3d getSpawnpoint() {
        return this.spawnpoint;
    }

    public long setSpawnpoint(Vector3d spawnpoint) {
        long delta = spawnTimestamp - Instant.now().getEpochSecond() + cooldown;
        if (delta < 0) {
            this.spawnpoint = spawnpoint;
            spawnTimestamp = Instant.now().getEpochSecond();
            return 0;
        }
        return delta;
    }

    public Color getColour() {
        return colour;
    }

    public long setColour(Color colour) {
        long delta = colourTimestamp - Instant.now().getEpochSecond() + cooldown;
        if (delta < 0) {
            this.colour = colour;
            colourTimestamp = Instant.now().getEpochSecond();
            return 0;
        }
        return delta;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public long getNameTimestamp() {
        return nameTimestamp;
    }

    public long getSpawnTimestamp() {
        return spawnTimestamp;
    }

    public long getColourTimestamp() {
        return colourTimestamp;
    }

    /// Messaging
    //Sends message to all members
    public void notifyMembers(String message) {
        for (UUID uuid : members.keySet()) {
            memberMessage(uuid, message);
        }
    }

    //Sends notification to all members in & above the given rank
    public void notifyMembersByRank(GuildRank rank, String message) {
        for (GuildMember member : members.values()) {
            if (member.getRank().getPermissionLevel() >= rank.getPermissionLevel()) {
                memberMessage(member.getPlayerUuid(), message);
            }
        }
    }

    //Messages the given uuid with a guild-formatted message
    public void memberMessage(UUID uuid, String message) {
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if (playerRef == null) { //Means player is offline, skip this message
            return;
        }
        playerRef.sendMessage(Message.raw("[" + name + "] " + message).color(colour).italic(true));
    }

    @Override
    public String toString() {
        return "Guild Name: " + name + "\nCreated at: " + Instant.ofEpochSecond(createdTimestamp).toString() + "\nUUID: " + uuid + "\nMember count: " + members.size();
    }
}
