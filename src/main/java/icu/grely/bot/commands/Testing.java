package icu.grely.bot.commands;

import discord4j.discordjson.possible.Possible;

import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;

public class Testing {
    public static void load() {
        setCategory("testing");
        registerCommand("lang", "", (e, args)->{
            Possible<String> l = e.getMessage().getAuthor().get().getUserData().locale();
            if(l.toOptional().isPresent())
                sendReply(e.getMessage(), l.get());
        });
    }
}
