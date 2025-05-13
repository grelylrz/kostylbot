package icu.grely.bot.commands;

import arc.struct.Seq;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import static icu.grely.Vars.*;
import static icu.grely.bot.SendUtils.sendEmbedReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;

public class Fun {
    public static void load() {
        registerCommand("ball", "Покатать шары", "<question...>", (e, args)->{
            Color color = Color.GRAY;
            String reply = "";
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg + " ");
            }

            int rand = random.nextInt(100);

            if (rand < 45) {
                reply = yesDialogs.get(random.nextInt(yesDialogs.size));
                color = Color.GREEN;
            } else if (rand < 90) {
                reply = noDialogs.get(random.nextInt(noDialogs.size));
                color = Color.RED;
            } else {
                reply = idkDialogs.get(random.nextInt(idkDialogs.size));
                color = Color.BLUE;
            }

            sb.setLength(255);
            sendEmbedReply(EmbedCreateSpec.builder().title(sb.toString()).addField("", reply, false).color(color).build(), e.getMessage());
            sb.setLength(0);
        }).setAliases(Seq.with("balls", "8ball"));
    }
}
