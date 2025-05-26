package icu.grely.bot.join;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberJoinEvent;

import static icu.grely.guilds.GuildSave.getGuild;

public class JoinRole {
    public static void handle(MemberJoinEvent e) {
        var gs = getGuild(e.getGuildId().asString());
        Snowflake role = gs.getSetting("join-role", Snowflake.class);
        if(role!=null) {
            e.getMember().addRole(role, "Join role.").subscribe();
        }
    }
}
