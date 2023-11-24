package io.github.wy.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rycat
 * @since 2023/11/22
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /**
     * The table's name
     *
     * @return the table's name
     */
    String name();
    
    /**
     * The table's alias name
     *
     * @return the table's alias name
     */
    String alias() default "";
    
    String database() default "MYSQL";
}
