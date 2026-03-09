package com.civtale.civguild.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class CivGuildCommand extends AbstractCommandCollection {

    public CivGuildCommand() {
        super("cg", "CivGuild"); //command name & description, doesn't require confirmation
        //Abstract Async commands to be run by anything ie the command line
        addSubCommand(new InfoCommand());
        addSubCommand(new CreateCommand());
        addSubCommand(new DisbandCommand());
        addSubCommand(new AddCommand());
        addSubCommand(new RemoveCommand());
        addSubCommand(new RankCommand());
        addSubCommand(new RenameCommand());
        addSubCommand(new SetspawnCommand());
        //Abstract Player commands to target the caller player
        addSubCommand(new UICommand());
        addSubCommand(new JoinCommand());
        addSubCommand(new CancelCommand());
        addSubCommand(new LeaveCommand());
        addSubCommand(new AcceptCommand());
        addSubCommand(new RejectCommand());
        //Permission (available to all players)
        requirePermission("civtale.user.civguild");
        //Aliases can be typed instead of 'cg'
        addAliases("civg", "civguild", "guild");
    }

}

    /*//help output
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
    } */
