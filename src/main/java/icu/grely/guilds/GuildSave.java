package icu.grely.guilds;

import arc.struct.Seq;
import arc.util.Log;
import discord4j.common.util.Snowflake;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;

import icu.grely.guilds.GuildSetting;

import static icu.grely.Vars.cachedGuilds;

@Getter
@Setter
public class GuildSave {
    String id;
    Seq<GuildSetting> settings = new Seq<>();

    GuildSave(String id) {
        this.id=id;
    }

    public static GuildSave rsToGuildSave(ResultSet rs) throws SQLException {
        return new GuildSave(
                rs.getString("id")
        );
    }
    public void rsToGuildSettings(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        if(type.equals("boolean")) {
            this.settings.add(new GuildSetting(rs.getString("key"), Boolean.parseBoolean(rs.getString("value"))));
        } else if(type.equals("int")){
            this.settings.add(new GuildSetting(rs.getString("key"), Integer.parseInt(rs.getString("value"))));
        } else if(type.equals("long")) {
            this.settings.add(new GuildSetting(rs.getString("key"), Long.parseLong(rs.getString("value"))));
        } else if(type.equals("String")){
            this.settings.add(new GuildSetting(rs.getString("key"), rs.getString("value")));
        } else if(type.equals("Snowflake")) {
            this.settings.add(new GuildSetting(rs.getString("key"), Snowflake.of(rs.getString("value"))));
        }
    }
    public static void saveGuilds() {
        Log.info("Time to save cached guilds!");
        for(GuildSave g : cachedGuilds) {
        }
    }
}