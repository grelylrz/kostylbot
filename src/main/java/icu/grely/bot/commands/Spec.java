package icu.grely.bot.commands;

import static icu.grely.Vars.*;
import static icu.grely.bot.SendUtils.*;
import static icu.grely.bot.commands.CommandsHandler.commands;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;
import static icu.grely.ranks.UserSave.getUser;

import arc.struct.Seq;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Image;
import icu.grely.bot.SendUtils;
import icu.grely.database.DatabaseConnector;
import icu.grely.ranks.UserSave;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;

public class Spec {
    public static void load() {
        registerCommand("help", "Посмотреть список команд.", "[command-name]", (e, args)->{
            EmbedCreateSpec.Builder em=EmbedCreateSpec.builder().color(Color.SEA_GREEN);
            if(args.length==0) {
                em.title("Список команд.");
                StringBuilder cname = new StringBuilder();
                for (CommandsHandler.BotCommand c : commands) {
                    if (c.isVisible() && c.isActive()) {
                        cname.append(c.getName());
                        for (String alias : c.getAliases())
                            cname.append("/" + alias);
                        // name/alias1/alias2
                        em.addField(cname.toString(), c.getDescription() + "\n" + c.getArgsN(), false);
                        cname.setLength(0);
                    }
                }
            } else {
                CommandsHandler.BotCommand c = commands.find(m->{
                    return m.name.equals(args[0]);
                });
                if(c==null)
                    c=commands.find(m-> m.getAliases().find(a->a.equals(args[0])) != null);
                if (c==null) {
                    sendReply(e.getMessage(), "Command not found.");
                    return;
                }
                if (!c.isVisible() || !c.isActive()) {
                    sendReply(e.getMessage(), "Command not found.");
                    return;
                }
                em.title("Подробная информация о команде.");
                StringBuilder cname = new StringBuilder();
                cname.append(c.name);
                for(String n : c.getAliases()) {
                    cname.append("/"+n);
                }
                em.addField(c.getAliases().isEmpty() ? "Name" : "Aliases", cname.toString(), false);
                em.addField("Description", c.getDescription(), false);
                cname.setLength(0);
            }
            em.footer("Подсказка: команды имеют алиасы, например, команду help можно вызвать написав "+prefix+"help или "+prefix+"хелп", "");
            SendUtils.sendEmbedReply(em.build(), e.getMessage());
        }).setAliases(Seq.with("хелп"));

        registerCommand("info", "Посмотреть информацию о боте.", (e, args)->{
            sendReply(e.getMessage(), "Команд обработано: "+handledCommands+"\nСообщений обработано: "+handledMessages+"\nЮзеров: "+gateway.getUsers().count().block()+"\nСерверов: "+gateway.getGuilds().count().block()+"\nВладелец бота: "+ owner.getUsername());
        }).setAliases("stats", "стата");

        registerCommand("avatar", "Посмотреть аватарку пользователя.", "<user>", (e, args)->{
            if(args.length!=1) {
                sendReply(e.getMessage(), "Должен быть ровно 1 арг.");
                return;
            }
            Snowflake id;
            try {
                id=Snowflake.of(Long.parseLong(getIdByPing(args[0])));
                gateway.getUserById(id)
                        .doOnNext(u -> sendEmbedReply(
                                EmbedCreateSpec.builder()
                                        .image(u.getAvatarUrl())
                                        .color(u.getAccentColor().orElse(Color.GREEN))
                                        .build(),
                                e.getMessage()
                        ))
                        .switchIfEmpty(Mono.fromRunnable(() ->
                                sendReply(e.getMessage(), "Неизвестный пользователь.")
                        ))
                        .subscribe();
            } catch (Exception er) {
                sendReply(e.getMessage(), "Неизвестный пользователь.");
            }
        }).setAliases("аватар");
        registerCommand("server", "Посмотреть информацию о сервере", (ev, args)->{
            ev.getGuild().flatMap(g->{
                // sendEmbedReply(EmbedCreateSpec.builder().addField(g.getName(), "Участников: "+g.getMemberCount()+"\nВладелец: <@"+g.getOwnerId()+">\nМожно выкинуть с сервера: "+g.getPruneCount(7)+" 7d/"+g.getPruneCount(30)+" 30d\nКаналов: "+g.getChannels().count()+"\nБанов: "+g.getBans().count()+"\nБустов: "+g.getPremiumSubscriptionCount().orElse(0), false).footer("", g.getIconUrl(Image.Format.PNG).orElse("")).color(Color.CYAN).build(), ev.getMessage());
                EmbedCreateSpec.Builder b = EmbedCreateSpec.builder().color(Color.LIGHT_SEA_GREEN);
                b.title(g.getName());

                b.addField("Участники",
                        "Всего: " + g.getMemberCount() + "/" + g.getMaxMembers().getAsInt(), true);

                b.addField("Специальное",
                        "Каналов и категорий: " + g.getChannels().count().block() + "\n" +
                                "Бустов: " + g.getPremiumSubscriptionCount().orElse(0) + "\n" +
                                "Фильтрация: " + g.getContentFilterLevel().name(), true);

                b.addField("Подробно",
                        "MFA: " + g.getMfaLevel().name() + "\n" +
                                "NSFW: " + g.getNsfwLevel().name() + "\n" +
                                "Верификация: " + g.getVerificationLevel().name(), true);

                b.addField("Владелец", "<@" + g.getOwnerId().asString() + ">", true);
                b.addField("Создан в", "<t:"+g.getId().getTimestamp().getEpochSecond()+">", true);

                sendEmbedReply(b.build(), ev.getMessage());

                return Mono.empty();
            }).subscribe();
        }).setAliases("сервер", "serverinfo", "серверинфо");
        registerCommand("user", "Посмотреть информацию о юзере", "[ping]", (e, args)->{
            EmbedCreateSpec.Builder em=EmbedCreateSpec.builder();
            if(args.length==0) {
                UserSave us =getUser(e.getMessage().getAuthor().get().getId().asString());
                User author = e.getMessage().getAuthor().get();
                em.color(Color.GREEN);
                em.addField("<@"+ e.getMessage().getAuthor().get().getId().asString()+">", "Создан: <t:"+author.getId().getTimestamp().getEpochSecond()+">", true);
                em.addField("Юзер в базе бота", "Level: "+us.getLevel()+"\nEXP: "+us.getExp()+"Social credit score: "+us.getSocialCredit(), true);
            } else {
                try {
                    User du = gateway.getUserById(Snowflake.of(Long.parseLong(getIdByPing(args[0])))).block();
                    if(du==null) {
                        em.color(Color.RED);
                        em.addField("Failed", "Юзер не найден в дискорде.", true);
                    } else {
                        UserSave us = getUser(du.getId().asString());
                        em.color(Color.GREEN);
                        //em.addField("<@"+du.getId().asString()+">", "Level: "+us.getLevel()+"\nEXP: "+us.getExp(), true);
                        em.addField("<@"+du.getId().asString()+">", "Создан: <t:"+du.getId().getTimestamp().getEpochSecond()+">\nАйди: "+du.getId().asString(), true);
                        em.addField("Юзер в базе бота", "Level: "+us.getLevel()+"\nEXP: "+us.getExp()+"\nSocial credit score: "+us.getSocialCredit(), true);
                    }
                } catch (NumberFormatException meow) {
                    em.color(Color.RED);
                    em.addField("Failed", "Не удалось запарсить дискорд айди.", true);
                }
            }
            em.footer("Подсказка: Юзеры имеют social credit score, данное значение является между-серверным, оно снимается за баны/мьюты/варны, также в скором будущем смогут повышать. Некоторые владельцы серверов могут опираться на него.", "");
            sendEmbedReply(em.build(), e.getMessage());
        }).setAliases("юзер");
        registerCommand("presence", "Set presence", "<query...>", owner.getId().asLong(), (e, args) -> {
            presence=String.join(" ", args);
            gateway.updatePresence(ClientPresence.doNotDisturb(ClientActivity.playing(presence))).subscribe();
        });
    }
}
