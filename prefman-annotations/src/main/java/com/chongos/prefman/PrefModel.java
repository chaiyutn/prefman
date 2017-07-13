package com.chongos.prefman;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ChongOS
 * @since 07-Jul-2017
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface PrefModel {
}
