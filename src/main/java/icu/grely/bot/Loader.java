package icu.grely.bot;

import arc.util.Log;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.gateway.GatewayOptions;
import discord4j.gateway.intent.IntentSet;
import icu.grely.bot.commands.*;
import icu.grely.database.DatabaseConnector;
import icu.grely.guilds.GuildCommands;
import icu.grely.ranks.ReputationHandler;
import icu.grely.ranks.UserSave;
import reactor.core.publisher.Mono;

import static icu.grely.Vars.*;
import static icu.grely.bot.commands.CommandsHandler.handleEvent;
import static icu.grely.logger.BLogger.write;
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
        Moderation.load();
        NSFW.load();
        GuildCommands.load();
        // end commands
        gateway.on(MessageCreateEvent.class, event -> {
            if(!event.getMessage().getAuthor().isPresent())
                return Mono.empty();
            if(!event.getGuildId().isPresent())
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
            executor.submit(()->{
                event.getGuild().flatMap(g->{
                    event.getMessage().getChannel().ofType(TextChannel.class).flatMap(ch->{
                        write("logs/"+g.getName().replace("/", "").replace("\\", "").replace(" ", "-")+".txt", "["+ch.getName()+"] "+"["+author.getUsername()+"] " + event.getMessage().getContent()+"\n");
                        return Mono.empty();
                    }).subscribe();
                    return Mono.empty();
                }).subscribe();
            });
            handledMessages+=1;
            handleEvent(event);
            // ReputationHandler.handle(event);
            return Mono.empty();
        }).subscribe();
        Log.info("Finally, bot can handle messages.");
        gateway.onDisconnect().doFinally(t->{
            Log.info("Bot disconnected!");
        }).block();
    }
}
