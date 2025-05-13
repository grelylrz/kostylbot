package icu.grely.bot.commands;

import static icu.grely.Vars.*;
import static icu.grely.bot.SendUtils.sendEmbedReply;
import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.commands;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;

import arc.struct.Seq;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import icu.grely.bot.SendUtils;
import reactor.core.publisher.Mono;

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
            sendReply(e.getMessage(), "Команд обработано: "+handledCommands+"\nВладелец бота: "+ owner.getUsername());
        }).setAliases("stats");
        registerCommand("avatar", "Посмотреть аватарку пользователя.", "<user>", (e, args)->{
            if(args.length!=1) {
                sendReply(e.getMessage(), "Должен быть ровно 1 арг.");
                return;
            }
            Snowflake id;
            try {
                id=Snowflake.of(Long.parseLong(args[0]));
                gateway.getUserById(id).flatMap(u->{
                    sendEmbedReply(EmbedCreateSpec.builder().image(u.getAvatarUrl()).color(u.getAccentColor().isPresent() ? Color.GRAY : u.getAccentColor().get()).build(), e.getMessage());
                    return Mono.empty();
                }).subscribe();
            } catch (Exception er) {
                sendReply(e.getMessage(), "Неизвестный пользователь.");
            }
        }).setAliases("аватар");
        Fun.load();
    }
}
