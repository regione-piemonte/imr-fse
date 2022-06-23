package it.csi.dmass.service.rest.service.sendCompleteNotice;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import it.csi.dmass.service.config.AppConfig;
import it.csi.dmass.service.config.Costanti;
import it.csi.dmass.service.dao.DmaccDCatalogoLogDao;
import it.csi.dmass.service.dao.DmaccLErroriDao;
import it.csi.dmass.service.dao.DmaccLMessaggiDao;
import it.csi.dmass.service.dao.DmaccLXmlMessaggiDao;
import it.csi.dmass.service.dao.DmaccTEventoNotificaDao;
import it.csi.dmass.service.dao.RichiestaScaricoDao;
import it.csi.dmass.service.model.DmaccLErrori;
import it.csi.dmass.service.model.DmaccLMessaggi;
import it.csi.dmass.service.model.DmaccLXmlMessaggi;
import it.csi.dmass.service.model.DmaccTEventoNotifica;
import it.csi.dmass.service.model.RichiestaScarico;
import it.csi.dmass.service.rest.service.BaseService;

@Service("completeNoticeService")
public class CompleteNoticeService extends BaseService {

	@Autowired
	private DmaccLErroriDao dmaccLErroriDao;

	@Autowired
	private DmaccLMessaggiDao dmaccLMessaggiDao;
	
	@Autowired
	private DmaccLXmlMessaggiDao dmaccLXmlMessaggiDao;

//	@Autowired
//	private DmaccLXmlMessaggiDao dmaccLXmlMessaggiDao;

	@Autowired
	private RichiestaScaricoDao richiestaScaricoDao;
	
	@Autowired
	private DmaccDCatalogoLogDao dmaccDCatalogoLogDao;

	@Autowired
	private DmaccTEventoNotificaDao dmaccTEventoNotificaDao;
	
	@Autowired
	Map<String,String> credentilas;

