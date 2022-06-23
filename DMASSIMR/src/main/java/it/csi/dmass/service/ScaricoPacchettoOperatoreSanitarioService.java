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
import it.csi.dmass.client.CCConsensoINIExtService.Paziente;
import it.csi.dmass.client.CCConsensoINIExtService.RegimeDMA;
import it.csi.dmass.client.CCConsensoINIExtService.RichiedenteExt;
import it.csi.dmass.client.CCConsensoINIExtService.RuoloDMA;
import it.csi.dmass.client.CCConsensoINIExtService.StatoConsensiExtRequeste;
import it.csi.dmass.client.CCConsensoINIExtService.StatoConsensiIN;
import it.csi.dmass.client.CCConsensoINIExtService.StatoConsensiResponse;
import it.csi.dmass.client.ScaricoStudiWSBean.AggiornaStatoRichiestaRequest;
import it.csi.dmass.client.ScaricoStudiWSBean.AggiornaStatoRichiestaResponse;
import it.csi.dmass.client.ScaricoStudiWSBean.ScaricoStudiWSBean;
import it.csi.dmass.client.ScaricoStudiWSBean.VerificaStatoRichiesta2Request;
import it.csi.dmass.client.ScaricoStudiWSBean.VerificaStatoRichiesta2Response;
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
import it.csi.dmass.client.verificaService.VerificaUtenteAbilitatoRequest;
import it.csi.dmass.client.verificaService.VerificaUtenteAbilitatoResponse;
import it.csi.dmass.dao.DmassDRuoloDao;
import it.csi.dmass.dao.DmassLChiamateServiziDao;
import it.csi.dmass.dao.DmassLErroriServiziDao;
import it.csi.dmass.dao.DmassLServiziDao;
import it.csi.dmass.dao.DmassTAuditDao;
import it.csi.dmass.dto.ScaricaPacchettoOperatoreSanitarioDto;
import it.csi.dmass.exception.DmassException;
import it.csi.dmass.model.DmassLChiamateServizi;
import it.csi.dmass.model.DmassLErroriServizi;
import it.csi.dmass.model.DmassLServizi;
import it.csi.dmass.model.DmassTAudit;

public class ScaricoPacchettoOperatoreSanitarioService  extends BaseService {
	
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
	CCConsensoINIExtServicePortType consensoINIExtServicesProxy;

