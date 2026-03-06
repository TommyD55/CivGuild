package com.civtale.civguild;

public enum GuildRank {
    MEMBER("Member", 1),
    COLEADER("Co-Leader", 2),
    LEADER("Leader", 3);

    private final String displayName;
    private final int permissionLevel;
    //have all access parameters here by assigning a level to each rank and ie canInvite(){return level>=2}

    GuildRank(String displayName, int permissionLevel) {
        this.displayName = displayName;
        this.permissionLevel = permissionLevel;
    }

    public String getDisplayName() {return this.displayName;}


    public static GuildRank stringToRank(String rankName) {
        return switch (rankName.toLowerCase()) {
            case "member" -> GuildRank.MEMBER;
            case "coleader" -> GuildRank.COLEADER;
            case "leader" -> GuildRank.LEADER;
            default -> null;
        };
    }

    public int getPermissionLevel() {
        return this.permissionLevel;
    }

    //Permissions
    public boolean canDisband() {
        return this.permissionLevel >= 3;
    }
    public boolean canAddMember() {
        return this.permissionLevel >= 2;
    }
    public boolean canKickMember() { //Kicked member must have a lower rank
        return this.permissionLevel >= 2;
    }
    public boolean canAssignRank() { //pro/demoted member must have a lower rank
        return this.permissionLevel >= 2;
    }
    public boolean canRename() {
        return this.permissionLevel >= 3;
    }
    public boolean canSetSpawn() {
        return this.permissionLevel >= 2;
    }
    public boolean canManageJoinRequests() {
        return this.permissionLevel >= 2;
    }
}
