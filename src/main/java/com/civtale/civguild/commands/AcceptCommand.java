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

public class AcceptCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> playerArg;

    public AcceptCommand() {
        super("accept", "Accept a join request");
        this.playerArg = this.withRequiredArg("player", "Player to accept", ArgTypes.STRING);
        requirePermission("civtale.user.civguild");
        addAliases("a");
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        GuildManager guildManager = GuildManager.getInstance();
        UUID uuid = guildManager.getUUIDByName(playerArg.get(commandContext));
        if (uuid == null) {
            playerRef.sendMessage(Message.raw("[CivGuild] Unknown player"));
            return;
        }
        guildManager.acceptJoin(playerRef.getUuid(), uuid);
    }
}
