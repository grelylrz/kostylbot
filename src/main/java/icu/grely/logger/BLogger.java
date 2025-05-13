package icu.grely.logger;

import arc.util.Log;

import static icu.grely.Vars.*;

public class BLogger {
    static boolean loadedLogger;
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
            System.out.println(result);
        };

        loadedLogger = true;
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
