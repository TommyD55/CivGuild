package com.civtale.civguild;

public enum GuildRank {
    MEMBER("Member", 1),
    COLEADER("Co-Leader", 2),
    LEADER("Leader", 3);

    private final String displayName;
    private final int permissionLevel;
    //have all access parameters here by assigning a level to each rank and ie canInvite(){return level>=2}

    private GuildRank(String displayName, int permissionLevel) {
        this.displayName = displayName;
        this.permissionLevel = permissionLevel;
    }

    public String getDisplayName() {return this.displayName;}

    public static GuildRank stringToRank(String rankName) {
        switch (rankName.toLowerCase()) {
            case "member":
            case "coleader":
            case "leader":
            default:
                return null;
        }
    }
}
