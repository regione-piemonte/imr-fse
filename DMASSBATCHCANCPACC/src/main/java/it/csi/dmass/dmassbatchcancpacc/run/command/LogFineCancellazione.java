/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.run.command;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.stereotype.Component;

import it.csi.dmass.dmassbatchcancpacc.env.Context;
import it.csi.dmass.dmassbatchcancpacc.model.DmassLServiziBatch;
import it.csi.dmass.dmassbatchcancpacc.model.DmassLServiziBatchInfo;
import it.csi.dmass.dmassbatchcancpacc.spring.dao.DmassLServiziBatchDao;
import it.csi.dmass.dmassbatchcancpacc.spring.dao.DmassLServiziBatchDaoImpl;
import it.csi.dmass.dmassbatchcancpacc.spring.dao.DmassLServiziBatchInfoDao;
import it.csi.dmass.dmassbatchcancpacc.spring.dao.DmassLServiziBatchInfoDaoImpl;

@Component
public class LogFineCancellazione implements Command {

	@Override
	public String step() {		
		return "Log fine cancellazione";
	}

	@Override
	public void execute() throws Exception {

		DmassLServiziBatchDao dmassLServiziBatchDao = Context.getBean(DmassLServiziBatchDaoImpl.class);
		DmassLServiziBatchInfoDao dmassLServiziBatchInfoDao = Context.getBean(DmassLServiziBatchInfoDaoImpl.class);
		
		DmassLServiziBatchInfo dmassLServiziBatchInfo = new DmassLServiziBatchInfo();
		dmassLServiziBatchInfo.setId_ser(Context.getId_ser());
		dmassLServiziBatchInfo.setInfo("Fine cancellazione pacchetti scaduti");
		dmassLServiziBatchInfo.setTipo_info("I");
		dmassLServiziBatchInfo.setData_ins(new Timestamp((new Date()).getTime()));						
		dmassLServiziBatchInfoDao.create(dmassLServiziBatchInfo);
		
		DmassLServiziBatch dmassLServiziBatch = new DmassLServiziBatch();
		dmassLServiziBatch.setStato_fine(Context.getStatoFine());
		dmassLServiziBatch.setData_fine(new Timestamp((new Date()).getTime()));
		dmassLServiziBatch.setId_ser(Context.getId_ser());
		dmassLServiziBatchDao.update(dmassLServiziBatch);

		//rimuovo lock				
		dmassLServiziBatchInfoDao.delete();
		
						
	}

}
