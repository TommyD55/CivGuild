package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class AddCommand extends AbstractAsyncCommand {
    private final RequiredArg<PlayerRef> playerArg;
    private final RequiredArg<String> guildArg;


    public AddCommand() {
        super("add", "Add a player to a guild");
        this.playerArg = this.withRequiredArg("player", "Player to add", ArgTypes.PLAYER_REF);
        this.guildArg = this.withRequiredArg("guild", "Guild to add to", ArgTypes.STRING);
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
        GuildManager guildManager = GuildManager.getInstance();
        Guild guild = guildManager.getGuildByName(guildArg.get(commandContext));
        if (guild == null) {
            commandContext.sendMessage(Message.raw("[CivGuild] Unknown guild"));
            return CompletableFuture.completedFuture(null);
        }
        guildManager.addMember(commandContext, guild, playerArg.get(commandContext));

        return CompletableFuture.completedFuture(null);
    }
}
