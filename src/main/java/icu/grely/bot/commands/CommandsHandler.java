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
import discord4j.rest.util.Permission;
import icu.grely.Vars;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import reactor.core.publisher.Mono;

import javax.print.DocFlavor;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import static icu.grely.Vars.*;
import static icu.grely.bot.SendUtils.sendMessage;
import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandCategory.unkown;
import static icu.grely.guilds.GuildSave.getGuild;

public class CommandsHandler {
    public static Seq<BotCommand> commands = new Seq<>();
    private static CommandCategory category = unkown;
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
    /**обычная+аргсы*/
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
    public static void setCategory(String c) {
        CommandCategory ct = CommandCategory.parseCategory(c);
        if(ct.name.equals(unkown.name))
            Log.warn("@ is unkown category!", c);
        category=ct;
    }
    public static void setCategory(CommandCategory ct) {
        category=ct;
    }
    /**Обработать эвент получения сообщения.*/
    public static void handleEvent(MessageCreateEvent event) {
        Message message = event.getMessage();
        Optional<User> authorOpt = message.getAuthor();
        if (authorOpt.isPresent() && authorOpt.get().isBot()) {
            return;
        }
        if(!event.getGuildId().isPresent())
            return;
        User author = authorOpt.get();
        String content = message.getContent();
        String firstChars = content.substring(0, 2);
        if(firstChars.toLowerCase().startsWith(prefix) || firstChars.toLowerCase().startsWith(prefixAlias)) {
            /*if(content.contains("@everyone") || content.contains("@here") || (content.contains("<@&") && content.contains(">"))) {
                return;
            }*/
            String[] args = content.replace(firstChars, "").split(" ");
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
                if(command.isDisailable()) {
                    var gs = getGuild(message.getGuildId().get().asString()); // получаю сохранение гильды
                    String some = command.getName() + "-DISAIBLE"; // формирую запрос
                    Boolean disaibled = gs.getSetting(some, Boolean.class); // получаю статус выключения команды.
                    // если выключение равно нуллу то ставлю на фалс и пишу в бд.
                    if (disaibled == null) {
                        disaibled = false;
                        gs.updateSetting(some, disaibled);
                    }
                    if (disaibled) {
                        sendReply(message, "Эта команда здесь отключена!");
                        return;
                    }
                }
                if(command.getMemberID() == 0) {
                    BotCommand finalCommand = command;
                    if(command.getPermissions().isEmpty()) {
                        executor.submit(() -> finalCommand.exec(event, Arrays.copyOfRange(args, 1, args.length)));
                    } else {
                        BotCommand finalCommand1 = command;
                        AtomicBoolean executed = new AtomicBoolean(false);

                        message.getAuthorAsMember().subscribe(m -> {
                            m.getRoles().subscribe(role -> {
                                role.getPermissions().forEach(perm -> {
                                    if (!executed.get() && finalCommand1.getPermissions().find(p -> p.name().equals(perm.name())) != null) {
                                        executed.set(true);
                                        executor.submit(() -> finalCommand.exec(event, Arrays.copyOfRange(args, 1, args.length)));
                                    }
                                });
                            });
                        });
                    }
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
    @Accessors(chain = true)
    public static class BotCommand {
        String name;
        String description;
        BiConsumer<MessageCreateEvent, String[]> executor;
        long memberID;
        boolean visible = true, active = true, disailable = false, disable = false/*Команда ЧЕРЕЗ которую отключаются/включаются другие*/;
        String argsN = "";
        CommandCategory category = unkown;
        Seq<String> aliases = new Seq<>();
        Seq<Permission> permissions = new Seq<>();

        BotCommand(String name, String description, BiConsumer<MessageCreateEvent, String[]> executor) {
            this.name = name;
            this.description = description;
            this.executor = executor;
            this.category=CommandsHandler.category;
        }

        public void exec(MessageCreateEvent e, String[] args) {
            executor.accept(e, args);
        }

        void setAliases(String... aliases) {
            this.aliases=Seq.with(aliases);
        }
        void setAliases(Seq<String> aliases) {
            this.aliases=aliases;
        }

        void setPermissions(Permission... p) {
            this.permissions=Seq.with(p);
        }
        void setPermissions(Seq<Permission> p) {
            this.permissions=p;
        }
    }
}
