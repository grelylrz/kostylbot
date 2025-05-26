package icu.grely.bans;

import arc.util.Log;
import discord4j.common.util.Snowflake;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import static icu.grely.Vars.gateway;
import static icu.grely.database.DatabaseConnector.unbanUser;

@Getter
public class Ban {
    String guild_id, admin_id, user_id, reason;
    boolean active;
    Instant unban_date;
    int id;

    Ban(String guild_id, String admin_id, String user_id, String reason, boolean active, Instant unban, int id) {
        this.guild_id=guild_id;
        this.admin_id=admin_id;
        this.user_id=user_id;
        this.reason=reason;
        this.id=id;
        this.active=active;
        this.unban_date=unban;
    }

    public boolean isExperied() {
        return Instant.now().isAfter(this.unban_date);
    }
    public static Ban resultSetToBan(ResultSet rs) throws SQLException {
        return new Ban(
                rs.getString("guild_id"),
                rs.getString("admin_id"),
                rs.getString("user_id"),
                rs.getString("reason"),
                rs.getBoolean("active"),
                rs.getTimestamp("unban_datetime").toInstant(),
                rs.getInt("ban_id")
        );
    }
    public static void handleUnBan(Ban ban) throws SQLException {
        gateway.getGuildById(Snowflake.of(ban.getGuild_id())).subscribe(g->{
            g.unban(Snowflake.of(ban.getUser_id()), "Ban expired.").subscribe();
            unbanUser(ban.getId());
            Log.info("Unbanned @ in @", ban.getUser_id(), g.getName());
        });
    }
}
