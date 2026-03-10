package com.civtale.civguild.commands;

import com.civtale.civguild.GuildManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;


public class RemoveCommand extends AbstractPlayerCommand {
    private final RequiredArg<PlayerRef> playerArg;
    private final RequiredArg<String> reasonArg;

    public RemoveCommand() {
        super("remove", "Remove a player from their guild");
        this.playerArg = this.withRequiredArg("player", "Player to remove", ArgTypes.PLAYER_REF);
        this.reasonArg = this.withRequiredArg("reason", "Reason for removal", ArgTypes.STRING);
        requirePermission("civtale.user.civguild");
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {

        GuildManager.getInstance().removeMember(playerRef, playerArg.get(commandContext), reasonArg.get(commandContext));
    }
}
