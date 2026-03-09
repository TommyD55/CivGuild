package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class DisbandCommand extends AbstractAsyncCommand {
    private final RequiredArg<String> guildArg;

    public DisbandCommand() {
        super("disband", "Disband a guild", true);
        this.guildArg = this.withRequiredArg("guild", "Guild to disband", ArgTypes.STRING);
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
        GuildManager guildManager = GuildManager.getInstance();
        Guild guild = guildManager.getGuildByName(guildArg.get(commandContext));
        if (guild == null) {
            commandContext.sendMessage(Message.raw("[CivGuild] Unknown guild"));
            return CompletableFuture.completedFuture(null);
        }
        guildManager.disbandGuild(commandContext, guild);
        return CompletableFuture.completedFuture(null);
    }
}
