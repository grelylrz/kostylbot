package icu.grely.logger;

import arc.struct.Seq;
import arc.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static icu.grely.Vars.*;

public class BLogger {
    static boolean loadedLogger;
    public static Seq<String> LogBuffer = new Seq<>();
    public static void loadLogger() {
        if (loadedLogger) return;
        // IDK how arc Log.debug works.
        String[] tags = {
                "\u001B[32m[D]\u001B[0m",
                "\u001B[34m[I]\u001B[0m",
                "\u001B[33m[W]\u001B[0m",
                "\u001B[31m[E]\u001B[0m"
        };

        Log.logger = (level, text) -> {
            //System.out.println(level.toString());
            String result = tags[level.ordinal()] + " " + text;
            LogBuffer.add(text.replace("\n", "")+"\n");
            System.out.println(result);
        };

        loadedLogger = true;
    }
    public static void write(String path, String content) {
        File file = new File(path);
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(content);
        } catch (IOException e) {
            Log.err(e);
        }
    }
    /**Дебаг сообщение видное только при включенном дебаге.*/
    public static void debug(Object o) {
        if(debug)
            System.out.println("\033[32m[D]\033[0m " + o);
    }
    /**Дебаг сообщение видное только при включенном дебаге.*/
    public static void debug(String s, Object... o) {
        if(debug)
            System.out.println("\033[32m[D]\033[0m " + s +" "+o);
    }
    /**Дебаг сообщение видное только при включенном дебаге.*/
    public static void debug(Object... o) {
        if(debug)
            System.out.println("\033[32m[D]\033[0m " + o);
    }
}
