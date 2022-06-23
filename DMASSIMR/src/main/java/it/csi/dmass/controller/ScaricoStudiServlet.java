/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import it.csi.dmass.client.CCConsensoINIExtService.StatoConsensiResponse;
import it.csi.dmass.client.ScaricoStudiWSBean.VerificaStatoRichiesta2Response;
import it.csi.dmass.client.delegaService.dma.Delegante;
import it.csi.dmass.client.delegaService.dmacc.Delega;
import it.csi.dmass.client.delegaService.dmacc.GetDeleganti2Response;
import it.csi.dmass.client.delegaService.dmacc.RisultatoCodice;
import it.csi.dmass.client.verificaService.Errore;
import it.csi.dmass.client.verificaService.VerificaOscuramentoDocResponse;
import it.csi.dmass.client.verificaService.VerificaPinResponse;
import it.csi.dmass.client.verificaService.VerificaUtenteAbilitatoResponse;
import it.csi.dmass.config.Costanti;
import it.csi.dmass.dto.ScaricaPacchettoOperatoreSanitarioDto;
import it.csi.dmass.dto.ScaricoPacchettoCitAutDto;
import it.csi.dmass.dto.ScaricoPacchettoCitNoAutDto;
import it.csi.dmass.exception.DmassException;
import it.csi.dmass.model.DmassLServizi;
import it.csi.dmass.service.ScaricoPacchettoCitAutService;
import it.csi.dmass.service.ScaricoPacchettoCitNoAutService;
import it.csi.dmass.service.ScaricoPacchettoOperatoreSanitarioService;

@RestController
public class ScaricoStudiServlet {
	
	private static Logger log = LoggerFactory.getLogger(ScaricoStudiServlet.class);

	@Autowired
	@Qualifier("scaricoPacchettoOperatoreSanitarioService")
	private ScaricoPacchettoOperatoreSanitarioService scaricoPacchettoOperatoreSanitarioService;
	
	@Autowired
	@Qualifier("scaricoPacchettoCitAutService")
	ScaricoPacchettoCitAutService scaricoPacchettoCitAutService;
	
	@Autowired
	@Qualifier("scaricoPacchettoCitNoAutService")
	ScaricoPacchettoCitNoAutService scaricoPacchettoCitNoAutService;
	
