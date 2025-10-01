/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform;

import org.vicky.VickyUtilitiesForge;
import org.vicky.platform.PlatformLogger;

public class ForgeLogger implements PlatformLogger {
    @Override
    public void info(String message) {
        VickyUtilitiesForge.LOGGER.info(message);
    }

    @Override
    public void warn(String msg) {
        VickyUtilitiesForge.LOGGER.warn(msg);
    }

    @Override
    public void error(String msg) {
        VickyUtilitiesForge.LOGGER.error(msg);
    }

    @Override
    public void debug(String msg) {
        VickyUtilitiesForge.LOGGER.debug(msg);
    }

    @Override
    public void error(String msg, Throwable throwable) {
        VickyUtilitiesForge.LOGGER.error(msg, throwable);
    }
}
