package com.civtale.civguild;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;

public class GuildMember {
    private GuildRank rank;
    private final UUID uuid;
    private String username;

    //Construct as a member
    public GuildMember(PlayerRef playerRef) {
        this.uuid = playerRef.getUuid();
        this.username = playerRef.getUsername();
        this.rank = GuildRank.MEMBER;
    }
    //Construct as set role, used to set leader as the first member upon guild creation
    public GuildMember(PlayerRef playerRef, GuildRank rank) {
        this.uuid = playerRef.getUuid();
        this.username = playerRef.getUsername();
        this.rank = rank;
    }
    //Construct from savefile
    public GuildMember(UUID uuid, String username, GuildRank rank) {
        this.uuid = uuid;
        this.username = username;
        this.rank = rank;
    }

    public GuildRank getRank() {
        return rank;
    }

    public void setRank(GuildRank rank) { this.rank = rank; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public UUID getPlayerUuid() { return uuid; }

}
