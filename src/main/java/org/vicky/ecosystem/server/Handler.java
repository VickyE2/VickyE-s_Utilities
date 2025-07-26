/* Licensed under Apache-2.0 2024. */
package org.vicky.ecosystem.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * Must return a {@link java.util.concurrent.CompletableFuture}
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Handler {
  String key();

  String description() default "[undefined]";

  String usage() default ""; // e.g., "openGui(player:UUID, plugin:String, items:List<ItemConfig>)"

  String[] examples() default {}; // Optional example calls
}
