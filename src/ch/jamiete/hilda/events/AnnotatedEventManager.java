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
import ch.jamiete.hilda.Util;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.channel.text.GenericTextChannelEvent;
import net.dv8tion.jda.core.events.channel.voice.GenericVoiceChannelEvent;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.core.events.role.GenericRoleEvent;
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
 *        {@literal @EventHandler}
 *         public void onMsg(MessageReceivedEvent event)
 *         {
 *             System.out.printf("%s: %s\n", event.getAuthor().getName(), event.getMessage().getContent());
 *         }
 *     }
 * </code></pre>
 *
 * @see net.dv8tion.jda.core.hooks.InterfacedEventManager
 * @see net.dv8tion.jda.core.hooks.IEventManager
 * @see EventHandler
 */
public class AnnotatedEventManager implements IEventManager {
    private final Set<Object> listeners = new HashSet<>();
    private final Map<Class<? extends Event>, Map<Object, List<Method>>> methods = new HashMap<>();

    @Override
    public List<Object> getRegisteredListeners() {
        return Collections.unmodifiableList(new LinkedList<>(this.listeners));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(final Event event) {
        Class<? extends Event> eventClass = event.getClass();

        do {
            final Map<Object, List<Method>> listeners = this.methods.get(eventClass);

            if (listeners != null) {
                final Class<? extends Event> eventClassFinal = eventClass;

                listeners.entrySet().forEach(e -> e.getValue().forEach(method -> {
                    try {
                        method.setAccessible(true);
                        method.invoke(e.getKey(), event);
                    } catch (final IllegalAccessException e1) {
                        Hilda.getLogger().log(Level.WARNING, "Encountered a reflection exception while handling event", e1);
                    } catch (final Throwable throwable) {
                        final StringBuilder sb = new StringBuilder();

                        sb.append("An event listener for ").append(eventClassFinal.getName()).append(" encountered an exception");

                        // Provide more information
                        if (event instanceof GenericGuildEvent) {
                            final GenericGuildEvent ev = (GenericGuildEvent) event;
                            sb.append(" in ").append(Util.getName(ev.getGuild()));
                        }

                        if (event instanceof GenericGuildMessageEvent) {
                            final GenericGuildMessageEvent ev = (GenericGuildMessageEvent) event;
                            sb.append(" with message ").append(ev.getMessageId());
                            sb.append(" in ").append(Util.getName(ev.getGuild()));
                        }

                        if (event instanceof GenericGuildMessageReactionEvent) {
                            final GenericGuildMessageReactionEvent ev = (GenericGuildMessageReactionEvent) event;
                            sb.append(" with message ").append(ev.getMessageId());
                            sb.append(" in ").append(Util.getName(ev.getGuild()));
                        }

                        if (event instanceof GenericRoleEvent) {
                            final GenericRoleEvent ev = (GenericRoleEvent) event;
                            sb.append(" with role ").append(ev.getRole().getId());
                            sb.append(" in ").append(Util.getName(ev.getGuild()));
                        }

                        if (event instanceof GenericTextChannelEvent) {
                            final GenericTextChannelEvent ev = (GenericTextChannelEvent) event;
                            sb.append(" with channel ").append(ev.getChannel().getId());
                            sb.append(" in ").append(Util.getName(ev.getGuild()));
                        }

                        if (event instanceof GenericVoiceChannelEvent) {
                            final GenericVoiceChannelEvent ev = (GenericVoiceChannelEvent) event;
                            sb.append(" with channel ").append(ev.getChannel().getId());
                            sb.append(" in ").append(Util.getName(ev.getGuild()));
                        }

                        // Log
                        if (throwable instanceof InvocationTargetException && throwable.getCause() != null) {
                            Hilda.getLogger().log(Level.WARNING, sb.toString(), throwable.getCause());
                        } else {
                            Hilda.getLogger().log(Level.WARNING, sb.toString(), throwable);
                        }

                        if (!(event instanceof UnhandledEventExceptionEvent)) {
                            this.handle(new UnhandledEventExceptionEvent(event.getJDA(), 0L, throwable, event));
                        }
                    }
                }));
            }

            eventClass = eventClass == Event.class ? null : (Class<? extends Event>) eventClass.getSuperclass();
        } while (eventClass != null);
    }

    @Override
    public void register(final Object listener) {
        if (this.listeners.add(listener)) {
            this.updateMethods();
        }
    }

    @Override
    public void unregister(final Object listener) {
        if (this.listeners.remove(listener)) {
            this.updateMethods();
        }
    }

    @SuppressWarnings("rawtypes")
    private void updateMethods() {
        this.methods.clear();
        for (final Object listener : this.listeners) {
            final boolean isClass = listener instanceof Class;
            final Class<?> c = isClass ? (Class) listener : listener.getClass();
            final Method[] allMethods = c.getDeclaredMethods();

            for (final Method m : allMethods) {
                if (!m.isAnnotationPresent(EventHandler.class) || isClass && !Modifier.isStatic(m.getModifiers())) {
                    continue;
                }

                final Class<?>[] pType = m.getParameterTypes();

                if (pType.length == 1 && Event.class.isAssignableFrom(pType[0])) {
                    @SuppressWarnings("unchecked")
                    final Class<? extends Event> eventClass = (Class<? extends Event>) pType[0];

                    if (!this.methods.containsKey(eventClass)) {
                        this.methods.put(eventClass, new HashMap<>());
                    }

                    if (!this.methods.get(eventClass).containsKey(listener)) {
                        this.methods.get(eventClass).put(listener, new ArrayList<>());
                    }

                    this.methods.get(eventClass).get(listener).add(m);
                }
            }
        }
    }
}