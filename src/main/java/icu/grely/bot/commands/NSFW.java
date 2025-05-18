package icu.grely.bot.commands;

import arc.util.Log;
import arc.util.Strings;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import icu.grely.nsfw.R34;

import java.util.Arrays;
import java.util.List;

import static icu.grely.Vars.prefix;
import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;

public class NSFW {
    public static void load() {
        registerCommand("r34", "Ох зря я сюда полез...", "<tags...> <page> <limit>", (e, args)->{
            if(args.length==0) {
                sendReply(e.getMessage(), "Неверные теги.\nПодсказка: вы можете указать только теги, а можете указать теги, страницу и лимит постов(максимальный лимит 20)\nПример: "+prefix+"r34 neko maid solo 2 20");
                return;
            }
            if(R34.containsBannedTags(String.join(" ", args))) {
                sendReply(e.getMessage(), "Сообщение имеет запрещенные теги.");
                return;
            }
            int limit = 1;
            int page=1;
            String[] tags = args.clone();
            if (args.length >= 3) {
                tags = Arrays.copyOfRange(args, 0, args.length - 2);
                String pageArg = args[args.length - 2];
                if (Strings.canParseInt(pageArg)) {
                    page = Integer.parseInt(pageArg);
                }
                String limitArg = args[args.length - 1];
                if (Strings.canParseInt(limitArg)) {
                    limit = Integer.parseInt(limitArg);
                    if (limit > 20) limit = 20;
                }
            }
            try {
                List<String> pr = R34.fetchGelbooru(String.join(" ", tags), limit, page);
                e.getMessage().getChannel().ofType(TextChannel.class).subscribe(ch->{
                    if(ch.isNsfw()) {
                        StringBuilder sb = new StringBuilder();
                        for(String n : pr)
                            sb.append(n+"\n");
                        sb.append("Всего: "+pr.size());
                        sendReply(e.getMessage(), sb.toString());
                    } else {
                        sendReply(e.getMessage(), "Результатов: "+pr.size()+"\nНе NSFW канал!");
                    }
                });
            } catch (Exception ex) {
                sendReply(e.getMessage(), "Что то не так!");
                Log.err(ex);
            }
        }).setActive(true).setDisailable(true);
    }
}
