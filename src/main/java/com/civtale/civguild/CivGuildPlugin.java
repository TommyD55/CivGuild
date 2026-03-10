package com.civtale.civguild;

import com.civtale.civguild.commands.CivGuildCommand;
import com.civtale.civguild.listeners.ChatListener;
import com.civtale.civguild.listeners.PlayerJoinListener;
import com.civtale.civguild.util.DataStorage;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class CivGuildPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public CivGuildPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("%s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        super.setup();

        try { //wrapping everything in try-catch in case an exception occurs it can be tracked
            //Command permission
            PermissionsModule.get().addGroupPermission("Adventure", Set.of("civtale.user.civguild"));
            PermissionsModule.get().addGroupPermission("Creative", Set.of("civtale.admin.civguild"));

            //Initialise plugin objects
            DataStorage.initialize(this.getDataDirectory(), LOGGER); //JavaPlugin knows of directory to use
            GuildManager.initialize(LOGGER);

            //Registers
            this.getCommandRegistry().registerCommand(new CivGuildCommand()); //CivGuild Command
            this.getEventRegistry().registerGlobal(PlayerChatEvent.class, ChatListener::onPlayerChat); //Chat listener
            PlayerJoinListener var10002 = new PlayerJoinListener(LOGGER); //Player join listener
            EventRegistry var10000 = this.getEventRegistry();
            Objects.requireNonNull(var10002);
            var10000.registerGlobal(PlayerReadyEvent.class, var10002::onPlayerReady);

            LOGGER.at(Level.INFO).log("Successfully setup");

        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).log("Failed setup: ", e.getMessage());
        }
    }
}
