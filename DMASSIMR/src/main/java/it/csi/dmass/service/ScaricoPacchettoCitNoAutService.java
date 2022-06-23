/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import it.csi.dmass.client.CCConsensoINIExtService.CCConsensoINIExtServicePortType;
import it.csi.dmass.client.ScaricoStudiWSBean.AggiornaStatoRichiestaRequest;
import it.csi.dmass.client.ScaricoStudiWSBean.AggiornaStatoRichiestaResponse;
import it.csi.dmass.client.ScaricoStudiWSBean.ScaricoStudiWSBean;
import it.csi.dmass.client.ScaricoStudiWSBean.VerificaStatoRichiesta2Request;
import it.csi.dmass.client.ScaricoStudiWSBean.VerificaStatoRichiesta2Response;
import it.csi.dmass.client.delegaService.dmacc.DelegaService;
import it.csi.dmass.client.utilityService.ApplicativoVerticale;
import it.csi.dmass.client.utilityService.ApplicazioneRichiedente;
import it.csi.dmass.client.utilityService.Errore;
import it.csi.dmass.client.utilityService.ObjectFactory;
import it.csi.dmass.client.utilityService.ParametroAudit;
import it.csi.dmass.client.utilityService.RichiedenteInfo;
import it.csi.dmass.client.utilityService.RisultatoCodice;
import it.csi.dmass.client.utilityService.Ruolo;
import it.csi.dmass.client.utilityService.SetAuditRequest;
import it.csi.dmass.client.utilityService.SetAuditResponse;
import it.csi.dmass.client.utilityService.UtilityService;
import it.csi.dmass.client.verificaService.DatiDocumento;
import it.csi.dmass.client.verificaService.VerificaPinRequest;
import it.csi.dmass.client.verificaService.VerificaPinResponse;
import it.csi.dmass.client.verificaService.VerificaService;
import it.csi.dmass.dao.DmassDRuoloDao;
import it.csi.dmass.dao.DmassLChiamateServiziDao;
import it.csi.dmass.dao.DmassLErroriServiziDao;
import it.csi.dmass.dao.DmassLServiziDao;
import it.csi.dmass.dao.DmassTAuditDao;
import it.csi.dmass.dto.ScaricoPacchettoCitNoAutDto;
import it.csi.dmass.exception.DmassException;
import it.csi.dmass.model.DmassLChiamateServizi;
import it.csi.dmass.model.DmassLErroriServizi;
import it.csi.dmass.model.DmassLServizi;
import it.csi.dmass.model.DmassTAudit;

public class ScaricoPacchettoCitNoAutService  extends BaseService {
	private static Logger log = LoggerFactory.getLogger(ScaricoPacchettoCitNoAutService.class);
	
	
	@Autowired
	DmassLServiziDao dmassLServiziDao;
	
	@Autowired
	DmassLErroriServiziDao dmassLErroriServiziDao;
	
	@Autowired
	DmassLChiamateServiziDao dmassLChiamateServiziDao;
	
	@Autowired
	DmassDRuoloDao dmassDRuoloDao;
	
	@Autowired
	DmassTAuditDao dmassTAuditDao;
	
	@Autowired
	UtilityService utilityServiceProxy;
	
	@Autowired
	VerificaService verificaServiceProxy;
	
	@Autowired
	ScaricoStudiWSBean scaricoStudiWSBeanProxy;
	
	@Autowired
	DelegaService delegaServicesProxy;
	
	@Autowired
	CCConsensoINIExtServicePortType consensoINIExtServicesProxy;

	public DmassLServizi createLog(ScaricoPacchettoCitNoAutDto dto) {
			
		DmassLServizi dmassLServizi = new DmassLServizi();
		dmassLServizi.setId_ser(dmassLServiziDao.getNextVal());
		dmassLServizi.setNome_servizio("scaricoPacchettoCitAutService");
		dmassLServizi.setData_richiesta(new Timestamp(new Date().getTime())); 
		dmassLServizi.setCf_utente(dto.getCfRichiedente());
		dmassLServizi.setCf_assistito(dto.getCfAssistito());		
		dmassLServizi.setApplicazione(dto.getCodApplicazione());
		dmassLServizi.setAppl_verticale(dto.getCodVerticale());
		if(!StringUtils.isEmpty(dto.getCfRichiedente())) {
			dmassLServizi.setRuolo_utente(dto.getCfRichiedente().equalsIgnoreCase(dto.getCfAssistito())?"CIT":"DEL");
		}
		dmassLServiziDao.create(dmassLServizi);
		
		DmassLChiamateServizi dmassLChiamateServizi = new DmassLChiamateServizi();
		dmassLChiamateServizi.setId_ser(dmassLServizi.getId_ser());
		dmassLChiamateServizi.setRequest(dto.getRequest());		
		dmassLChiamateServiziDao.create(dmassLChiamateServizi);				
		
		return dmassLServizi;
	}
	
