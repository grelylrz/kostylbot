package icu.grely.bot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import icu.grely.ranks.UserSave;

import java.util.Optional;

import static icu.grely.Vars.gateway;
import static icu.grely.bot.SendUtils.getIdByPing;
import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;
import static icu.grely.database.DatabaseConnector.createOrGetUser;

public class RankCommands {
    public static void load() {
        registerCommand("rank", "Посмотреть лвл/ранг, возвращает ваш ранг если вы не пинганули пользователя.", "[user-ping]", (ev, args)->{
            if(args.length==0) {
                Optional<UserSave> u = createOrGetUser(ev.getMessage().getAuthor().get().getId().asString());
                if (u.isPresent()) {
                    sendReply(ev.getMessage(), "Level: "+u.get().getLevel());
                } else {
                    sendReply(ev.getMessage(), "Юзер не найден! Сохранение не найдено!");
                }
            } else {
                try {
                    User du = gateway.getUserById(Snowflake.of(args[0])).block();
                    if(du==null) {
                        sendReply(ev.getMessage(), "Юзер не найден! Проверьте пинг.");
                        return;
                    }
                    Optional<UserSave> u = createOrGetUser(du.getId().asString());
                    if (u.isPresent()) {
                        sendReply(ev.getMessage(), "Level: " + u.get().getLevel());
                    } else {
                        sendReply(ev.getMessage(), "Юзер не найден! Сохранение не найдено!");
                    }
                } catch (NumberFormatException meow) {
                    sendReply(ev.getMessage(), "Юзер не найден! Не удалось запарсить айди!");
                }
            }
        });
    }
}
