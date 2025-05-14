package icu.grely.ranks;

import arc.util.Log;
import discord4j.common.util.Snowflake;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import static icu.grely.Vars.cachedUsers;
import static icu.grely.Vars.expScale;
import static icu.grely.database.DatabaseConnector.*;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;

@Getter
@Setter
public class UserSave {
    String id;
    long exp;

    /**
     * @return лвл по формуле √exp/expScale=lvl
     * как пример, 10 лвл √1250/12.5=10
     * */
     public int getLevel() {
        return (int)Math.sqrt(exp / expScale);
    }

    public UserSave(String id, long exp){
        this.id=id;
        this.exp=exp;
    }
    public static UserSave ResultSetToUserSave(ResultSet rs) throws SQLException {
         return new UserSave(
                 rs.getString("id"),
                 rs.getLong("exp")
         );
    }
    public synchronized static void saveUsers() {
         Log.info("Saving cached users to db!");
         for(UserSave u : cachedUsers) {
             createOrUpdateUser(u.getId(), u.getExp());
         }
         cachedUsers.clear();
         Log.info("Cached users saved!");
    }
    public synchronized static UserSave getUser(String id) {
        UserSave us = cachedUsers.find(u->u.getId().equals(id));
        Optional<UserSave> usopt;
        if(us==null) {
            usopt = createOrGetUser(id);
            if (!usopt.isPresent())
                us = new UserSave(id, 0);
            else
                us=usopt.get();
        }
        cachedUsers.addUnique(us);
        return us;
    }
}
