/*******************************************************************************
 * Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Modifications copyright 2017 jamietech 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
  *******************************************************************************/
package ch.jamiete.hilda.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import ch.jamiete.hilda.Hilda;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.IEventManager;

/**
 * Implementation for {@link net.dv8tion.jda.core.hooks.IEventManager IEventManager}
 * which checks for {@link EventHandler EventHandler} annotations on both
 * <b>static</b> and <b>member</b> methods.
 *
 * <p>Listeners for this manager do <u>not</u> need to implement {@link net.dv8tion.jda.core.hooks.EventListener EventListener}
 * <br>Example
 * <pre><code>
 *     public class Foo
 *     {
 *        {@literal @SubscribeEvent}
 *         public void onMsg(MessageReceivedEvent event)
 *         {
 *             System.out.printf("%s: %s\n", event.getAuthor().getName(), event.getMessage().getContent());
 *         }
 *     }
 * </code></pre>
 *
 * @see net.dv8tion.jda.core.hooks.InterfacedEventManager
 * @see net.dv8tion.jda.core.hooks.IEventManager
 * @see net.dv8tion.jda.core.hooks.SubscribeEvent
 */
public class AnnotatedEventManager implements IEventManager {
    private final Set<Object> listeners = new HashSet<>();
    private final Map<Class<? extends Event>, Map<Object, List<Method>>> methods = new HashMap<>();

    @Override
    public void register(Object listener) {
        if (listeners.add(listener)) {
            updateMethods();
        }
    }

    @Override
    public void unregister(Object listener) {
        if (listeners.remove(listener)) {
            updateMethods();
        }
    }

    @Override
    public List<Object> getRegisteredListeners() {
        return Collections.unmodifiableList(new LinkedList<>(listeners));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Event event) {
        Class<? extends Event> eventClass = event.getClass();

        do {
            Map<Object, List<Method>> listeners = methods.get(eventClass);

            if (listeners != null) {
                listeners.entrySet().forEach(e -> e.getValue().forEach(method -> {
                    try {
                        method.setAccessible(true);
                        method.invoke(e.getKey(), event);
                    } catch (IllegalAccessException
                            | InvocationTargetException e1) {
                        Hilda.getLogger().log(Level.WARNING, "Encountered a reflection exception while handling event", e1);
                    } catch (Throwable throwable) {
                        Hilda.getLogger().log(Level.WARNING, "An event listener encountered an exception", throwable);
                        this.handle(new UnhandledEventExceptionEvent(event.getJDA(), 0L, throwable, event));
                    }
                }));
            }

            eventClass = eventClass == Event.class ? null : (Class<? extends Event>) eventClass.getSuperclass();
        } while (eventClass != null);
    }

    @SuppressWarnings("rawtypes")
    private void updateMethods() {
        methods.clear();
        for (Object listener : listeners) {
            boolean isClass = listener instanceof Class;
            Class<?> c = isClass ? (Class) listener : listener.getClass();
            Method[] allMethods = c.getDeclaredMethods();

            for (Method m : allMethods) {
                if (!m.isAnnotationPresent(EventHandler.class) || (isClass && !Modifier.isStatic(m.getModifiers()))) {
                    continue;
                }

                Class<?>[] pType = m.getParameterTypes();

                if (pType.length == 1 && Event.class.isAssignableFrom(pType[0])) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) pType[0];

                    if (!methods.containsKey(eventClass)) {
                        methods.put(eventClass, new HashMap<>());
                    }

                    if (!methods.get(eventClass).containsKey(listener)) {
                        methods.get(eventClass).put(listener, new ArrayList<>());
                    }

                    methods.get(eventClass).get(listener).add(m);
                }
            }
        }
    }
}