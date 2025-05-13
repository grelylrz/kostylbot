package icu.grely.bot.commands;

import arc.struct.Seq;
import arc.util.Log;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.MessageReferenceData;
import icu.grely.Vars;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import static icu.grely.Vars.*;
import static icu.grely.bot.SendUtils.sendMessage;
import static icu.grely.bot.SendUtils.sendReply;

public class CommandsHandler {
    public static Seq<BotCommand> commands = new Seq<>();
    /**Зарегестрировать обычную команду.*/
    public static BotCommand registerCommand(String name, String description, BiConsumer<MessageCreateEvent, String[]> executor) {
        BotCommand c = new BotCommand(name, description, executor);
        commands.add(c);
        return c;
    }
    /**Зарегестрировать команду с требованием к доступу.*/
    public static BotCommand registerCommand(String name, String description, long member, BiConsumer<MessageCreateEvent, String[]> executor) {
        BotCommand c = new BotCommand(name, description, executor);
        c.setMemberID(member);
        commands.add(c);
        return c;
    }
    /*обычная+аргсы*/
    public static BotCommand registerCommand(String name, String description, String argsN, BiConsumer<MessageCreateEvent, String[]> executor) {
        BotCommand c = new BotCommand(name, description, executor);
        c.setArgsN(argsN);
        commands.add(c);
        return c;
    }
    /**доступ+аргсы*/
    public static BotCommand registerCommand(String name, String description, String argsN, long member, BiConsumer<MessageCreateEvent, String[]> executor) {
        BotCommand c = new BotCommand(name, description, executor);
        c.setMemberID(member);
        c.setArgsN(argsN);
        commands.add(c);
        return c;
    }
    /**Обработать эвент получения сообщения.*/
    public static void handleEvent(MessageCreateEvent event) {
        Message message = event.getMessage();
        Optional<User> authorOpt = message.getAuthor();
        if (authorOpt.isPresent() && authorOpt.get().isBot()) {
            return;
        }
        User author = authorOpt.get();
        String content = message.getContent();
        if(content.toLowerCase().startsWith(Vars.prefix)) {
            String[] args = content.replace(Vars.prefix, "").trim().split(" ");
            BotCommand command = commands.find(c->{
                return c.getName().equals(args[0]) && c.isActive();
            });
            if(command == null) {
                command = commands.find(c->{
                    return c.getAliases().find(a->a.equals(args[0])) != null;
                });
            }
            if(command != null) {
                handledCommands+=1;
                // Arrays.copyOfRange(args, 1, args.length)
                if (!command.getAliases().isEmpty()) {
                    String[] commandArgs = command.getArgsN().split(" ");
                    boolean allValid = true;

                    int requiredIndex = 0;
                    int userArgIndex = 0;

                    while (requiredIndex < commandArgs.length) {
                        String carg = commandArgs[requiredIndex];

                        boolean isRequired = carg.startsWith("<") && carg.endsWith(">");
                        boolean isOptional = carg.startsWith("[") && carg.endsWith("]");
                        boolean isMultiple = carg.endsWith("...>") || carg.endsWith("...]");

                        if (isRequired) {
                            if (isMultiple) {
                                // <arg...> → обязательно хотя бы один аргумент, можно больше
                                if (userArgIndex >= args.length) {
                                    allValid = false;
                                    break;
                                }
                                // всё остальное — часть мультиарга, можно завершить
                                break;
                            } else {
                                // <arg> → один обязательный арг
                                if (userArgIndex >= args.length) {
                                    allValid = false;
                                    break;
                                }
                                userArgIndex++;
                            }
                        } else if (isOptional) {
                            if (isMultiple) {
                                // [arg...] → необязательный мульти арг, можно игнорировать
                                break;
                            } else {
                                // [arg] → один необязательный арг, если есть — берём
                                if (userArgIndex < args.length) {
                                    userArgIndex++;
                                }
                                // если нет — пропускаем
                            }
                        }

                        requiredIndex++;
                    }

                    // Если пользователь ввел больше аргументов, чем возможно
                    if (userArgIndex < args.length) {
                        // если последний был мультиарг — норм
                        String lastArg = commandArgs[commandArgs.length - 1];
                        boolean lastIsMultiple = lastArg.endsWith("...>") || lastArg.endsWith("...]");
                        if (!lastIsMultiple) {
                            allValid = false;
                        }
                    }

                    if (!allValid) {
                        sendReply(event.getMessage(), "Invalid args!");
                        return;
                    }
                }

                if(command.getMemberID() == 0) {
                    command.exec(event, Arrays.copyOfRange(args, 1, args.length));
                } else {
                    try {
                        /*author.asMember(Vars.guild).flatMap(m -> {
                            if (m.getRoleIds().contains(Snowflake.of(command.getMemberID())) || command.getMemberID() == 0) {
                                command.exec(event, Arrays.copyOfRange(args, 1, args.length));
                            } else {
                                if(command.isVisible()) {
                                    sendMessage(message.getChannelId(), MessageCreateSpec.builder().messageReference(MessageReferenceData.builder().channelId(message.getChannelId().asLong()).messageId(message.getId().asLong()).build()).content("No access").build());
                                    message.addReaction(Emoji.unicode("❌")).subscribe();
                                }
                            }
                            return Mono.empty();
                        }).subscribe();*.
                         */
                        if(author.getId().equals(Snowflake.of(command.getMemberID()))) {
                            command.exec(event, Arrays.copyOfRange(args, 1, args.length));
                        }
                    } catch (Exception e) {
                        Log.err(e);
                    }
                }
            }
        }
    }
    @Getter
    @Setter
    @ToString
    public static class BotCommand {
        public String name;
        public String description;
        public BiConsumer<MessageCreateEvent, String[]> executor;
        public long memberID;
        public boolean visible = true, active = true;
        public String argsN = "";
        Seq<String> aliases = new Seq<>();

        BotCommand(String name, String description, BiConsumer<MessageCreateEvent, String[]> executor) {
            this.name = name;
            this.description = description;
            this.executor = executor;
        }

        public void exec(MessageCreateEvent e, String[] args) {
            executor.accept(e, args);
        }
    }
}
