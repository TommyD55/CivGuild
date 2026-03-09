package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.hypixel.hytale.protocol.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class SetspawnCommand extends AbstractAsyncCommand {
    public SetspawnCommand() {
        super("setspawn", "Sets a guild's default spawn location");
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
        Guild sGuild = lookupGuild(args[2]);
        if (sGuild == null) {
            playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
            return;
        }
        Vector3d spawnCoords = new Vector3d(Float.parseFloat(args[3]), Float.parseFloat(args[4]), Float.parseFloat(args[5]));
        guildManager.setSpawn(playerRef, sGuild, spawnCoords);

        return null;
    }
}
