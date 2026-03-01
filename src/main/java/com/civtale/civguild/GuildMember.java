package com.civtale.civguild;

public class GuildMember {
    private GuildRank rank;

    //Construct as a member
    public GuildMember() {
        rank = GuildRank.MEMBER;
    }
    //Construct as set role, used to set leader as the first member upon guild creation
    public GuildMember(GuildRank rank) {
        this.rank = rank;
    }

    public GuildRank getRank() {
        return rank;
    }
}
