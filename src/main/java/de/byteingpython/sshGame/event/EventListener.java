package de.byteingpython.sshGame.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks a method for the EventHandler
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {
    /**
     * Marks when an EventListener should be called. The higher the priority, the earlier it will be executed
     * @return The priority of this EventListener
     */
    ListenerPriority priority() default ListenerPriority.NORMAL;
}
