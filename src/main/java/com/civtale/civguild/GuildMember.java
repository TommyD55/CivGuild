package com.civtale.civguild;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;

public class GuildMember {
    private GuildRank rank;
    private final String playerName; //TODO track player name changes, perhaps when they log in the server can find the member with their uuid & update this variable

    //Construct as a member
    public GuildMember(PlayerRef playerRef) {
        this.rank = GuildRank.MEMBER;
        this.playerName = playerRef.getUsername();
    }
    //Construct as set role, used to set leader as the first member upon guild creation
    public GuildMember(PlayerRef playerRef, GuildRank rank) {
        this.rank = rank;
        this.playerName = playerRef.getUsername();
    }

    public GuildRank getRank() {
        return rank;
    }

    public void setRank(GuildRank rank) { this.rank = rank; }

    public String getPlayerName() { return playerName; }
}
