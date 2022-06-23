package it.csi.dmass.service.dao;

import it.csi.dmass.service.model.DmaccLXmlMessaggi;

public interface DmaccLXmlMessaggiDao {

	public final static String SQL_INSERT = "INSERT INTO dmacc_l_xml_messaggi "
			+ "(id, wso2_id, xml_in, xml_out, xml_in_promemoria, xml_out_promemoria, data_inserimento, xml_in_servizio, xml_out_servizio) "
			+ "VALUES(nextval('seq_dmacc_l_xml_messaggi'), ?, ?, ?, ?, ?, ?, ?, ?)";
	
	public final static String SQL_UPDATE = " UPDATE dmacc_l_xml_messaggi "
			+ " set xml_out = ? where wso2_id = ? ";
	
	public boolean create(DmaccLXmlMessaggi model);
	
	public boolean update(DmaccLXmlMessaggi model);
}
