package eu.ec.dgempl.eessi.rina.tool.migration.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateResolver {

    private DateResolver() {
    }

    /**
     * Method for converting a stringified date to a {@link ZonedDateTime}. If the date is null, the method returns null. If the date format
     * is unsupported, the method throws a {@link RuntimeException}
     *
     * @param date
     * @return
     */
    public static ZonedDateTime parse(String date) throws DateTimeParseException {
        if (date == null) {
            return null;
        }

        ZonedDateTime result = parseZonedDateTime(date);

        if (result == null) {
            result = parseLocalDateTime(date);
        }

        if (result == null) {
            result = parseEpochTime(date);
        }

        if (result != null) {
            return result;
        } else {
            throw new RuntimeException("Could not parse ZonedDateTime: " + date);
        }
    }

    /**
     * Method for converting a stringified date in {@link ZonedDateTime} format to a {@link ZonedDateTime}. If the date is null, the method
     * returns null. If the date is invalid, the method suppresses the parsing exception and returns null
     *
     * @param date
     * @return
     */
    private static ZonedDateTime parseZonedDateTime(String date) {
        // @formatter:off
        final DateTimeFormatter[] SUPPORTED_DATE_FORMATS = new DateTimeFormatter[] {
                DateTimeFormatter.ISO_ZONED_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        };
        // @formatter:on

        ZonedDateTime result = null;

        // try to parse it with any of the supported formats
        for (DateTimeFormatter format : SUPPORTED_DATE_FORMATS) {
            try {
                result = ZonedDateTime.parse(date, format);
            } catch (DateTimeParseException dtpe) {
                // ignore and try another format
            }
        }

        return result;
    }

    /**
     * Method for converting a stringified date in {@link LocalDateTime} format to a {@link ZonedDateTime}. If the date is null, the method
     * returns null. If the date is invalid, the method suppresses the parsing exception and returns null
     *
     * @param date
     * @return
     */
    private static ZonedDateTime parseLocalDateTime(String date) {
        try {
            LocalDateTime result = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return ZonedDateTime.of(result, ZoneOffset.UTC);
        } catch (Exception e) {
            // ignore exception
        }
        return null;
    }

    /**
     * Method for converting a stringified date in epoch format to a {@link ZonedDateTime}. If the date is null, the method returns null. If
     * the date is invalid, the method suppresses the parsing exception and returns null. If the date is lower or equal to 11 digits
     * (excepting sign), the mehtod assumes the date is in seconds. If the date is higher or equal to 12 digits (excepting sign), the
     * methods assumes the date is in millis.
     * 
     * @param date
     * @return
     */
    private static ZonedDateTime parseEpochTime(String date) {
        try {
            int length = date.length();
            if (date.startsWith("-")) {
                length--;
            }
            if (length > 11) {
                return ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(date)), ZoneOffset.UTC);
            } else {
                return ZonedDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(date)), ZoneOffset.UTC);
            }
        } catch (Exception e) {
            // ignore exception
        }

        return null;
    }
}
