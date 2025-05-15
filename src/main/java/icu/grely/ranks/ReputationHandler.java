package icu.grely.ranks;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import static icu.grely.Vars.gateway;
import static icu.grely.bot.SendUtils.sendReply;

public class ReputationHandler {
    public static void handle(MessageCreateEvent event) {
        Message message = event.getMessage();
        message.getMessageReference().ifPresent(msgr->{
            msgr.getMessageId().ifPresent(msgid->{
                gateway.getMessageById(message.getChannelId(), msgid).flatMap(msgrep->{
                    if(message.getContent().equals("-") || message.getContent().equalsIgnoreCase("-rep")) {
                        UserSave us = UserSave.getUser(msgrep.getAuthor().get().getId().asString());
                        us.setSocialCredit(us.getSocialCredit()-0.1f);
                        sendReply(message, "Уважение отобрано.");
                    } else if (message.getContent().equals("+") || message.getContent().equalsIgnoreCase("+rep")){
                        UserSave us = UserSave.getUser(msgrep.getAuthor().get().getId().asString());
                        us.setSocialCredit(us.getSocialCredit()+0.1f);
                        sendReply(message, "Уважение оказано.");
                    }
                    return Mono.empty();
                }).subscribe();
            });
        });
    }
}
