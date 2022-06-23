package it.csi.dmass.service.dao;

import java.util.List;

import it.csi.dmass.service.model.RichiestaScarico;

public interface RichiestaScaricoDao
{    	
			
	public static final String RICERCA_PER_REQUEST_ID = "select codicefiscale, email , errore, idreferto, pin, "+
			"statorichiesta, strutturasanitaria, asr, sistemaoperativo, accession_numbers, datainsrichiesta ,datainviomail, dataoraerrore, "+
			"dataultimodownload, idrichiestascarico, numerotentativi, periodoconservazione, mail_id, pacchetto_id, richiestacancellazione_id, "+
			"scaricopacchetto_id, fuoriregione, requestid from richiesta_scarico WHERE requestid=?";
	
	public final static String SQL_UPDATE_STATO_ERRORE = "update richiesta_scarico set statorichiesta=?, errore=? where requestid=?";
	
	public final static String SQL_UPDATE_STATO = "update richiesta_scarico set statorichiesta=? where requestid=?";
	
	public final static String SQL_UPDATE_STATO_NOTIFICATO = "update richiesta_scarico set statorichiesta=?, datacreazionepac=? where requestid=?";
	
	public final static String SQL_UPDATE = "update richiesta_scarico set zipname=?, directory=?, jobuid=?, checksum=? where requestid=?";
	
	
	public final static String SQL_INSERT = "INSERT INTO dmacc_rti.richiesta_scarico\n"
			+ "(idrichiestascarico, codicefiscale, datainsrichiesta, datainviomail, dataoraerrore, dataultimodownload, email, "
			+ "errore, fuoriregione, idreferto, numerotentativi, periodoconservazione, pin, statorichiesta, "
			+ "strutturasanitaria, mail_id, pacchetto_id, richiestacancellazione_id, scaricopacchetto_id, asr, sistemaoperativo, "
			+ "accession_numbers, requestid, zipname, directory, jobuid, checksum, datacreazionepac)"
			+ "VALUES(nextval('richiesta_scarico_seq'), ?, ?, ?, ?, ?, ?, "
			+        "?, ?, ?, ?, ?, ?, ?, "
			+        "?, ?, ?, ?, ?, ?, ?, "
			+        "?, ?, ?, ?, ?, ?, ?);";			
	
	public List<RichiestaScarico> findByRequetID(String requestId);
	
	public boolean updateStato(RichiestaScarico model);
	
	public boolean updateStatoNotificato(RichiestaScarico model);
	
	public boolean updateStatoConErrore(RichiestaScarico model);
	
	public boolean update(RichiestaScarico model);
	
	public boolean insert(RichiestaScarico model);
	
}
