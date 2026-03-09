package com.civtale.civguild.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class RemoveCommand extends AbstractAsyncCommand {
    public RemoveCommand() {
        super("remove", "Remove a player from their guild");
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
        PlayerRef rPlayer = Universe.get().getPlayerByUsername(args[2], NameMatching.EXACT);
        if (rPlayer == null) {
            playerRef.sendMessage(Message.raw("[CivGuild] Unknown player name"));
            return;
        }
        guildManager.removeMember(playerRef, rPlayer, args[3].replace("_", " ")); //again passing in a string with _ instead of spaces

        return null;
    }
}
