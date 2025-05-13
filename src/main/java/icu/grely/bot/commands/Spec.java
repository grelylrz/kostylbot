package icu.grely.bot.commands;

import static icu.grely.Vars.prefix;
import static icu.grely.bot.commands.CommandsHandler.commands;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;

import arc.struct.Seq;
import discord4j.core.object.Embed;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import icu.grely.bot.SendUtils;

public class Spec {
    public static void load() {
        registerCommand("help", "Посмотреть список команд.", "[command-name]", (e, args)->{
            EmbedCreateSpec.Builder em=EmbedCreateSpec.builder().title("Список команд.").color(Color.SEA_GREEN);
            StringBuilder cname=new StringBuilder();
            for(CommandsHandler.BotCommand c : commands) {
                if(c.isVisible() && c.isActive()) {
                    cname.append(c.getName());
                    for(String alias : c.getAliases())
                        cname.append("/"+alias);
                    // name/alias1/alias2
                    em.addField(cname.toString(), c.getDescription() + "\n" + c.getArgsN(), false);
                    cname.setLength(0);
                }
            }
            em.description("Подсказка: команды имеют алиасы, например, команду help можно вызвать написав "+prefix+"help или "+prefix+"хелп");
            SendUtils.sendEmbedReply(em.build(), e.getMessage());
        }).setAliases(Seq.with("хелп"));
        Fun.load();
    }
}