	@RequestMapping(value = "/scaricoPacchettoCitNoAut", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> scaricoPacchettoCitNoAut(@RequestParam("cfRichiedente") final String cfRichiedente,
			@RequestParam("cfAssistito") final String cfAssistito, @RequestParam("idDocumentoIlec") final String idDocumentoIlec,
			@RequestParam("codCL") final String codCL,@RequestParam("archivioDocumentoIlec") final String archivioDocumentoIlec, 
			@RequestParam("codApplicazione") final String codApplicazione, @RequestParam("codVerticale") final String codVerticale,
			@RequestParam("codRuolo") final String codRuolo,@RequestParam("pin") final String pin,
			HttpServletRequest request, HttpServletResponse response) {
		
		ScaricoPacchettoCitNoAutDto dto = new ScaricoPacchettoCitNoAutDto();
				
		try {
			
			if(StringUtils.isEmpty(pin)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("access denided");
			}
			
			dto.setCfAssistito(cfAssistito);
			dto.setCfRichiedente(cfRichiedente);
			dto.setCodApplicazione(codApplicazione);
			dto.setCodCL(codCL);			
			dto.setCodVerticale(codVerticale);			
			dto.setIdDocumentoIlec(idDocumentoIlec);
			dto.setIp(request.getRemoteAddr());
			dto.setArchivioDocumentoIlec(archivioDocumentoIlec);
			dto.setCodRuolo(codRuolo);
			dto.setPin(pin);
			
			dto.setRequest(request.getRequestURL()+"?"+request.getQueryString());
			
			DmassLServizi dmassLServizi = scaricoPacchettoCitNoAutService.createLog(dto);
			dto.setIdSer(dmassLServizi.getId_ser());
			
			scaricoPacchettoCitNoAutService.controlli(dto);
						
			VerificaStatoRichiesta2Response verificaStatoRichiesta2Response = scaricoPacchettoCitNoAutService.verificaStatoRichiesta2(dto);		
			String statoRichiesta = verificaStatoRichiesta2Response.getStatoRichiesta();
			dto.setCodDocumentoIlec(verificaStatoRichiesta2Response.getCodDocumentoDipartimentale());
			
			VerificaPinResponse verificaPinResponse = scaricoPacchettoCitNoAutService.verificaPin(dto);
			 
			if(it.csi.dmass.client.verificaService.RisultatoCodice.FALLIMENTO.equals(verificaPinResponse.getEsito())) {
				List<Errore> errori = verificaPinResponse.getErrori();
				StringBuffer erroriBuffer = new StringBuffer(); 
				for (Errore errore : errori) {
					erroriBuffer.append(errore.getDescrizione());
				}
				dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);	
				throw new DmassException("Errore servizio delega: "+String.join(",", erroriBuffer));
			}else {							
				
				if(Costanti.STATO_PACCHETTO_NOTIFICATO.equalsIgnoreCase(statoRichiesta) ||				
						Costanti.STATO_PACCHETTO_DISPONIBILE.equalsIgnoreCase(statoRichiesta) ||
						Costanti.STATO_PACCHETTO_INVIO_MAIL_IN_CORSO.equalsIgnoreCase(statoRichiesta) ||
						Costanti.STATO_PACCHETTO_ERRORE_INVIO_MAIL.equalsIgnoreCase(statoRichiesta) ||
						Costanti.STATO_PACCHETTO_SCARICATO.equalsIgnoreCase(statoRichiesta)) {
					
					String pathFile = verificaStatoRichiesta2Response.getDirectory().replaceAll("2F", "/")+verificaStatoRichiesta2Response.getZipName();
					File file = new File(pathFile);
					//8) Ricerca pacchetto su file system
					if(file.exists()) {							
						//9) Restituzione stream pacchetto
						scaricaZip(response, pathFile, verificaStatoRichiesta2Response.getZipName());
						
						//10) Aggiorna stato pacchetto sulla richiesta
						dto.setStatoRichiesta("SCARICATO");
						scaricoPacchettoCitNoAutService.aggiornaStatoRichiesta(dto);
						
						//11) Audit
						dto.setCodiceAudit(Costanti.IMR_SCA_PAC_NA);
						scaricoPacchettoCitNoAutService.audit(dto);
						
						//12) aggiorna log successo
						dto.setCodEsitoRisposta(HttpStatus.OK);
						scaricoPacchettoCitNoAutService.updateLog(dto);
						
					}else {
						//aggiorna stato richiesta
						dto.setErrore("File zip dello studio non trovato");
						dto.setStatoRichiesta(Costanti.STATO_ERRORE_COMPONI_PACCHETTO);
						scaricoPacchettoCitNoAutService.aggiornaStatoRichiesta(dto);							
						dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);																		
						throw new DmassException("File non trovato: "+pathFile);
					}
					
				}else {
					if(Costanti.STATO_RICHIESTA_PACCHETTO_NON_PRESENTE.equalsIgnoreCase(statoRichiesta)) {
						dto.setCodEsitoRisposta(HttpStatus.NOT_FOUND);
						throw new DmassException("Richiesta non presente");
						
					}else {
						dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);
						throw new DmassException("Pacchetto non scaricabile");
					}
				}
				
			}
			
