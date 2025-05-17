package icu.grely.guilds;

import arc.struct.Seq;
import arc.util.Log;
import discord4j.common.util.Snowflake;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import icu.grely.guilds.GuildSetting;

import static icu.grely.Vars.cachedGuilds;
import static icu.grely.database.DatabaseConnector.createGuildSettingOrUpdate;
import static icu.grely.database.DatabaseConnector.createOrGetGuild;

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
        switch (type) {
            case "boolean" -> settings.add(new GuildSetting(rs.getString("key"), Boolean.parseBoolean(rs.getString("value"))));
            case "int"     -> settings.add(new GuildSetting(rs.getString("key"), Integer.parseInt(rs.getString("value"))));
            case "long"    -> settings.add(new GuildSetting(rs.getString("key"), Long.parseLong(rs.getString("value"))));
            case "String"  -> settings.add(new GuildSetting(rs.getString("key"), rs.getString("value")));
            case "Snowflake" -> settings.add(new GuildSetting(rs.getString("key"), Snowflake.of(rs.getString("value"))));
        }
    }
    public static void saveGuilds() {
        for(GuildSave g : cachedGuilds) {
            for(GuildSetting gs : g.getSettings()) {
                createGuildSettingOrUpdate(g.getId(), gs.getKey(), gs.getValue().toString(), gs.getValue().getClass().getSimpleName());
            }
        }
        cachedGuilds.clear();
    }
    public static GuildSave getGuild(String id) {
        GuildSave gs = cachedGuilds.find(g -> g.getId().equals(id));
        if (gs == null) {
            Optional<GuildSave> gsopt = createOrGetGuild(id);
            if (gsopt.isPresent()) {
                gs = gsopt.get();
            } else {
                gs = new GuildSave(id);
            }
            cachedGuilds.add(gs);
        }
        return gs;
    }

    public void updateSetting(String key, Object value) {
        GuildSetting gs = this.getSettings().find(s->s.getKey().equals(key));
        if(gs==null) {
            gs = new GuildSetting(key, value);
            gs.setKey(key);
            gs.setValue(value);
            this.getSettings().add(gs);
        } else {
            gs.setKey(key);
            gs.setValue(value);
        }
    }
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String key, Class<T> type) {
        GuildSetting setting = this.settings.find(s -> s.getKey().equals(key));
        if (setting == null) return null;

        Object value = setting.getValue();

        if (type.isInstance(value)) {
            return (T) value;
        }
        String stringValue = value.toString();
        try {
            return switch (type.getSimpleName()) {
                case "Boolean" -> (T) Boolean.valueOf(stringValue);
                case "Integer" -> (T) Integer.valueOf(stringValue);
                case "Long"    -> (T) Long.valueOf(stringValue);
                case "String"  -> (T) stringValue;
                case "Snowflake" -> (T) Snowflake.of(stringValue);
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }
}