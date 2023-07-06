/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class helps in the management of Date/Time ranges. This class offers four methods
 * for the setting of the time boundaries: two for the left boundary (setFromDate,
 * setFromTime) and two for the right boundary (setToDate, setToTime).<br>
 * All of these methods accepts Date values, but as the name of the methods suggests, the
 * parameter passed to the setFromDate and setToDate are used to store information about
 * year, month. The methods setFromTime and setToTime are used to store information about
 * hour, minutes and seconds.<br>
 * The reason why this class is useful is that not every field has to be set, in fact it
 * is sufficient to set one of the four boundary fields.<br>
 * The getter methods returns the left/right boundary according to what has been set.<br>
 * Usage examples:<br>
 * x stands for fromDate, j stands for fromTime<br>
 * y stands for toDate, k stands for toTime<br>
 * "-" stands for value not set<br>
 *<br>
 * setting x.j and y.k, getFromDateTime() will return x.j, getToDateTime() will return y.k<br>
 * setting x.j and y.-, getFromDateTime() will return x.j, getToDateTime() will return
 * y.(23:59:59)<br>
 * setting x.- and y.k, getFromDateTime() will return x.(00:00:00), getToDateTime() will
 * return y.k<br>
 * setting -.j and y.k, getFromDateTime() will return null (unbounded), getToDateTime()
 * will return y.k<br>
 * setting -.j and -.k, getFromDateTime() will return null (today).j, getToDateTime() will
 * return (today).k<br>
 * 
 * @author giacomo petronio
 * 
 */
public class DateTimeRange {
	Date fromDate;
	Date fromTime;
	Date toDate;
	Date toTime;

	boolean isDateSet = false;
	boolean isTimeSet = false;

	public boolean isDateSet() {
		return isDateSet;
	}

	public boolean isTimeSet() {
		return isTimeSet;
	}

	public void setFromDate(Date from) {
		isDateSet = true;
		this.fromDate = from;
	}

	public void setFromTime(Date from) {
		isTimeSet = true;
		this.fromTime = from;
	}

	public void setToDate(Date to) {
		isDateSet = true;
		this.toDate = to;
	}

	public void setToTime(Date to) {
		isTimeSet = true;
		this.toTime = to;
	}

	/**
	 * Returns a Date with fromDate and fromTime fields combined together. May be null if
	 * the left limit is unbounded.
	 * 
	 * @return
	 */
	public Date getFromDateTime() {
		Calendar fromCalendar = Calendar.getInstance();
		fromCalendar.clear();

		// NB: Do not change the order of the following if-statements!

		// unbounded
		if (fromDate == null && toDate != null) {
			return null;
		}

		// date.time
		if (fromDate != null && fromTime != null) {
			Calendar dc = Calendar.getInstance();
			Calendar tc = Calendar.getInstance();
			dc.setTime(fromDate);
			tc.setTime(fromTime);
			fromCalendar.set(dc.get(Calendar.YEAR), dc.get(Calendar.MONTH), dc.get(Calendar.DAY_OF_MONTH), tc.get(Calendar.HOUR_OF_DAY), tc.get(Calendar.MINUTE), tc.get(Calendar.SECOND));
			return fromCalendar.getTime();
		}

		// date.(00:00:00)
		if (fromDate != null && fromTime == null) {
			Calendar dc = Calendar.getInstance();
			dc.setTime(fromDate);
			fromCalendar.set(dc.get(Calendar.YEAR), dc.get(Calendar.MONTH), dc.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			return fromCalendar.getTime();
		}

		// (today).time
		if (fromTime != null) {
			Calendar today = Calendar.getInstance();
			Calendar tc = Calendar.getInstance();
			tc.setTime(fromTime);
			fromCalendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), tc.get(Calendar.HOUR_OF_DAY), tc.get(Calendar.MINUTE), tc.get(Calendar.SECOND));
			return fromCalendar.getTime();
		}

		// unbounded
		return null;
	}

	/**
	 * Returns a Date with toDate and toTime fields combined together. May be null if the
	 * left limit is unbounded.
	 * 
	 * @return
	 */
	public Date getToDateTime() {
		Calendar toCalendar = Calendar.getInstance();
		toCalendar.clear();
		
		// NB: Do not change the order of the following if-statements!

		// unbounded
		if (fromDate != null && toDate == null) {
			return null;
		}

		// date.time
		if (toDate != null && toTime != null) {
			Calendar dc = Calendar.getInstance();
			Calendar tc = Calendar.getInstance();
			dc.setTime(toDate);
			tc.setTime(toTime);
			toCalendar.set(dc.get(Calendar.YEAR), dc.get(Calendar.MONTH), dc.get(Calendar.DAY_OF_MONTH), tc.get(Calendar.HOUR_OF_DAY), tc.get(Calendar.MINUTE), tc.get(Calendar.SECOND));
			return toCalendar.getTime();
		}

		// date.(23:59:59)
		if (toDate != null && toTime == null) {
			Calendar dc = Calendar.getInstance();
			dc.setTime(toDate);
			toCalendar.set(dc.get(Calendar.YEAR), dc.get(Calendar.MONTH), dc.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
			return toCalendar.getTime();
		}

		// (today).time
		if (toDate == null && toTime != null) {
			Calendar today = Calendar.getInstance();
			Calendar tc = Calendar.getInstance();
			tc.setTime(toTime);
			toCalendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), tc.get(Calendar.HOUR_OF_DAY), tc.get(Calendar.MINUTE), tc.get(Calendar.SECOND));
			return toCalendar.getTime();
		}

		// unbounded
		return null;
	}

	// test
	public static void main(String[] args) {
		DateTimeRange dtr = new DateTimeRange();

		Calendar x = Calendar.getInstance();
		Calendar y = Calendar.getInstance();
		Calendar j = Calendar.getInstance();
		Calendar k = Calendar.getInstance();

		x.set(2011, 3, 4, 8, 30, 0); // x = 1982-11-08 ******
		y.set(2011, 3, 4, 15, 45, 0); // y = 1990-10-15 ******

		j.set(1998, 5, 10, 9, 2, 00); // j = ****** 09:02:00
		k.set(1998, 5, 10, 15, 28, 00); // k = ****** 15:28:00

		dtr.setFromDate(x.getTime());
		dtr.setFromTime(j.getTime());

		dtr.setToDate(y.getTime());
		dtr.setToTime(k.getTime());

		Format df = new SimpleDateFormat("yyyy-MM-dd");
		Format tf = new SimpleDateFormat("HH:mm:ss");
		Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date fromDateTime = dtr.getFromDateTime();
		Date toDateTime = dtr.getToDateTime();
		String from = ((fromDateTime != null) ? f.format(fromDateTime) : "unbounded");
		String to = ((toDateTime != null) ? f.format(toDateTime) : "unbounded");

		System.out.println("fromDate: " + df.format(x.getTime()));
		System.out.println("fromTime: " + tf.format(j.getTime()));

		System.out.println("\ntoDate: " + df.format(y.getTime()));
		System.out.println("toTime: " + tf.format(k.getTime()));

		System.out.println("\n\nFROM: " + from);
		System.out.println("TO: " + to);
	}
}
