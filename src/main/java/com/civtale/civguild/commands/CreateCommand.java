package com.civtale.civguild.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class CreateCommand extends AbstractAsyncCommand {
    public CreateCommand() {
        super("create", "Create a new guild");
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
        PlayerRef leader = Universe.get().getPlayerByUsername(args[3], NameMatching.EXACT);
        if (leader == null) {
            playerRef.sendMessage(Message.raw("[CivGuild] Unknown player name"));
            return;
        }
        guildManager.createGuild(playerRef, args[2].replace("_", " "), leader); //Passing in a name rather than a guild object so process the spaces here

        return null;
    }
}
