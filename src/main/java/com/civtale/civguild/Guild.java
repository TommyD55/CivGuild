package com.civtale.civguild;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Guild {
    private static final int MAX_MEMBERS = 100; //TODO configurable value
    private String name; //guild name, can be changed
    private final UUID uuid; //final uuid never changes
    private final Map<UUID, GuildMember> members;

    //Player UUID List
    public Guild(String name, PlayerRef leaderRef) {
        this.name = name;
        this.uuid = UUID.randomUUID(); //get a unique UUID for this guild
        this.members = new HashMap<>();
        this.members.put(leaderRef.getUuid(), new GuildMember(leaderRef, GuildRank.LEADER)); //save the leader as the first member TODO setup GuildMember class & add constructors
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Collection<GuildMember> getMembers() { return members.values(); }

    public void setName(String name) {
        this.name = name;
    } //TODO cooldown for renaming


    @Override
    public String toString() {
        return "Guild Name: " + name + "\nUUID: " + uuid + "\nMember count: " + members.size();
    }
}
