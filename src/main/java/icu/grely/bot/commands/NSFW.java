package icu.grely.bot.commands;

import icu.grely.nsfw.R34;

import java.util.List;

import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;

public class NSFW {
    public static void load() {
        registerCommand("r34", "Ох зря я сюда полез...", "<tags>", (e, args)->{
            try {
                List<String> l = R34.Companion.fetchRule34Links(String.join(" ", args), 10);
                sendReply(e.getMessage(), "Результатов: "+l.size());
            } catch (Exception ex) {
                sendReply(e.getMessage(), "Что то не так!");
            }
        });
    }
}