			return ResponseEntity.status(HttpStatus.OK)
					.body("");
			
		}catch (Exception e) {
			log.error("scaricoPAcchettoCitNoAut",e);
			//aggiornamento log su errore
			if(dto.getCodEsitoRisposta()!=null) {
				scaricoPacchettoCitNoAutService.updateLog(dto);
				scaricoPacchettoCitNoAutService.createLogErrore(dto, e);
				return ResponseEntity.status(dto.getCodEsitoRisposta())
						.body("errore generico");
			}else {
				dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);
				scaricoPacchettoCitNoAutService.updateLog(dto);
				scaricoPacchettoCitNoAutService.createLogErrore(dto, e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("errore generico");
			}
		}
				
	}
	
	@RequestMapping(value = "/scaricoPacchettoCitAut", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<String> ScaricoPacchettoCitAut(@RequestParam("cfAssistito") final String cfAssistito, 
			@RequestParam("idDocumentoIlec") final String idDocumentoIlec,
			@RequestParam("codCL") final String codCL,@RequestParam("archivioDocumentoIlec") final String archivioDocumentoIlec, 
			@RequestParam("codApplicazione") final String codApplicazione, @RequestParam("codVerticale") final String codVerticale,
			@RequestParam("codRuolo") final String codRuolo,
			HttpServletRequest request, HttpServletResponse response) {
		
		ScaricoPacchettoCitAutDto dto = new ScaricoPacchettoCitAutDto();
		
		try {
			
			String cfRichiedente = request.getHeader("Shib-Identita-CodiceFiscale");
			
			if(StringUtils.isEmpty(cfRichiedente)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("access denided");
			}
			
			dto.setCfAssistito(cfAssistito);
			dto.setCfRichiedente(cfRichiedente);
			dto.setCodApplicazione(codApplicazione);
			dto.setCodCL(codCL);			
			dto.setCodVerticale(codVerticale);			
			dto.setIdDocumentoIlec(idDocumentoIlec);
			dto.setIp(request.getRemoteAddr());
			dto.setArchivioDocumentoIlec(archivioDocumentoIlec);
			dto.setCodRuolo(codRuolo);
			dto.setIp(request.getRemoteAddr());
			
			dto.setRequest(request.getRequestURL()+"?"+request.getQueryString());
			
			DmassLServizi dmassLServizi = scaricoPacchettoCitAutService.createLog(dto);
			dto.setIdSer(dmassLServizi.getId_ser());						
			
			scaricoPacchettoCitAutService.controlli(dto);						
			
			VerificaStatoRichiesta2Response verificaStatoRichiesta2Response = scaricoPacchettoCitAutService.verificaStatoRichiesta2(dto);		
			String statoRichiesta = verificaStatoRichiesta2Response.getStatoRichiesta();
			dto.setCodDocumentoIlec(verificaStatoRichiesta2Response.getCodDocumentoDipartimentale());
			if(Costanti.STATO_PACCHETTO_NOTIFICATO.equalsIgnoreCase(statoRichiesta) ||				
					Costanti.STATO_PACCHETTO_DISPONIBILE.equalsIgnoreCase(statoRichiesta) ||
					Costanti.STATO_PACCHETTO_INVIO_MAIL_IN_CORSO.equalsIgnoreCase(statoRichiesta) ||
					Costanti.STATO_PACCHETTO_ERRORE_INVIO_MAIL.equalsIgnoreCase(statoRichiesta) ||
					Costanti.STATO_PACCHETTO_SCARICATO.equalsIgnoreCase(statoRichiesta)) {
				
				if (!cfAssistito.equalsIgnoreCase(cfRichiedente)) {					
					GetDeleganti2Response getDeleganti2Response = scaricoPacchettoCitAutService.getDeleganti2(dto);

					if (RisultatoCodice.SUCCESSO.equals(getDeleganti2Response.getEsito())) {

						Delegante delegante = getDeleganti2Response.getDeleganti().getDelegante().get(0);
						String grado = "";
						for (Delega delega : delegante.getDelega()) {
							if("FSEDOC".equalsIgnoreCase(delega.getCodiceServizio())) {
								grado = delega.getGrado();
							}
						}

						if ("FORTE".equalsIgnoreCase(grado)) {
							//ricerca pacchetto
							String pathFile = verificaStatoRichiesta2Response.getDirectory().replaceAll("2F", "/")+verificaStatoRichiesta2Response.getZipName();
							File file = new File(pathFile);
							//8) Ricerca pacchetto su file system
							if(file.exists()) {							
								//9) Restituzione stream pacchetto
								scaricaZip(response, pathFile, verificaStatoRichiesta2Response.getZipName());
								
								//10) Aggiorna stato pacchetto sulla richiesta
								dto.setStatoRichiesta("SCARICATO");
								scaricoPacchettoCitAutService.aggiornaStatoRichiesta(dto);
								
								//11) Audit
								dto.setCodiceAudit(Costanti.IMR_SCA_PAC_CIT);
								scaricoPacchettoCitAutService.audit(dto);
								
								//12) aggiorna log successo
								dto.setCodEsitoRisposta(HttpStatus.OK);
								scaricoPacchettoCitAutService.updateLog(dto);
								
							}else {
								//aggiorna stato richiesta
								dto.setErrore("File zip dello studio non trovato");
								dto.setStatoRichiesta(Costanti.STATO_ERRORE_COMPONI_PACCHETTO);
								scaricoPacchettoCitAutService.aggiornaStatoRichiesta(dto);							
								dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);																		
								throw new DmassException("File non trovato: "+pathFile);
							}
							
						} else if ("DEBOLE".equalsIgnoreCase(grado)) {
							//oscuramento
							VerificaOscuramentoDocResponse verificaOscuramentoDocResponse = scaricoPacchettoCitAutService.verificaOscuramentoDoc(dto);							
							if(verificaOscuramentoDocResponse == null ||
									it.csi.dmass.client.verificaService.RisultatoCodice.FALLIMENTO.equals(verificaOscuramentoDocResponse.getEsito()) ||
									"N".equalsIgnoreCase(verificaOscuramentoDocResponse.getDatiDocumentoResponse().getOscurato())) {
								dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);
								throw new DmassException("Pacchetto non scaricabile");
							}else {

								String pathFile = verificaStatoRichiesta2Response.getDirectory().replaceAll("2F", "/")+verificaStatoRichiesta2Response.getZipName();
								File file = new File(pathFile);
								//8) Ricerca pacchetto su file system
								if(file.exists()) {							
									//9) Restituzione stream pacchetto
									scaricaZip(response, pathFile, verificaStatoRichiesta2Response.getZipName());
									
									//10) Aggiorna stato pacchetto sulla richiesta
									dto.setStatoRichiesta("SCARICATO");
									scaricoPacchettoCitAutService.aggiornaStatoRichiesta(dto);
									
									//11) Audit
									dto.setCodiceAudit(Costanti.IMR_SCA_PAC_CIT);
									scaricoPacchettoCitAutService.audit(dto);
									
									//12) aggiorna log successo
									dto.setCodEsitoRisposta(HttpStatus.OK);
									scaricoPacchettoCitAutService.updateLog(dto);
									
								}else {
									//aggiorna stato richiesta
									dto.setErrore("File zip dello studio non trovato");
									dto.setStatoRichiesta(Costanti.STATO_ERRORE_COMPONI_PACCHETTO);
									scaricoPacchettoCitAutService.aggiornaStatoRichiesta(dto);							
									dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);																		
									throw new DmassException("File non trovato: "+pathFile);
								}
							}
						} else {
							throw new DmassException("Nessuna Delega");
						}
					} else {
						List<it.csi.dmass.client.delegaService.dmacc.Errore> errori = getDeleganti2Response.getErrori();
						StringBuffer erroriBuffer = new StringBuffer(); 
						for (it.csi.dmass.client.delegaService.dmacc.Errore errore : errori) {
							erroriBuffer.append(errore.getDescrizione());
						}
						dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);	
						throw new DmassException("Errore servizio delega: "+String.join(",", erroriBuffer));
					}
				} else {
					//ricerca pacchetto
					String pathFile = verificaStatoRichiesta2Response.getDirectory().replaceAll("2F", "/")+verificaStatoRichiesta2Response.getZipName();
					File file = new File(pathFile);
					//8) Ricerca pacchetto su file system
					if(file.exists()) {							
						//9) Restituzione stream pacchetto
						scaricaZip(response, pathFile, verificaStatoRichiesta2Response.getZipName());
						
						//10) Aggiorna stato pacchetto sulla richiesta
						dto.setStatoRichiesta("SCARICATO");
						scaricoPacchettoCitAutService.aggiornaStatoRichiesta(dto);
						
						//11) Audit
						dto.setCodiceAudit(Costanti.IMR_SCA_PAC_CIT);
						scaricoPacchettoCitAutService.audit(dto);
						
						//12) aggiorna log successo
						dto.setCodEsitoRisposta(HttpStatus.OK);
						scaricoPacchettoCitAutService.updateLog(dto);
						
					}else {
						//aggiorna stato richiesta
						dto.setErrore("File zip dello studio non trovato");
						dto.setStatoRichiesta(Costanti.STATO_ERRORE_COMPONI_PACCHETTO);
						scaricoPacchettoCitAutService.aggiornaStatoRichiesta(dto);							
						dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);																		
						throw new DmassException("File non trovato: "+pathFile);
					}
				}
				
				
				
			}else {
				if(Costanti.STATO_RICHIESTA_PACCHETTO_NON_PRESENTE.equalsIgnoreCase(statoRichiesta)) {
					dto.setCodEsitoRisposta(HttpStatus.NOT_FOUND);
					throw new DmassException("Richiesta non presente");
					
				}else {
					dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);
					throw new DmassException("Pacchetto non scaricabile");
				}
			}
			
			return ResponseEntity.status(HttpStatus.OK)
					.body("");

		} catch (Exception e) {
			log.error("scaricoPAcchettoCitAut",e);
			//aggiornamento log su errore
			if(dto.getCodEsitoRisposta()!=null) {
				scaricoPacchettoCitAutService.updateLog(dto);
				scaricoPacchettoCitAutService.createLogErrore(dto, e);
				return ResponseEntity.status(dto.getCodEsitoRisposta())
						.body("errore generico");
			}else {
				dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);
				scaricoPacchettoCitAutService.updateLog(dto);
				scaricoPacchettoCitAutService.createLogErrore(dto, e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("errore generico");
			}		
			
		}
	}
	

	@RequestMapping(value = "/scaricoPacchettoOperatoreSanitario", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<String> scaricoPacchettoOperatoreSanitario(
			@RequestParam("cfAssistito") final String cfAssistito, @RequestParam("idDocumentoIlec") final String idDocumentoIlec,
			@RequestParam("codCL") final String codCL,@RequestParam("codApplicazione") final String codApplicazione, 
			@RequestParam("codVerticale") final String codVerticale, @RequestParam("codRuolo") final String codRuolo, @RequestParam("codRegime") final String codRegime, 
			@RequestParam(required = false) final String idCollocazione,@RequestParam("archivioDocumentoIlec") final String archivioDocumentoIlec,
			@RequestParam("pin") final String pin,
			HttpServletRequest request, HttpServletResponse response) {

		ScaricaPacchettoOperatoreSanitarioDto dto = new ScaricaPacchettoOperatoreSanitarioDto();
		
		try {
																	 	    	        			
			String cfRichiedente = request.getHeader("Shib-Identita-CodiceFiscale");
			
			if(StringUtils.isEmpty(cfRichiedente)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("access denided");
			}
			
			dto.setCfAssistito(cfAssistito);
			dto.setCfRichiedente(cfRichiedente);
			dto.setCodApplicazione(codApplicazione);
			dto.setCodCL(codCL);
			dto.setCodRegime(codRegime);
			dto.setCodRuolo(codRuolo);
			dto.setCodVerticale(codVerticale);
			dto.setIdCollocazione(idCollocazione);
			dto.setIdDocumentoIlec(idDocumentoIlec);
			dto.setIp(request.getRemoteAddr());
			dto.setArchivioDocumentoIlec(archivioDocumentoIlec);
			dto.setPin(pin);
			
			dto.setRequest(request.getRequestURL()+"?"+request.getQueryString());
									
			DmassLServizi dmassLServizi = scaricoPacchettoOperatoreSanitarioService.createLog(dto);
			dto.setIdSer(dmassLServizi.getId_ser());						
			
			scaricoPacchettoOperatoreSanitarioService.controlli(dto);								
			
			VerificaStatoRichiesta2Response verificaStatoRichiesta2Response = scaricoPacchettoOperatoreSanitarioService.verificaStatoRichiesta2(dto);		
			String statoRichiesta = verificaStatoRichiesta2Response.getStatoRichiesta();
			dto.setCodiceDocumentoIlec(verificaStatoRichiesta2Response.getCodDocumentoDipartimentale());
			if(Costanti.STATO_PACCHETTO_NOTIFICATO.equalsIgnoreCase(statoRichiesta) ||				
					Costanti.STATO_PACCHETTO_DISPONIBILE.equalsIgnoreCase(statoRichiesta) ||
					Costanti.STATO_PACCHETTO_INVIO_MAIL_IN_CORSO.equalsIgnoreCase(statoRichiesta) ||
					Costanti.STATO_PACCHETTO_ERRORE_INVIO_MAIL.equalsIgnoreCase(statoRichiesta) ||
					Costanti.STATO_PACCHETTO_SCARICATO.equalsIgnoreCase(statoRichiesta)) {
				
				
				StatoConsensiResponse statoConsensiResponse = scaricoPacchettoOperatoreSanitarioService.statoConsensi(dto);
				
				if(statoConsensiResponse.getEsito().equals(it.csi.dmass.client.CCConsensoINIExtService.RisultatoCodice.FALLIMENTO)) {
					//In caso di esito negativo, l'errore restituito da INI sarà inserito nel log. ???					
					for( it.csi.dmass.client.CCConsensoINIExtService.Errore err : statoConsensiResponse.getErrori()) {
							
						if(Costanti.ERRORE_2035.equals(err.getCodice())) { //2035 viene restituito quando non esiste un fascicolo aperto per l'utente.
							dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);
							throw new DmassException("L'assistito non ha fornito il consenso alla consultazione del FSE");		
						}else{
							dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);
							throw new DmassException("Errore chiamata INI");	
						}
					}
				}
				
				if("false".equalsIgnoreCase(statoConsensiResponse.getStatoConsensiOUT().getConsensoAlimentazione()) ||
						"false".equalsIgnoreCase(statoConsensiResponse.getStatoConsensiOUT().getConsensoConsultazione())){
					dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);
					throw new DmassException("L'assistito non ha fornito il consenso alla consultazione del FSE");
				}
				
				//6) Verifica medico abilitato a FSE
				VerificaUtenteAbilitatoResponse verificaUtenteAbilitatoResponse = scaricoPacchettoOperatoreSanitarioService.verificaUtenteAbilitatoFSE(dto);
				if(verificaUtenteAbilitatoResponse.getEsito().equals(it.csi.dmass.client.verificaService.RisultatoCodice.SUCCESSO) && 
						verificaUtenteAbilitatoResponse.getListaProfili().stream().anyMatch(c -> c.getCodice().equalsIgnoreCase(Costanti.P_OPESAN))) {
					
				
					//7) Call verificaOscuramentoDoc
					VerificaOscuramentoDocResponse verificaOscuramentoDocResponse = scaricoPacchettoOperatoreSanitarioService.verificaOscuramentoDoc(dto);
					
					if(verificaOscuramentoDocResponse == null ||
							it.csi.dmass.client.verificaService.RisultatoCodice.FALLIMENTO.equals(verificaOscuramentoDocResponse.getEsito()) ||
							"S".equalsIgnoreCase(verificaOscuramentoDocResponse.getDatiDocumentoResponse().getOscurato())) {
						dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);
						throw new DmassException("Pacchetto non scaricabile");
					}else {
						String pathFile = verificaStatoRichiesta2Response.getDirectory().replaceAll("2F", "/")+verificaStatoRichiesta2Response.getZipName();
						File file = new File(pathFile);
						//8) Ricerca pacchetto su file system
						if(file.exists()) {							
							//9) Restituzione stream pacchetto
							scaricaZip(response, pathFile, verificaStatoRichiesta2Response.getZipName());
							
							//10) Aggiorna stato pacchetto sulla richiesta
							dto.setStatoRichiesta("SCARICATO");
							scaricoPacchettoOperatoreSanitarioService.aggiornaStatoRichiesta(dto);
							
							//11) Audit
							dto.setCodiceAudit(Costanti.IMR_SCA_PAC_WA);
							scaricoPacchettoOperatoreSanitarioService.audit(dto);
							
							//12) aggiorna log successo
							dto.setCodEsitoRisposta(HttpStatus.OK);
							scaricoPacchettoOperatoreSanitarioService.updateLog(dto);
							
						}else {
							//aggiorna stato richiesta
							dto.setErrore("File zip dello studio non trovato");
							dto.setStatoRichiesta(Costanti.STATO_ERRORE_COMPONI_PACCHETTO);
							scaricoPacchettoOperatoreSanitarioService.aggiornaStatoRichiesta(dto);							
							dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);																		
							throw new DmassException("File non trovato: "+pathFile);
						}
					}
					
				}else {
					List<Errore> errori = verificaUtenteAbilitatoResponse.getErrori();
					StringBuffer erroriBuffer = new StringBuffer(); 
					for (Errore errore : errori) {
						erroriBuffer.append(errore.getDescrizione());
					}
					dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);	
					throw new DmassException("utente non abilitato: "+String.join(",", erroriBuffer));
				}				
				
			}else {
				if(Costanti.STATO_RICHIESTA_PACCHETTO_NON_PRESENTE.equalsIgnoreCase(statoRichiesta)) {
					dto.setCodEsitoRisposta(HttpStatus.NOT_FOUND);
					throw new DmassException("Richiesta non presente");
					
				}else {
					dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);
					throw new DmassException("Pacchetto non scaricabile");
				}
			}								

			return ResponseEntity.status(HttpStatus.OK)
					.body("");

		} catch (Exception e) {
			log.error("scaricoPacchettoOperatoreSanitario",e);
			//aggiornamento log su errore
			if(dto.getCodEsitoRisposta()!=null) {
				scaricoPacchettoOperatoreSanitarioService.updateLog(dto);
				scaricoPacchettoOperatoreSanitarioService.createLogErrore(dto, e);
				return ResponseEntity.status(dto.getCodEsitoRisposta())
						.body("errore generico");
			}else {
				dto.setCodEsitoRisposta(HttpStatus.INTERNAL_SERVER_ERROR);
				scaricoPacchettoOperatoreSanitarioService.updateLog(dto);
				scaricoPacchettoOperatoreSanitarioService.createLogErrore(dto, e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("errore generico");
			}
			
			
		}
	}
	
	public static void scaricaZip(HttpServletResponse response, String pathFile, String nomeFileUser) throws IOException{
		 	
		ServletOutputStream out = response.getOutputStream();
		InputStream in = new FileInputStream(new File(pathFile));
		try {
			byte[] bytes = new byte[8192];
			int bytesRead;

			response.setHeader("Expires", "0");
			response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			response.setHeader("Pragma", "public");

			response.setHeader("Content-Disposition", "attachment; filename=" + nomeFileUser);
			response.setContentType("application/zip");

			while ((bytesRead = in.read(bytes)) != -1) {
				out.write(bytes, 0, bytesRead);
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			in.close();
			out.flush();
			out.close();
		}
	}
}
