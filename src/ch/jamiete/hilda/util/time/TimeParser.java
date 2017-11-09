/**
 * *****************************************************************************
 * Copyright 2017 jamietech
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * *****************************************************************************
 */
package ch.jamiete.hilda.util.time;

import java.util.Arrays;
import ch.jamiete.hilda.Sanity;

public class TimeParser {

    public static long getTime(final String[] args) throws TimeParseException {
        return TimeParser.getTimeBundle(args).time;
    }

    public static TimeBundle getTimeBundle(final String[] args) throws TimeParseException {
        Sanity.truthiness(args.length != 0, "Must supply arguments.");

        final TimeBundle bundle = new TimeBundle();
        int last = -1;
        long time = -1;

        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];

            if (Character.isDigit(arg.charAt(0))) {
                last = i;
            } else {
                break;
            }

            if (i == 0) {
                time = 0;
            }

            long unit;

            try {
                unit = Long.parseLong(arg.substring(0, arg.length() - 1));
            } catch (final NumberFormatException ex) {
                throw new TimeParseException("Could not parse number " + arg + ". "); // Shouldn't happen
            }

            final String unitType = arg.substring(arg.length() - 1, arg.length());

            switch (unitType) {
                case "s":
                    time += unit * 1000;
                    break;

                case "m":
                    time += unit * 1000 * 60;
                    break;

                case "h":
                    time += unit * 1000 * 60 * 60;
                    break;

                case "d":
                    time += unit * 1000 * 60 * 60 * 24;
                    break;

                case "w":
                    time += unit * 1000 * 60 * 60 * 24 * 7;
                    break;

                default:
                    throw new TimeParseException("Invalid time unit type " + unitType + ". I only know seconds (s), minutes (m), hours (h), days (d) and weeks (w).");
            }
        }

        bundle.time = time;

        if (last > -1) {
            bundle.rejects = Arrays.copyOfRange(args, last + 1, args.length);
        } else {
            bundle.rejects = new String[0];
        }

        return bundle;
    }
}
