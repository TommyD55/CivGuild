package com.civtale.civguild;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;

public class GuildMember {
    private GuildRank rank;
    private final UUID uuid;

    //Construct as a member
    public GuildMember(PlayerRef playerRef) {
        this.rank = GuildRank.MEMBER;
        //this.playerName = playerRef.getUsername();
        this.uuid = playerRef.getUuid();
    }
    //Construct as set role, used to set leader as the first member upon guild creation
    public GuildMember(PlayerRef playerRef, GuildRank rank) {
        this.rank = rank;
        this.uuid = playerRef.getUuid();
    }
    //Construct from savefile
    public GuildMember(UUID uuid, GuildRank rank) {
        this.rank = rank;
        this.uuid = uuid;
    }

    public GuildRank getRank() {
        return rank;
    }

    public void setRank(GuildRank rank) { this.rank = rank; }

    public UUID getPlayerUuid() { return uuid; }

}
