/* Licensed under Apache-2.0 2024. */
package org.vicky.ecosystem.plugin.guide;

import java.lang.reflect.Method;
import org.vicky.ecosystem.server.Handler;

public record HandlerMeta(Method method, Handler annotation) {}
