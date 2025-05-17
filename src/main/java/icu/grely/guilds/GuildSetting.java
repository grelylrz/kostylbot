package icu.grely.guilds;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuildSetting {
    String key;
    Object value;
    public GuildSetting(String key, Object value) {
        this.key=key;
        this.value=value;
    }
}
