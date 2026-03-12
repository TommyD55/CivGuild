package com.civtale.civguild;

import com.civtale.civguild.commands.CivGuildCommand;
import com.civtale.civguild.listeners.ChatListener;
import com.civtale.civguild.listeners.DeathListener;
import com.civtale.civguild.listeners.PlayerJoinListener;
import com.civtale.civguild.util.DataStorage;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.util.Set;
import java.util.logging.Level;

public class CivGuildPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final PlayerJoinListener playerJoinListener;

    public CivGuildPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("%s version %s", this.getName(), this.getManifest().getVersion().toString());
        playerJoinListener = new PlayerJoinListener(LOGGER);
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
            this.getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, (e) -> e.setBroadcastJoinMessage(false)); //Stops broadcast message being sent
            this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, playerJoinListener::onPlayerReady); //Player Ready Listener

            LOGGER.at(Level.INFO).log("Successfully setup");

        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).log("Failed setup: ", e.getMessage());
        }
    }

    @Override //Store systems register here
    protected void start() {
        super.start();
        this.getEntityStoreRegistry().registerSystem(new DeathListener()); //Register Death Listener
    }
}