	public DmassLServizi updateLog(ScaricoPacchettoCitNoAutDto dto) {
		
		DmassLServizi dmassLServizi = new DmassLServizi();
		dmassLServizi.setId_ser(dto.getIdSer());
		dmassLServizi.setCod_esito_risposta_servizio(dto.getCodEsitoRisposta().toString());
		dmassLServiziDao.update(dmassLServizi);
		
		DmassLChiamateServizi dmassLChiamateServizi = new DmassLChiamateServizi();
		dmassLChiamateServizi.setId_ser(dmassLServizi.getId_ser());
		dmassLChiamateServizi.setResponse(dto.getResponse());		
		dmassLChiamateServiziDao.update(dmassLChiamateServizi);
		return dmassLServizi;
	}
	
	public void createLogErrore(ScaricoPacchettoCitNoAutDto dto, Exception e) {

		try {
			DmassLErroriServizi dmassLErroriServizi = new DmassLErroriServizi();
			dmassLErroriServizi.setCod_errore(dto.getCodEsitoRisposta().toString());
			dmassLErroriServizi.setDescr_errore(e.getMessage());
			dmassLErroriServizi.setId_ser(dto.getIdSer());
			dmassLErroriServizi.setTipo_errore("BLOCCANTE");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			if (sw != null) {
				dmassLErroriServizi.setInfo_aggiuntive(sw.toString());
			}
			dmassLErroriServiziDao.create(dmassLErroriServizi);
		} catch (Exception e2) {
			log.warn("[ScaricoPacchettoCitNoAutService::createLogErrore] ", e2);
		}

	}
	
	public void controlli(ScaricoPacchettoCitNoAutDto dto) {
		
		if (StringUtils.isEmpty(dto.getCfAssistito())) {
			throw new DmassException("Codice fiscale dell'assistito non valorizzato");
		}

		if (StringUtils.isEmpty(dto.getIdDocumentoIlec())) {
			throw new DmassException("Identificativo del documento non valorizzato");
		}				
		if (StringUtils.isEmpty(dto.getArchivioDocumentoIlec())) {
			throw new DmassException("Tipo di archivio del documento non valorizzato");
		}
		if (StringUtils.isEmpty(dto.getCodCL())) {
			throw new DmassException("Codice della componente locale non valorizzato");
		}
		if (StringUtils.isEmpty(dto.getCodVerticale())) {
			throw new DmassException("Codice del servizio verticale non valorizzato");
		}		
		if (StringUtils.isEmpty(dto.getPin())) {
			throw new DmassException("Pin non valorizzato");						
		}
		
		try {
			Long.parseLong(dto.getIdDocumentoIlec());
		}catch (Exception e) {
			throw new DmassException("Identificativo del documento non numerico");
		}
	}
	
	public void audit(ScaricoPacchettoCitNoAutDto dto) {
		
		DmassTAudit dmassTAudit = new DmassTAudit();
		dmassTAudit.setCod_audit(dto.getCodiceAudit());
		dmassTAudit.setId_transazione(UUID.randomUUID().toString());
		dmassTAudit.setCf_utente(dto.getCfRichiedente());
		dmassTAudit.setRuolo_utente(dto.getCodRuolo());
		dmassTAudit.setRegime(dto.getCodRegime());
		dmassTAudit.setApplicazione(dto.getCodApplicazione());
		dmassTAudit.setAppl_verticale(dto.getCodVerticale()); 
		dmassTAudit.setIp(dto.getIp());		
		dmassTAudit.setCodice_servizio(dto.getCodiceServizio());
		dmassTAuditDao.create(dmassTAudit);
		
		ObjectFactory factory = new ObjectFactory();
		SetAuditRequest setAuditRequest = factory.createSetAuditRequest();
		setAuditRequest.setCfPaziente(dto.getCfAssistito());
		
		RichiedenteInfo richiedenteInfo = new RichiedenteInfo();
		
		ApplicativoVerticale applicativoVerticale = new ApplicativoVerticale();
		applicativoVerticale.setCodice(dto.getCodVerticale());
		richiedenteInfo.setApplicativoVerticale(applicativoVerticale);
		
		ApplicazioneRichiedente applicazioneRichiedente = new ApplicazioneRichiedente();
		applicazioneRichiedente.setCodice(dto.getCodApplicazione());		
		richiedenteInfo.setApplicazione(applicazioneRichiedente);
		
		richiedenteInfo.setCodiceFiscale(dto.getCfRichiedente());
		richiedenteInfo.setIp(dto.getIp());
		richiedenteInfo.setNumeroTransazione(UUID.randomUUID().toString());
		
		Ruolo ruolo = new Ruolo();
		ruolo.setCodice(dto.getCfRichiedente().equalsIgnoreCase(dto.getCfAssistito())?"CIT":"DEL");
		richiedenteInfo.setRuolo(ruolo);
		
		setAuditRequest.setRichiedente(richiedenteInfo);
		setAuditRequest.setCodiceAudit(dto.getCodiceAudit());		
		
		List<ParametroAudit> listParametroAudit = setAuditRequest.getParametroAudit();
		ParametroAudit parametroAuditCfRichiedente = new ParametroAudit();
		parametroAuditCfRichiedente.setIndice(1);
		parametroAuditCfRichiedente.setValore(dto.getCfRichiedente());
		ParametroAudit parametroAuditCfAssistito = new ParametroAudit();
		parametroAuditCfAssistito.setIndice(2);
		parametroAuditCfAssistito.setValore(dto.getCfAssistito());
		ParametroAudit parametroAuditCodiceDocumento = new ParametroAudit();
		parametroAuditCodiceDocumento.setIndice(3);
		parametroAuditCodiceDocumento.setValore(dto.getIdDocumentoIlec());
		listParametroAudit.add(parametroAuditCfRichiedente);
		listParametroAudit.add(parametroAuditCfAssistito);
		listParametroAudit.add(parametroAuditCodiceDocumento);	
		
		SetAuditResponse response = utilityServiceProxy.setAudit(setAuditRequest);
		if(response.getEsito().equals(RisultatoCodice.FALLIMENTO)) {											
			throw new RuntimeException(String.join(",", response.getErrori().stream().map(Errore::getDescrizione).collect(Collectors.toList())));					
		}		

	}
	
