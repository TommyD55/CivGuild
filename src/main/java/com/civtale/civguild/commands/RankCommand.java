package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.civtale.civguild.GuildRank;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class RankCommand extends AbstractAsyncCommand {
    private final RequiredArg<PlayerRef> playerArg;
    private final RequiredArg<String> rankArg;

    public RankCommand() {
        super("rank", "Assign a rank to a guild member");
        this.playerArg = this.withRequiredArg("player", "Player to assign rank to", ArgTypes.PLAYER_REF);
        this.rankArg = this.withRequiredArg("rank", "Rank to assign", ArgTypes.STRING);
        addAliases("assign");
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
        GuildRank rank = GuildRank.stringToRank(rankArg.get(commandContext));
        if (rank == null) {
            commandContext.sendMessage(Message.raw("[CivGuild] Unknown rank"));
            return CompletableFuture.completedFuture(null);
        }
        GuildManager.getInstance().assignRank(commandContext, playerArg.get(commandContext), rank);
        return CompletableFuture.completedFuture(null);
    }
}
