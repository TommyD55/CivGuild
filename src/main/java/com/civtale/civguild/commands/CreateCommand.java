package com.civtale.civguild.commands;

import com.civtale.civguild.GuildManager;
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

import java.util.UUID;


public class CreateCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> guildArg;
    private final RequiredArg<String> playerArg;

    public CreateCommand() {
        super("create", "Create a new guild");
        this.guildArg = this.withRequiredArg("guild_name", "Name of new guild", ArgTypes.STRING);
        this.playerArg = this.withRequiredArg("leader", "Player to assign as the leader", ArgTypes.STRING);
        requirePermission("civtale.user.civguild");
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        GuildManager guildManager = GuildManager.getInstance();
        UUID uuid = guildManager.getUUIDByName(playerArg.get(commandContext));
        if (uuid == null) {
            playerRef.sendMessage(Message.raw("[CivGuild] Unknown player"));
            return;
        }
        guildManager.createGuild(playerRef.getUuid(), guildArg.get(commandContext), uuid);
    }
}
