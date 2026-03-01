package com.civtale.civguild.commands;

import com.civtale.civguild.GuildManager;
import com.civtale.civguild.pages.GuildUIPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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
        String inputString = commandContext.getInputString().toLowerCase(); //get full command
        String[] args = inputString.trim().split("\\s+"); //split command into components //trim removes extra spaces //split separates into multiple Strings separated by spaces "\\s+"
        //Sift out invalid input
        if (args.length < 2) {
            sendInvalidCommand(playerRef);
            return;
        }
        // Common refs
        Player player = store.getComponent(ref, Player.getComponentType()); //Retrieve player component by looking up entity ref & component type
        GuildManager guildManager = GuildManager.getInstance();
        //TODO ref to guild manager to run the command
        //Match the 2nd arg
        switch (args[1]) {
            case "ui": // open the CivGuild UI
                // TODO check if player has perms for each command // sendNoPermission()
                //Create instance of the Guild UI page
                GuildUIPage page = new GuildUIPage(playerRef);
                //Use player's PageManager to open the new page
                player.getPageManager().openCustomPage(ref, store, page);
                break;
            case "info": //Displays guild information
                UUID uuid = GuildManager.getGuildUUID(args[2]);
                uuid != null ?
                        playerRef.sendMessage(Message.raw(GuildManager.getGuild(uuid)))
                        : playerRef.sendMessage(Message.raw("[CivGuild]: Unknown guild!"));
                break;
            case "create": // Create a new guild - playerRef (needs the uuid) & guild's name
                guildManager.createGuild(playerRef, args[2]); //TODO check these methods & params, must have right amount of args, manager should do param checks so possibly just get bool from manager and run error commands
                break;
            case "disband": // Disband a guild - guild's name
                guildManager.disbandGuild(args[2]);
                playerRef.sendMessage(Message.raw("disband: " + args[2])); //TEMP
                break;
            case "join": // Request to join a guild - Player & guild's name
                guildManager.joinGuild(player, args[2]);
                break;
            case "accept": // Accept a request to join current guild - Player & requester's name //TODO implement the invites
                guildManager.acceptJoin(player, args[2]);
                break;
            case "reject": // Reject a request to join current guild - Player & requester's name
                guildManager.rejectJoin(player, args[2]);
                break;
            case "kick": // Remove a player from the current guild - Player & removing player's name
                guildManager.kick(player, args[2]);
                break;
            case "assign": //Assign a rank to a player - Player, player's name, GuildRank
                guildManager.assignRank(player, args[2], args[3]);
                break;
            case "rename": //rename the current guild - Player & new name
                guildManager.renameGuild(args[2]);
                break;
            case "setspawn": //setspawn of the current guild - Player
                guildManager.setSpawn(player);
                break;
            default:
                sendInvalidCommand(playerRef);
        }
    }


    // Run when a command doesnt match TODO help page and variations
    private static void sendInvalidCommand(PlayerRef playerRef){
        playerRef.sendMessage(Message.raw("[CivGuild]: Invalid command!"));
    }

    // Run when player doesn't have permission for the command
    private static void sendNoPermission(PlayerRef playerRef){
        playerRef.sendMessage(Message.raw("[CivGuild]: You don't have permission for this!"));
    }
}
