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

public class RenameCommand extends AbstractAsyncCommand {
    private final RequiredArg<String> guildArg;
    private final RequiredArg<String> nameArg;

    public RenameCommand() {
        super("rename", "Rename a guild");
        this.guildArg = this.withRequiredArg("guild", "Guild to rename", ArgTypes.STRING);
        this.nameArg = this.withRequiredArg("name", "New guild name", ArgTypes.STRING);
        addAliases("name");
    }

    @Override
    protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
        GuildManager guildManager = GuildManager.getInstance();
        Guild guild = guildManager.getGuildByName(guildArg.get(commandContext));
        if (guild == null) {
            commandContext.sendMessage(Message.raw("[CivGuild] Unknown guild"));
            return CompletableFuture.completedFuture(null);
        }
        guildManager.renameGuild(commandContext, guild, nameArg.get(commandContext));
        return CompletableFuture.completedFuture(null);
    }
}
