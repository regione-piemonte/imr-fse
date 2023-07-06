/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

public class DateHelper {

	private static Logger log = Logger.getLogger(DateHelper.class);

	/**
	 * Parse the argument, if it is a valid value in the future, return the corresponding
	 * java.util.Date value. The expected format is "yyyy-MM-dd HH.mm". If the value is
	 * null or is not a valid date in the expected format, returns null. If the value is a
	 * valid date in the past, returns the nearest Date in the future with the same hour.
	 * 
	 * @param dateTime
	 * @return
	 */
	public static Date getFirstUsefulDate(String dateTime) {

		if (dateTime != null) {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm");
			Date parsedDate = null;
			try {
				parsedDate = sdf.parse(dateTime);
			} catch (ParseException e) {
				log.warn("Error parsing datetime ('yyyy-MM-dd HH.mm'): " + dateTime);
				return null;
			}
			GregorianCalendar instantGc = new GregorianCalendar();
			GregorianCalendar parsedGc = new GregorianCalendar();
			parsedGc.setTime(parsedDate);
			instantGc.setTime(new Date());

			if (parsedGc.after(instantGc)) {
				log.debug("datetime represents a valid time in the future: " + parsedDate);
				return parsedDate;
			} else {
				log.warn("datetime represents a time in the past: " + parsedDate);

				int instantHour = instantGc.get(Calendar.HOUR_OF_DAY);
				int instantMinute = instantGc.get(Calendar.MINUTE);
				int parsedHour = parsedGc.get(Calendar.HOUR_OF_DAY);
				int parsedMinute = parsedGc.get(Calendar.MINUTE);

				int instantAbsoluteMinute = instantHour * 60 + instantMinute;
				int parsedAbsoluteMinute = parsedHour * 60 + parsedMinute;

				if (instantAbsoluteMinute < parsedAbsoluteMinute) {
					// today
					GregorianCalendar todayGc = new GregorianCalendar();
					todayGc.set(Calendar.HOUR_OF_DAY, parsedHour);
					todayGc.set(Calendar.MINUTE, parsedMinute);
					todayGc.set(Calendar.SECOND, 0);
					return todayGc.getTime();

				} else {
					// tomorrow
					GregorianCalendar tomorrowGc = new GregorianCalendar();
					tomorrowGc.add(Calendar.DAY_OF_MONTH, 1);
					tomorrowGc.set(Calendar.HOUR_OF_DAY, parsedHour);
					tomorrowGc.set(Calendar.MINUTE, parsedMinute);
					tomorrowGc.set(Calendar.SECOND, 0);
					return tomorrowGc.getTime();
				}
			}
		} else {
			log.warn("returning null datetime parameter");
			return null;
		}
	}
}
