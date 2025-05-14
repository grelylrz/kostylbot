package icu.grely.bot;

import arc.util.Log;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.gateway.GatewayOptions;
import discord4j.gateway.intent.IntentSet;
import icu.grely.bot.commands.CommandsHandler;
import icu.grely.bot.commands.Fun;
import icu.grely.bot.commands.RankCommands;
import icu.grely.bot.commands.Spec;
import icu.grely.database.DatabaseConnector;
import icu.grely.ranks.UserSave;
import reactor.core.publisher.Mono;

import static icu.grely.Vars.*;
import static icu.grely.bot.commands.CommandsHandler.handleEvent;
import static icu.grely.ranks.UserSave.getUser;

/**Подгрузчик бота.*/
public class Loader {
    /**Подгрузить бота*/
    public static void load() {
        client=DiscordClient.create(token);
        GatewayBootstrap<GatewayOptions> gp = client.gateway()
                .setEnabledIntents(IntentSet.all());
        gateway = gp.login().block();
        if(gateway==null)
            return;
        gateway.updatePresence(ClientPresence.doNotDisturb(ClientActivity.playing(presence))).subscribe();
        Log.info("Gateway connected!");
        gateway.getApplicationInfo().flatMap(a->{
            Log.info("Running as @", a.getName());
            a.getOwner().flatMap(o->{
                owner=o;
                return Mono.empty();
            }).block();
            return Mono.empty();
        }).block();
        // commands
        Spec.load();
        Fun.load();
        DatabaseConnector.loadSQLCommands();
        RankCommands.load();
        // end commands
        gateway.on(MessageCreateEvent.class, event -> {
            if(!event.getMessage().getAuthor().isPresent())
                return Mono.empty();
            User author = event.getMessage().getAuthor().get();
            if(author.isBot())
                return Mono.empty();
            event.getMessage().getChannel().flatMap(ch->{
                if (ch instanceof ThreadChannel threadChannel) {
                    threadChannel.join().subscribe();
                }
                return Mono.empty();
            }).subscribe();
            UserSave us =getUser(author.getId().asString());
            us.setExp(us.getExp()+expPerMessage);
            handleEvent(event);
            return Mono.empty();
        }).subscribe();

        gateway.onDisconnect().doFinally(t->{
            Log.info("Bot disconnected!");
        }).block();
    }
}
