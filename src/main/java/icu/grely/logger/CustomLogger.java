package icu.grely.logger;

import arc.util.Log;
import reactor.util.Logger;

import static icu.grely.Vars.*;

public class CustomLogger implements Logger {
    @Override
    public String getName() {
        return "Main Loger";
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String s) {
    }

    @Override
    public void trace(String s, Object... objects) {
    }

    @Override
    public void trace(String s, Throwable throwable) {
    }

    @Override
    public boolean isDebugEnabled() {
        return d4jdebug;
    }

    @Override
    public void debug(String s) {
        if(d4jdebug)
            BLogger.debug(s);
    }

    @Override
    public void debug(String s, Object... objects) {
        if(d4jdebug)
            BLogger.debug(s.replace("{}", "@"), objects);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        if(d4jdebug)
            BLogger.debug(s.replace("{}", "@")+"\n"+throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String s) {
        Log.info(s);
    }

    @Override
    public void info(String s, Object... objects) {
        Log.info(s.replace("{}", "@"), objects);
    }

    @Override
    public void info(String s, Throwable throwable) {
        Log.info(s.replace("{}", "@"), throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String s) {
        Log.warn(s);
    }

    @Override
    public void warn(String s, Object... objects) {
        Log.warn(s.replace("{}", "@"), objects);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        Log.warn(s.replace("{}", "@"), throwable.toString());
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String s) {
        Log.err(s);
    }

    @Override
    public void error(String s, Object... objects) {
        Log.err(s.replace("{}", "@"), objects);
    }

    @Override
    public void error(String s, Throwable throwable) {
        Log.err(s.replace("{}", "@"), throwable);
    }
}