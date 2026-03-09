package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class RenameCommand extends AbstractAsyncCommand {
    public RenameCommand() {
        super("rename", "Rename a guild");
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
        Guild rGuild = lookupGuild(args[2]);
        if (rGuild == null) {
            playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
            return;
        }
        guildManager.renameGuild(playerRef, rGuild, args[3].replace("_", " "));  //Passing in a name rather than a guild object so process the spaces here

        return null;
    }
}
