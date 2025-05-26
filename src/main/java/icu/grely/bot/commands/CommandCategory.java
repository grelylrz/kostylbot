package icu.grely.bot.commands;

public enum CommandCategory {
    fun("fun"),
    brainrot("brainrot"),
    mod("moderation"),
    ranking("ranking"),
    spec("spec"), // aka utils
    unkown("unkown"),
    disable("disable"),
    testing("testing", false),
    NSFW("nsfw");
    String name;
    boolean visilbe;
    CommandCategory(String name) {
        this.name=name;
        this.visilbe=true;
    }
    CommandCategory(String name, boolean visible) {
        this.name=name;
        this.visilbe=visible;
    }

    public static CommandCategory parseCategory(String name) {
        for(CommandCategory ct : values()) {
            if(ct.name.equals(name)) return ct;
        }
        return unkown;
    }
}
