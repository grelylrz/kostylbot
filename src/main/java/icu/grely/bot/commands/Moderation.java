package icu.grely.bot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.core.spec.BanQuerySpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Permission;

import java.time.Instant;
import java.util.Arrays;

import static icu.grely.Utils.parseTime;
import static icu.grely.Vars.gateway;
import static icu.grely.bot.SendUtils.getIdByPing;
import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;
import static icu.grely.bot.commands.CommandsHandler.setCategory;
import static icu.grely.database.DatabaseConnector.banUser;

public class Moderation {
    public static void load() {
        setCategory("moderation");
        registerCommand("ban", "ban lil bro", "<ping> <time> <reason...>", (e, args)->{
            if(args.length<3) {
                sendReply(e.getMessage(), "Нужно указать как минимум участника, время разбана и причину бана!\nВремя принимается так:\n1 - минута\n1h - 1 час\n1d - 1 день \n 1w - 1 неделя\n1m - 1 месяц\n1y - 1 год");
                return;
            }
            long seconds = parseTime(args[1]);
            if (seconds == -1) {
                sendReply(e.getMessage(), "Неизвестный тип времени, обратитесь к гайду написав команду без аргов.");
                return;
            }
            try {
                gateway.getUserById(Snowflake.of(Long.parseLong(getIdByPing(args[0])))).subscribe(u->{
                    e.getMessage().getGuild().subscribe(g->{
                        Instant un=Instant.now().plusSeconds(seconds);
                        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        User aut =  e.getMessage().getAuthor().get();
                        banUser(g, u, aut, reason, un);
                        u.getPrivateChannel().subscribe(pr->{
                            pr.createMessage(MessageCreateSpec.builder().content("Вы были заблокированы на "+g.getName()+"\nАдминистратор: "+aut.getUsername()+"\nДата разбана <t:"+seconds+">").build()).subscribe();
                        });
                        g.ban(u.getId(), BanQuerySpec.builder().reason(aut.getUsername()+": "+reason).build()).subscribe();
                        sendReply(e.getMessage(), "Success.")
                    });
                });
            } catch (NumberFormatException ex) {
                sendReply(e.getMessage(), "Пинг не действителен.");
            }
        }).setPermissions(Permission.BAN_MEMBERS, Permission.ADMINISTRATOR);
    }
}
