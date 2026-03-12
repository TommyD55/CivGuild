package com.civtale.civguild.listeners;

import com.civtale.civguild.Guild;
import com.civtale.civguild.GuildManager;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class ChatListener {
    public static void onPlayerChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        Guild guild = GuildManager.getInstance().getGuildByMember(sender.getUuid());
        if (guild != null) {
            event.setFormatter((playerRef, message) -> Message.join(//transform the chat message
                    Message.raw("[" + guild.getName() + "]" + playerRef.getUsername() + ": " + message).color(guild.getColour()) //Add guild name and change colour of the message
            ));
        }
    }

}
