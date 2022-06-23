package it.csi.dmass.service.dao;

import it.csi.dmass.service.model.DmaccLMessaggi;

public interface DmaccLMessaggiDao {
	
	public final static String SQL_INSERT = "INSERT INTO dmacc_l_messaggi "
			+ "(id_xml, wso2_id, servizio_xml, uuid, chiamante, stato_xml, data_ricezione, data_risposta, data_invio_a_promemoria, data_risposta_a_promemoria, data_ins, data_mod, id_messaggio_orig, cf_assistito, cf_utente, ruolo_utente, nre, cod_esito_risposta_promemoria, tipo_prescrizione, regione_prescrizione, info_aggiuntive_errore, data_invio_servizio, data_risposta_servizio, cod_esito_risposta_servizio, lista_codici_servizio, stato_delega, applicazione, codice_servizio, appl_verticale, ip_richiedente) "
			+ "VALUES(nextval('seq_dmacc_l_messaggi'), ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	public final static String SQL_UPDATE = "UPDATE dmacc_l_messaggi set data_risposta = ?, cod_esito_risposta_servizio = ? WHERE wso2_id = ? ";
	
	public boolean create(DmaccLMessaggi model);
	
	public boolean update(DmaccLMessaggi model);

}
