package icu.grely.bot.commands;

import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.rest.util.Permission;
import icu.grely.guilds.GuildSave;

import static icu.grely.Vars.owner;
import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;
import static icu.grely.bot.commands.CommandsHandler.setCategory;
import static icu.grely.guilds.GuildSave.getGuild;

public class GuildCommands {
    public static void load() {
        setCategory(CommandCategory.guilds);
        registerCommand("set-joinrole", "Установить роль дающуюся при заходе.", "<role>", (e, args)->{
            if(args.length==0) {
                getGuild(e.getGuildId().get().asString()).updateSetting("join-role", null);
            }
            if(args.length!=1) {
                sendReply(e.getMessage(), "Only one role.");
                return;
            }
            e.getMessage().getRoleMentions().subscribe(r->{
                getGuild(e.getGuildId().get().asString()).updateSetting("join-role", r.getId());
            });
        }).setPermissions(Permission.ADMINISTRATOR, Permission.MANAGE_ROLES);
        registerCommand("add-role", "Добавить роль каждому участнику.", (e, args)->{
            if(args.length!=1) {
                sendReply(e.getMessage(), "Only one role.");
                return;
            }
            e.getMessage().getRoleMentions().subscribe(r->{
                e.getGuild().subscribe(g->{
                    g.getMembers().subscribe(m->{
                        m.addRole(r.getId(), "add-role command used.");
                    });
                });
            });
        }).setPermissions(Permission.ADMINISTRATOR);
    }
}
