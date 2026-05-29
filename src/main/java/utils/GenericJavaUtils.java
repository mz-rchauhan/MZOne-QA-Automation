package utils;

import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.Duration;
import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;

import core.base.LoggerManager;
public class GenericJavaUtils {

    private static final Logger LOGGER = LoggerManager.getLogger(GenericJavaUtils.class);

    private static final String SF_DATE_FORMAT = "MM/dd/yyyy";

    // gives a random number between start and end. Start inclusive and end
    // exclusive
    public static int selectAnyRandomNumber(int start, int end) {
        Random r = new Random();
        return r.nextInt(end - start) + start;
    }

    public static String changeDateFormat(String date, String timezone, String oldFormat, String newFormat)
            throws Exception {
        Date date1 = null;
        SimpleDateFormat simpleDateFormatOld = new SimpleDateFormat(oldFormat);
        simpleDateFormatOld.setTimeZone(TimeZone.getTimeZone(timezone));
        SimpleDateFormat simpleDateFormatNew = new SimpleDateFormat(newFormat);
        simpleDateFormatNew.setTimeZone(TimeZone.getTimeZone(timezone));
        if (date.equalsIgnoreCase("Today")) {
            date1 = new Date();
        } else {
            date1 = simpleDateFormatOld.parse(date);
        }
        return simpleDateFormatNew.format(date1);
    }

    // Below method verifies mm/dd/yyyy date format
    public static boolean verifyDateFormatIsMMDDYYYY(String date) {
        String regex = "^(1[0-2]|0[1-9]|[1-9])/(3[01]|[12][0-9]|0[1-9]|[1-9])/[0-9]{4}$";
        return date.matches(regex);
    }

    public static String generateOTP(String base32Secret) {
        try {
            TimeBasedOneTimePasswordGenerator totp =
                    new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(30));

            byte[] decodedKey = new org.apache.commons.codec.binary.Base32().decode(base32Secret);

            SecretKey key = new javax.crypto.spec.SecretKeySpec(decodedKey, "HmacSHA1");

            int otp = totp.generateOneTimePassword(key, Instant.now());
            System.out.println("OTP: "+otp);

            return String.format("%06d", otp);

        } catch (Exception e) {
            throw new RuntimeException("Error generating OTP", e);
        }
    }


    public static String changeTimeFormat(String time) throws ParseException {
        String[] timeParts = time.split(" ");
        Date time1 = new SimpleDateFormat("HH:mm").parse(time);
        String convertDate = new SimpleDateFormat("h:mm").format(time1);
        return convertDate + " " + timeParts[1];
    }

    public static String changeDateFormatFromOldToNew(String date, String oldDateFormat, String newDateFormat) {
        String formattedDate = "";
        SimpleDateFormat originalFormat = new SimpleDateFormat(oldDateFormat);
        SimpleDateFormat targetFormat = new SimpleDateFormat(newDateFormat);

        try {
            Date d = originalFormat.parse(date);
            formattedDate = targetFormat.format(d);
        } catch (ParseException e) {
            LOGGER.error("Invalid date format: " + e.getMessage());
        }
        return formattedDate;
    }

    public static String getDate(int days) {
        SimpleDateFormat dt = new SimpleDateFormat("M/d/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, days);
        return dt.format(calendar.getTime());
    }

    public static String getMonthDate(int months) {
        SimpleDateFormat dt = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, months);
        return dt.format(calendar.getTime());
    }

    public static String getDateByMonthMinusOneDay(int months, String format) {
        LocalDateTime sameDayLastMonth = LocalDateTime.now().plusMonths(months);
        LocalDate yesterday = LocalDate.from(sameDayLastMonth.minusDays(1));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return yesterday.format(formatter);
    }

    public static Properties readPropertiesFile(String path) {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            prop.load(fis);
        } catch (IOException e) {
            LOGGER.error("Failed to read properties file at " + path, e);
        }
        return prop;
    }

    public static String getToday() {
        return getToday(SF_DATE_FORMAT);
    }

    /**
     * Get today's date in a custom format.
     *
     * @param format Java SimpleDateFormat pattern
     * @return Formatted date string
     */
    public static String getToday(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    // ==========================================
    // Future / Past Dates
    // ==========================================

    /**
     * Get a date N days in the future from today.
     *
     * @param days Number of days to add
     * @return Formatted date string
     */
    public static String getTodayPlusDays(int days) {
        return getTodayPlusDays(days, SF_DATE_FORMAT);
    }

    /**
     * Get a date N days in the future from today in a custom format.
     *
     * @param days   Number of days to add
     * @param format Date format pattern
     * @return Formatted date string
     */
    public static String getTodayPlusDays(int days, String format) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        return new SimpleDateFormat(format).format(cal.getTime());
    }

    /**
     * Get a date N days in the past from today.
     *
     * @param days Number of days to subtract
     * @return Formatted date string
     */
    public static String getTodayMinusDays(int days) {
        return getTodayMinusDays(days, SF_DATE_FORMAT);
    }

    /**
     * Get a date N days in the past from today in a custom format.
     *
     * @param days   Number of days to subtract
     * @param format Date format pattern
     * @return Formatted date string
     */
    public static String getTodayMinusDays(int days, String format) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        return new SimpleDateFormat(format).format(cal.getTime());
    }

    // ==========================================
    // Start / End of Month
    // ==========================================

    /**
     * Get the first day of the current month.
     *
     * @return Formatted date string (Salesforce format)
     */
    public static String getStartOfMonth() {
        return getStartOfMonth(SF_DATE_FORMAT);
    }

    /**
     * Get the first day of the current month in a custom format.
     */
    public static String getStartOfMonth(String format) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat(format).format(cal.getTime());
    }

    /**
     * Get the last day of the current month.
     *
     * @return Formatted date string (Salesforce format)
     */
    public static String getEndOfMonth() {
        return getEndOfMonth(SF_DATE_FORMAT);
    }

    /**
     * Get the last day of the current month in a custom format.
     */
    public static String getEndOfMonth(String format) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return new SimpleDateFormat(format).format(cal.getTime());
    }

    // ==========================================
    // Formatting / Conversion
    // ==========================================

    /**
     * Format a date string from one pattern to another.
     *
     * @param dateStr    Input date string
     * @param fromFormat Input format
     * @param toFormat   Output format
     * @return Reformatted date string
     */
    public static String formatDate(String dateStr, String fromFormat, String toFormat) {
        try {
            Date parsed = new SimpleDateFormat(fromFormat).parse(dateStr);
            return new SimpleDateFormat(toFormat).format(parsed);
        } catch (Exception e) {
            LOGGER.warn("DateUtility: Could not reformat '{}' from '{}' to '{}'", dateStr, fromFormat, toFormat);
            return dateStr;
        }
    }

    /**
     * Get current year as string.
     */
    public static String getCurrentYear() {
        return new SimpleDateFormat("yyyy").format(new Date());
    }

    /**
     * Get current timestamp as string (useful for unique record names).
     *
     * @return e.g., "20260308_195423"
     */
    public static String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

}