	public VerificaPinResponse verificaPin(ScaricoPacchettoCitNoAutDto dto) {
		
		VerificaPinRequest verificaPinRequest = new VerificaPinRequest();
		it.csi.dmass.client.verificaService.RichiedenteInfo richiedenteInfo = new it.csi.dmass.client.verificaService.RichiedenteInfo();
		richiedenteInfo.setCodiceFiscale(dto.getCfRichiedente());
		it.csi.dmass.client.verificaService.ApplicativoVerticale applicativoVerticale = new it.csi.dmass.client.verificaService.ApplicativoVerticale();
		applicativoVerticale.setCodice(dto.getCodVerticale());		
		richiedenteInfo.setApplicativoVerticale(applicativoVerticale);
		it.csi.dmass.client.verificaService.ApplicazioneRichiedente applicazioneRichiedente = new it.csi.dmass.client.verificaService.ApplicazioneRichiedente();
		applicazioneRichiedente.setCodice(dto.getCodApplicazione());
		richiedenteInfo.setApplicazione(applicazioneRichiedente);		
		it.csi.dmass.client.verificaService.Ruolo ruolo = new it.csi.dmass.client.verificaService.Ruolo();
		ruolo.setCodice(dto.getCodRuolo());
		richiedenteInfo.setRuolo(ruolo);
		richiedenteInfo.setIp(dto.getIp());
		richiedenteInfo.setNumeroTransazione(UUID.randomUUID().toString());		
		verificaPinRequest.setRichiedente(richiedenteInfo);
		DatiDocumento datiDocumento = new DatiDocumento();
		datiDocumento.setCodCL(dto.getCodCL());
		datiDocumento.setCodDocumentoDipartimentale(dto.getCodDocumentoIlec());
		datiDocumento.setIdDocumentoIlec(dto.getIdDocumentoIlec());
		datiDocumento.setPin(dto.getPin());	
		verificaPinRequest.setDatiDocumento(datiDocumento );
									
		try {
			VerificaPinResponse response = verificaServiceProxy.verificaPin(verificaPinRequest);
			return response;
		} catch (Exception e) {
			throw new DmassException("Errore chiamata servizio Verifica Pin");
		}		
		
	}	
	
	public VerificaStatoRichiesta2Response verificaStatoRichiesta2(ScaricoPacchettoCitNoAutDto dto) {
		
		VerificaStatoRichiesta2Request request = new VerificaStatoRichiesta2Request();
		request.setCodiceFiscale(dto.getCfAssistito());
		request.setIdDocumentoIlec(Long.parseLong(dto.getIdDocumentoIlec()));
		request.setCodCL(dto.getCodCL());
		request.setArvchivioDocumentoIlec(dto.getArchivioDocumentoIlec());
		
		VerificaStatoRichiesta2Response response = scaricoStudiWSBeanProxy.verificaStatoRichiesta2(request);
		
		return response;
		
	}
	
	public AggiornaStatoRichiestaResponse aggiornaStatoRichiesta(ScaricoPacchettoCitNoAutDto dto) {
		
		AggiornaStatoRichiestaRequest request = new AggiornaStatoRichiestaRequest();
		request.setCodiceFiscale(dto.getCfRichiedente());
		request.setCodDocumentoDipartimentale(dto.getCodDocumentoIlec());
		request.setCodCL(dto.getCodCL());
		request.setArvchivioDocumentoIlec(dto.getArchivioDocumentoIlec());
		request.setStatoRichiesta(dto.getStatoRichiesta());
		request.setErrore(dto.getErrore());
		
		AggiornaStatoRichiestaResponse response = scaricoStudiWSBeanProxy.aggiornaStatoRichiesta(request);
		
		return response;
		
	}
	
}
