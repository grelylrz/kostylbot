package icu.grely.bot.commands;

import arc.util.Log;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import icu.grely.nsfw.R34;

import java.util.List;

import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;

public class NSFW {
    public static void load() {
        registerCommand("r34", "Ох зря я сюда полез...", "<tags>", (e, args)->{
            try {
                List<String> pr = R34.fetchGelbooru(String.join(" ", args), 10);
                e.getMessage().getChannel().ofType(TextChannel.class).subscribe(ch->{
                    if(ch.isNsfw()) {
                        StringBuilder sb = new StringBuilder();
                        for(String n : pr)
                            sb.append(n+"\n");
                        sendReply(e.getMessage(), sb.toString());
                    } else {
                        sendReply(e.getMessage(), "Результатов: "+pr.size()+"\nНе NSFW канал!");
                    }
                });
            } catch (Exception ex) {
                sendReply(e.getMessage(), "Что то не так!");
                Log.err(ex);
            }
        }).setActive(true);
    }
}
