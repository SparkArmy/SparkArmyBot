package de.SparkArmy.utils;

import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

@SuppressWarnings("unused")
public class MessageUtil {
    private static @NotNull String timeToEpochSecond(@NotNull OffsetDateTime parsedTime){
        return String.valueOf(parsedTime.toEpochSecond());
    }

    public static String discordTimestamp(OffsetDateTime time){
        /*
        * t	16:20	Short Time
        * T	16:20:30	Long Time
        * d	20/04/2021	Short Date
        * D	20 April 2021	Long Date
        * f* 20 April 2021 16:20	Short Date/Time
        * F	Tuesday, 20 April 2021 16:20	Long Date/Time
        * R	2 months ago	Relative Time
        * *default
        * */
        return String.format("<t:%s>",timeToEpochSecond(time));
    }

    public static String discordTimestamp(OffsetDateTime time,String formating){
        /*
         * t	16:20	Short Time
         * T	16:20:30	Long Time
         * d	20/04/2021	Short Date
         * D	20 April 2021	Long Date
         * f* 20 April 2021 16:20	Short Date/Time
         * F	Tuesday, 20 April 2021 16:20	Long Date/Time
         * R	2 months ago	Relative Time
         * *default
         * */
        return String.format("<t:%s:%s>",timeToEpochSecond(time),formating);
    }
}