	public DmassLServizi createLog(ScaricaPacchettoOperatoreSanitarioDto dto) {
			
		DmassLServizi dmassLServizi = new DmassLServizi();
		dmassLServizi.setId_ser(dmassLServiziDao.getNextVal());
		dmassLServizi.setNome_servizio("ScaricoPacchettoOperatoreSanitario");
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
	
	public DmassLServizi updateLog(ScaricaPacchettoOperatoreSanitarioDto dto) {
		
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
	
	public void createLogErrore(ScaricaPacchettoOperatoreSanitarioDto dto,Exception e) {
		
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
	
	public void controlli(ScaricaPacchettoOperatoreSanitarioDto dto) {
		
		if (StringUtils.isEmpty(dto.getCfAssistito())) {
			throw new DmassException("Codice fiscale dell'assistito non valorizzato: "+dto.getCfAssistito());
		}

		if (StringUtils.isEmpty(dto.getIdDocumentoIlec())) {
			throw new DmassException("Identificativo del documento non valorizzato: "+dto.getIdDocumentoIlec());
		}		
		if (StringUtils.isEmpty(dto.getCodCL())) {
			throw new DmassException("Codice della componente locale non valorizzato: "+dto.getCodCL());
		}
		if (StringUtils.isEmpty(dto.getCodVerticale())) {
			throw new DmassException(" Codice del servizio verticale non valorizzato:"+dto.getCodVerticale());
		}
		if (StringUtils.isEmpty(dto.getPin())) {
			throw new DmassException("pin non valorizzato: "+dto.getPin());
		}
		
		try {
			Long.parseLong(dto.getIdDocumentoIlec());
		}catch (Exception e) {
			throw new DmassException("Identificativo del documento non numerico: "+dto.getIdDocumentoIlec());
		}
	}
	
	public void audit(ScaricaPacchettoOperatoreSanitarioDto dto) {
		
		DmassTAudit dmassTAudit = new DmassTAudit();
		dmassTAudit.setCod_audit(dto.getCodiceAudit());
		dmassTAudit.setId_transazione(UUID.randomUUID().toString());
		dmassTAudit.setCf_utente(dto.getCfRichiedente());
		dmassTAudit.setRuolo_utente(dto.getCodRuolo());
		dmassTAudit.setRegime(dto.getCodRegime());
		dmassTAudit.setApplicazione(dto.getCodApplicazione());
		dmassTAudit.setAppl_verticale(dto.getCodVerticale()); 
		dmassTAudit.setIp(dto.getIp());
		if(dto.getIdCollocazione()!=null) {
			dmassTAudit.setId_collocazione(Long.parseLong(dto.getIdCollocazione()));
		}
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
		ruolo.setCodice(dto.getCodRuolo());
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
	
	public VerificaStatoRichiesta2Response verificaStatoRichiesta2(ScaricaPacchettoOperatoreSanitarioDto dto) {
		
		VerificaStatoRichiesta2Request request = new VerificaStatoRichiesta2Request();
		request.setCodiceFiscale(dto.getCfAssistito());
		request.setIdDocumentoIlec(Long.parseLong(dto.getIdDocumentoIlec()));
		request.setCodCL(dto.getCodCL());
		request.setArvchivioDocumentoIlec(dto.getArchivioDocumentoIlec());
		
		VerificaStatoRichiesta2Response response = scaricoStudiWSBeanProxy.verificaStatoRichiesta2(request);
		
		return response;
		
	}
	
	public StatoConsensiResponse statoConsensi(ScaricaPacchettoOperatoreSanitarioDto dto) {
		
		
		StatoConsensiExtRequeste request = new StatoConsensiExtRequeste();
					
		Paziente paziente = new Paziente();
		paziente.setCodiceFiscale(dto.getCfAssistito());
		request.setPaziente(paziente);
		
		RichiedenteExt richiedente = new RichiedenteExt();		
		it.csi.dmass.client.CCConsensoINIExtService.ApplicazioneRichiedente applicazioneRichiedente = new it.csi.dmass.client.CCConsensoINIExtService.ApplicazioneRichiedente();
		applicazioneRichiedente.setCodice(dto.getCodApplicazione());
		richiedente.setApplicazione(applicazioneRichiedente);		
		richiedente.setCodiceFiscale(dto.getCfRichiedente());
		String uuid = UUID.randomUUID().toString();
		richiedente.setNumeroTransazione(uuid);				
		RegimeDMA regimeDMA = new RegimeDMA();
		regimeDMA.setCodice(dto.getCodRegime());
		richiedente.setRegime(regimeDMA);		
		RuoloDMA ruoloDMA = new RuoloDMA();
		ruoloDMA.setCodice(dmassDRuoloDao.getCodiceRuoloIni(dto.getCodRuolo()));		
		richiedente.setRuolo(ruoloDMA);			
		richiedente.setTokenOperazione(uuid);
		request.setRichiedente(richiedente);
		
		StatoConsensiIN statoConsensiIn = new StatoConsensiIN();
		statoConsensiIn.setTipoAttivita("READ");
		statoConsensiIn.setStrutturaUtente("------");//????
		statoConsensiIn.setIdentificativoAssistitoConsenso(dto.getCfAssistito());
		statoConsensiIn.setIdentificativoAssistitoGenitoreTutore(dto.getCfAssistito());
		statoConsensiIn.setIdentificativoOrganizzazione("010");
		statoConsensiIn.setIdentificativoUtente(dto.getCfAssistito());
		request.setStatoConsensiIN(statoConsensiIn);
		
		int numeroChiamate = 0;
		int NUM_MAX_TENTATIVI_INI_PROM = 0;
		return chiamaConsensoINI(request, numeroChiamate, NUM_MAX_TENTATIVI_INI_PROM);
	
	}

	private StatoConsensiResponse chiamaConsensoINI(StatoConsensiExtRequeste request, int numeroChiamate, int NUM_MAX_TENTATIVI_INI_PROM) {		
		try {					
			StatoConsensiResponse response = consensoINIExtServicesProxy.statoConsensi(request);
			return response;
		}catch (Exception e) {
			if(numeroChiamate<NUM_MAX_TENTATIVI_INI_PROM) {
				numeroChiamate++;
				return chiamaConsensoINI(request, numeroChiamate, NUM_MAX_TENTATIVI_INI_PROM);
			}else {
				//TODO log
				throw new DmassException("Errore chiamata INI: "+e);//TODO codificare
			}
		}
	}
	
	public VerificaUtenteAbilitatoResponse verificaUtenteAbilitatoFSE(ScaricaPacchettoOperatoreSanitarioDto dto) {
			
		VerificaUtenteAbilitatoRequest verificaUtenteAbilitatoRequest = new VerificaUtenteAbilitatoRequest();
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
		verificaUtenteAbilitatoRequest.setRichiedente(richiedenteInfo);
		
		VerificaUtenteAbilitatoResponse verificaUtenteAbilitatoResponse = verificaServiceProxy.verificaUtenteAbilitato(verificaUtenteAbilitatoRequest);
		
		return verificaUtenteAbilitatoResponse;
						
	}
	
	public VerificaOscuramentoDocResponse verificaOscuramentoDoc(ScaricaPacchettoOperatoreSanitarioDto dto) {
	
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
		datiDocumento.setCodDocumentoDipartimentale(dto.getCodiceDocumentoIlec());
		verificaOscuramentoDocRequest.setDatiDocumento(datiDocumento );		
		VerificaOscuramentoDocResponse verificaOscuramentoDocResponse = verificaServiceProxy.verificaOscuramentoDoc(verificaOscuramentoDocRequest);
		return verificaOscuramentoDocResponse;
	}
	
	public AggiornaStatoRichiestaResponse aggiornaStatoRichiesta(ScaricaPacchettoOperatoreSanitarioDto dto) {
		
		AggiornaStatoRichiestaRequest request = new AggiornaStatoRichiestaRequest();
		request.setCodiceFiscale(dto.getCfRichiedente());
		request.setCodDocumentoDipartimentale(dto.getCodiceDocumentoIlec());
		request.setCodCL(dto.getCodCL());
		request.setArvchivioDocumentoIlec(dto.getArchivioDocumentoIlec());
		request.setStatoRichiesta(dto.getStatoRichiesta());
		request.setErrore(dto.getErrore());
		
		AggiornaStatoRichiestaResponse response = scaricoStudiWSBeanProxy.aggiornaStatoRichiesta(request);
		
		return response;
		
	}
	
}
