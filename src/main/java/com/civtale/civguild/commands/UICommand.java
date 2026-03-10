package com.civtale.civguild.commands;

import com.civtale.civguild.pages.GuildUIPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

public class UICommand extends AbstractPlayerCommand {
    public UICommand() {
        super("ui", "Open CivGuild User Interface");
        requirePermission("civtale.user.civguild");
        addAliases("gui", "window", "interface", "user interface");
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        //Create instance of the Guild UI page //TODO once hotkey is implemented, the below could probably be a func to call elsewhere
        GuildUIPage page = new GuildUIPage(playerRef);
        //Use player's PageManager to open the new page
        Player player = store.getComponent(ref, Player.getComponentType()); //Retrieve player component by looking up entity ref & component type
        assert player != null; //throws exception is this is the case
        player.getPageManager().openCustomPage(ref, store, page);
    }
}
