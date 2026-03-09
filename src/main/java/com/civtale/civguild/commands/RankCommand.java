package com.civtale.civguild.commands;

import com.civtale.civguild.GuildManager;
import com.civtale.civguild.GuildRank;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;


public class RankCommand extends AbstractPlayerCommand {
    private final RequiredArg<PlayerRef> playerArg;
    private final RequiredArg<String> rankArg;

    public RankCommand() {
        super("rank", "Assign a rank to a guild member");
        this.playerArg = this.withRequiredArg("player", "Player to assign rank to", ArgTypes.PLAYER_REF);
        this.rankArg = this.withRequiredArg("rank", "Rank to assign", ArgTypes.STRING);
        addAliases("assign");
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        GuildRank rank = GuildRank.stringToRank(rankArg.get(commandContext));
        if (rank == null) {
            playerRef.sendMessage(Message.raw("[CivGuild] Unknown rank"));
            return;
        }
        GuildManager.getInstance().assignRank(playerRef, playerArg.get(commandContext), rank);
    }
}
