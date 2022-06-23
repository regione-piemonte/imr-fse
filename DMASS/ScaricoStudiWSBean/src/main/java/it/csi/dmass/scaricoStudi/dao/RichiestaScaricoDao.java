package it.csi.dmass.scaricoStudi.dao;

import java.util.List;

import it.csi.dmass.scaricoStudi.model.RichiestaScarico;



public interface RichiestaScaricoDao
{    	
	
	public static final String RICERCA_RICHIESTE_ASR = "select codicefiscale, email , errore, idreferto, pin, "+
			"statorichiesta, strutturasanitaria, asr, sistemaoperativo, accession_numbers, datainsrichiesta ,datainviomail, dataoraerrore, "+
			"dataultimodownload, idrichiestascarico, numerotentativi, periodoconservazione, mail_id, pacchetto_id, richiestacancellazione_id, "+
			"scaricopacchetto_id, fuoriregione, zipname, directory, checksum from richiesta_scarico WHERE codicefiscale = ? and idreferto = ? and asr = ? "+
			"and directory is not null and zipname is not null order by idrichiestascarico desc";
	
	public static final String RICERCA_RICHIESTE_PACCHETTO = "select codicefiscale, email , errore, idreferto, pin, "+
			"statorichiesta, strutturasanitaria, asr, sistemaoperativo, accession_numbers, datainsrichiesta ,datainviomail, dataoraerrore, "+
			"dataultimodownload, idrichiestascarico, numerotentativi, periodoconservazione, mail_id, pacchetto_id, richiestacancellazione_id, "+
			"scaricopacchetto_id, fuoriregione, zipname, directory, checksum from richiesta_scarico WHERE pacchetto_id = ?";
	
	public static final String RICERCA_RICHIESTE = "select codicefiscale, email , errore, idreferto, pin, "+
			"statorichiesta, strutturasanitaria, asr, sistemaoperativo, accession_numbers, datainsrichiesta ,datainviomail, dataoraerrore, "+
			"dataultimodownload, idrichiestascarico, numerotentativi, periodoconservazione, mail_id, pacchetto_id, richiestacancellazione_id, "+
			"scaricopacchetto_id, fuoriregione, zipname, directory, checksum from richiesta_scarico WHERE codicefiscale = ? and idreferto = ?";
	
	public static final String VERIFICA_RICHIESTE = "select codicefiscale, email , errore, idreferto, pin, "+
			"statorichiesta, strutturasanitaria, asr, sistemaoperativo, accession_numbers, datainsrichiesta ,datainviomail, dataoraerrore, "+
			"dataultimodownload, idrichiestascarico, numerotentativi, periodoconservazione, mail_id, pacchetto_id, richiestacancellazione_id, "+
			"scaricopacchetto_id, fuoriregione, zipname, directory, checksum from richiesta_scarico WHERE codicefiscale = ? and statorichiesta = ? "
			+ "and idreferto = ? and asr = ? and accession_numbers = ? ";
	
	public static final String ELENCO_PACCHETTI_SCADUTI = "select codicefiscale, email , errore, idreferto, pin, "+
			"statorichiesta, strutturasanitaria, asr, sistemaoperativo, accession_numbers, datainsrichiesta ,datainviomail, dataoraerrore, "+
			"dataultimodownload, idrichiestascarico, numerotentativi, periodoconservazione, mail_id, pacchetto_id, richiestacancellazione_id, "+
			"scaricopacchetto_id, fuoriregione, zipname, directory, checksum from richiesta_scarico WHERE datacreazionepac <= ? order by datacreazionepac limit ? ";
	
	public static final String UPDATE_STATO_RICHIESTA = "update richiesta_scarico set statorichiesta = ?, errore = ? "						
			+ " WHERE codicefiscale = ? and idreferto = ? and asr = ?";
	
	public static final String UPDATE_STATO_RICHIESTA_BY_ID = "update richiesta_scarico set statorichiesta = ? "
			+ " WHERE idrichiestascarico = ? ";
		
	public static final String INSERT_RICHIESTA_SCARICO = "INSERT INTO richiesta_scarico"
			+ "(idrichiestascarico, codicefiscale, datainsrichiesta, email, fuoriregione, idreferto, "
			+ "numerotentativi, periodoconservazione, pin, statorichiesta, strutturasanitaria, "
			+ "asr, sistemaoperativo, accession_numbers)"
			+ "VALUES(nextval('richiesta_scarico_seq'), ?, current_timestamp, ?, ?, ?, 0, ?, ?, 'DA_ELABORARE', ?, ?, ?, ?);";
	
	
	public final static String SQL_UPDATE_IN_ERRORE = "update richiesta_scarico set statorichiesta='ERRORE_COMPONI_PACCHETTO', errore=?, dataoraerrore=current_timestamp where idrichiestascarico=?";
	
	public final static String SQL_UPDATE = "update richiesta_scarico set statorichiesta='ELAB_IN_CORSO', requestid=?, errore=null, dataoraerrore=null  where idrichiestascarico=?";
	
	public final static String SQL_UPDATE_ERRORE = "update richiesta_scarico set statorichiesta='ELAB_IN_CORSO', requestid=?, numerotentativi=numerotentativi+1 "
			+ " where idrichiestascarico=? and numerotentativi=?";
	
	public List<RichiestaScarico> getByQuery(String query, Object... params);	
	
	public boolean update(String update, Object... params);
	
	public boolean insert(String insert, Object... params);
	
	public boolean update(String statoRichiesta, String errore, String codiceFiscale, String idReferto, String asr);
}
