package org.vicky.platform.defaults;

import org.vicky.platform.PlatformLogger;

public class DefaultPlatformLogger implements PlatformLogger {
    private final String prefix;

    public DefaultPlatformLogger(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void info(String msg) {
        System.out.println("[INFO][" + prefix + "] " + msg);
    }

    @Override
    public void warn(String msg) {
        System.out.println("[WARN][" + prefix + "] " + msg);
    }

    @Override
    public void error(String msg) {
        System.err.println("[ERROR][" + prefix + "] " + msg);
    }

    @Override
    public void error(String msg, Throwable throwable) {
        System.err.println("[ERROR][" + prefix + "] " + msg);
        throwable.printStackTrace(System.err);
    }

    @Override
    public void debug(String msg) {
        System.out.println("[DEBUG][" + prefix + "] " + msg);
    }
}

