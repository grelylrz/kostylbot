package icu.grely.ranks;

import discord4j.common.util.Snowflake;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

import static icu.grely.Vars.expScale;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;

@Getter
@Setter
public class UserSave {
    Snowflake id;
    long exp;

    /**
     * @return лвл по формуле √exp/expScale=lvl
     * как пример, 10 лвл √1250/12.5=10
     * */
     public int getLevel() {
        return (int)Math.sqrt(exp / expScale);
    }

    UserSave(Snowflake id, long exp) {
        this.id=id;
        this.exp=exp;
    }
}
