/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.helpers;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import eu.o3c.o3e.flex.dao.DbDealer;
import eu.o3c.o3e.flex.utils.OperatorDetail;
import eu.o3c.o3e.flex.utils.OperatorStat;

public class OperatorsStatsHandler {
    static Logger log = Logger.getLogger(OperatorsStatsHandler.class);
    
    
    
    
    public ArrayList<OperatorStat>  getOpeStats(Date startDate, Date endDate) throws Exception{
        DbDealer dbReader = new DbDealer();
        return dbReader.getOperatorsStats(startDate, endDate);
    }
    
    
    public ArrayList<OperatorDetail> getOperatorDetails(String operatorName, Date startDate, Date endDate) throws Exception{
        
        DbDealer dbDealer = new DbDealer();
        return dbDealer.getOperatorDetails(operatorName, startDate, endDate);
    }
}
