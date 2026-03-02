package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.civtale.civguild.GuildMember;
import com.civtale.civguild.GuildRank;
import com.civtale.civguild.pages.GuildUIPage;
import com.hypixel.hytale.builtin.buildertools.tooloperations.transform.Translate;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.i18n.parser.LangFileParser;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.UUID;

public class CivGuildCommand extends AbstractPlayerCommand {

    public CivGuildCommand() {
        super("cg", "CivGuild base command", false); //command name & description, doesn't require confirmation
        this.setAllowsExtraArguments(true); //further args /cg ...
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {

        //Process the command into arguments
        String inputString = commandContext.getInputString(); //get full command
        String[] args = inputString.trim().split("\\s+"); //split command into components //trim removes extra spaces //split separates into multiple Strings separated by spaces "\\s+"
        //Sift out invalid input
        if (args.length < 2) {
            helpMessage(playerRef);
            return;
        }
        // Common refs
        GuildManager guildManager = GuildManager.getInstance();

        //Match the 2nd arg/Subcommand
        switch (args[1].toLowerCase()) { //subcommand arg must be lowercase to match, the rest must maintain capitals & will handle themselves
            case "ui": // open the CivGuild UI
                // No perms requirements
                if (args.length != 2) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg ui"));
                    return;
                }
                //Create instance of the Guild UI page
                GuildUIPage page = new GuildUIPage(playerRef);
                //Use player's PageManager to open the new page
                Player player = store.getComponent(ref, Player.getComponentType()); //Retrieve player component by looking up entity ref & component type
                assert player != null; //throws exception is this is the case
                player.getPageManager().openCustomPage(ref, store, page);
                break;

            case "help":
                //no perms & avoid further checks, just display the help info
                helpMessage(playerRef);
                break;

            case "info": //Displays guild information
                //no perms requirements
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg info <Guild_Name>"));
                    return;
                }
                UUID uuid = guildManager.getGuildUUID(args[2].replace("_", " ")); //replacing spaces with _ plays nice with the arg system, something to polish in the future
                if (uuid != null) {
                    playerRef.sendMessage(Message.raw(guildManager.getGuild(uuid).toString()));
                } else {
                    playerRef.sendMessage(Message.raw("[CivGuild]: Unknown guild"));
                }
                break;

            case "list_guilds": //list all guilds by name
                //no perms requirements
                if (args.length != 2) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg list_guilds"));
                    return;
                }
                playerRef.sendMessage(Message.raw("[CivGuild]: Listing guilds"));
                for (Guild guild : guildManager.getGuilds()){
                    playerRef.sendMessage(Message.raw("\n" + guild.getName()));
                }
                break;

            case "list_members": //list all players in a guild
                //no perms requirements
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg list_members <Guild_Name>"));
                    return;
                }
                UUID guildUuid = guildManager.getGuildUUID(args[2].replace("_", " "));
                if (guildUuid == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild]: Unknown guild"));
                }
                playerRef.sendMessage(Message.raw("[CivGuild]: Listing members"));
                guildManager.getGuild(guildUuid).getMembers().forEach((member) -> {
                    playerRef.sendMessage(Message.raw("\n[" + member.getRank().getDisplayName() + "] " + member.getPlayerName())); //[RANK] Name
                });
                break;

            case "create": // Create a new guild - playerRef (needs the uuid) & guild's name
                //no perms requirements
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg create <Guild_Name>"));
                    return;
                }
                // TODO check these methods & params, must have right amount of args, manager should do param checks so possibly just get bool from manager and run error commands
                if (!guildManager.createGuild(playerRef, args[2].replace("_", " "))){//replacing spaces with _ plays nice with the arg system, something to polish in the future
                    playerRef.sendMessage(Message.raw("[CivGuild]: Guild could not be created")); //TODO reason
                } else {
                    playerRef.sendMessage(Message.raw("[CivGuild]: Guild created"));
                }
                break;

            case "disband": // Disband a guild - guild's name
                //no perms requirements - guild manager checks rank
                if (args.length != 2) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg disband"));
                    return;
                }
                if (!guildManager.disbandGuild(playerRef)){
                    playerRef.sendMessage(Message.raw("[CivGuild]: Guild could not be disbanded")); //TODO reason
                } else {
                    playerRef.sendMessage(Message.raw("[CivGuild]: Guild disbanded"));
                }
                break;

            case "join": // Request to join a guild - Player & guild's name //TODO implement the invites
                //no perms requirements
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg join <Guild_Name>"));
                    return;
                }
                guildManager.joinGuild(playerRef, args[2].replace("_", " ")); //TODO error response
                break;

            case "accept": // Accept a request to join current guild - Player & requester's name //TODO implement the invites
                //no perms requirements - guild manager checks rank
                if (args.length != 2) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg accept"));
                    return;
                }
                guildManager.acceptJoin(playerRef); //TODO error response
                break;

            case "reject": // Reject a request to join current guild - Player & requester's name //TODO implement the invites
                //no perms requirements - guild manager checks rank
                if (args.length != 2) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg reject"));
                    return;
                }
                guildManager.rejectJoin(playerRef); //TODO error response
                break;

            case "kick": // Remove a player from the current guild - Player & removing player's name
                //no perms requirements - guild manager checks rank
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg kick <PlayerName>"));
                    return;
                }
                guildManager.kick(playerRef, args[2]); //TODO error response
                break;

            case "assign": //Assign a rank to a player - Player, player's name, GuildRank
                //no perms requirements - guild manager checks rank
                if (args.length != 4) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg assign <PlayerName> [member/coleader/leader]"));
                    return;
                }
                GuildRank rank = GuildRank.stringToRank(args[3]);
                if (rank == null) {
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid rank"));
                    return;
                }
                guildManager.assignRank(playerRef, args[2], rank); //TODO error response
                break;

            case "rename": //rename the current guild - Player & new name
                //no perms requirements - guild manager checks rank
                if (args.length != 3) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg rename <Guild_Name>"));
                    return;
                }
                guildManager.renameGuild(playerRef, args[2].replace("_", " ")); //TODO error response
                break;

            case "setspawn": //setspawn of the current guild - Player
                //no perms requirements - guild manager checks rank
                if (args.length != 2) { //ensure args match this subcommand
                    playerRef.sendMessage(Message.raw("[CivGuild]: Invalid arguments, try /cg setspawn"));
                    return;
                }
                guildManager.setSpawn(playerRef); //TODO error response
                break;

            default:
                playerRef.sendMessage(Message.raw("[CivGuild]: Invalid command, try /cg help"));
        }
    }

    //help output
    private void helpMessage(PlayerRef playerRef) {
        playerRef.sendMessage(Message.raw("[CivGuild]: Please see below command line options, some are subject to guild status or rank permissions\n" +
                " - /cg help\n" +
                " - /cg ui\n" +
                " - /cg info <Guild_Name>\n" +
                " - /cg list_guilds>\n" +
                " - /cg list_members <Guild_Name>\n" +
                " - /cg create <Guild_Name>\n" +
                " - /cg disband\n" +
                " - /cg join <Guild_Name>\n" +
                " - /cg [accept/reject]\n" +
                " - /cg kick <PlayerName>\n" +
                " - /cg assign <PlayerName> [member/coleader/leader]\n" +
                " - /cg rename <Guild_Name>\n" +
                " - /cg setspawn\n"));
    }
}
