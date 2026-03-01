package com.civtale.civguild;

public enum GuildRank {
    MEMBER("Member"),
    COLEADER("Co-Leader"),
    LEADER("Leader");

    private final String displayName;
    //have all access parameters here by assining a level to each rank and ie canInvite(){return level>=2}

    private GuildRank(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {return this.displayName;}
}
