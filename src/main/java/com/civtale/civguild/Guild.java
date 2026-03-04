package com.civtale.civguild;

import com.hypixel.hytale.protocol.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.*;

public class Guild {
    private static final int MAX_MEMBERS = 100; //TODO configurable value
    private String name; //guild name, can be changed
    private Vector3d guildSpawn;
    private final UUID uuid; //final uuid never changes
    private final Map<UUID, GuildMember> members;
    private final Set<UUID> leaderUuids;

    //Cosnstructor for new guild
    public Guild(String name, PlayerRef leaderRef) {
        this.name = name;
        this.uuid = UUID.randomUUID(); //get a unique UUID for this guild
        this.members = new HashMap<>();
        this.members.put(leaderRef.getUuid(), new GuildMember(leaderRef, GuildRank.LEADER)); //save the leader as the first member
        this.leaderUuids = new HashSet<>();
        this.leaderUuids.add(leaderRef.getUuid());
    }
    //Constructor for guild from save
    public Guild(String name, UUID uuid, Map<UUID, GuildMember> members) {
        this.name = name;
        this.uuid = uuid; //get a unique UUID for this guild
        this.members = members;
        this.leaderUuids = new HashSet<>();
        for (GuildMember member : members.values()) { //Find the leaders
            if (member.getRank() == GuildRank.LEADER) {
                this.leaderUuids.add(member.getPlayerUuid());
            }
        }
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Collection<GuildMember> getMembers() { return members.values(); }

    public GuildMember getMember(UUID uuid) { return members.get(uuid); }

    public Set<UUID> getLeaderUuids() {
        return leaderUuids;
    }

    public void addMember(PlayerRef playerRef) {
        this.members.put(playerRef.getUuid(), new GuildMember(playerRef, GuildRank.MEMBER));
    }

    public void removeMember(UUID playerUuid) { //GuildManager ensures this isn't the leader
        this.members.remove(playerUuid);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpawn(Vector3d guildSpawn) { //TODO implement this feature & save to file
        this.guildSpawn = guildSpawn;
    }

    public Vector3d getSpawn() {
        //TODO return world's spawn instead (how to get the world for this instance?)
        return Objects.requireNonNullElseGet(guildSpawn, () -> new Vector3d(0, 0, 0));
    }

    public void notifyMembers(String message) {
        for (UUID uuid : members.keySet()) {
            PlayerRef playerRef = Universe.get().getPlayer(uuid);
            assert playerRef != null;
            playerRef.sendMessage(Message.raw(message));
        }
    }

    @Override
    public String toString() {
        return "Guild Name: " + name + "\nUUID: " + uuid + "\nMember count: " + members.size();
    }
}
