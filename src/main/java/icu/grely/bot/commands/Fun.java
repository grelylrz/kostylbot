package icu.grely.bot.commands;

import arc.struct.Seq;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import static icu.grely.Vars.*;
import static icu.grely.bot.SendUtils.sendEmbedReply;
import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;

public class Fun {
    public static void load() {
        registerCommand("ball", "Покатать шары", "<question...>", (e, args)->{
            if(args.length==0) {
                sendReply(e.getMessage(), "Invalid args.");
                return;
            }
            Color color;
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
            sendEmbedReply(EmbedCreateSpec.builder().addField(sb.toString(), reply, true).color(color).build(), e.getMessage());
            sb.setLength(0);
        }).setAliases(Seq.with("balls", "8ball", "шары", "шар"));
        registerCommand("coinflip", "Подбросить монетку.", "<question...>", (event, args)->{
            if(random.nextInt(2)==0) {
                sendEmbedReply(EmbedCreateSpec.builder().color(Color.LIGHT_SEA_GREEN).addField(String.join(" ", args), "Выпала решка!", false).build(), event.getMessage());
            } else {
                sendEmbedReply(EmbedCreateSpec.builder().color(Color.LIGHT_SEA_GREEN).addField(String.join(" ", args), "Выпал орел!", false).build(), event.getMessage());
            }
        }).setAliases(Seq.with("монетка"));

        registerCommand("dice", "Кубик", "[dX] [adv|dis]", (e, args) -> {
            int max = 6;
            boolean advantage = false;
            boolean disadvantage = false;

            for (String arg : args) {
                if (arg.equalsIgnoreCase("adv")) advantage = true;
                else if (arg.equalsIgnoreCase("dis")) disadvantage = true;
                else if (arg.matches("d\\d+")) {
                    max = Integer.parseInt(arg.substring(1));
                }
            }

            if (max <= 1) {
                sendReply(e.getMessage(), "Максимум должен быть больше 1.");
                return;
            }

            int roll1 = 1 + random.nextInt(max);
            int roll2 = 1 + random.nextInt(max);
            int result;

            if (advantage) {
                result = Math.max(roll1, roll2);
                sendReply(e.getMessage(), "С преимуществом: " + roll1 + " и " + roll2 + " → " + result);
            } else if (disadvantage) {
                result = Math.min(roll1, roll2);
                sendReply(e.getMessage(), "С помехой: " + roll1 + " и " + roll2 + " → " + result);
            } else {
                result = roll1;
                sendReply(e.getMessage(), "Выпадает " + result + " (1–" + max + ")");
            }
        }).setAliases(Seq.with("кубик"));

        registerCommand("stupid", "Оценка глупости", "[target]", (e, args) -> {
            int iq = random.nextInt(50) + 50;
            String target = args.length > 0 ? String.join(" ", args) : e.getMessage().getAuthor().map(u -> u.getUsername()).orElse("Ты");
            sendReply(e.getMessage(), target + " имеет IQ: " + iq);
        }).setDisailable(true);
        registerCommand("sus", "Подозрительность", "[target]", (e, args) -> {
            int percent = random.nextInt(101);
            String target = args.length > 0 ? String.join(" ", args) : e.getMessage().getAuthor().map(u -> u.getUsername()).orElse("Ты");
            sendReply(e.getMessage(), target + " подозрителен на " + percent + "% 🔺");
        }).setDisailable(true);
        registerCommand("egg", "Яйцо", "", (e, args) -> {
            String[] eggs = {"🥚", "🥚🥚", "🍳", "🐣", "🐔", "🥚🍳🐣"};
            sendReply(e.getMessage(), eggs[random.nextInt(eggs.length)]);
        }).setDisailable(true);
        registerCommand("howgay", "Оценка геевости", "[target]", (e, args) -> {
            int percent = random.nextInt(101);
            String target = args.length > 0 ? String.join(" ", args) : e.getMessage().getAuthor().map(u -> u.getUsername()).orElse("Ты");
            sendReply(e.getMessage(), target + " гей на " + percent + "% 🌈");
        }).setDisailable(true);
    }
}
