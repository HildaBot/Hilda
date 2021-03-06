/*******************************************************************************
 * Copyright 2017 jamietech
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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;

import javax.annotation.Nonnull;

public class UnhandledEventExceptionEvent implements GenericEvent {
    private final Throwable throwable;
    private final GenericEvent event;

    public UnhandledEventExceptionEvent(final JDA api, final long responseNumber, final Throwable throwable, final GenericEvent event) {
        this.throwable = throwable;
        this.event = event;
    }

    /**
     * Get the throwable that was thrown.
     * @return
     */
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Nonnull
    @Override
    public JDA getJDA() {
        return null;
    }

    @Override
    public long getResponseNumber() {
        return 0;
    }
}
