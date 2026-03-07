package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.civtale.civguild.GuildRank;
import com.civtale.civguild.pages.GuildUIPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.UUID;

public class CivGuildCommand extends AbstractPlayerCommand {

    public CivGuildCommand() {
        super("cg", "CivGuild base command", false); //command name & description, doesn't require confirmation
        this.setAllowsExtraArguments(true); //further args /cg ...
        requirePermission("civtale.user.civguild");
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        //Process the command into arguments
        String[] args = commandContext.getInputString().trim().split("\\s+"); //trim removes extra spaces //split separates into multiple Strings separated by spaces "\\s+"
        //Sift out invalid input
        if (args.length < 2) {
            helpMessage(playerRef);
            return;
        }
        GuildManager guildManager = GuildManager.getInstance(); //access to guild manager

        //Match the 2nd arg/Subcommand
        switch (args[1].toLowerCase()) { //subcommand arg must be lowercase to match, the rest must maintain capitals & will handle themselves
            case "ui": //Open the CivGuild UI
                if (args.length != 2) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg ui"));
                    return;
                }
                //Create instance of the Guild UI page //TODO once hotkey is implemented, the below could probably be a func to call elsewhere
                GuildUIPage page = new GuildUIPage(playerRef);
                //Use player's PageManager to open the new page
                Player player = store.getComponent(ref, Player.getComponentType()); //Retrieve player component by looking up entity ref & component type
                assert player != null; //throws exception is this is the case
                player.getPageManager().openCustomPage(ref, store, page);
                break;

            case "help": //Displays help information
                helpMessage(playerRef);
                break;

            case "info": //Displays guild information
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg info <Guild_Name>"));
                    return;
                }
                Guild guildInfo = lookupGuild(args[2]);
                if (guildInfo != null) { playerRef.sendMessage(Message.raw(guildInfo.toString()));
                } else { playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));}
                break;

            case "list_guilds": //list all guilds by name
                if (args.length != 2) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg list_guilds"));
                    return;
                }
                if (guildManager.getGuilds().isEmpty()) {
                    playerRef.sendMessage(Message.raw("[CivGuild] No guilds exist"));
                    return;
                }
                playerRef.sendMessage(Message.raw("[CivGuild] Listing guilds..."));
                for (Guild guild : guildManager.getGuilds()){
                    playerRef.sendMessage(Message.raw(guild.getName()));
                }
                break;

            case "list_members": //list all players in a guild
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg list_members <Guild_Name>"));
                    return;
                }
                Guild guild = lookupGuild(args[2]);
                if (guild == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
                    return;
                }
                playerRef.sendMessage(Message.raw("[CivGuild] Listing members..."));
                guild.getMembers().forEach((member) -> {
                    UUID playerUuid = member.getPlayerUuid();
                    String memberName = Objects.requireNonNull(Universe.get().getPlayer(playerUuid)).getUsername();
                    playerRef.sendMessage(Message.raw("[" + member.getRank().getDisplayName() + "] " + memberName)); //[RANK] Name
                });
                break;

            case "create": // Create a new guild
                if (args.length != 4) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg create <Guild_Name> <Leader_Name>"));
                    return;
                }
                PlayerRef leader = Universe.get().getPlayerByUsername(args[3], NameMatching.EXACT);
                if (leader == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown player name"));
                    return;
                }
                guildManager.createGuild(playerRef, args[2].replace("_", " "), leader); //Passing in a name rather than a guild object so process the spaces here
                break;

            case "disband": // Disband a guild
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg disband <Guild_Name>"));
                    return;
                }
                Guild guildDisband = lookupGuild(args[2]);
                if (guildDisband == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
                    return;
                }
                guildManager.disbandGuild(playerRef, guildDisband);
                break;

            case "join": // Request to join a guild
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg join <Guild_Name>"));
                    return;
                }
                Guild guildJoin = lookupGuild(args[2]);
                if (guildJoin == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
                    return;
                }
                guildManager.joinRequest(playerRef, guildJoin);
                break;

            case "cancel": // Cancels a join request
                if (args.length != 2) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg cancel"));
                    return;
                }
                guildManager.cancelRequest(playerRef);
                break;

            case "leave": // Removes caller from their own guild
                if (args.length != 2) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg leave"));
                    return;
                }
                guildManager.removeMember(playerRef, playerRef, "Self removal from guild");
                return;

            case "accept": // Accept a request to join current guild
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg accept <Player_Name>"));
                    return;
                }
                PlayerRef joiningPlayer = Universe.get().getPlayerByUsername(args[2], NameMatching.EXACT);
                if (joiningPlayer == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown player name"));
                    return;
                }
                guildManager.acceptJoin(playerRef, joiningPlayer);
                break;

            case "reject": // Reject a request to join current guild
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg reject <Player_Name>"));
                    return;
                }
                PlayerRef jPlayer = Universe.get().getPlayerByUsername(args[2], NameMatching.EXACT);
                if (jPlayer == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown player name"));
                    return;
                }
                guildManager.rejectJoin(playerRef, jPlayer);
                break;

            case "add": //Add player to a guild
                if (args.length != 4) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg add <Guild_Name> <Player_Name>"));
                    return;
                }
                Guild aGuild = lookupGuild(args[2]);
                if (aGuild == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
                    return;
                }
                PlayerRef aPlayer = Universe.get().getPlayerByUsername(args[3], NameMatching.EXACT);
                if (aPlayer == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown player name"));
                    return;
                }
                guildManager.addMember(playerRef, aGuild, aPlayer);
                break;

            case "remove": // Remove a player from a guild (aka 'kick' as it will be named in the UI)
                if (args.length != 4) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg remove <Player_Name> <kick_reason>"));
                    return;
                }
                PlayerRef rPlayer = Universe.get().getPlayerByUsername(args[2], NameMatching.EXACT);
                if (rPlayer == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown player name"));
                    return;
                }
                guildManager.removeMember(playerRef, rPlayer, args[3].replace("_", " ")); //again passing in a string with _ instead of spaces
                break;

            case "assign": //Assign a rank to a player
                if (args.length != 4) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg assign <PlayerName> [member/coleader/leader]"));
                    return;
                }
                PlayerRef asPlayer = Universe.get().getPlayerByUsername(args[2], NameMatching.EXACT);
                if (asPlayer == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown player name"));
                    return;
                }
                GuildRank rank = GuildRank.stringToRank(args[3]);
                if (rank == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid rank"));
                    return;
                }
                guildManager.assignRank(playerRef, asPlayer, rank);
                break;

            case "rename": //rename a guild
                if (args.length != 4) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg rename <Guild_Name> <New_Name>"));
                    return;
                }
                Guild rGuild = lookupGuild(args[2]);
                if (rGuild == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
                    return;
                }
                guildManager.renameGuild(playerRef, rGuild, args[3].replace("_", " "));  //Passing in a name rather than a guild object so process the spaces here
                break;

            case "setspawn": //set default spawn point of a guild
                if (args.length != 6) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild] Invalid arguments, try /cg setspawn <Guild_Name> <x> <y> <z>"));
                    return;
                }
                Guild sGuild = lookupGuild(args[2]);
                if (sGuild == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
                    return;
                }
                Vector3d spawnCoords = new Vector3d(Float.parseFloat(args[3]), Float.parseFloat(args[4]), Float.parseFloat(args[5]));
                guildManager.setSpawn(playerRef, sGuild, spawnCoords);
                break;

            default:
                playerRef.sendMessage(Message.raw("[CivGuild] Invalid command, try /cg help"));
                break;
        }
    }

    //help output
    private void helpMessage(PlayerRef playerRef) {
        playerRef.sendMessage(Message.raw("[CivGuild] Please see below command line options, some are subject to guild status or rank permissions" +
                "\n - /cg help" +
                "\n - /cg ui" +
                "\n - /cg info <Guild_Name>" + //replacing spaces with _ plays nice with the arg system, TODO something to polish in the future
                "\n - /cg list_guilds" +
                "\n - /cg list_members <Guild_Name>" +
                "\n - /cg create <Guild_Name> <Leader_Name>" +
                "\n - /cg disband <Guild_Name>" +
                "\n - /cg join <Guild_Name>" +
                "\n - /cg leave" +
                "\n - /cg [accept/reject] <Player_Name>" +
                "\n - /cg add <Guild_Name> <Player_Name>" +
                "\n - /cg remove <PlayerName> <kick_reason>" +
                "\n - /cg assign <PlayerName> [member/coleader/leader]" +
                "\n - /cg rename <Guild_Name> <New_Name>" +
                "\n - /cg setspawn <Guild_Name> <x> <y> <z>"));
    }

    private Guild lookupGuild(String guildName) {
        UUID uuid = GuildManager.getInstance().getGuildUUID(guildName.replace("_", " "));
        if (uuid == null) { return null; }
        return GuildManager.getInstance().getGuild(uuid);
    }

}
