package it.csi.dmass.scaricoStudi.dao;

public interface RichiestaCancellazioneDao
{    	
			
	public static final String INSERT_RICHIESTA_CANCELLAZIONE = "INSERT INTO richiesta_cancellazione"
			+ "(id, datainsrichiesta, idpacchetto, numerotentativi, pin, cancellazioneeffettuata ) "
			+ "VALUES(nextval('richiesta_cancellazione_seq'), current_timestamp, ?, 0, ?, null);";
			
		
	public boolean insert(String insert, Object... params);
		
}
