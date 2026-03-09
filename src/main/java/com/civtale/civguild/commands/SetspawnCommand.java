package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class SetspawnCommand extends AbstractAsyncCommand {
    private final RequiredArg<String> guildArg;
    private final RequiredArg<Vector3i> positionArg;

    public SetspawnCommand() {
        super("setspawn", "Sets a guild's default spawn location");
        this.guildArg = this.withRequiredArg("guild", "Guild to change spawn", ArgTypes.STRING);
        this.positionArg = this.withRequiredArg("coordinates", "New spawn position", ArgTypes.VECTOR3I); //NOTE only supports int vector
        addAliases("spawn");
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
        GuildManager guildManager = GuildManager.getInstance();
        Guild guild = guildManager.getGuildByName(guildArg.get(commandContext));
        if (guild == null) {
            commandContext.sendMessage(Message.raw("[CivGuild] Unknown guild"));
            return CompletableFuture.completedFuture(null);
        }
        Vector3d spawnCoords = new Vector3d(positionArg.get(commandContext).x, positionArg.get(commandContext).y, positionArg.get(commandContext).z); //convert to double vector
        guildManager.setSpawn(commandContext, guild, spawnCoords);

        return CompletableFuture.completedFuture(null);
    }
}
