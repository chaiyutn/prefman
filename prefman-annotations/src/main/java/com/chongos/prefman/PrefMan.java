package com.chongos.prefman;

import android.content.Context;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ChongOS
 * @since 06-Jul-2017
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface PrefMan {

  /**
   * The name of the preferences. If empty the interface name is used.
   *
   * @return the name of the preferences
   */
  String name() default "";

  /**
   * The operating mode.
   *
   * @see Context#MODE_PRIVATE
   * @see Context#MODE_WORLD_READABLE
   * @see Context#MODE_WORLD_WRITEABLE
   *
   * @return the operating mode
   */
  int mode() default Context.MODE_PRIVATE;
}