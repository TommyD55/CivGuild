package com.civtale.civguild.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class CivGuildCommand extends AbstractCommandCollection {

    public CivGuildCommand() {
        super("civguild", "CivGuild"); //command name & description, doesn't require confirmation
        //Commands which can be run by any player, ie OP (NOTE rank perms are tied to PlayerRef objects so can't use CLI)
        addSubCommand(new InfoCommand());
        addSubCommand(new CreateCommand());
        addSubCommand(new DisbandCommand());
        addSubCommand(new AddCommand());
        addSubCommand(new RemoveCommand());
        addSubCommand(new RankCommand());
        addSubCommand(new RenameCommand());
        addSubCommand(new SetspawnCommand());
        //Commands to target the caller player
        addSubCommand(new UICommand());
        addSubCommand(new JoinCommand());
        addSubCommand(new CancelCommand());
        addSubCommand(new LeaveCommand());
        addSubCommand(new AcceptCommand());
        addSubCommand(new RejectCommand());
        addSubCommand(new RequestsCommand());
        //Permission (available to all players)
        requirePermission("civtale.user.civguild");
        //Aliases can be typed instead of 'cg'
        addAliases("civg", "cg");
    }

}