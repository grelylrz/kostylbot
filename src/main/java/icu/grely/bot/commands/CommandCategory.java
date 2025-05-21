package icu.grely.bot.commands;

public enum CommandCategory {
    fun("fun"),
    brainrot("brainrot"),
    mod("moderation"),
    ranking("ranking"),
    spec("spec"), // aka utils
    unkown("unkown"),
    disable("disable"),
    testing("testing"),
    NSFW("nsfw");
    String name;
    CommandCategory(String name) {
        this.name=name;
    }

    public static CommandCategory parseCategory(String name) {
        for(CommandCategory ct : values()) {
            if(ct.name.equals(name)) return ct;
        }
        return unkown;
    }
}
