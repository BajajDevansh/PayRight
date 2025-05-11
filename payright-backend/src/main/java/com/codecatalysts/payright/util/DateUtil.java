package com.codecatalysts.payright.util;

import java.time.LocalDate;
import java.time.YearMonth;

public class DateUtil {

    private DateUtil() {
    }

    public static LocalDate calculateNextBillingDate(LocalDate fromDate, String frequency) {
        if (fromDate == null) {
            return LocalDate.now().plusMonths(1);
        }
        if (frequency == null || frequency.trim().isEmpty()) {
            return fromDate.plusMonths(1);
        }

        int dayOfMonth = fromDate.getDayOfMonth();

        switch (frequency.toLowerCase()) {
            case "monthly":
                YearMonth nextMonthYear = YearMonth.from(fromDate).plusMonths(1);
                int lastDayOfNextMonth = nextMonthYear.lengthOfMonth();
                return nextMonthYear.atDay(Math.min(dayOfMonth, lastDayOfNextMonth));
            case "yearly":
                YearMonth nextYearMonth = YearMonth.from(fromDate).plusYears(1);
                int lastDayOfNextYearMonth = nextYearMonth.lengthOfMonth();
                return nextYearMonth.atDay(Math.min(dayOfMonth, lastDayOfNextYearMonth));
            case "weekly":
                return fromDate.plusWeeks(1);
            default:
                YearMonth defaultNextMonthYear = YearMonth.from(fromDate).plusMonths(1);
                int defaultLastDayOfNextMonth = defaultNextMonthYear.lengthOfMonth();
                return defaultNextMonthYear.atDay(Math.min(dayOfMonth, defaultLastDayOfNextMonth));
        }
    }
}
