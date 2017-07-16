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
package ch.jamiete.hilda.commands;

import ch.jamiete.hilda.Hilda;
import net.dv8tion.jda.core.entities.Message;

public abstract class ChannelSubCommand extends ChannelCommand {
    private final ChannelSeniorCommand senior;

    protected ChannelSubCommand(final Hilda hilda, final ChannelSeniorCommand senior) {
        super(hilda);

        this.senior = senior;
    }

    /**
     * Sends a reply to the channel in which the message was received that the argument was not recognised and shows the label that was used.
     * @param received The message to be replied to.
     * @param params The parameters that the command recognises and can respond to.
     * @param label The label that was used to invoke the command
     */
    @Override
    protected void usage(final Message received, final String params, final String label) {
        this.reply(received, "Incorrect usage. " + CommandManager.PREFIX + this.senior.getName() + " " + label + " " + params);
    }

    /**
     * Gets the senior command for this subcommand.
     * @return The senior command.
     */
    protected ChannelSeniorCommand getSenior() {
        return this.senior;
    }

}
