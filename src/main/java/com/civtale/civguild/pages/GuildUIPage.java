package com.civtale.civguild.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

//BasicCustomUIPage is UI without event handling
//InteractiveCustomUIPage for buttons & inputs with events, generic is the class used by handleDataEvent()
public class GuildUIPage extends InteractiveCustomUIPage<GuildUIPage.GuildEventData> {

    //Subclass to handle input data between client and server, in this case the name of a guild
    //Player clicks button -> Client reads input value -> Client deserializes into GuildEventData -> Send to Server -> Server deserializes -> Server runs handleDataEvent()
    public static class GuildEventData {
        public String guildName; //Server receives the value against 'GuildName' and has to load it into this variable
        //Build a codec for this class
        public static final BuilderCodec<GuildEventData> CODEC = BuilderCodec.<GuildEventData>builder(GuildEventData.class, GuildEventData::new)
                .append( //define codec behaviour with lambda functions
                        new KeyedCodec<>("@GuildName", Codec.STRING), //handle this field as a String, @ binds to UI, is read from an input
                        (obj, val) -> obj.guildName = val, //Setter (received from client): take the 'GuildName' value and put it into GuildEventData.guildName
                        obj -> obj.guildName //Getter (sending to client)
                ).add()
                .build(); //build the codec
    }

    //Constructor
    public GuildUIPage(@NonNull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, GuildEventData.CODEC); //
        //CustomPageLifetime: CantClose Player cannot close, CanDismiss ESC to close, CanDismissOrClose... ESC or click outside
    }

    @Override //Loads the UI file & creates events
    public void build(@NonNull Ref<EntityStore> ref, @NonNull UICommandBuilder uiCommandBuilder, @NonNull UIEventBuilder uiEventBuilder, @NonNull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/GuildUI.ui"); //Builds the graphical ui //directory relative to resources/Common/UI/Custom
        //Builds an event - when button is clicked, ID of the button, create new event data named '@GuildName' using the input value from ID of the text input
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CreateGuildButton", new EventData().append("@GuildName", "#GuildInput.Value"));
    }

    @Override
    public void handleDataEvent(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, @NonNull GuildEventData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        //retrieve data from the handler object //NOTE better practice would be to use a getter and have guildName private
        String guildName = data.guildName != null && !data.guildName.isEmpty() //uses the default value if the string is null
                ? data.guildName : "DefaultGuild";

        //Now action based on this event, ie check guild name is correct, change the UI
        playerRef.sendMessage(Message.raw("Guild: " + guildName));
        player.getPageManager().setPage(ref, store, Page.None); //closes the page
    }
}

/* //Set element properties dynamically Java -> UI
class extends InteractiveCustomUIPage<class.CloseEventData> { //this example includes a simple close button to show data being passed both directions
    private final int playersOnline; //add this value to be set by the constructor

subclass CloseEventData { //just need to build the codec if no info is being passed
    public static final BuilderCodec<CloseEventData> CODEC = BuilderCodec.builder(CloseEventData.class, CloseEventData::new).build();
    }

build():
.append(Page)
uiCommandBuilder.set("#Stat1Value.Text", String.valueOf(playersOnline)); //set dynamic values, convert numerals to String
uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton"); //build event for close button - in handleDataEvent() simply close the page
 */