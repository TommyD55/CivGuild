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


public class CreateCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> guildArg;
    private final RequiredArg<PlayerRef> playerArg;

    public CreateCommand() {
        super("create", "Create a new guild");
        this.guildArg = this.withRequiredArg("guild_name", "Name of new guild", ArgTypes.STRING);
        this.playerArg = this.withRequiredArg("player", "Player to assign as the leader", ArgTypes.PLAYER_REF);
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {

        GuildManager.getInstance().createGuild(playerRef, guildArg.get(commandContext), playerArg.get(commandContext));
    }
}
