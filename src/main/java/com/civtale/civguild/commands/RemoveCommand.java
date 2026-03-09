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

public class RemoveCommand extends AbstractAsyncCommand {
    private final RequiredArg<PlayerRef> playerArg;
    private final RequiredArg<String> reasonArg;

    public RemoveCommand() {
        super("remove", "Remove a player from their guild");
        this.playerArg = this.withRequiredArg("player", "Player to remove", ArgTypes.PLAYER_REF);
        this.reasonArg = this.withRequiredArg("reason", "Reason for removal", ArgTypes.STRING);
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {

        GuildManager.getInstance().removeMember(commandContext, playerArg.get(commandContext), reasonArg.get(commandContext));
        return CompletableFuture.completedFuture(null);
    }
}
