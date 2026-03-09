package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class AddCommand() extends AbstractAsyncCommand {
    public AddCommand() {
        super("add", "Add a player to a guild");
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
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

        return null;
    }
}
