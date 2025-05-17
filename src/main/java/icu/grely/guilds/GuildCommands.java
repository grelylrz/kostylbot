package icu.grely.guilds;

import discord4j.core.object.entity.channel.GuildMessageChannel;

import static icu.grely.Vars.owner;
import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;
import static icu.grely.guilds.GuildSave.getGuild;

public class GuildCommands {
    public static void load() {
        registerCommand("guild-test", "dont use", owner.getId().asLong(), (e, args)->{
            e.getMessage().getChannel().ofType(GuildMessageChannel.class).subscribe(ch->{
                String id = ch.getGuildId().asString();
                GuildSave gs = getGuild(id);
                sendReply(e.getMessage(), gs.getSetting("test-value", Boolean.class));
                gs.updateSetting("test-value", !gs.getSetting("test-value", Boolean.class));
            });
        }).setVisible(false);
    }
}
