/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.run.command;

import java.sql.Timestamp;
import java.util.Date;
import java.util.GregorianCalendar;

import org.springframework.stereotype.Component;

import it.csi.dmass.dmassbatchcancpacc.env.Context;
import it.csi.dmass.dmassbatchcancpacc.exception.BatchInEsecuzioneException;
import it.csi.dmass.dmassbatchcancpacc.model.DmassLServiziBatchInfo;
import it.csi.dmass.dmassbatchcancpacc.spring.dao.DmassLServiziBatchInfoDao;
import it.csi.dmass.dmassbatchcancpacc.spring.dao.DmassLServiziBatchInfoDaoImpl;

@Component
public class VerificaBatchInEsecuzione implements Command{

	@Override
	public String step() {
		return "Verifica se il batch e' gia' in esecuzione";
	}

	@Override
	public void execute() throws Exception {

		DmassLServiziBatchInfoDao dmassLServiziBatchInfoDao = Context.getBean(DmassLServiziBatchInfoDaoImpl.class);
		
		Long imr_cancella_pac_timelock = dmassLServiziBatchInfoDao.getImr_cancella_pac_timelock();
		
		Timestamp t_lock_cancella_pacc_data_ins = dmassLServiziBatchInfoDao.getT_lock_cancella_pacc_data_ins();

		if(t_lock_cancella_pacc_data_ins!=null) {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(new Date(t_lock_cancella_pacc_data_ins.getTime()));
			gc.add(GregorianCalendar.MINUTE, imr_cancella_pac_timelock.intValue());		
			
			GregorianCalendar now = new GregorianCalendar();
			now.setTime(new Date());
			
			if(t_lock_cancella_pacc_data_ins!=null && gc.after(now)) {
				
				DmassLServiziBatchInfo dmassLServiziBatchInfo = new DmassLServiziBatchInfo();
				dmassLServiziBatchInfo.setId_ser(Context.getId_ser());
				dmassLServiziBatchInfo.setInfo("Il servizio e' gia' in esecuzione");
				dmassLServiziBatchInfo.setTipo_info("E");
				dmassLServiziBatchInfo.setData_ins(new Timestamp((new Date()).getTime()));	
				
				dmassLServiziBatchInfoDao.create(dmassLServiziBatchInfo);								
				throw new BatchInEsecuzioneException();
			}
		}
			
		dmassLServiziBatchInfoDao.insertBatchInEsecuzione(new Timestamp(System.currentTimeMillis()));
	}

}
