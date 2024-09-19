package de.byteingpython.sshGame.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHandler {
    Map<Class<?>, Map<ListenerPriority, List<Method>>> events =new HashMap<>();
    Map<Method, List<Object>> objects = new HashMap<>();

    /**
     * Registers all public methods marked with @EventListener as an event listener for the Event type that is the only argument of that method
     * @param listener The Object whose methods should be registered
     */
    public void registerListeners(Object listener){
        for(Method method:listener.getClass().getMethods()){
            registerListener(listener, method);
        }
    }

    /**
     * Registers this Method as an event listener for the Event type that should be its only argument
     * @param object The Object that the listener method should be invoked on
     * @param method The Method that should be registered as a listener
     */
    public void registerListener(Object object, Method method){
        if(!method.isAnnotationPresent(EventListener.class)) return;
        if(method.getParameterTypes().length != 1) return;
        if(!Event.class.isAssignableFrom(method.getParameterTypes()[0])) return;
        if(!events.containsKey(method.getParameterTypes()[0])) {
            events.put(method.getParameterTypes()[0], new HashMap<>());
        }
        Map<ListenerPriority, List<Method>> eventListeners = events.get(method.getParameterTypes()[0]);
        ListenerPriority priority = method.getAnnotation(EventListener.class).priority();
        if(!eventListeners.containsKey(method.getParameterTypes()[0])) {
            eventListeners.put(priority, new ArrayList<>());
        }
        if(!eventListeners.get(priority).contains(method)) {
            eventListeners.get(priority).add(method);
            objects.put(method, new ArrayList<>());
        }
        if(objects.get(method).contains(object)) return;
        objects.get(method).add(object);
    }

    /**
     * Unregister all listener methods of the given object
     * @param listener The object whose listener methods should be unregistered
     */
    public void unregisterListeners(Object listener){
        for(Method method:listener.getClass().getMethods()){
            unregisterListener(listener, method);
        }
    }

    /**
     * Unregister the listener method for the specified object
     * @param object The object whose method should be unregistered
     * @param method The listener method
     */
    public void unregisterListener(Object object, Method method){
        if(!method.isAnnotationPresent(EventListener.class)) return;
        if(method.getParameterTypes().length != 1) return;
        if(!Event.class.isAssignableFrom(method.getParameterTypes()[0])) return;
        if(!events.containsKey(method.getParameterTypes()[0])) return;
        if(!objects.containsKey(method)) return;
        objects.get(method).remove(object);
    }

    /**
     * Notify all listeners registered for that event type
     * @param event The event that should be thrown
     */
    public void handle(Event event){
        if(!events.containsKey(event.getClass())) return;
        Map<ListenerPriority, List<Method>> eventListeners = events.get(event.getClass());
        for(ListenerPriority priority:ListenerPriority.values()){
            if(!eventListeners.containsKey(priority))continue;
            for(Method method:eventListeners.get(priority)){
                for(Object object:objects.get(method)){
                    try {
                        method.invoke(object, event);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
