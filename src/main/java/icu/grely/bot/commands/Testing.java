package icu.grely.bot.commands;

import discord4j.discordjson.possible.Possible;

import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;
import static icu.grely.bot.commands.CommandsHandler.setCategory;

public class Testing {
    public static void load() {
        setCategory("testing");
    }
}
