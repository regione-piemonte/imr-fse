package it.csi.dmass.service.dao;

import it.csi.dmass.service.model.DmaccTEventoNotifica;

public interface DmaccTEventoNotificaDao {

	public final static String SQL_INSERT = "INSERT INTO dmacc_t_evento_notifica "
			+ "(id, id_evento, cf_destinatario, flag_stato_notifica, data_inserimento) "
			+ "VALUES(nextval('seq_dmacc_t_evento_notifica'), ?, ?, ?, current_timestamp);";
	
	public final static String SQL_ID_EVENTO = "select id from dmacc_d_evento_per_notificatore where codice_evento=?";
	
	public boolean create(DmaccTEventoNotifica model);	

	public Long getIdEvento(String codiceEvento);
}
