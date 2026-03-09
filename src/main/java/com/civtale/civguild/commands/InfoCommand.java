package com.civtale.civguild.commands;

import com.civtale.civguild.Guild;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.universe.Universe;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class InfoCommand extends AbstractCommandCollection {
    public InfoCommand() {
        super("info", "Show information");
        addSubCommand(new DisplayCommand());
        addSubCommand(new ListGuildsCommand());
        addSubCommand(new ListMembersCommand());
    }

    public class DisplayCommand extends AbstractAsyncCommand {
        public DisplayCommand() {
            super("display", "Display guild information");
        }
        @Override
        protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
            Guild guildInfo = lookupGuild(args[2]);
            if (guildInfo != null) { playerRef.sendMessage(Message.raw(guildInfo.toString()));
            } else { playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));}

            return null;
        }
    }
    public class ListGuildsCommand extends AbstractAsyncCommand {
        public ListGuildsCommand() {
            super("listguilds", "List all guilds");
        }
        @Override
        protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
            if (guildManager.getGuilds().isEmpty()) {
                playerRef.sendMessage(Message.raw("[CivGuild] No guilds exist"));
                return;
            }
            playerRef.sendMessage(Message.raw("[CivGuild] Listing guilds..."));
            for (Guild guild : guildManager.getGuilds()){
                playerRef.sendMessage(Message.raw(guild.getName()));
            }

            return null;
        }
    }
    public class ListMembersCommand extends AbstractAsyncCommand {
        public ListMembersCommand() {
            super("listmembers", "List members in a guild");
        }
        @Override
        protected @NonNull CompletableFuture<Void> executeAsync(@NonNull CommandContext commandContext) {
            Guild guild = lookupGuild(args[2]);
            if (guild == null) {
                playerRef.sendMessage(Message.raw("[CivGuild] Unknown guild"));
                return;
            }
            playerRef.sendMessage(Message.raw("[CivGuild] Listing members..."));
            guild.getMembers().forEach((member) -> {
                UUID playerUuid = member.getPlayerUuid();
                String memberName = Objects.requireNonNull(Universe.get().getPlayer(playerUuid)).getUsername();
                playerRef.sendMessage(Message.raw("[" + member.getRank().getDisplayName() + "] " + memberName)); //[RANK] Name
            });

            return null;
        }
    }
}
