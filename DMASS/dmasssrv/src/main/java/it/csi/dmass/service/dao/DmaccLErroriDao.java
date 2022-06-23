package it.csi.dmass.service.dao;

import it.csi.dmass.service.model.DmaccLErrori;

public interface DmaccLErroriDao {

	public final static String SQL_INSERT = "INSERT INTO dmacc_l_errori "
			+ "(id, wso2_id, cod_errore, descr_errore, tipo_errore, data_ins, informazioni_aggiuntive) "
			+ "VALUES(nextval('seq_dmacc_l_errori'), ?, ?, ?, ?, current_timestamp, ?);";
	
	
	public final static String SQL_DESCRIOZNE_ERRORE = "select descrizione_errore from dmacc_d_catalogo_log where codice=?";
	
	public boolean create(DmaccLErrori model);
	
	public String getDescrizioneErrore(String codiceErrore);

}
