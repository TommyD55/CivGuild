package com.civtale.civguild;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;

public class GuildMember {
    private GuildRank rank;
    private final UUID uuid;

    //Construct as a member
    public GuildMember(UUID uuid) {
        this.uuid = uuid;
        this.rank = GuildRank.MEMBER;
    }

    //Construct from savefile or as set role (leader as the first member upon guild creation)
    public GuildMember(UUID uuid, GuildRank rank) {
        this.uuid = uuid;
        this.rank = rank;
    }

    public GuildRank getRank() {
        return rank;
    }

    public void setRank(GuildRank rank) { this.rank = rank; }


    public UUID getPlayerUuid() { return uuid; }

}
