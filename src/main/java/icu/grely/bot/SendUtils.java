package icu.grely.bot;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.MessageReferenceData;
import reactor.core.publisher.Mono;

import static icu.grely.Vars.*;
import arc.util.Log;

public class SendUtils {
    /*Разновидности отправки сообщений*/
    public static void sendMessage(Snowflake cha, String content) {
        if(content.isEmpty())
            return;
        gateway.getChannelById(cha)
                .flatMap(ch -> {
                    ch.getRestChannel().createMessage(content.replace("@", "")).doOnError(Log::err).subscribe();
                    return Mono.empty();
                }).doOnError(Log::err).subscribe();
    }
    public static void sendMessage(String cha, String content) {
        sendMessage(Snowflake.of(cha), content);
    }
    public static void sendMessage(long cha, String content) {
        sendMessage(Snowflake.of(cha), content);
    }
    public static void sendMessageP(Snowflake cha, String content) {
        if(content.isEmpty())
            return;
        gateway.getChannelById(cha)
                .flatMap(ch ->{
                    ch.getRestChannel().createMessage(content).doOnError(Log::err).subscribe();
                    return Mono.empty();
                })
                .doOnError(Log::err)
                .subscribe();
    }
    public static void sendMessageP(String cha, String content) {
        sendMessageP(Snowflake.of(cha), content);
    }
    public static void sendMessageP(long cha, String content) {
        sendMessageP(Snowflake.of(cha), content);
    }
    /*Отправка с MessageCreateSpec*/
    public static void sendMessage(Snowflake cha, MessageCreateSpec m) {
        gateway.getChannelById(cha)
                .ofType(MessageChannel.class)
                .flatMap(ch->{
                    ch.createMessage(m)
                            .doOnError(Log::err).
                            subscribe();
                    return Mono.empty();
                }).doOnError(Log::err).subscribe();
    }
    public static void sendMessage(String cha, MessageCreateSpec m){
        sendMessage(Snowflake.of(cha), m);
    }
    public static void sendMessage(Long cha, MessageCreateSpec m){
        sendMessage(Snowflake.of(cha), m);
    }
    /*Отправка эмбеда*/
    public static void sendEmbed(Snowflake cha, EmbedCreateSpec e) {
        gateway.getChannelById(cha)
                .ofType(MessageChannel.class)
                .flatMap(ch->{
                    ch.createMessage(MessageCreateSpec.builder().addEmbed(e).build()).doOnError(Log::err).subscribe();
                    return Mono.empty();
                }).doOnError(Log::err).subscribe();
    }
    public static void sendEmbed(String cha, EmbedCreateSpec e){
        sendEmbed(Snowflake.of(cha), e);
    }
    public static void sendEmbed(Long cha, EmbedCreateSpec e){
        sendEmbed(Snowflake.of(cha), e);
    }
    /*Ответить на сообщение*/
    public static void sendReply(Message msg, String content) {
        if(content.isEmpty())
            return;
        sendMessage(msg.getChannelId(), MessageCreateSpec.builder().messageReference(MessageReferenceData.builder().channelId(msg.getChannelId().asLong()).messageId(msg.getId().asLong()).build()).content(content).build());
    }
    /*Ответ с эмбедом*/
    public static void sendEmbedReply(EmbedCreateSpec e, Message msg) {
        msg.getChannel()
                .flatMap(ch -> {
                    ch.createMessage(MessageCreateSpec.builder().addEmbed(e).messageReference(MessageReferenceData.builder().channelId(msg.getChannelId().asLong()).messageId(msg.getId().asLong()).build()).build()).doOnError(Log::err).subscribe();
                    return Mono.empty();
                }).doOnError(Log::err).subscribe();
    }
    public static String getIdByPing(String ping) {
        return ping.replace("@", "").replace("<", "").replace(">", "").trim();
    }
}
