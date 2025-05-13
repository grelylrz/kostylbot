package icu.grely.bot.commands;

import static icu.grely.bot.commands.CommandsHandler.commands;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;

import discord4j.core.object.Embed;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import icu.grely.bot.SendUtils;

public class Spec {
    public static void load() {
        registerCommand("help", "Посмотреть список команд.", "[command-name]", (e, args)->{
            EmbedCreateSpec.Builder em=EmbedCreateSpec.builder().title("Список команд.").color(Color.SEA_GREEN);
            for(CommandsHandler.BotCommand c : commands) {
                if(c.isVisible() && c.isActive())
                    em.addField(c.name, c.getDescription()+"\n"+c.getArgsN(), false);
            }
            SendUtils.sendEmbedReply(em.build(), e.getMessage());
        });
    }
}
