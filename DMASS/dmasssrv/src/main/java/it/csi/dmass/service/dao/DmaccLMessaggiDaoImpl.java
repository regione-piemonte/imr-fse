package it.csi.dmass.service.dao;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.dmass.service.model.DmaccLMessaggi;

@Repository
public class DmaccLMessaggiDaoImpl implements DmaccLMessaggiDao {
	
	private JdbcTemplate jdbcTemplate;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public DmaccLMessaggiDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public boolean create(DmaccLMessaggi model) {
		logger.info(SQL_INSERT);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_INSERT, model.getWso2_id(), model.getServizio_xml(), model.getUuid(), model.getChiamante(), model.getStato_xml(), model.getData_ricezione(), 
				model.getData_risposta(), model.getData_invio_a_promemoria(), model.getData_risposta_a_promemoria(),model.getData_mod(), model.getId_messaggio_orig(), 
				model.getCf_assistito(), model.getCf_utente(), model.getRuolo_utente(), model.getNre(), model.getCod_esito_risposta_promemoria(), model.getTipo_prescrizione(), 
				model.getRegione_prescrizione(), model.getInfo_aggiuntive_errore(), model.getData_invio_servizio(), model.getData_risposta_servizio(), model.getCod_esito_risposta_servizio(), 
				model.getLista_codici_servizio(), model.getStato_delega(), model.getApplicazione(), model.getCodice_servizio(), model.getAppl_verticale(), model.getIp_richiedente()) > 0;
	}

	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public boolean update(DmaccLMessaggi model) {
		logger.info(SQL_UPDATE);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_UPDATE, model.getData_risposta(), model.getCod_esito_risposta_servizio(), model.getWso2_id()) > 0;
	}

}
