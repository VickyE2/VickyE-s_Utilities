/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.Denoations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.ApiStatus;

/**
 * Marks a parameter or method as unstable but recommended for use.
 * Indicates that the API may change in future versions.
 * <p>  </p>
 * ⚠️ Indicates that the annotated element is unstable and may change in future releases,
 * but is currently the preferred or recommended way to achieve a goal.
 * @apiNote This is useful for features that are in active iteration.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@ApiStatus.Experimental
public @interface UnstableRecommended {
  String since() default "";

  String reason() default "This API may change, but it's the preferred way currently.";
}
