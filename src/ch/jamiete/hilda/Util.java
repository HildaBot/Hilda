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

import ch.jamiete.hilda.runnables.MessageDeletionTask;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Util {
    public static long TIME_5M = 300000, TIME_10M = 600000, TIME_15M = 900000,
            TIME_1H = 3600000, TIME_24H = 86400000;

    private static Hilda HILDA = null;

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

    public static Consumer<Message> deleteAfter(final int seconds) {
        return message -> {
            Util.HILDA.getExecutor().schedule(new MessageDeletionTask(message), seconds, TimeUnit.SECONDS);
        };
    }

    /**
     * Gets a user-friendly String of the strings passed.
     * @param strings The strings to list.
     * @return The list of strings. For example, "Apple, Banana and Carrot".
     */
    public static String getAsList(final List<String> strings) {
        final StringBuilder sb = new StringBuilder();

        if (strings.size() == 0) {
            return "";
        }

        if (strings.size() == 1) {
            sb.append(strings.get(0));
        } else {
            for (final String string : strings.subList(0, strings.size() - 1)) {
                sb.append(string).append(", ");
            }

            sb.setLength(sb.length() - 2);
            sb.append(" and ").append(strings.get(strings.size() - 1));
        }

        return sb.toString();
    }

    /**
     * Gets a user-friendly String of the strings passed.
     * @param strings The strings to list.
     * @return The list of strings. For example, "Apple, Banana and Carrot".
     */
    public static String getAsList(final String... strings) {
        return Util.getAsList(Arrays.asList(strings));
    }

    /**
     * Gets a user-friendly String of the channels passed.
     * @param channels The channels to list.
     * @return The list of channels. For example, "#general, #voice and #bots".
     */
    public static String getChannelsAsString(final List<? extends Channel> channels) {
        final List<String> strings = new ArrayList<>();
        channels.forEach(c -> strings.add("#" + c.getName()));
        return Util.getAsList(strings);
    }

    /**
     * Gets a human-readable String containing the time.
     * @param duration Time to convert
     * @return Human-readable String
     */
    public static String getFriendlyTime(long duration) {
        final StringBuilder sb = new StringBuilder();

        long months = TimeUnit.MILLISECONDS.toDays(duration);
        if (months >= 30) {
            months = months / 30;
            duration -= TimeUnit.DAYS.toMillis(months * 30);
        } else {
            months = 0;
        }

        final long years = months / 12;
        if (years > 0) {
            duration -= TimeUnit.DAYS.toMillis(years * 365);
        }

        final long days = TimeUnit.MILLISECONDS.toDays(duration);
        duration -= TimeUnit.DAYS.toMillis(days);

        final long hours = TimeUnit.MILLISECONDS.toHours(duration);
        duration -= TimeUnit.HOURS.toMillis(hours);

        final long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        duration -= TimeUnit.MINUTES.toMillis(minutes);

        final long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);

        if (years > 0) {
            sb.append(" ").append(years);
            sb.append(" ").append(years == 1 ? "year" : "years");
        }

        if (months > 0) {
            sb.append(" ").append(months);
            sb.append(" ").append(months == 1 ? "month" : "months");
        }

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

    /**
     * Gets a user-friendly String of the members passed. The members will be mentioned.
     * @param members The members to list.
     * @return The list of members. For example, "{@literal @Jane, @Bob and @Suzie}".
     */
    public static String getMembersAsString(final List<Member> members) {
        final List<User> users = new ArrayList<>();
        members.forEach(m -> users.add(m.getUser()));
        return Util.getUsersAsString(users);
    }

    /**
     * Gets the name of the Member in a format that is amenable to use in logs or administrator-facing contexts. <br>
     * Wraps {@link #getName(User)} to avoid long lines of code.
     * @param member The user whose name should be given
     * @return The user's underlying username and their discriminator (e.g. BobDoe#1234)
     */
    public static String getName(final Member member) {
        return Util.getName(member.getUser());
    }

    /**
     * Gets the name of the User in a format that is amenable to use in logs or administrator-facing contexts.
     * @param user The user whose name should be given
     * @return The user's underlying username and their discriminator (e.g. BobDoe#1234)
     */
    public static String getName(final User user) {
        return user.getName() + "#" + user.getDiscriminator();
    }

    /**
     * Gets the name of the Guild in a format that is amenable to use in logs or administrator-facing contexts.
     * @param guild The guild whose name should be given
     * @return The guild's underlying username and its id in parentheses (e.g. Example#23482348203984)
     */
    public static String getName(final Guild guild) {
        return guild.getName() + " (" + guild.getId() + ")";
    }

    /**
     * Gets the milliseconds of the nearest hour in timezone.
     * @param timezone The timezone to check.
     * @return The next hour in timezone.
     */
    public static long getNextHour(final String timezone) {
        final Calendar time = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        time.add(Calendar.HOUR, 1);
        return time.getTimeInMillis();
    }

    /**
     * Gets the milliseconds of the next midnight in timezone.
     * @param timezone The timezone to check.
     * @return The next midnight in timezone.
     */
    public static long getNextMidnightInMillis(final String timezone) {
        final Calendar time = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        time.set(Calendar.HOUR_OF_DAY, 0);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        time.add(Calendar.DAY_OF_MONTH, 1);
        return time.getTimeInMillis();
    }

    /**
     * Gets a user-friendly String of the roles passed. The roles will not be mentioned.
     * @param roles The roles to list.
     * @return The list of roles. For example, "Administrator, Moderator and Member".
     */
    public static String getRolesAsString(final List<Role> roles) {
        final List<String> strings = new ArrayList<>();
        roles.forEach(r -> strings.add(r.getName()));
        return Util.getAsList(strings);
    }

    /**
     * Gets a user-friendly String of the users passed. The users will be mentioned.
     * @param users The users to list.
     * @return The list of users. For example, "{@literal @Jane, @Bob and @Suzie}".
     */
    public static String getUsersAsString(final List<User> users) {
        final List<String> strings = new ArrayList<>();
        users.forEach(u -> strings.add(u.getAsMention()));
        return Util.getAsList(strings);
    }

    public static String sanitise(final String input) {
        return input.replace("@everyone", "\\@\u200Beveryone").replace("@here", "\\@\u200Bhere");
    }

    protected static void setHilda(final Hilda hilda) {
        Util.HILDA = hilda;
    }

}
