package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
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

import java.awt.*;


public class ColourCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> guildArg;
    private final RequiredArg<Integer> colourArg;

    public ColourCommand() {
        super("colour", "Change guild colour");
        this.guildArg = this.withRequiredArg("guild", "Guild to recolour", ArgTypes.STRING);
        this.colourArg = this.withRequiredArg("colour", "Guild colour", ArgTypes.COLOR); //colours assigned to ints
        requirePermission("civtale.user.civguild");
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        GuildManager guildManager = GuildManager.getInstance();
        Guild guild = guildManager.getGuildByName(guildArg.get(commandContext));
        if (guild == null) {
            playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
            return;
        }
        guildManager.setColour(playerRef.getUuid(), guild, new Color(colourArg.get(commandContext)));
    }
}
