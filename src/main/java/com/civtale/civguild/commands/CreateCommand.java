package com.civtale.civguild.commands;

import com.civtale.civguild.GuildManager;
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

public class CreateCommand extends AbstractAsyncCommand {
    private final RequiredArg<String> guildArg;
    private final RequiredArg<PlayerRef> playerArg;

    public CreateCommand() {
        super("create", "Create a new guild");
        this.guildArg = this.withRequiredArg("guild_name", "Name of new guild", ArgTypes.STRING);
        this.playerArg = this.withRequiredArg("player", "Player to assign as the leader", ArgTypes.PLAYER_REF);
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {

        GuildManager.getInstance().createGuild(commandContext, guildArg.get(commandContext), playerArg.get(commandContext));
        return CompletableFuture.completedFuture(null);
    }
}
