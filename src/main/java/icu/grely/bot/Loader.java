package icu.grely.bot;

import arc.util.Log;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.gateway.GatewayOptions;
import discord4j.gateway.intent.IntentSet;
import icu.grely.bot.commands.CommandsHandler;
import icu.grely.bot.commands.Spec;
import reactor.core.publisher.Mono;

import static icu.grely.Vars.*;
import static icu.grely.bot.commands.CommandsHandler.handleEvent;

public class Loader {
    public static void load() {
        client=DiscordClient.create(token);
        GatewayBootstrap<GatewayOptions> gp = client.gateway()
                .setEnabledIntents(IntentSet.all());
        gateway = gp.login().block();
        if(gateway==null)
            return;
        gateway.updatePresence(ClientPresence.doNotDisturb(ClientActivity.playing(presence))).subscribe();
        Log.info("Gateway connected!");
        // commands
        Spec.load();
        // end commands
        gateway.getApplicationInfo().flatMap(a->{
            Log.info("Running as @", a.getName());
            a.getOwner().flatMap(o->{
                owner=o;
                return Mono.empty();
            }).subscribe();
            return Mono.empty();
        }).subscribe();
        gateway.on(MessageCreateEvent.class, event -> {
            event.getMessage().getChannel().flatMap(ch->{
                if (ch instanceof ThreadChannel threadChannel) {
                    threadChannel.join().subscribe();
                }
                return Mono.empty();
            }).subscribe();
            handleEvent(event);
            return Mono.empty();
        }).subscribe();

        gateway.onDisconnect().doFinally(t->{
            Log.info("Bot disconnected!");
        }).block();
    }
}
