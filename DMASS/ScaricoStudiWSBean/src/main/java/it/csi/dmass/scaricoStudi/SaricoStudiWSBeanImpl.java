package it.csi.dmass.scaricoStudi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.w3c.dom.Element;

import it.csi.dmass.AggiornaStatoRichiestaRequest;
import it.csi.dmass.AggiornaStatoRichiestaResponse;
import it.csi.dmass.CancellaPacchettoRequest;
import it.csi.dmass.CancellaPacchettoResponse;
import it.csi.dmass.ElencoPacchetti;
import it.csi.dmass.Errore;
import it.csi.dmass.GetElencoPacchettiScadutiRequest;
import it.csi.dmass.GetElencoPacchettiScadutiResponse;
import it.csi.dmass.RisultatoCodice;
import it.csi.dmass.ScaricoStudiRequest;
import it.csi.dmass.ScaricoStudiResponse;
import it.csi.dmass.ScaricoStudiWSBean;
import it.csi.dmass.SetPacchettoCancellatoRequest;
import it.csi.dmass.SetPacchettoCancellatoResponse;
import it.csi.dmass.SpringApplicationContextProvider;
import it.csi.dmass.StatoRichiestaScarico;
import it.csi.dmass.VerificaStatoListaRichiesteRequest;
import it.csi.dmass.VerificaStatoListaRichiesteResponse;
import it.csi.dmass.VerificaStatoRichiesta2Request;
import it.csi.dmass.VerificaStatoRichiesta2Response;
import it.csi.dmass.VerificaStatoRichiestaRequest;
import it.csi.dmass.VerificaStatoRichiestaResponse;
import it.csi.dmass.scaricoStudi.dao.CredenzialiServiziLowDao;
import it.csi.dmass.scaricoStudi.dao.CredenzialiServiziLowDaoException;
import it.csi.dmass.scaricoStudi.dao.DmaccDCatalogoLogDao;
import it.csi.dmass.scaricoStudi.dao.DmaccDCatalogoLogDaoImpl;
import it.csi.dmass.scaricoStudi.dao.DmaccTDocumentoScaricabileDao;
import it.csi.dmass.scaricoStudi.dao.DmaccTDocumentoScaricabileDaoImpl;
import it.csi.dmass.scaricoStudi.dao.DmaccidxTDocumentoDao;
import it.csi.dmass.scaricoStudi.dao.DmaccidxTDocumentoDaoImpl;
import it.csi.dmass.scaricoStudi.dao.PacchettoDao;
import it.csi.dmass.scaricoStudi.dao.PacchettoDaoImpl;
import it.csi.dmass.scaricoStudi.dao.RichiestaCancellazioneDao;
import it.csi.dmass.scaricoStudi.dao.RichiestaCancellazioneDaoImpl;
import it.csi.dmass.scaricoStudi.dao.RichiestaScaricoDao;
import it.csi.dmass.scaricoStudi.dao.RichiestaScaricoDaoImpl;
import it.csi.dmass.scaricoStudi.dao.SettingDao;
import it.csi.dmass.scaricoStudi.dao.SettingDaoImpl;
import it.csi.dmass.scaricoStudi.model.CredenzialiServiziLowDto;
import it.csi.dmass.scaricoStudi.model.RichiestaScarico;
import it.csi.dmass.scaricoStudi.util.LogUtil;
import it.csi.dmass.scaricoStudi.util.Utils;

public class SaricoStudiWSBeanImpl implements ScaricoStudiWSBean {
	
	protected LogUtil log = new LogUtil(this.getClass());

	RichiestaScaricoDao richiestaScaricoDaoImpl;
	DmaccDCatalogoLogDao dmaccDCatalogoLogDaoImpl;
	PacchettoDao pacchettoDaoImpl;
	RichiestaCancellazioneDao richiestaCancellazioneDaoImpl;
	SettingDaoImpl settingDaoImpl;
	DmaccidxTDocumentoDao dmaccidxTDocumentoDaoImpl;
	DmaccTDocumentoScaricabileDao dmaccTDocumentoScaricabileDaoImpl;
	
	CredenzialiServiziLowDao credenzialiServiziLowDaoImpl;
	
	@Resource
	private WebServiceContext wsContext;

