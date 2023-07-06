/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.worklist.dao;

import it.units.htl.dpacs.helpers.DateTimeRange;
import it.units.htl.dpacs.servers.worklist.utils.WlKeys;
import it.units.htl.dpacs.statistics.Timer;
import it.units.htl.dpacs.valueObjects.DicomConstants;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class WlQueryDealer {
	private final Log log = LogFactory.getLog(WlQueryDealer.class);

	DateTimeRange dateTimeRange = new DateTimeRange();
	Date matchingSpsStartDate = null;
	Date matchingSpsStartTime = null;

	public ResultSet getResults(DicomObject data, Connection con, String _viewName, String callingAeTitle, String datePattern, String dateTimePattern, String dateFormula, String dateTimeFormula) throws SQLException {
		// do not use Dbms.isOracle(conn) here!

		StringBuilder wlQuery = new StringBuilder().append("SELECT * FROM ").append(_viewName).append(" WHERE ");
		for (Integer tag : WlKeys.requiredFilterKeys.keySet()) {
			if (WlKeys.requiredFilterKeys.get(tag) == null) {
				String filter = getFilter(data.get(tag));
				if (!"".equals(filter)) {
					wlQuery.append(filter).append(" AND ");
				}
			} else {
				for (Integer nestedTag : WlKeys.requiredFilterKeys.get(tag)) {
					String filter = getFilter(data.getNestedDicomObject(tag).get(nestedTag));
					if (!"".equals(filter)) {
						wlQuery.append(filter).append(" AND ");
					}
				}
			}
		}
		
		for (Integer tag : WlKeys.optionalFilterKeys.keySet()) {
            if (WlKeys.optionalFilterKeys.get(tag) == null) {
                String filter = getFilter(data.get(tag));
                if (!"".equals(filter)) {
					wlQuery.append(filter).append(" AND ");
				}
            } else {
                for (Integer nestedTag : WlKeys.optionalFilterKeys.get(tag)) {
                    String filter = getFilter(data.getNestedDicomObject(tag).get(nestedTag));
                    if (!"".equals(filter)) {
    					wlQuery.append(filter).append(" AND ");
    				}
                }
            }
        }

		String timeFilter = getSpsDateTimeQueryFilter(datePattern, dateTimePattern, dateFormula, dateTimeFormula);
		wlQuery.append(timeFilter);

		String wlQueryString=wlQuery.toString();
		if (wlQueryString.trim().endsWith(" AND")) {
			wlQueryString = wlQueryString.substring(0, wlQuery.lastIndexOf("AND") - 1);
		}

		log.info(callingAeTitle + ": I will execute : " + wlQueryString);
		Statement stat = con.createStatement();
		Timer qryTimer = new Timer();
		qryTimer.restart();
		ResultSet reSet = stat.executeQuery(wlQueryString);
		qryTimer.stop();
		log.info(callingAeTitle + ": Query executed in : " + qryTimer.getMeasure() + "ms.");
		return reSet;
	}

	/**
	 * This method builds a sql query filter for date/time.<br>
	 * There are 8+1 cases to be considered:
	 * <ol>
	 * <li>exact spsStartDate, exact spsStartTime</li>
	 * <li>exact spsStartDate, range spsStartTime</li>
	 * <li>exact spsStartDate, no spsStartTime specified</li>
	 * <li>range spsStartDate, exact spsStartTime (inapplicable)</li>
	 * <li>range spsStartDate, range spsStartTime</li>
	 * <li>range spsStartDate, no spsStartTime specified</li>
	 * <li>no spsStartDate specified, exact spsStartTime</li>
	 * <li>no spsStartDate specified, range spsStartTime</li>
	 * <li>no spsStartDate specified, no spsStartTime specified</li>
	 * </ol>
	 * 
	 * @return
	 */
	private String getSpsDateTimeQueryFilter(String datePattern, String dateTimePattern, String dateFormula, String dateTimeFormula) {
		StringBuilder queryFilter = new StringBuilder();
		SimpleDateFormat f;

		if (matchingSpsStartDate != null && matchingSpsStartTime != null) {
			// log.debug("date match, time match");
			
			f=new SimpleDateFormat(dateTimePattern);
			Date d = combineDateTime(matchingSpsStartDate, matchingSpsStartTime);
			queryFilter.append("spsStartTime = ").append(dateTimeFormula.replace("?", f.format(d))).append(" AND ");
			
		} else if (matchingSpsStartDate != null && dateTimeRange.isTimeSet()) {
			// log.debug("date match, time range");
			dateTimeRange.setFromDate(matchingSpsStartDate);
			dateTimeRange.setToDate(matchingSpsStartDate);
			
			f=new SimpleDateFormat(dateTimePattern);
			queryFilter.append("spsStartTime >= ").append(dateTimeFormula.replace("?", f.format(dateTimeRange.getFromDateTime()))).append(" AND ");
			queryFilter.append("spsStartTime <= ").append(dateTimeFormula.replace("?", f.format(dateTimeRange.getToDateTime())));
			
		} else if (matchingSpsStartDate != null) {
			// log.debug("date match only");
			
			f=new SimpleDateFormat(datePattern);
			queryFilter.append("spsStartDate = ").append(dateFormula.replace("?", f.format(matchingSpsStartDate))).append(" AND ");
			
			
		} else if (dateTimeRange.isDateSet() && matchingSpsStartTime != null) {
			// log.debug("date range, time match");
			log.warn("querying a specific time in a date range, inapplicable query (considering only the date range)");
			
			f=new SimpleDateFormat(datePattern);
			String and = "";
			if (dateTimeRange.getFromDateTime() != null) {
				queryFilter.append("spsStartDate >= ").append(dateFormula.replace("?", f.format(dateTimeRange.getFromDateTime())));
				and = " AND ";
			}
			if (dateTimeRange.getToDateTime() != null) {
				queryFilter.append(and).append("spsStartDate <= ").append(dateFormula.replace("?", f.format(dateTimeRange.getToDateTime())));	
			}
			

		} else if (dateTimeRange.isDateSet() && dateTimeRange.isTimeSet()) {
			// log.debug("date range, time range");
			
			
			f=new SimpleDateFormat(dateTimePattern);
			String and = "";
			if (dateTimeRange.getFromDateTime() != null) {
				queryFilter.append("spsStartTime >= ").append(dateTimeFormula.replace("?", f.format(dateTimeRange.getFromDateTime())));
				and = " AND ";
			}
			if (dateTimeRange.getToDateTime() != null) {
				queryFilter.append(and).append("spsStartTime <= ").append(dateTimeFormula.replace("?", f.format(dateTimeRange.getToDateTime())));	
			}
			
		} else if (dateTimeRange.isDateSet()) {
			// log.debug("date range only");
			f=new SimpleDateFormat(datePattern);
			String and = "";
			if (dateTimeRange.getFromDateTime() != null) {
				queryFilter.append("spsStartDate >= ").append(dateFormula.replace("?", f.format(dateTimeRange.getFromDateTime())));
				and = " AND ";
			}
			if (dateTimeRange.getToDateTime() != null) {
				queryFilter.append(and).append("spsStartDate <= ").append(dateFormula.replace("?", f.format(dateTimeRange.getToDateTime())));	
			}
			
		} else if (matchingSpsStartTime != null) {
			// log.debug("time match only");
			
			f=new SimpleDateFormat(dateTimePattern);
			queryFilter.append("spsStartTime = ").append(dateTimeFormula.replace("?", f.format(matchingSpsStartTime))).append(" AND ");
			
		} else if (dateTimeRange.isTimeSet()) {
			// log.debug("time range only"); 
			
			f=new SimpleDateFormat(dateTimePattern);
			String and = "";
			if (dateTimeRange.getFromDateTime() != null) {
				queryFilter.append("spsStartTime >= ").append(dateTimeFormula.replace("?", f.format(dateTimeRange.getFromDateTime())));
				and = " AND ";
			}
			if (dateTimeRange.getToDateTime() != null) {
				queryFilter.append(and).append("spsStartTime <= ").append(dateTimeFormula.replace("?", f.format(dateTimeRange.getToDateTime())));	
			}
			
		}

		return queryFilter.toString();
	}

	private String getFilter(DicomElement data) {
		StringBuilder qryStringBuilder = new StringBuilder();
		if (data != null) {
			String tagName = WlKeys.filterKeysOnDb.get(data.tag());

			switch (data.tag()) {
			// must be SV or R matched
			case Tag.ScheduledProcedureStepStartDate:
				if (data.getString(null, false) != null) {
					String date = data.getString(null, false);

					if (date.contains("-")) {
						dateTimeRange.setFromDate(data.getDateRange(false).getStart());
						dateTimeRange.setToDate(data.getDateRange(false).getEnd());
					} else {
						matchingSpsStartDate = data.getDate(false);
					}
				}
				break;
			// must be SV or R matched
			case Tag.ScheduledProcedureStepStartTime:
				if (data.getString(null, false) != null) {
					String time = data.getString(null, false);

					if (time.contains("-")) {
						dateTimeRange.setFromTime(data.getDateRange(false).getStart());
						dateTimeRange.setToTime(data.getDateRange(false).getEnd());
					} else {
						matchingSpsStartTime = data.getDate(false);
						setToday(matchingSpsStartTime);
					}
				}
				break;
			// following filters must be SV or WC matched
			case Tag.ScheduledPerformingPhysicianName:
			case Tag.PatientName:
				if (data.getString(null, false) != null) {
					qryStringBuilder.append(tagName + " LIKE '" + data.getString(null, false).replace(DicomConstants.MULTIPLE_WC_MATCHING, "%") + "' ");
				}
				break;
			// following filter must be only SV matched (DICOM DOCET)
			case Tag.Modality:
			case Tag.PatientID:
			case Tag.RequestedProcedureID:
			case Tag.AccessionNumber:
			case Tag.ScheduledStationAETitle:
			case Tag.CurrentPatientLocation:
				if (data.getString(null, false) != null) {
					qryStringBuilder.append(tagName + " = '" + data.getString(null, false) + "' ");
				}
				break;
			} //switch
		}
		return qryStringBuilder.toString();
	}

	/**
	 * Change the current day to today keeping the information about the hour
	 * 
	 * @param date
	 */
	private void setToday(Date date) {
		Calendar c = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		c.setTime(date);
		c.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
		date.setTime(c.getTimeInMillis());
	}

	private Date combineDateTime(Date date, Date time) {
		Calendar dt = Calendar.getInstance();
		Calendar tm = Calendar.getInstance();
		dt.setTime(date);
		tm.setTime(time);
		tm.set(dt.get(Calendar.YEAR), dt.get(Calendar.MONTH), dt.get(Calendar.DAY_OF_MONTH));
		return tm.getTime();
	}
}
