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
package ch.jamiete.hilda;

import java.util.concurrent.TimeUnit;

public class Util {

    /**
     * Turns a String[] into a single String separated by the passed separator.
     * @param startIndex The index to start combining from.
     * @param string The array to combine from.
     * @param seperator The String to append between each String in the string array.
     * @return A {@link String} containing all items from a string array with the provided separator between them.
     */
    public static String combineSplit(final int startIndex, final String[] string, final String seperator) {
        if (startIndex + 1 > string.length) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();

        for (int i = startIndex; i < string.length; i++) {
            builder.append(string[i]);
            builder.append(seperator);
        }

        builder.deleteCharAt(builder.length() - seperator.length());
        return builder.toString();
    }

    /**
     * Gets a human-readable String containing the time.
     * @param duration Time to convert
     * @return Human-readable String
     */
    public static String getFriendlyTime(long duration) {
        final StringBuilder sb = new StringBuilder();

        final long days = TimeUnit.MILLISECONDS.toDays(duration);
        duration -= TimeUnit.DAYS.toMillis(days);

        final long hours = TimeUnit.MILLISECONDS.toHours(duration);
        duration -= TimeUnit.HOURS.toMillis(hours);

        final long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        duration -= TimeUnit.MINUTES.toMillis(minutes);

        final long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);

        if (days > 0) {
            sb.append(" ").append(days);
            sb.append(" ").append(days == 1 ? "day" : "days");
        }

        if (hours > 0) {
            sb.append(" ").append(hours);
            sb.append(" ").append(hours == 1 ? "hour" : "hours");
        }

        if (minutes > 0) {
            sb.append(" ").append(minutes);
            sb.append(" ").append(minutes == 1 ? "minute" : "minutes");
        }

        if (seconds > 0) {
            sb.append(" ").append(seconds);
            sb.append(" ").append(seconds == 1 ? "second" : "seconds");
        }

        return sb.toString().trim();
    }

}
