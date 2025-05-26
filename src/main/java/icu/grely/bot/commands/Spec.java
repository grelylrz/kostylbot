package icu.grely.bot.commands;

import static icu.grely.Vars.*;
import static icu.grely.bot.SendUtils.*;
import static icu.grely.bot.commands.CommandCategory.parseCategory;
import static icu.grely.bot.commands.CommandsHandler.*;
import static icu.grely.guilds.GuildSave.getGuild;
import static icu.grely.ranks.UserSave.getUser;

import arc.Core;
import arc.struct.Seq;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.ForumTag;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.ForumChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.spec.*;
import discord4j.rest.util.Color;
import discord4j.rest.util.Image;
import discord4j.rest.util.Permission;
import icu.grely.bot.SendUtils;
import icu.grely.database.DatabaseConnector;
import icu.grely.ranks.UserSave;
import lombok.val;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;

public class Spec {
    public static void load() {
        setCategory("spec");
        registerCommand("help", "Посмотреть список команд.", "[category]", (e, args)->{
            EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
            StringBuilder sb = new StringBuilder();
            if(args.length==0) {
                for(CommandCategory ct : CommandCategory.values()) {
                    if(ct.visilbe)
                        sb.append(ct.name+"\n");
                }
                sb.setLength(1024);
                embed.color(Color.SEA_GREEN);
                embed.addField("Доступные категории", sb.toString(), true);
                sb.setLength(0);
            } else {
                CommandCategory ct = parseCategory(args[0]);
                if(ct.name.equals("unkown")) {
                    embed.color(Color.RED);
                    embed.addField("Ошибка", "Категория не найдена.", true);
                } else {
                    embed.color(Color.SEA_GREEN);
                    for(BotCommand c : commands) {
                        if((c.isActive() && c.isVisible()) && c.getCategory().name.equals(ct.name)) {
                            sb.append(c.getName());
                            for (String s : c.getAliases())
                                sb.append("/" + s);
                            embed.addField(sb.toString(), c.getDescription() + "\n" + c.getArgsN() + (c.isDisailable() ? "\nЭту команду можно отключить!" : ""), true);
                            sb.setLength(0);
                        }
                    }
                    sb.setLength(0);
                }
            }
            embed.footer("Команды имеют алиасы, например, help команду можно вызвать написав "+prefix+"help или "+prefix+"хелп.\nПрефикс бота тоже имеет алиасы, команды можно написать вызвав "+prefix+" или "+prefixAlias, "");
            sendEmbedReply(embed.build(), e.getMessage());
        }).setAliases(Seq.with("хелп"));

        registerCommand("info", "Посмотреть информацию о боте.", (e, args) -> {
            long heapUsed = Core.app.getJavaHeap() / (1024 * 1024);
            long uptime = System.currentTimeMillis() - startedOn;
            StringBuilder start = new StringBuilder();
            if (uptime >= 86400000) start.append(uptime / 86400000).append("d ");
            if (uptime >= 3600000) start.append((uptime / 3600000) % 24).append("h ");
            start.append((uptime / 1000) % 60).append("s");
            sendReply(e.getMessage(),
                    "Команд обработано: " + handledCommands +
                            "\nСообщений обработано: " + handledMessages +
                            "\nЮзеров: " + gateway.getUsers().count().block() +
                            "\nСерверов: " + gateway.getGuilds().count().block() +
                            "\nRAM Used: " + heapUsed + "MB" +
                            "\nВладелец бота: " + owner.getUsername()
                            +"\nРаботаю уже: " + start.toString().trim()
            );
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
        }).setDisailable(true).setAliases("аватар");
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
        }).setDisailable(true).setAliases("сервер", "serverinfo", "серверинфо");
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
        }).setDisailable(true).setAliases("юзер");
        registerCommand("presence", "Set presence", "<query...>", owner.getId().asLong(), (e, args) -> {
            presence=String.join(" ", args);
            gateway.updatePresence(ClientPresence.doNotDisturb(ClientActivity.playing(presence))).subscribe();
        });
    }
    public static void generateDisaibleCommand() {
        setCategory("disable");
        for(CommandsHandler.BotCommand c : commands) {
            if(c.isDisailable()) {
                registerCommand("disaible-"+c.getName(), "Переключить такую то команду, т.е. смогут ли ее использовать.", (e, args)->{
                    val gs = getGuild(e.getGuildId().get().asString());
                    e.getMember().ifPresent(m->{
                        if(m.getId().asString().equals(owner.getId().asString())) {
                            Boolean current = gs.getSetting(c.getName()+"-DISAIBLE", Boolean.class);
                            if(current==null)
                                current=false;
                            sendReply(e.getMessage(), "Переключено "+current+" -> "+!current);
                            gs.updateSetting(c.getName()+"-DISAIBLE", !current);
                            return;
                        }
                        m.getRoles().subscribe(role->{
                            if(role.getPermissions().contains(Permission.MANAGE_GUILD) || role.getPermissions().contains(Permission.ADMINISTRATOR)) {
                                Boolean current = gs.getSetting(c.getName()+"-DISAIBLE", Boolean.class);
                                if(current==null)
                                    current=false;
                                sendReply(e.getMessage(), "Переключено "+current+" -> "+!current);
                                gs.updateSetting(c.getName()+"-DISAIBLE", !current);
                                return;
                            }
                        });
                    });
                }).setDisable(true).setDisailable(false);
            }
        }
    }
}
