package com.civtale.civguild.commands;

import com.civtale.civguild.GuildManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class RequestsCommand extends AbstractPlayerCommand {

    public RequestsCommand() {
        super("requests", "Show join requests");
        requirePermission("civtale.user.civguild");
        addAliases("req");
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {

        Set<String> requests = GuildManager.getInstance().getInvitesByGuild(playerRef);
        if (requests.isEmpty()) {
            playerRef.sendMessage(Message.raw("[CivGuild] No pending join requests"));
            return;
        }
        playerRef.sendMessage(Message.raw("[CivGuild] Listing pending join requests..."));
        for (String request : requests) {
            playerRef.sendMessage(Message.raw(" - " + request));
        }
    }
}
