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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import it.csi.dmass.client.CCConsensoINIExtService.CCConsensoINIExtServicePortType;
import it.csi.dmass.client.ScaricoStudiWSBean.AggiornaStatoRichiestaRequest;
import it.csi.dmass.client.ScaricoStudiWSBean.AggiornaStatoRichiestaResponse;
import it.csi.dmass.client.ScaricoStudiWSBean.ScaricoStudiWSBean;
import it.csi.dmass.client.ScaricoStudiWSBean.VerificaStatoRichiesta2Request;
import it.csi.dmass.client.ScaricoStudiWSBean.VerificaStatoRichiesta2Response;
import it.csi.dmass.client.delegaService.dma.CittadinoDelegante;
import it.csi.dmass.client.delegaService.dma.CittadinoDelegato;
import it.csi.dmass.client.delegaService.dma.GetDeleganti2IN;
import it.csi.dmass.client.delegaService.dmacc.DelegaService;
import it.csi.dmass.client.delegaService.dmacc.GetDeleganti2Request;
import it.csi.dmass.client.delegaService.dmacc.GetDeleganti2Response;
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
import it.csi.dmass.client.verificaService.VerificaOscuramentoDocRequest;
import it.csi.dmass.client.verificaService.VerificaOscuramentoDocResponse;
import it.csi.dmass.client.verificaService.VerificaService;
import it.csi.dmass.dao.DmassDRuoloDao;
import it.csi.dmass.dao.DmassLChiamateServiziDao;
import it.csi.dmass.dao.DmassLErroriServiziDao;
import it.csi.dmass.dao.DmassLServiziDao;
import it.csi.dmass.dao.DmassTAuditDao;
import it.csi.dmass.dto.ScaricoPacchettoCitAutDto;
import it.csi.dmass.exception.DmassException;
import it.csi.dmass.model.DmassLChiamateServizi;
import it.csi.dmass.model.DmassLErroriServizi;
import it.csi.dmass.model.DmassLServizi;
import it.csi.dmass.model.DmassTAudit;

public class ScaricoPacchettoCitAutService  extends BaseService {
	
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

