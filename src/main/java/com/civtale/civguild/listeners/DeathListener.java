package com.civtale.civguild.listeners;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.event.KillFeedEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.UUID;

public class DeathListener extends DeathSystems.OnDeathSystem {
    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { //Only applicable to players
        return Query.and(Player.getComponentType());
    }

    @Override //Runs when death component is added to a player
    public void onComponentAdded(@NonNull Ref<EntityStore> ref, @NonNull DeathComponent deathComponent, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {
        Player player = store.getComponent(ref, Player.getComponentType());
        assert player != null;
        assert player.getWorld() != null;
        PlayerConfigData config = player.getPlayerConfigData();
        PlayerWorldData worldData = config.getPerWorldData(player.getWorld().getName()); //player's config data in this world

        //If player doesn't have saved respawn points (ie at a bed), then add the guild's respawn point
        if (worldData.getRespawnPoints() == null || worldData.getRespawnPoints().length == 0) {
            UUID uuid = player.getUuid(); //TODO Non-deprecated method??
            Guild guild = GuildManager.getInstance().getGuildByMember(uuid);
            if (guild != null) { //skip if no guild
                Vector3d guildSpawnpoint = guild.getSpawnpoint();
                Vector3i blockSpawnpoint = new Vector3i((int)guildSpawnpoint.x, (int)guildSpawnpoint.y, (int)guildSpawnpoint.z); //required so just re-use the guild spawnpoint
                worldData.setRespawnPoints(new PlayerRespawnPointData[]{new PlayerRespawnPointData(blockSpawnpoint, guildSpawnpoint, "Guild Spawnpoint")});
                config.markChanged(); //update player config data
            } //TODO random spawn if doesn't have one saved?
        }

    }

}
