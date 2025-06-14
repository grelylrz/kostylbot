package icu.grely;

import arc.Core;
import arc.struct.Seq;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import icu.grely.annotatins.SaveSetting;
import icu.grely.guilds.GuildSave;
import icu.grely.ranks.UserSave;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.OkHttpClient;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Vars {
    // main
    public static boolean debug, d4jdebug;
    // spec.
    public static final Dotenv dotenv = Dotenv.load();
    public static final Random random = new Random();
    public static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static final OkHttpClient Okclient = new OkHttpClient();
    // db
    public static String DB_USER = dotenv.get("DB_USER");
    public static String DB_PASSWORD = dotenv.get("DB_PASSWORD");
    public static final String JDBC_URL = "jdbc:postgresql://localhost:5432/kostylbot";
    //bot
    public static final String token = dotenv.get("token");
    public static final String prefix = dotenv.get("prefix");
    public static final String prefixAlias = dotenv.get("prefix_alias");
    public static DiscordClient client;
    public static GatewayDiscordClient gateway;
    @SaveSetting
    public static long handledCommands=0;
    @SaveSetting
    public static long handledMessages=0;
    public static long startedOn=System.currentTimeMillis();
    @SaveSetting
    public static String presence="Hello, im kostylbot.";
    public static User owner=null;
    //ranks
    public static double expScale=20.1;
    public static long expPerMessage=11;
    public static Seq<UserSave> cachedUsers = new Seq<>();
    //guilds
    public static Seq<GuildSave> cachedGuilds = new Seq<>();
    //commands
    public static Seq<String> yesDialogs = Seq.with("✅ Уверен в этом!", "\uD83D\uDC4D Отличная идея!", "\uD83D\uDC4C Звучит хорошо.");
    public static Seq<String> noDialogs = Seq.with("❌ Плохая идея", "\uD83D\uDE35 Даже не думай об этом!", "\uD83D\uDC4E Не думаю, что это кончится хорошо.");
    public static Seq<String> idkDialogs = Seq.with("❓ Я не знаю!", "☁ Я не уверен.", "\uD83D\uDD2E Спроси позже.", "Я Сейчас не в духе, извини.", "Не могу сказать.");
}
