/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.run.command;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.dmass.dmassbatchcancpacc.client.ScaricoStudiWSBean.CancellaPacchettoRequest;
import it.csi.dmass.dmassbatchcancpacc.client.ScaricoStudiWSBean.CancellaPacchettoResponse;
import it.csi.dmass.dmassbatchcancpacc.client.ScaricoStudiWSBean.ElencoPacchetti;
import it.csi.dmass.dmassbatchcancpacc.client.ScaricoStudiWSBean.GetElencoPacchettiScadutiRequest;
import it.csi.dmass.dmassbatchcancpacc.client.ScaricoStudiWSBean.GetElencoPacchettiScadutiResponse;
import it.csi.dmass.dmassbatchcancpacc.client.ScaricoStudiWSBean.RisultatoCodice;
import it.csi.dmass.dmassbatchcancpacc.client.ScaricoStudiWSBean.ScaricoStudiWSBean;
import it.csi.dmass.dmassbatchcancpacc.env.Context;
import it.csi.dmass.dmassbatchcancpacc.exception.CancellaPacchettiException;
import it.csi.dmass.dmassbatchcancpacc.exception.GetElencoPacchettiScadutiException;
import it.csi.dmass.dmassbatchcancpacc.model.DmassLServiziBatchInfo;
import it.csi.dmass.dmassbatchcancpacc.spring.dao.DmassLServiziBatchInfoDao;
import it.csi.dmass.dmassbatchcancpacc.spring.dao.DmassLServiziBatchInfoDaoImpl;

@Component
public class CancellaPacchetti implements Command {
	
	@Autowired
	ScaricoStudiWSBean scaricoStudiWSBeanProxy;

	@Override
	public String step() {		
		return "Cancella pacchetti";
	}

	@Override
	public void execute() throws Exception {
		
		
		GetElencoPacchettiScadutiRequest getElencoPacchettiScadutiRequest = new GetElencoPacchettiScadutiRequest();		
		GetElencoPacchettiScadutiResponse getElencoPacchettiScadutiResponse = scaricoStudiWSBeanProxy.getElencoPacchettiScaduti(getElencoPacchettiScadutiRequest);
		
		if(getElencoPacchettiScadutiResponse==null || RisultatoCodice.FALLIMENTO.equals(getElencoPacchettiScadutiResponse.getEsito())) {
			
			DmassLServiziBatchInfoDao dmassLServiziBatchInfoDao = Context.getBean(DmassLServiziBatchInfoDaoImpl.class);			
			DmassLServiziBatchInfo dmassLServiziBatchInfo = new DmassLServiziBatchInfo(); 
			dmassLServiziBatchInfo.setId_ser(Context.getId_ser());
			dmassLServiziBatchInfo.setInfo("Chiamata a getElencoPacchettiScaduti con errori");
			dmassLServiziBatchInfo.setInfo_dettaglio(String.join(",", getElencoPacchettiScadutiResponse.getErrori().
					stream().map( e -> e.getCodice()+": "+e.getDescrizione()).
					collect(Collectors.toList())));
			dmassLServiziBatchInfo.setTipo_info("E");
			dmassLServiziBatchInfo.setData_ins(new Timestamp(System.currentTimeMillis()));
			dmassLServiziBatchInfoDao.create(dmassLServiziBatchInfo);
			
			throw new GetElencoPacchettiScadutiException();
			
		}
		
		DmassLServiziBatchInfoDao dmassLServiziBatchInfoDao = Context.getBean(DmassLServiziBatchInfoDaoImpl.class);		
		for (ElencoPacchetti elencoPacchetti : getElencoPacchettiScadutiResponse.getElencoPacchetti()) {
			
			//4) Cancella pacchetti
			try {
				File file = new File(elencoPacchetti.getDirectory()+elencoPacchetti.getZipName());			
				if(file.exists() && !file.delete()) {
					throw new Exception("impossibile cancellare il file: "+elencoPacchetti.getDirectory()+elencoPacchetti.getZipName());
				}
				
				//5) Call DMASS.setPacchettoCancellato
				try {
					CancellaPacchettoRequest cancellaPacchettoRequest = new CancellaPacchettoRequest();
					cancellaPacchettoRequest.setIdPacchetto(elencoPacchetti.getIdRichiestaScarico());			
					CancellaPacchettoResponse cancellaPacchettoResponse = scaricoStudiWSBeanProxy.cancellaPacchetto(cancellaPacchettoRequest);
					
					if(RisultatoCodice.FALLIMENTO.equals(cancellaPacchettoResponse.getEsito())) {									
						throw new CancellaPacchettiException(cancellaPacchettoResponse.getErrori());					
					}				
									
				}catch(Exception e) {
					DmassLServiziBatchInfo dmassLServiziBatchInfo = new DmassLServiziBatchInfo();
					dmassLServiziBatchInfo.setId_ser(Context.getId_ser());
					dmassLServiziBatchInfo.setInfo("Chiamata a setPacchettoCancellato con errori");
					dmassLServiziBatchInfo.setInfo_dettaglio(e.getMessage());
					dmassLServiziBatchInfo.setTipo_info("E");
					dmassLServiziBatchInfo.setData_ins(new Timestamp((new Date()).getTime()));					
					dmassLServiziBatchInfoDao.create(dmassLServiziBatchInfo);
					Context.setStatoFine("E");
				}
			
			}catch(Exception e) {
				DmassLServiziBatchInfo dmassLServiziBatchInfo = new DmassLServiziBatchInfo();
				dmassLServiziBatchInfo.setId_ser(Context.getId_ser());
				dmassLServiziBatchInfo.setInfo("Cancellazione del pacchetto id="+elencoPacchetti.getIdRichiestaScarico() + " fallita");
				dmassLServiziBatchInfo.setInfo_dettaglio(e.getMessage());
				dmassLServiziBatchInfo.setTipo_info("E");
				dmassLServiziBatchInfo.setData_ins(new Timestamp((new Date()).getTime()));					
				dmassLServiziBatchInfoDao.create(dmassLServiziBatchInfo);	
				Context.setStatoFine("E");
			}						
			
		} 
				
	}

}
