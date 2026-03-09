package com.civtale.civguild.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

public class AcceptCommand extends AbstractPlayerCommand {
    public AcceptCommand() {
        super("accept", "Accept a join request");
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        PlayerRef joiningPlayer = Universe.get().getPlayerByUsername(args[2], NameMatching.EXACT);
        if (joiningPlayer == null) {
            playerRef.sendMessage(Message.raw("[CivGuild] Unknown player name"));
            return;
        }
        guildManager.acceptJoin(playerRef, joiningPlayer);
    }
}
