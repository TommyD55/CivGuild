package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class DisbandCommand extends AbstractAsyncCommand {
    public DisbandCommand() {
        super("disband", "Disband a guild");
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
        Guild guildDisband = lookupGuild(args[2]);
        if (guildDisband == null) {
            playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
            return;
        }
        guildManager.disbandGuild(playerRef, guildDisband);

        return null;
    }
}
