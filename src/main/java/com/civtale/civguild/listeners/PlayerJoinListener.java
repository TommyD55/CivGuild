package com.civtale.civguild.listeners;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.civtale.civguild.GuildMember;
import com.civtale.civguild.GuildRank;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
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

            UUID uuid = getUUID(player);
            Guild guild = GuildManager.getInstance().getGuildByMember(uuid);
            updateMapFilter(uuid, mapTracker, guild); //update map filter

            if (guild == null) { //Update player nameplate if they are in a guild
                return;
            }
            String displayText = "[" + guild.getName() + "]"; //add guild name
            GuildRank rank = guild.getMember(uuid).getRank();
            if (rank.getPermissionLevel() > 1){ //add rank if higher than member #TODO shorten this?
                displayText += "[" + rank.getDisplayName() + "]";
            }
            displayText += player.getDisplayName();
            updatePlayerNameplate(player, displayText); //TODO colour not yet supported?

            GuildMember member = guild.getMember(uuid); //ensure saved player username matches
            if (!player.getDisplayName().equals(member.getUsername())) {
                member.setUsername(player.getDisplayName());
            }

        } catch (Exception e) {
            logger.at(Level.WARNING).log("Could not initialize world map tracker & nameplate: " + e.getMessage());
        }

    }

    public void updatePlayerNameplate(Player player) { //TODO when is this required? pass in guild, uuid instead?

    }

    public void updatePlayerMapFilter(Player player) { //TODO when is this required? pass in guild, uuid instead?
        try {
            WorldMapTracker mapTracker = player.getWorldMapTracker();
            assert player.getReference() != null;
            UUID uuid = getUUID(player);
            updateMapFilter(uuid, mapTracker, GuildManager.getInstance().getGuildByMember(uuid));

        } catch (Exception e) {
            logger.at(Level.WARNING).log("Could not update world map tracker: " + e.getMessage());
        }
    }


    private void updateMapFilter(UUID playerUuid, WorldMapTracker mapTracker, Guild playerGuild) {
        mapTracker.setPlayerMapFilter((otherPlayer) -> { //run through all players
            UUID otherPlayerUUID = otherPlayer.getUuid();
            if (otherPlayerUUID == playerUuid) { //show if same player
                return false;
            }
            if (playerGuild == null){ // this player isn't in a guild, don't show them
                return true;
            }
            return !playerGuild.hasMember(otherPlayerUUID); //don't show if not in same guild
        });
    }

    //Must access player UUID component using thread-safe method //TODO player.getUuid() is deprecated, surely a better way of getting it?
    private UUID getUUID(Player player) {
        assert player.getReference() != null; //must access components to edit the player
        Ref<EntityStore> ref = player.getReference();
        Store<EntityStore> store = ref.getStore();
        World world = player.getWorld();
        assert world != null;
        AtomicReference<UUID> uuid = new AtomicReference<>();
        world.execute(()->{
            uuid.set(Objects.requireNonNull(store.getComponent(ref, UUIDComponent.getComponentType())).getUuid());});
        return uuid.get();
    }
    //Access component using thread-safe method
    private void updatePlayerNameplate(Player player, String displayText) {
        Ref<EntityStore> ref = player.getReference();
        assert ref != null;
        Store<EntityStore> store = ref.getStore();
        World world = player.getWorld();
        assert world != null;
        world.execute(()->{
            Objects.requireNonNull(store.getComponent(ref, Nameplate.getComponentType())).setText(displayText);
        });
    }
}