	public VerificaStatoRichiesta2Response verificaStatoRichiesta2(
			VerificaStatoRichiesta2Request verificaStatoRichiesta2Request) {

		log.info("verificaStatoRichiesta2", "inizio");
		
		String codDocumentoDipartimentale=null;
		Long idDocumentoIlec=null;
		try {

			VerificaStatoRichiesta2Response response = new VerificaStatoRichiesta2Response();
			
			//Verifica credenziali
			if(!validateCredenziali(wsContext, "ScaricoStudi")){
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("credenziali non valide");
				errori.add(errore);
				return response;
			}						
			
			// controlli
			if (StringUtils.isEmpty(verificaStatoRichiesta2Request.getCodiceFiscale())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti codiceFiscale");
				errori.add(errore);
				return response;
			}
			if (StringUtils.isEmpty(verificaStatoRichiesta2Request.getCodCL())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti codCL");
				errori.add(errore);
				return response;
			}
			if (StringUtils.isEmpty(verificaStatoRichiesta2Request.getArvchivioDocumentoIlec())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti archivioDocumentoIlec");
				errori.add(errore);
				return response;
			}
			if (StringUtils.isEmpty(verificaStatoRichiesta2Request.getCodDocumentoDipartimentale()) && 
					(verificaStatoRichiesta2Request.getIdDocumentoIlec()==null || verificaStatoRichiesta2Request.getIdDocumentoIlec()==0)) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti codDocumentoDipartimentale o idDocumentoIlec");
				errori.add(errore);
				return response;
			}									
			
			
			//FSE
			if("FSE".equalsIgnoreCase(verificaStatoRichiesta2Request.getArvchivioDocumentoIlec())) {
				
				dmaccidxTDocumentoDaoImpl = (DmaccidxTDocumentoDaoImpl) SpringApplicationContextProvider.getApplicationContext().getBean("dmaccidxTDocumentoDaoImpl");
				
				if (!StringUtils.isEmpty(verificaStatoRichiesta2Request.getCodDocumentoDipartimentale())) {				
					codDocumentoDipartimentale=verificaStatoRichiesta2Request.getCodDocumentoDipartimentale();
					idDocumentoIlec=dmaccidxTDocumentoDaoImpl.getIdDocumentoIlecByCodiceDipartimentale(verificaStatoRichiesta2Request.getCodDocumentoDipartimentale(),verificaStatoRichiesta2Request.getCodCL());
				}			
			
				if (!(verificaStatoRichiesta2Request.getIdDocumentoIlec()==null || verificaStatoRichiesta2Request.getIdDocumentoIlec()==0)) {				
					codDocumentoDipartimentale = dmaccidxTDocumentoDaoImpl.getCodiceDocumentoDipartimentaleByIdDocumentoIlec(verificaStatoRichiesta2Request.getIdDocumentoIlec(),verificaStatoRichiesta2Request.getCodCL());
					idDocumentoIlec=verificaStatoRichiesta2Request.getIdDocumentoIlec();
				}
			
			}
			
			//ROL
			if("ROL".equalsIgnoreCase(verificaStatoRichiesta2Request.getArvchivioDocumentoIlec())) {
				
				dmaccTDocumentoScaricabileDaoImpl = (DmaccTDocumentoScaricabileDaoImpl) SpringApplicationContextProvider.getApplicationContext().getBean("dmaccTDocumentoScaricabileDaoImpl");
				
				if (!StringUtils.isEmpty(verificaStatoRichiesta2Request.getCodDocumentoDipartimentale())) {				
					codDocumentoDipartimentale=verificaStatoRichiesta2Request.getCodDocumentoDipartimentale();
					idDocumentoIlec=dmaccTDocumentoScaricabileDaoImpl.getIdDocumentoIlecByCodiceDipartimentale(verificaStatoRichiesta2Request.getCodDocumentoDipartimentale(),verificaStatoRichiesta2Request.getCodCL(), verificaStatoRichiesta2Request.getCodiceFiscale());
				}			
			
				if (!(verificaStatoRichiesta2Request.getIdDocumentoIlec()==null || verificaStatoRichiesta2Request.getIdDocumentoIlec()==0)) {				
					codDocumentoDipartimentale = dmaccTDocumentoScaricabileDaoImpl.getCodiceDocumentoDipartimentaleByIdDocumentoIlec(verificaStatoRichiesta2Request.getIdDocumentoIlec(),verificaStatoRichiesta2Request.getCodCL(), verificaStatoRichiesta2Request.getCodiceFiscale());
					idDocumentoIlec=verificaStatoRichiesta2Request.getIdDocumentoIlec();
				}							
			}						
			
			
//			if (StringUtils.isEmpty(verificaStatoRichiesta2Request.getArvchivioDocumentoIlec())) {
//				response.setEsito(RisultatoCodice.FALLIMENTO);
//				List<Errore> errori = response.getErrori();
//				Errore errore = new Errore();
//				errore.setCodice("01");
//				errore.setDescrizione("parametri obbligatori mancanti arvchivioDocumentoIlec");
//				errori.add(errore);
//				return response;
//			}
			richiestaScaricoDaoImpl = (RichiestaScaricoDaoImpl) SpringApplicationContextProvider.getApplicationContext()
					.getBean("richiestaScaricoDaoImpl");
			
			List<RichiestaScarico> listRichiestaScarico = new ArrayList<RichiestaScarico>(); 
			
			if(codDocumentoDipartimentale!=null) {
			
				listRichiestaScarico = richiestaScaricoDaoImpl.getByQuery(
					RichiestaScaricoDao.RICERCA_RICHIESTE_ASR,
					new Object[] { verificaStatoRichiesta2Request.getCodiceFiscale(),
							codDocumentoDipartimentale,
							verificaStatoRichiesta2Request.getCodCL() });
			}

			String statoRichiesta = "RICHIESTA_NON_PRESENTE";
			String checksum = "";
			String zipname = "";
			String directory = "";
			if (listRichiestaScarico != null && listRichiestaScarico.size() > 0) {
				RichiestaScarico richiestaScarico = listRichiestaScarico.get(0);
				statoRichiesta = richiestaScarico.getStatorichiesta();
				checksum = richiestaScarico.getChecksum();
				zipname = richiestaScarico.getZipname();
				directory = richiestaScarico.getDirectory();
			}

			response.setEsito(RisultatoCodice.SUCCESSO);
			response.setStatoRichiesta(statoRichiesta);
			response.setChecksum(checksum);
			response.setDirectory(directory);
			response.setZipName(zipname);
			
			response.setIdDocumentoIlec(idDocumentoIlec);
			response.setCodDocumentoDipartimentale(codDocumentoDipartimentale);

			return response;

		} catch (Exception e) {
			log.error("aggiornaStatoRichiesta", "errore", e,verificaStatoRichiesta2Request);
			throw new Fault(e);
		}finally {
			log.info("verificaStatoRichiesta2", "fine");
		}
	}

	@Override
	public AggiornaStatoRichiestaResponse aggiornaStatoRichiesta(AggiornaStatoRichiestaRequest aggiornaStatoRichiestaRequest) {
				
		log.info("aggiornaStatoRichiesta", "inizio");
		try {

			AggiornaStatoRichiestaResponse response = new AggiornaStatoRichiestaResponse();
					
			//Verifica credenziali
			if(!validateCredenziali(wsContext, "ScaricoStudi")){
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("credenziali non valide");
				errori.add(errore);
				return response;
			}
			
			// controlli
			if (StringUtils.isEmpty(aggiornaStatoRichiestaRequest.getCodiceFiscale())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti codiceFiscale");
				errori.add(errore);
				return response;
			}
			if (StringUtils.isEmpty(aggiornaStatoRichiestaRequest.getCodCL())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti codCL");
				errori.add(errore);
				return response;
			}
			if (StringUtils.isEmpty(aggiornaStatoRichiestaRequest.getCodDocumentoDipartimentale())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti codDocumentoDipartimentale");
				errori.add(errore);
				return response;
			}

			richiestaScaricoDaoImpl = (RichiestaScaricoDaoImpl) SpringApplicationContextProvider.getApplicationContext()
					.getBean("richiestaScaricoDaoImpl");
			List<RichiestaScarico> listRichiestaScarico = richiestaScaricoDaoImpl.getByQuery(
					RichiestaScaricoDao.RICERCA_RICHIESTE_ASR,
					new Object[] { aggiornaStatoRichiestaRequest.getCodiceFiscale(),
							aggiornaStatoRichiestaRequest.getCodDocumentoDipartimentale(),
							aggiornaStatoRichiestaRequest.getCodCL() });
			
			if (listRichiestaScarico != null && listRichiestaScarico.size() > 0) {
				RichiestaScarico richiestaScarico = listRichiestaScarico.get(0);
				//String statoRichiesta = richiestaScarico.getStatorichiesta();			
				//bisognerebbe controllare lo stato se è congruente
				//TODO una volta definiti gli stati stato richiesta deve essere una enum
				
				//aggiorno stato
				if(!StringUtils.isEmpty(aggiornaStatoRichiestaRequest.getStatoRichiesta())) {
					richiestaScarico.setStatorichiesta(aggiornaStatoRichiestaRequest.getStatoRichiesta());
				}
				
				if(!StringUtils.isEmpty(aggiornaStatoRichiestaRequest.getErrore())) {
					richiestaScarico.setErrore(aggiornaStatoRichiestaRequest.getErrore());
				}
				
				richiestaScaricoDaoImpl.update(aggiornaStatoRichiestaRequest.getStatoRichiesta(),richiestaScarico.getErrore(), 
						richiestaScarico.getCodicefiscale(), richiestaScarico.getIdreferto(), 
						richiestaScarico.getAsr());
				
				response.setEsito(RisultatoCodice.SUCCESSO);	
			}else {
				List<Errore> listErrore = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("02");
				errore.setDescrizione("Richiesta non presente");
				listErrore.add(errore);
				response.setEsito(RisultatoCodice.FALLIMENTO);
			}

			return response;

		} catch (Exception e) {
			log.error("aggiornaStatoRichiesta", "errore", e,aggiornaStatoRichiestaRequest);
			throw new Fault(e);
		}finally {
			log.info("aggiornaStatoRichiesta", "fine");
		}
		
	}

	@Override
	public ScaricoStudiResponse scaricoStudi(ScaricoStudiRequest scaricoStudiRequest) {
		
		
		log.info("scaricoStudi", "inizio");
		try {
			ScaricoStudiResponse response = new ScaricoStudiResponse();
			
			//Verifica credenziali
			if(!validateCredenziali(wsContext, "ScaricoStudi")){
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("credenziali non valide");
				errori.add(errore);
				return response;
			}
			
			// controlli
			if (StringUtils.isEmpty(scaricoStudiRequest.getCodiceFiscale())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti codiceFiscale");
				errori.add(errore);
				return response;
			}

			if (StringUtils.isEmpty(scaricoStudiRequest.getIdReferto())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti idReferto");
				errori.add(errore);
				return response;
			}

			if (StringUtils.isEmpty(scaricoStudiRequest.getPeriodoConservazione())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti periodoConservazione");
				errori.add(errore);
				return response;
			}	
			
			if (!isInteger(scaricoStudiRequest.getPeriodoConservazione())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("02");
				errore.setDescrizione("valore non valido periodoConservazione");
				errori.add(errore);
				return response;
			}
			
			if (StringUtils.isEmpty(scaricoStudiRequest.getPin())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti pin");
				errori.add(errore);
				return response;
			}
			
			if (StringUtils.isEmpty(scaricoStudiRequest.getSistemaOperativo())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti sistemaOperativo");
				errori.add(errore);
				return response;
			}
				
			if (StringUtils.isEmpty(scaricoStudiRequest.getAcessionNumbers())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti acessionNumbers");
				errori.add(errore);
				return response;
			}
			
			if (StringUtils.isEmpty(scaricoStudiRequest.getAsr())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti asr");
				errori.add(errore);
				return response;
			}
								
			richiestaScaricoDaoImpl = (RichiestaScaricoDaoImpl) SpringApplicationContextProvider.getApplicationContext()
					.getBean("richiestaScaricoDaoImpl");
			
			List<RichiestaScarico> listRichiestaScarico = richiestaScaricoDaoImpl.getByQuery(RichiestaScaricoDao.VERIFICA_RICHIESTE, scaricoStudiRequest.getCodiceFiscale(), "DA_ELABORARE",
					scaricoStudiRequest.getIdReferto(), scaricoStudiRequest.getAsr(), scaricoStudiRequest.getAcessionNumbers());
			
			//richiesta gia' presente
			if(listRichiestaScarico!=null && listRichiestaScarico.size()>0) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				dmaccDCatalogoLogDaoImpl = (DmaccDCatalogoLogDaoImpl) SpringApplicationContextProvider.getApplicationContext()						
						.getBean("dmaccDCatalogoLogDaoImpl");	
				String descrizioneErrore = dmaccDCatalogoLogDaoImpl.getDescrizioneErroreByCodice("FSE_ER_559");			
				List<Errore> listErrore = response.getErrori();
				Errore e = new Errore();
				e.setCodice("FSE_ER_559");
				e.setDescrizione(descrizioneErrore);
				listErrore.add(e);
			}									
			
			richiestaScaricoDaoImpl.insert(RichiestaScaricoDao.INSERT_RICHIESTA_SCARICO, scaricoStudiRequest.getCodiceFiscale(),scaricoStudiRequest.getEmail(),
					scaricoStudiRequest.isFuoriRegione(),scaricoStudiRequest.getIdReferto(),Long.parseLong(scaricoStudiRequest.getPeriodoConservazione()),scaricoStudiRequest.getPin(),
					scaricoStudiRequest.getStrutturaSanitaria(),scaricoStudiRequest.getAsr(),scaricoStudiRequest.getSistemaOperativo(),scaricoStudiRequest.getAcessionNumbers());
			
			response.setEsito(RisultatoCodice.SUCCESSO);
			
			return response;
			
		}catch (Exception e) {
			log.error("verificaStatoRichiesta", "errore", e,scaricoStudiRequest);
			throw new Fault(e);
		}finally {
			log.info("scaricoStudi", "fine");
		}
	}
	
	private static boolean isInteger(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        double d = Integer.parseInt(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
	private static boolean isBigDecimal(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	    	BigDecimal d = new BigDecimal(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}

	@Override
	public VerificaStatoRichiestaResponse verificaStatoRichiesta(
			VerificaStatoRichiestaRequest verificaStatoRichiestaRequest) {

		log.info("verificaStatoRichiesta", "inizio");
		try {
			VerificaStatoRichiestaResponse response = new VerificaStatoRichiestaResponse();
			
			//Verifica credenziali
			if(!validateCredenziali(wsContext, "ScaricoStudi")){
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("credenziali non valide");
				errori.add(errore);
				return response;
			}
			
			// controlli
			if (StringUtils.isEmpty(verificaStatoRichiestaRequest.getCodiceFiscale())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti codiceFiscale");
				errori.add(errore);
				return response;
			}

			if (StringUtils.isEmpty(verificaStatoRichiestaRequest.getIdReferto())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti idReferto");
				errori.add(errore);
				return response;
			}			
			
			if (StringUtils.isEmpty(verificaStatoRichiestaRequest.getPin())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti pin");
				errori.add(errore);
				return response;
			}						
								
			richiestaScaricoDaoImpl = (RichiestaScaricoDaoImpl) SpringApplicationContextProvider.getApplicationContext()
					.getBean("richiestaScaricoDaoImpl");
			
			List<RichiestaScarico> listRichiestaScarico = richiestaScaricoDaoImpl.getByQuery(
					RichiestaScaricoDao.RICERCA_RICHIESTE,
					new Object[] { verificaStatoRichiestaRequest.getCodiceFiscale(),
							verificaStatoRichiestaRequest.getIdReferto()});
						
			if(listRichiestaScarico!=null && listRichiestaScarico.size()>0) {	
				
				RichiestaScarico richiestaScarico = listRichiestaScarico.get(0);
				
				response.setEsito(RisultatoCodice.SUCCESSO);				
				response.setStatoRichiesta(richiestaScarico.getStatorichiesta());
				if(!isEmptyLong(richiestaScarico.getPacchetto_id()) && isEmptyLong(richiestaScarico.getRichiestacancellazione_id())) {					
					response.setIdPacchetto(richiestaScarico.getPacchetto_id().toString());
					pacchettoDaoImpl = (PacchettoDaoImpl) SpringApplicationContextProvider.getApplicationContext()
							.getBean("pacchettoDaoImpl");
					BigDecimal dimensione = pacchettoDaoImpl.getDimensionePacchetto(new BigDecimal(richiestaScarico.getPacchetto_id()));
					response.setDimensione(dimensione.toString());					
				}
			}else {
				response.setEsito(RisultatoCodice.SUCCESSO);
				response.setStatoRichiesta("RICHIESTA_NON_PRESENTE");					
			}
			
			return response;
			
		}catch (Exception e) {
			log.error("verificaStatoRichiesta", "errore", e,verificaStatoRichiestaRequest);
			throw new Fault(e);
		}finally {
			log.info("verificaStatoRichiesta", "fine");
		}
		
	}
	
	private boolean isEmptyLong(Long value) {
		if(value==null) {
			return false;
		}
		if(value==0) {
			return false;
		}
		return true;
	}

	@Override
	public VerificaStatoListaRichiesteResponse verificaStatoListaRichieste(
			VerificaStatoListaRichiesteRequest verificaStatoListaRichiesteRequest) {

		log.info("verificaStatoListaRichieste", "inizio");
		try {
			VerificaStatoListaRichiesteResponse response = new VerificaStatoListaRichiesteResponse();
			
			//Verifica credenziali
			if(!validateCredenziali(wsContext, "ScaricoStudi")){
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("credenziali non valide");
				errori.add(errore);
				return response;
			}
			
			// controlli
			if (StringUtils.isEmpty(verificaStatoListaRichiesteRequest.getCodiceFiscale())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti codiceFiscale");
				errori.add(errore);
				return response;
			}

			if (verificaStatoListaRichiesteRequest.getIdReferto() == null || verificaStatoListaRichiesteRequest.getIdReferto().size()==0) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti almeno un idReferto");
				errori.add(errore);
				return response;
			}			
			
			if (StringUtils.isEmpty(verificaStatoListaRichiesteRequest.getPin())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti pin");
				errori.add(errore);
				return response;
			}						
			
			richiestaScaricoDaoImpl = (RichiestaScaricoDaoImpl) SpringApplicationContextProvider.getApplicationContext()
					.getBean("richiestaScaricoDaoImpl");
			
			response.setEsito(RisultatoCodice.SUCCESSO);
			for (String idReferto : verificaStatoListaRichiesteRequest.getIdReferto()) {
				
				List<RichiestaScarico> listRichiestaScarico = richiestaScaricoDaoImpl.getByQuery(
						RichiestaScaricoDao.RICERCA_RICHIESTE,
						new Object[] { verificaStatoListaRichiesteRequest.getCodiceFiscale(),
								idReferto});
				
				if(listRichiestaScarico!=null && listRichiestaScarico.size()>0) {	
					
					RichiestaScarico richiestaScarico = listRichiestaScarico.get(0);										
					StatoRichiestaScarico statoRichiestaScarico = new StatoRichiestaScarico();
					statoRichiestaScarico.setStatoRichiesta(richiestaScarico.getStatorichiesta());
					statoRichiestaScarico.setIdReferto(idReferto);
					if(!isEmptyLong(richiestaScarico.getPacchetto_id()) && isEmptyLong(richiestaScarico.getRichiestacancellazione_id())) {					
						statoRichiestaScarico.setIdPacchetto(richiestaScarico.getPacchetto_id().toString());
						pacchettoDaoImpl = (PacchettoDaoImpl) SpringApplicationContextProvider.getApplicationContext()
								.getBean("pacchettoDaoImpl");
						BigDecimal dimensione = pacchettoDaoImpl.getDimensionePacchetto(new BigDecimal(richiestaScarico.getPacchetto_id()));
						statoRichiestaScarico.setDimensione(dimensione.toString());					
					}
					response.getListaStatoRichiestaScarico().add(statoRichiestaScarico);														
				}
			
			}						
			
			return response;
			
		}catch (Exception e) {
			log.error("verificaStatoListaRichieste", "errore", e,verificaStatoListaRichiesteRequest);
			throw new Fault(e);
		}finally {
			log.info("verificaStatoListaRichieste", "fine");
		}		
	}

	@Override
	public CancellaPacchettoResponse cancellaPacchetto(CancellaPacchettoRequest cancellaPacchettoRequest) {

		log.info("cancellaPacchetto", "inizio");
		try {
			CancellaPacchettoResponse response = new CancellaPacchettoResponse();
			
			//Verifica credenziali
			if(!validateCredenziali(wsContext, "ScaricoStudi")){
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("credenziali non valide");
				errori.add(errore);
				return response;
			}
			
			// controlli
			if (StringUtils.isEmpty(cancellaPacchettoRequest.getIdPacchetto())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti idPacchetto");
				errori.add(errore);
				return response;
			}
			
			if (!isBigDecimal(cancellaPacchettoRequest.getIdPacchetto())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri non valido idPacchetto");
				errori.add(errore);
				return response;
			}
			
			if (StringUtils.isEmpty(cancellaPacchettoRequest.getPin())) {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("parametri obbligatori mancanti pin");
				errori.add(errore);
				return response;
			}							
			
			richiestaScaricoDaoImpl = (RichiestaScaricoDaoImpl) SpringApplicationContextProvider.getApplicationContext()					
					.getBean("richiestaScaricoDaoImpl");
			pacchettoDaoImpl = (PacchettoDaoImpl) SpringApplicationContextProvider.getApplicationContext()
					.getBean("pacchettoDaoImpl");
			
			if(!"AUTO".equalsIgnoreCase(cancellaPacchettoRequest.getPin())) {
				
				List<RichiestaScarico> listRichiestaScarico = richiestaScaricoDaoImpl.getByQuery(RichiestaScaricoDao.RICERCA_RICHIESTE_PACCHETTO, Long.parseLong(cancellaPacchettoRequest.getIdPacchetto()));
				BigDecimal dimensione = null;
				try {
					dimensione = pacchettoDaoImpl.getDimensionePacchetto(new BigDecimal(cancellaPacchettoRequest.getIdPacchetto()));
				}catch (Exception e) {
					log.info("cancellaPacchetto", "Pacchetto inesistente "+cancellaPacchettoRequest.getIdPacchetto());
				}
				
				if(listRichiestaScarico == null || listRichiestaScarico.size()==0 || dimensione==null) {
					response.setEsito(RisultatoCodice.FALLIMENTO);
					Errore e = new Errore();					
					e.setCodice("Pacchetto inesistente");
					e.setDescrizione("Pacchetto inesistente");
					response.getErrori().add(e);
					return response;
				}
				
				richiestaCancellazioneDaoImpl = (RichiestaCancellazioneDaoImpl) SpringApplicationContextProvider.getApplicationContext()
						.getBean("richiestaCancellazioneDaoImpl");
				
				richiestaCancellazioneDaoImpl.insert(RichiestaCancellazioneDao.INSERT_RICHIESTA_CANCELLAZIONE, cancellaPacchettoRequest.getIdPacchetto(),
						cancellaPacchettoRequest.getPin());
				
				try {
					RichiestaScarico richiestaScarico = listRichiestaScarico.get(0);				
					richiestaScaricoDaoImpl.update(RichiestaScaricoDao.UPDATE_STATO_RICHIESTA, "PACCHETTO_CANCELLATO", null,richiestaScarico.getCodicefiscale(),
							richiestaScarico.getIdreferto(),richiestaScarico.getAsr());
				}catch(Exception e) {
					log.error("cancellaPacchetto", "errore", e,cancellaPacchettoRequest);
					response.setEsito(RisultatoCodice.FALLIMENTO);										 
					Errore errore = new Errore();
					errore.setCodice("Runtime Error");
					StringWriter sw = new StringWriter();
			    	PrintWriter pw = new PrintWriter(sw);
			    	e.printStackTrace(pw);					
					errore.setDescrizione(sw.toString());
					response.getErrori().add(errore);	
					return response;
				}
			}
									
			response.setEsito(RisultatoCodice.SUCCESSO);			
			return response;
			
		}catch (Exception e) {
			log.error("cancellaPacchetto", "errore", e,cancellaPacchettoRequest);
			throw new Fault(e);
		}finally {
			log.info("cancellaPacchetto", "fine");
		}	
	}

	@Override
	public GetElencoPacchettiScadutiResponse getElencoPacchettiScaduti(
			GetElencoPacchettiScadutiRequest getElencoPacchettiScadutiRequest) {

		log.info("getElencoPacchettiScaduti", "inizio");
		GetElencoPacchettiScadutiResponse response = new GetElencoPacchettiScadutiResponse();	
		try {						
			
			//Verifica credenziali
			if(!validateCredenziali(wsContext, "ScaricoStudi")){
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("credenziali non valide");
				errori.add(errore);
				return response;
			}
			
			settingDaoImpl = (SettingDaoImpl) SpringApplicationContextProvider.getApplicationContext()					
					.getBean("settingDaoImpl");
			richiestaScaricoDaoImpl = (RichiestaScaricoDaoImpl) SpringApplicationContextProvider.getApplicationContext()					
					.getBean("richiestaScaricoDaoImpl");
			
			String imrScadPacchetti = settingDaoImpl.findByQuery(SettingDao.SQL_GET_VALUE_BY_KEY, "IMR_SCAD_PACCHETTI").getValue();
			String imrNpacCanc = settingDaoImpl.findByQuery(SettingDao.SQL_GET_VALUE_BY_KEY, "IMR_NPAC_CANC").getValue();
			
			//Il sistema legge dalla tabella RICHIESTA_SCARICO i record per cui DATA_CREAZIONE_PACC 
			//+valore di IMR_SCAD_PACCHETTI (in giorni) >=data corrente limitato ad un numero di pacchetti più vecchi minore o uguale di IMR_NPAC_CANC.
			
			Date now = new Date();
			
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(now);
			gc.add(GregorianCalendar.DAY_OF_YEAR, -Integer.parseInt(imrScadPacchetti));
			Timestamp pacchettiScadutiTimeStamp = new Timestamp(gc.getTime().getTime()); 
			
			List<RichiestaScarico> listaPacchettiScaduti = richiestaScaricoDaoImpl.getByQuery(RichiestaScaricoDao.ELENCO_PACCHETTI_SCADUTI, 
					pacchettiScadutiTimeStamp, Long.parseLong(imrNpacCanc));
			
			//if(listaPacchettiScaduti!=null && listaPacchettiScaduti.size()>0) {
			response.setEsito(RisultatoCodice.SUCCESSO);
			List<ElencoPacchetti> elencoPacchettiScaduti = response.getElencoPacchetti();
			for (RichiestaScarico richiestaScarico : listaPacchettiScaduti) {
				ElencoPacchetti elencoPacchetti = new ElencoPacchetti();
				elencoPacchetti.setDirectory(richiestaScarico.getDirectory());
				elencoPacchetti.setIdRichiestaScarico(""+richiestaScarico.getIdrichiestascarico());
				elencoPacchetti.setZipName(richiestaScarico.getZipname());
				elencoPacchettiScaduti.add(elencoPacchetti);
			}
			//}
															
		}catch (Exception e) {
			log.error("getElencoPacchettiScaduti", "errore", e,getElencoPacchettiScadutiRequest);
			response.setEsito(RisultatoCodice.FALLIMENTO);
			dmaccDCatalogoLogDaoImpl = (DmaccDCatalogoLogDaoImpl) SpringApplicationContextProvider.getApplicationContext()					
					.getBean("dmaccDCatalogoLogDaoImpl");
			Errore errore = new Errore();
			errore.setCodice("FSE_ER_561");
			errore.setDescrizione(dmaccDCatalogoLogDaoImpl.getDescrizioneErroreByCodice("FSE_ER_561"));
			response.getErrori().add(errore);
		}finally {
			log.info("getElencoPacchettiScaduti", "fine");
		}	
	
		return response;
	}

	@Override
	public SetPacchettoCancellatoResponse setPacchettoCancellato(
			SetPacchettoCancellatoRequest setPacchettoCancellatoRequest) {


		log.info("setPacchettoCancellato", "inizio");
		SetPacchettoCancellatoResponse response = new SetPacchettoCancellatoResponse();	
		try {						
						
			//Verifica credenziali
			if(!validateCredenziali(wsContext, "ScaricoStudi")){
				response.setEsito(RisultatoCodice.FALLIMENTO);
				List<Errore> errori = response.getErrori();
				Errore errore = new Errore();
				errore.setCodice("01");
				errore.setDescrizione("credenziali non valide");
				errori.add(errore);
				return response;
			}
			
			richiestaScaricoDaoImpl = (RichiestaScaricoDaoImpl) SpringApplicationContextProvider.getApplicationContext()					
					.getBean("richiestaScaricoDaoImpl");
			
			if(richiestaScaricoDaoImpl.update(RichiestaScaricoDao.UPDATE_STATO_RICHIESTA_BY_ID, "PACCHETTO_CANCELLATO", Long.parseLong(setPacchettoCancellatoRequest.getIdRichiestaScarico()))) {
				response.setEsito(RisultatoCodice.SUCCESSO);
			}else {
				response.setEsito(RisultatoCodice.FALLIMENTO);
				Errore errore = new Errore();
				dmaccDCatalogoLogDaoImpl = (DmaccDCatalogoLogDaoImpl) SpringApplicationContextProvider.getApplicationContext()					
						.getBean("dmaccDCatalogoLogDaoImpl");
				errore.setCodice("FSE_ER_560");
				errore.setDescrizione(dmaccDCatalogoLogDaoImpl.getDescrizioneErroreByCodice("FSE_ER_560"));
				response.getErrori().add(errore);
			}						
															
		}catch (Exception e) {
			log.error("setPacchettoCancellato", "errore", e,setPacchettoCancellatoRequest);
			response.setEsito(RisultatoCodice.FALLIMENTO);
			dmaccDCatalogoLogDaoImpl = (DmaccDCatalogoLogDaoImpl) SpringApplicationContextProvider.getApplicationContext()					
					.getBean("dmaccDCatalogoLogDaoImpl");
			Errore errore = new Errore();
			errore.setCodice("FSE_ER_560");
			errore.setDescrizione(dmaccDCatalogoLogDaoImpl.getDescrizioneErroreByCodice("FSE_ER_560"));
			response.getErrori().add(errore);
		}finally {
			log.info("setPacchettoCancellato", "fine");
		}	
	
		return response;
		
	}
	
	private boolean validateCredenziali(WebServiceContext wsContext, String codiceServizio){


		String username = null;
		String password = null;

		MessageContext mctx = wsContext.getMessageContext();

		Message message = ((WrappedMessageContext) mctx).getWrappedMessage();

		List<Header> headers = CastUtils.cast((List<?>) message.get(Header.HEADER_LIST));
		Header header = Utils.getFirstRecord(headers);

		CredenzialiServiziLowDto credenzialiServiziDto = new CredenzialiServiziLowDto();		

		if (header != null) {
			Element e = (Element) header.getObject();
			username = Utils.getValueFromHeader(e.getChildNodes(), "Username");
			password = Utils.getValueFromHeader(e.getChildNodes(), "Password");
		} else {			
			return false;
		}

		credenzialiServiziDto.setCodiceServizio(codiceServizio);
		credenzialiServiziDto.setUsername(username);
		credenzialiServiziDto.setPassword(password);
		try {
			credenzialiServiziLowDaoImpl = (CredenzialiServiziLowDao) SpringApplicationContextProvider.getApplicationContext().getBean("credenzialiServiziLowDaoImpl");
			List<CredenzialiServiziLowDto> listaCredenziali = credenzialiServiziLowDaoImpl
					.findByCodiceServizioUserPassword(credenzialiServiziDto);

			boolean isValidAccess = false;

			for (CredenzialiServiziLowDto dto : listaCredenziali) {

				if (dto.getUsername().equals(username) && dto.getPassword().equals(password)) {
					return true;
				}
			}
			if (!isValidAccess) {

				return false;
			}

		} catch (CredenzialiServiziLowDaoException e) {
			return false;			
		}

//
		return false;
	}
	
		
	
}