	public DmassLServizi createLog(ScaricoPacchettoCitAutDto dto) {
			
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
	
	public DmassLServizi updateLog(ScaricoPacchettoCitAutDto dto) {
		
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
	
	public void createLogErrore(ScaricoPacchettoCitAutDto dto,Exception e) {
		
		DmassLErroriServizi dmassLErroriServizi = new DmassLErroriServizi();
		dmassLErroriServizi.setCod_errore(dto.getCodEsitoRisposta().toString());
		dmassLErroriServizi.setDescr_errore(StringUtils.isEmpty(e.getMessage())?"errore generico":e.getMessage());
		dmassLErroriServizi.setId_ser(dto.getIdSer());
		dmassLErroriServizi.setTipo_errore("BLOCCANTE");
		StringWriter sw = new StringWriter();
    	PrintWriter pw = new PrintWriter(sw);
    	e.printStackTrace(pw);
    	if(sw!=null) {
    		dmassLErroriServizi.setInfo_aggiuntive(sw.toString());
    	}
						
		dmassLErroriServiziDao.create(dmassLErroriServizi);
		
	}
	
	public void controlli(ScaricoPacchettoCitAutDto dto) {
		
		if (StringUtils.isEmpty(dto.getCfAssistito())) {
			throw new DmassException("Codice fiscale dell'assistito non valorizzato: "+dto.getCfAssistito());
		}

		if (StringUtils.isEmpty(dto.getIdDocumentoIlec())) {
			throw new DmassException("Identificativo del documento non valorizzato: "+dto.getIdDocumentoIlec());
		}				
		if (StringUtils.isEmpty(dto.getArchivioDocumentoIlec())) {
			throw new DmassException("Tipo di archivio del documento non valorizzato: "+dto.getArchivioDocumentoIlec());
		}
		if (StringUtils.isEmpty(dto.getCodCL())) {
			throw new DmassException("Codice della componente locale non valorizzato: "+dto.getCodCL());
		}
		if (StringUtils.isEmpty(dto.getCodVerticale())) {
			throw new DmassException(" Codice del servizio verticale non valorizzato: "+dto.getCodVerticale());
		}		
		
		try {
			Long.parseLong(dto.getIdDocumentoIlec());
		}catch (Exception e) {
			throw new DmassException("Identificativo del documento non numerico: "+dto.getIdDocumentoIlec());
		}
	}
	
	public void audit(ScaricoPacchettoCitAutDto dto) {
		
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
	
	public VerificaStatoRichiesta2Response verificaStatoRichiesta2(ScaricoPacchettoCitAutDto dto) {
		
		VerificaStatoRichiesta2Request request = new VerificaStatoRichiesta2Request();
		request.setCodiceFiscale(dto.getCfAssistito());
		request.setIdDocumentoIlec(Long.parseLong(dto.getIdDocumentoIlec()));
		request.setCodCL(dto.getCodCL());
		request.setArvchivioDocumentoIlec(dto.getArchivioDocumentoIlec());
		
		VerificaStatoRichiesta2Response response = scaricoStudiWSBeanProxy.verificaStatoRichiesta2(request);
		
		return response;
		
	}
	
	public GetDeleganti2Response getDeleganti2(ScaricoPacchettoCitAutDto dto) {
		
		GetDeleganti2Request request = new GetDeleganti2Request();

		//// Set DELEGANTI2IN
		GetDeleganti2IN getDeleganti2IN = new GetDeleganti2IN();

		CittadinoDelegante cittadinoDelegante = new CittadinoDelegante();
		cittadinoDelegante.setCodiceFiscale(dto.getCfAssistito());
		getDeleganti2IN.setCittadinoDelegante(cittadinoDelegante);

		CittadinoDelegato cittadinoDelegato = new CittadinoDelegato();
		cittadinoDelegato.setCodiceFiscale(dto.getCfRichiedente());
		getDeleganti2IN.setCittadinoDelegato(cittadinoDelegato);

		getDeleganti2IN.setStatoDelega("ATTIVO");

		request.setGetDeleganti2IN(getDeleganti2IN);

		//// Set RICHIEDENTE
		it.csi.dmass.client.delegaService.dma.RichiedenteInfo richiedente = new it.csi.dmass.client.delegaService.dma.RichiedenteInfo();
		it.csi.dmass.client.delegaService.dmacc.ApplicazioneRichiedente applicazioneRichiedente = new it.csi.dmass.client.delegaService.dmacc.ApplicazioneRichiedente();
		applicazioneRichiedente.setCodice(dto.getCodApplicazione());
		richiedente.setApplicazione(applicazioneRichiedente);

//		it.csi.dmass.client.delegaService.dmacc.ApplicativoVerticale applicativoVerticale = new it.csi.dmass.client.delegaService.dmacc.ApplicativoVerticale();
//		applicativoVerticale.setCodice(dto.getCodVerticale());
//		richiedente.setApplicativoVerticale(applicativoVerticale);

		it.csi.dmass.client.delegaService.dma.Ruolo ruolo = new it.csi.dmass.client.delegaService.dma.Ruolo();
		ruolo.setCodice(dto.getCodRuolo());
		richiedente.setRuolo(ruolo);

		richiedente.setCodiceFiscale(dto.getCfRichiedente());

		richiedente.setIp(dto.getIp());

		richiedente.setNumeroTransazione(UUID.randomUUID().toString());

		request.setRichiedente(richiedente);

		GetDeleganti2Response response = null;
		try {
			response = delegaServicesProxy.getDeleganti2(request);
		} catch (Exception e) {
			throw new DmassException("Errore chiamata servizio Delega");
		}


		return response;
		
	}
	
	public VerificaOscuramentoDocResponse verificaOscuramentoDoc(ScaricoPacchettoCitAutDto dto) {
	
		VerificaOscuramentoDocRequest verificaOscuramentoDocRequest = new VerificaOscuramentoDocRequest();
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
		verificaOscuramentoDocRequest.setRichiedente(richiedenteInfo);
		DatiDocumento datiDocumento = new DatiDocumento();
		datiDocumento.setCodCL(dto.getCodCL());
		datiDocumento.setCodDocumentoDipartimentale(dto.getCodDocumentoIlec());
		verificaOscuramentoDocRequest.setDatiDocumento(datiDocumento );		
		VerificaOscuramentoDocResponse verificaOscuramentoDocResponse = verificaServiceProxy.verificaOscuramentoDoc(verificaOscuramentoDocRequest);
		return verificaOscuramentoDocResponse;
	}
	
	public AggiornaStatoRichiestaResponse aggiornaStatoRichiesta(ScaricoPacchettoCitAutDto dto) {
		
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
