package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;


public class SetspawnCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> guildArg;
    private final RequiredArg<Vector3i> positionArg;

    public SetspawnCommand() {
        super("setspawn", "Sets a guild's default spawn location");
        this.guildArg = this.withRequiredArg("guild", "Guild to change spawn", ArgTypes.STRING);
        this.positionArg = this.withRequiredArg("coordinates", "New spawn position", ArgTypes.VECTOR3I); //NOTE only supports int vector
        requirePermission("civtale.user.civguild");
        addAliases("spawn");
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        GuildManager guildManager = GuildManager.getInstance();
        Guild guild = guildManager.getGuildByName(guildArg.get(commandContext));
        if (guild == null) {
            playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
            return;
        }
        com.hypixel.hytale.math.vector.Vector3d spawnCoords = new com.hypixel.hytale.math.vector.Vector3d(positionArg.get(commandContext).x, positionArg.get(commandContext).y, positionArg.get(commandContext).z); //convert to double vector
        guildManager.setSpawn(playerRef.getUuid(), guild, spawnCoords);

    }
}
