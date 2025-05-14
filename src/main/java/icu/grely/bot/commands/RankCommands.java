package icu.grely.bot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import icu.grely.ranks.UserSave;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static icu.grely.Vars.cachedUsers;
import static icu.grely.Vars.gateway;
import static icu.grely.bot.SendUtils.*;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;
import static icu.grely.database.DatabaseConnector.*;
import static icu.grely.ranks.UserSave.getUser;

public class RankCommands {
    public static void load() {
        registerCommand("rank", "Посмотреть лвл/ранг, возвращает ваш ранг если вы не пинганули пользователя.", "[user-ping]", (ev, args)->{
            EmbedCreateSpec.Builder em=EmbedCreateSpec.builder();
            if(args.length==0) {
                UserSave us =getUser(ev.getMessage().getAuthor().get().getId().asString());
                em.color(Color.GREEN);
                sendReply(ev.getMessage(), "Level: "+us.getLevel()+"\nEXP: "+us.getExp());
            } else {
                try {
                    User du = gateway.getUserById(Snowflake.of(Long.parseLong(getIdByPing(args[0])))).block();
                    if(du==null) {
                        em.color(Color.RED);
                        em.addField("Failed", "Юзер не найден.", true);
                    } else {
                        UserSave us = getUser(du.getId().asString());
                        em.color(Color.GREEN);
                        em.addField("<@"+du.getId().asString()+">", "Level: "+us.getLevel()+"\nEXP: "+us.getExp(), true);
                    }
                } catch (NumberFormatException meow) {
                    em.color(Color.RED);
                    em.addField("Failed", "Не удалось запарсить айди.", true);
                }
            }
            sendEmbedReply(em.build(), ev.getMessage());
        });
        registerCommand("leaderboard", "Посмотреть лидерборд по серверам", (e, args)->{
            List<UserSave> lb = getLeaderboard();
            StringBuilder sb = new StringBuilder();
            for(UserSave us : lb) {
                //sb.append(gateway.getUserById(Snowflake.of(us.getId())).block().getUsername()+" "+us.getExp()+" exp "+us.getLevel()+ " lvl\n");
                gateway.getUserById(Snowflake.of(us.getId())).flatMap(user->{
                    sb.append(user.getUsername()+" "+us.getExp()+" exp "+us.getLevel()+ " lvl\n");
                    return Mono.empty();
                }).subscribe();
            }
            sb.setLength(1024);
            sendEmbed(e.getMessage().getChannelId(), EmbedCreateSpec.builder().color(Color.ORANGE).addField("Список лидеров", sb.toString(), true).build());
        }).setAliases("lb", "лидеры");
    }
}
