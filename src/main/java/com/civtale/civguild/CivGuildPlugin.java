package com.civtale.civguild;

import com.civtale.civguild.commands.CivGuildCommand;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

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

            //Initialise plugin objects
            GuildManager.initialize();

            //Registers
            this.getCommandRegistry().registerCommand(new CivGuildCommand()); //CivGuild Command

        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).log("[CivGuild] Failed setup: %s", e.getMessage());
        }
    }
}
