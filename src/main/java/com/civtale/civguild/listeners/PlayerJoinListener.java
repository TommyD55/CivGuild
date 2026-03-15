package com.civtale.civguild.listeners;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.civtale.civguild.GuildMember;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;

import java.util.UUID;
import java.util.logging.Level;

public class PlayerJoinListener {
    private static HytaleLogger logger;
   public PlayerJoinListener(HytaleLogger logger) {
        PlayerJoinListener.logger = logger;
    }

    //Player ready in world - player entity must be set up before being edited
    public void onPlayerReady(PlayerReadyEvent event) {
        try {
            Player player = event.getPlayer();
            WorldMapTracker mapTracker = player.getWorldMapTracker();
            //UUID uuid = getUUID(player);
            UUID uuid = player.getUuid(); //TODO Non-deprecated method??
            GuildManager guildManager = GuildManager.getInstance();
            Guild guild = guildManager.getGuildByMember(uuid); //null if no guild

            //Set Map filter
            mapTracker.setPlayerMapFilter((otherPlayer) -> { // This lambda runs everytime the map needs to set the filter TODO more documentation on this would be good
                UUID otherPlayerUUID = otherPlayer.getUuid();
                if (otherPlayerUUID == uuid) { //show if same player
                    return false;
                }
                if (guild == null){ // this player isn't in a guild, don't show them
                    return true;
                }
                return !guild.hasMember(otherPlayerUUID); //only show if in same guild
            });

            //ensure saved player username matches
            String username = player.getDisplayName();
            guildManager.updateUsername(uuid, username);

            if (guild != null) {
                //Notify guild that this player has joined
                guild.notifyMembers(username + " joined the server");
            }

            //update nameplate regardless of guild status
            guildManager.updatePlayerNameplate(guild, uuid);


        } catch (Exception e) {
            logger.at(Level.WARNING).log("Could not initialize world map tracker & nameplate: " + e.getMessage());
        }
    }

}