	public void completeNotice(String zipName, String jobUID, String status, String requestID, String checksum,
			String dist,String codeError,HttpServletRequest httpRequest) throws Exception {
		
		logger.info("CompleteNoticeService method completeNotice --- start");
		try {
			
			//Verifica credenziali
			final String authorization = httpRequest.getHeader("Authorization");
			if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
			    // Authorization: Basic base64credentials
			    String base64Credentials = authorization.substring("Basic".length()).trim();
			    byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			    String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			    // credentials = username:password
			    final String[] values = credentials.split(":", 2);
			    if(values!=null) {
			    	String userNameFSE = values[0];
			    	String passwordFSE = values[1];	
			    	
			    	if(userNameFSE==null || !credentilas.get(AppConfig.UserNameFSE).equals(userNameFSE)) {
			    		throw new Exception("access denided");
			    	}
			    	
			    	if(passwordFSE==null || !credentilas.get(AppConfig.PasswordFSE).equals(passwordFSE)) {
			    		throw new Exception("access denided");
			    	}
			    	
			    }else {
			    	throw new Exception("access denided");
			    }
			}else {
				throw new Exception("access denided");
			}
						
			
			// 1) Verifica stato della notifica
			// status=ERROR
			if (Costanti.ERROR.equals(status)) {
				String xmlIn="/CompleteNotice?zipName="+zipName+"&jobUID="+jobUID+"&status="+status+"&requestID="+requestID+"&checksum="+checksum+"&"
						+ "dist="+dist+"&codeError"+codeError;
				createLog(requestID,xmlIn);
								
				if(!Costanti.CC_ERR_300.equalsIgnoreCase(codeError)) {				
					if (requestID != null && !"".equals(requestID)) {						
						setCodError(requestID, codeError);												
					}															
				}else if(Costanti.CC_ERR_300.equalsIgnoreCase(codeError)) {									
					nuovaRichiestaScarico(requestID, codeError);										
				}				
				
				return;
			}
			// status=ERROR

			// 2) Ricerca richiesta scarico per RequestID
			List<RichiestaScarico> listRichiestaScarico = richiestaScaricoDao.findByRequetID(requestID);
			if (listRichiestaScarico != null && listRichiestaScarico.size() > 0) {
				// trovato

				// 3) Verifica ricerca
				RichiestaScarico richiestaScarico = aggiornaRichiestaStatoDisponibile(zipName, jobUID, requestID,
						checksum, dist, listRichiestaScarico);

				// 4) Registra notifica
				registraNotifica(requestID, richiestaScarico);
			} else {
				// non trovato
				String xmlIn="/CompleteNotice?zipName="+zipName+"&jobUID="+jobUID+"&status="+status+"&requestID="+requestID+"&checksum="+checksum+
						"&dist="+dist+"&codeError"+codeError;
				createLog(requestID,xmlIn);
				throw new Exception("richiesta scarico non trovata per RequestID: "+ requestID);
			}
			
			updateLog(requestID, "200");
			
		} catch (Exception e) {
			logger.error("completeNotice method: ", e);
			updateLog(requestID, "400");
			throw e;
		}finally {
			logger.info("CompleteNoticeService method completeNotice --- stop");	
		}
				
	}

	private void nuovaRichiestaScarico(String requestID, String codeError) {
		
		
		logger.info("CompleteNoticeService inserimento nuova richiesta scarico DA_ELABORARE");
		
		String descrizioneErrore = dmaccDCatalogoLogDao.getDescrizioneErroreByCodice(codeError);						
		RichiestaScarico richiestaScaricoOld = new RichiestaScarico();
		richiestaScaricoOld.setStatorichiesta(Costanti.ERRORE_COMPONI_PACCHETTO_DA_RIPRENOTARE);
		richiestaScaricoOld.setRequestid(requestID);
		richiestaScaricoOld.setErrore(descrizioneErrore);
		richiestaScaricoDao.updateStatoConErrore(richiestaScaricoOld);
		
		List<RichiestaScarico> listRichiestaScarico = richiestaScaricoDao.findByRequetID(requestID);
		if(listRichiestaScarico!=null && listRichiestaScarico.size()>0) {
			
			RichiestaScarico richiestaScaricoNew = listRichiestaScarico.get(0);
			richiestaScaricoNew.setDatainsrichiesta(new Timestamp((new Date()).getTime()));
			richiestaScaricoNew.setDataoraerrore(null);
			richiestaScaricoNew.setDataultimodownload(null);
			richiestaScaricoNew.setErrore(null);
			richiestaScaricoNew.setNumerotentativi(0L);
			richiestaScaricoNew.setStatorichiesta(Costanti.DA_ELABORARE);
			richiestaScaricoNew.setRequestid(null);
			richiestaScaricoNew.setZipname(null);
			richiestaScaricoNew.setDirectory(null);
			richiestaScaricoNew.setJobuid(null);
			richiestaScaricoNew.setChecksum(null);
			richiestaScaricoNew.setRichiestacancellazione_id(null);
			richiestaScaricoNew.setMail_id(null);
			richiestaScaricoNew.setPacchetto_id(null);
			richiestaScaricoNew.setScaricopacchetto_id(null);
			richiestaScaricoDao.insert(richiestaScaricoNew);
		}
	}

	private void setCodError(String requestID, String codeError) {
		//String descrizioneErrore = dmaccDCatalogoLogDao.getDescrizioneErroreByCodice(codeError);						
		RichiestaScarico richiestaScarico = new RichiestaScarico();
		richiestaScarico.setStatorichiesta(Costanti.ERRORE_COMPONI_PACCHETTO);
		richiestaScarico.setRequestid(requestID);
		richiestaScarico.setErrore(codeError);
		richiestaScaricoDao.updateStatoConErrore(richiestaScarico);
	}

	private void registraNotifica(String requestID, RichiestaScarico richiestaScarico) {
		DmaccTEventoNotifica dmaccTEventoNotifica = new DmaccTEventoNotifica();
		dmaccTEventoNotifica.setId_evento(dmaccTEventoNotificaDao.getIdEvento(Costanti.EVNS18));
		dmaccTEventoNotifica.setCf_destinatario(richiestaScarico.getCodicefiscale());
		dmaccTEventoNotifica.setFlag_stato_notifica(Costanti.I);
		try {
			if (dmaccTEventoNotificaDao.create(dmaccTEventoNotifica)) {
				richiestaScarico = new RichiestaScarico();
				richiestaScarico.setStatorichiesta(Costanti.NOTIFICATO);
				richiestaScarico.setRequestid(requestID);
				richiestaScarico.setDatacreazionepac(new Timestamp(System.currentTimeMillis()));
				richiestaScaricoDao.updateStatoNotificato(richiestaScarico);
			} else {
				richiestaScarico = new RichiestaScarico();
				richiestaScarico.setStatorichiesta(Costanti.ERRORE_INVIO_MAIL);
				richiestaScarico.setRequestid(requestID);
				richiestaScaricoDao.updateStato(richiestaScarico);
			}
		} catch (Exception e) {
			richiestaScarico = new RichiestaScarico();
			richiestaScarico.setStatorichiesta(Costanti.ERRORE_INVIO_MAIL);
			richiestaScarico.setRequestid(requestID);
			richiestaScaricoDao.updateStato(richiestaScarico);
		}
	}

	private RichiestaScarico aggiornaRichiestaStatoDisponibile(String zipName, String jobUID, String requestID,
			String checksum, String dist, List<RichiestaScarico> listRichiestaScarico) {
		RichiestaScarico richiestaScarico = listRichiestaScarico.get(0);
		richiestaScarico.setZipname(zipName);
		richiestaScarico.setDirectory(dist);
		richiestaScarico.setJobuid(jobUID);
		richiestaScarico.setChecksum(checksum);
		richiestaScarico.setStatorichiesta(Costanti.DISPONIBILE);
		richiestaScarico.setRequestid(requestID);
		richiestaScaricoDao.update(richiestaScarico);
		return richiestaScarico;
	}

	private void createLog(String requestID,String xml_in) {
		DmaccLMessaggi dmaccLMessaggi = new DmaccLMessaggi();
		dmaccLMessaggi.setWso2_id(requestID);
		dmaccLMessaggi.setServizio_xml(Costanti.SENDCOMPLETENOTICE);
		dmaccLMessaggi.setUuid(UUID.randomUUID().toString());
		dmaccLMessaggi.setStato_xml(2L);
		dmaccLMessaggi.setData_ricezione(new Timestamp(System.currentTimeMillis()));
		dmaccLMessaggi.setData_invio_servizio(new java.sql.Timestamp(new Date().getTime()));
		dmaccLMessaggi.setCod_esito_risposta_servizio(HttpStatus.OK.toString());
		dmaccLMessaggi.setCodice_servizio(Costanti.SEND_COM_NOT);
		dmaccLMessaggi.setChiamante(" ");

		// DmaccLXmlMessaggi ???
		DmaccLXmlMessaggi dmaccLXmlMessaggi = new DmaccLXmlMessaggi();
		dmaccLXmlMessaggi.setWso2_id(requestID);
		dmaccLXmlMessaggi.setData_inserimento(new Timestamp(System.currentTimeMillis()));
		dmaccLXmlMessaggi.setXml_in(xml_in);			
		
		DmaccLErrori dmaccLErrori = new DmaccLErrori();
		dmaccLErrori.setWso2Id(requestID);
		dmaccLErrori.setCodErrore(Costanti.FSE_ER_561);
		dmaccLErrori.setTipoErrore(Costanti.BLOCCANTE);
		dmaccLErrori.setDescrErrore(dmaccDCatalogoLogDao.getDescrizioneErroreByCodice(Costanti.FSE_ER_561));
		dmaccLErrori.setInformazioniAggiuntive("REQUESTID=" + requestID);		

		dmaccLErroriDao.create(dmaccLErrori);	 
		dmaccLXmlMessaggiDao.create(dmaccLXmlMessaggi);		
		dmaccLMessaggi.setData_risposta_servizio(new java.sql.Timestamp(new Date().getTime()));
		dmaccLMessaggiDao.create(dmaccLMessaggi);

	}
	
	private void updateLog(String requestID,String xml_out) {
		
		// DmaccLXmlMessaggi ???
		DmaccLXmlMessaggi dmaccLXmlMessaggi = new DmaccLXmlMessaggi();
		dmaccLXmlMessaggi.setWso2_id(requestID);		
		dmaccLXmlMessaggi.setXml_in(xml_out);							
		dmaccLXmlMessaggiDao.update(dmaccLXmlMessaggi);
		
		DmaccLMessaggi dmaccLMessaggi = new DmaccLMessaggi();
		dmaccLMessaggi.setWso2_id(requestID);		
		dmaccLMessaggi.setCod_esito_risposta_servizio(xml_out);		
		dmaccLMessaggi.setData_risposta(new Timestamp(System.currentTimeMillis()));
		dmaccLMessaggiDao.update(dmaccLMessaggi);
		

	}
}
