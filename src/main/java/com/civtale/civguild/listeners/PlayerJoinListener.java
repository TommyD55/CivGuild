package com.civtale.civguild.listeners;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.civtale.civguild.GuildMember;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;

import java.util.UUID;
import java.util.logging.Level;

public class PlayerJoinListener {
    private static HytaleLogger logger;
   public PlayerJoinListener(HytaleLogger logger) {
        PlayerJoinListener.logger = logger;
    }

    public void onPlayerReady(PlayerReadyEvent event) {
        try {
            Player player = event.getPlayer();
            WorldMapTracker mapTracker = player.getWorldMapTracker();
            //UUID uuid = getUUID(player);
            UUID uuid = player.getUuid(); //TODO Non-deprecated method??
            GuildManager guildManager = GuildManager.getInstance();
            Guild guild = guildManager.getGuildByMember(uuid);

            updateMapFilter(uuid, mapTracker, guild); //update map filter
            guildManager.updatePlayerNameplate(guild, uuid); //update nameplate

            GuildMember member = guild.getMember(uuid); //ensure saved player username matches
            if (!player.getDisplayName().equals(member.getUsername())) {
                member.setUsername(player.getDisplayName());
            }

        } catch (Exception e) {
            logger.at(Level.WARNING).log("Could not initialize world map tracker & nameplate: " + e.getMessage());
        }
    }

    private static void updateMapFilter(UUID playerUuid, WorldMapTracker mapTracker, Guild playerGuild) {
        mapTracker.setPlayerMapFilter((otherPlayer) -> { // This lambda runs everytime the map needs to set the filter TODO more documentation on this would be good
            UUID otherPlayerUUID = otherPlayer.getUuid();
            if (otherPlayerUUID == playerUuid) { //show if same player
                return false;
            }
            if (playerGuild == null){ // this player isn't in a guild, don't show them
                return true;
            }
            return !playerGuild.hasMember(otherPlayerUUID); //only show if in same guild
        });
    }
}


/*
    //Must access player UUID component using thread-safe method //TODO player.getUuid() is deprecated, surely a better way of getting it?
    private static UUID getUUID(Player player) {
        assert player.getReference() != null; //must access components to edit the player
        Ref<EntityStore> ref = player.getReference();
        Store<EntityStore> store = ref.getStore();
        World world = player.getWorld();
        assert world != null;
        AtomicReference<UUID> uuid = new AtomicReference<>();
        world.execute(()->{
            uuid.set(Objects.requireNonNull(store.getComponent(ref, UUIDComponent.getComponentType())).getUuid());});
        logger.at(Level.WARNING).log("player: " + player.toString() + "ref: " + ref.toString() + "store" + store.toString() + "world: " + world.toString());
        return uuid.get();
    }*/

