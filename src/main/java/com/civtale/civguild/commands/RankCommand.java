package com.civtale.civguild.commands;

import com.civtale.civguild.GuildRank;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class RankCommand extends AbstractAsyncCommand {
    public RankCommand() {
        super("rank", "Assign a rank to a guild member");
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
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

        return null;
    }
}
