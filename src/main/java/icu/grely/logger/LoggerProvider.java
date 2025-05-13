package icu.grely.logger;

import java.util.function.Function;
import reactor.util.Logger;

public class LoggerProvider implements Function<String, Logger> {
    @Override
    public Logger apply(String string) {
        return new CustomLogger();
    }
}
