package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;
import java.net.URI;
import java.text.*;
import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2022 Guidehouse. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by Guidehouse and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "Guidehouse" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or GUIDEHOUSE
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      GUIDEHOUSE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */
public class DateUtils {

	public static String getToday() {
		return getToday("yyyy-MM");
	}

	public static String getToday(String format) {
		java.util.Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static int getNumberOfWeeks() {
		String m = getToday();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		try {
			Date date = format.parse(m);
			Calendar c = Calendar.getInstance();
			c.setTime(date);

			int start = c.get(Calendar.WEEK_OF_MONTH);

			c.add(Calendar.MONTH, 1);
			c.add(Calendar.DATE, -1);
			int end = c.get(Calendar.WEEK_OF_MONTH);
			return end - start + 1;
		} catch (Exception ex) {
			return -1;
		}
	}

	public static int getDayNumberNew(LocalDate date) {
		DayOfWeek day = date.getDayOfWeek();
		return day.getValue();
	}

    public static LocalDate getLocalDate() {
		//Current Date
		LocalDate today = LocalDate.now();
		System.out.println("Current Date="+today);
		return today;
	}

    public static LocalDate getLocalDate(int year, int month, int day) { // month: Month.JANUARY
		//Creating LocalDate by providing input arguments
		LocalDate local_date = LocalDate.of(year, month, day);
		return local_date;
    }

    public static int getDayNumber(LocalDate date) {
		DayOfWeek day = date.getDayOfWeek();
		printDayOfWeek(day);
		return day.getValue();
	}

	public static int getDayOfMonth() {
		return LocalDate.now().getDayOfMonth();
	}

	public static int getCurrentMonth() {
		 return Calendar.getInstance().get(Calendar.MONTH) + 1;
	}

	public static int getCurrentYear() {
		 return Calendar.getInstance().get(Calendar.YEAR);
	}

	public static String getCurrDate() {
		int year = getCurrentYear();
		int month = getCurrentMonth();
		int day = getDayOfMonth();
        LocalDate date = LocalDate.of(year, month, day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        return formattedDate;
    }

    public static void printDayOfWeek(DayOfWeek dow) {
		Locale locale = Locale.getDefault();
		System.out.println(dow.getDisplayName(TextStyle.FULL, locale));
		System.out.println(dow.getDisplayName(TextStyle.NARROW, locale));
		System.out.println(dow.getDisplayName(TextStyle.SHORT, locale));
	}

    public static int getDaysInMonth(int year, int month) {
		YearMonth yearMonthObject = YearMonth.of(year, month);
		int daysInMonth = yearMonthObject.lengthOfMonth();
		return daysInMonth;
	}

	public static int getNumberOfMondaysInAMonth(int year, int month) {
		String weekDay = "Monday";
		return getNumberOfWeekDaysInAMonth(year, month, weekDay);
	}

    public static String getMonthName(int month) {
        int year = DateUtils.getCurrentYear();
        LocalDate date = LocalDate.of(year, month, 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM", Locale.US);
        String monthName = date.format(formatter);
        return monthName;
    }

	public static int getNumberOfWeekDaysInAMonth(int year, int month, String weekDay) {
		int m = 0;
		Locale locale = Locale.getDefault();
		int num_days = getDaysInMonth(year, month);
		for (int day=1; day<=num_days; day++) {
			LocalDate date = getLocalDate(year, month, day);
			DayOfWeek dow = date.getDayOfWeek();
			String fullName = dow.getDisplayName(TextStyle.FULL, locale);
			if (fullName.compareTo(weekDay) == 0) {
				m++;
			}
		}
		return m;
	}

	public static int getLastWeekdayOfAMonth(int year, int month, String weekDay) {
		int m = 0;
		Locale locale = Locale.getDefault();
		int num_days = getDaysInMonth(year, month);
		for (int day=1; day<=num_days; day++) {
			int j = num_days-day+1;
			LocalDate date = getLocalDate(year, month, j);
			DayOfWeek dow = date.getDayOfWeek();
			String fullName = dow.getDisplayName(TextStyle.FULL, locale);
			if (fullName.compareTo(weekDay) == 0) {
				return j;
			}
		}
		return 0;
	}


    public static String getMonthString(int month) {
		return new DateFormatSymbols().getMonths()[month-1];
	}



////////////////////////////////////////////////////////////////////////////////////////////
	public static Vector getWeekdayOfAMonth(int year, int month, String weekDay) {
		int m = 0;
		Locale locale = Locale.getDefault();
		int num_days = getDaysInMonth(year, month);
		Vector weekday_vec = new Vector();
		for (int day=1; day<=num_days; day++) {
			LocalDate date = getLocalDate(year, month, day);
			DayOfWeek dow = date.getDayOfWeek();
			String fullName = dow.getDisplayName(TextStyle.FULL, locale);
			if (fullName.compareTo(weekDay) == 0) {
				weekday_vec.add(Integer.valueOf(day));
			}
		}
		return weekday_vec;
	}

	public int getWeekdayOfAMonth(int year, int month, int n) {
		Vector weekday_vec = getWeekdayOfAMonth(year, month, "Monday");
		Integer int_obj = (Integer) weekday_vec.elementAt(n);
		return int_obj.intValue();
	}

	public static int getWeekOfMonth() {
		Calendar calendar = Calendar.getInstance(Locale.US);
		LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();
		calendar.set(year, month, day);
		int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
		return weekOfMonth;
	}



/*
	public static void main(String[] args) throws Exception {
		int currentYear = getCurrentYear();
		int currentMonth = getCurrentMonth();
        Vector w = getNCItReleaseSchedule(currentYear, false);
        Utils.dumpVector("NCIt Release Schedule (Year " + currentYear + ")", w);
	}
*/

}
