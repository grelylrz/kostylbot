package icu.grely.bot.commands;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.possible.Possible;

import static icu.grely.Vars.gateway;
import static icu.grely.Vars.owner;
import static icu.grely.bot.SendUtils.sendEmbedReply;
import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;
import static icu.grely.bot.commands.CommandsHandler.setCategory;

public class Testing {
    public static void load() {
        setCategory("testing");
        registerCommand("guilds", "", owner.getId().asLong(), (e, args)->{
            EmbedCreateSpec.Builder em = EmbedCreateSpec.builder();
            gateway.getGuilds().subscribe(s->{
                em.addField(s.getName(), s.getMembers().count()+s.getOwner().block().getUsername(), true);
            });
            sendEmbedReply(em.build(), e.getMessage());
        });
    }
}
