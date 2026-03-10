package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;


public class InfoCommand extends AbstractCommandCollection {
    public InfoCommand() {
        super("info", "Show information");
        addSubCommand(new DisplayCommand());
        addSubCommand(new ListGuildsCommand());
        addSubCommand(new ListMembersCommand());
        requirePermission("civtale.user.civguild");
        addAliases("i");
    }

    public static class DisplayCommand extends AbstractAsyncCommand {
        private final RequiredArg<String> guildArg;

        public DisplayCommand() {
            super("display", "Display guild information");
            this.guildArg = this.withRequiredArg("guild", "Guild to display", ArgTypes.STRING);
            requirePermission("civtale.user.civguild");
            addAliases("d");
        }
        @Override
        protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
            GuildManager guildManager = GuildManager.getInstance();
            Guild guild = guildManager.getGuildByName(guildArg.get(commandContext));
            if (guild == null) {
                commandContext.sendMessage(Message.raw("[CivGuild] Unknown guild"));
                return CompletableFuture.completedFuture(null);
            }
            commandContext.sendMessage(Message.raw(guild.toString()));
            return CompletableFuture.completedFuture(null);
        }
    }
    public static class ListGuildsCommand extends AbstractAsyncCommand {

        public ListGuildsCommand() {
            super("listguilds", "List all guilds");
            requirePermission("civtale.user.civguild");
            addAliases("g");
        }
        @Override
        protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
            GuildManager guildManager = GuildManager.getInstance();
            if (guildManager.getGuilds().isEmpty()) {
                commandContext.sendMessage(Message.raw("[CivGuild] No guilds exist"));
                return CompletableFuture.completedFuture(null);
            }
            commandContext.sendMessage(Message.raw("[CivGuild] Listing guilds..."));
            for (Guild guild : guildManager.getGuilds()){
                commandContext.sendMessage(Message.raw(guild.getName()));
            }
            return CompletableFuture.completedFuture(null);
        }
    }
    public static class ListMembersCommand extends AbstractAsyncCommand {
        private final RequiredArg<String> guildArg;

        public ListMembersCommand() {
            super("listmembers", "List members in a guild");
            this.guildArg = this.withRequiredArg("guild", "Guild to lookup", ArgTypes.STRING);
            requirePermission("civtale.user.civguild");
            addAliases("m");
        }
        @Override
        protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
            GuildManager guildManager = GuildManager.getInstance();
            Guild guild = guildManager.getGuildByName(guildArg.get(commandContext));
            if (guild == null) {
                commandContext.sendMessage(Message.raw("[CivGuild] Unknown guild"));
                return CompletableFuture.completedFuture(null);
            }
            commandContext.sendMessage(Message.raw("[CivGuild] Listing members..."));
            guild.getMembers().forEach((member) -> {
                String memberName = member.getUsername();
                commandContext.sendMessage(Message.raw("[" + member.getRank().getDisplayName() + "] " + memberName)); //[RANK] Name
            });
            return CompletableFuture.completedFuture(null);
        }
    }
}
