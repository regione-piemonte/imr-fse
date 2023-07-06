/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.movement;

import java.lang.reflect.Constructor;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

public class StudyMovementWorkerFactory {
	
	private static Log log = LogFactory.getLog(StudyMovementWorkerFactory.class);
	
	public static StudyMovementWorker getInstance(String type, Boolean isNearline, Boolean isOffline, Integer daysOnline, Integer daysNearline, NodeList additionalConfig){
		// default configuration instantiates BasicStudyMovementWorker
		StudyMovementWorker ret=null;
		try {
			Class<?> cl=Class.forName(type);
			Constructor<?> con= cl.getConstructor(Boolean.class, Boolean.class, Integer.class, Integer.class, NodeList.class);
			ret=(StudyMovementWorker)con.newInstance(isNearline, isOffline, daysOnline, daysNearline, additionalConfig);
		} catch (ClassNotFoundException cnfex) {
			log.error(type+" NOT FOUND", cnfex);
		} catch (IllegalAccessException iaex) {
			log.error("Reflection not allowed", iaex);
		} catch (InstantiationException iex) {
			log.error("Error instantiating "+type, iex);
		} catch(Exception ex){
			log.error(ex);
		}
		return ret;
		
	}

}
