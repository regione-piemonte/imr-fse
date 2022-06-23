package it.csi.dmass.service.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.dmass.service.model.RichiestaScarico;
import it.csi.dmass.service.model.RichiestaScaricoMapper;

@Repository
public class RichiestaScaricoDaoImpl implements RichiestaScaricoDao {
	
	@Autowired
	Environment environment;

	JdbcTemplate jdbcTemplate;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public RichiestaScaricoDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}
	
	public String toString(Object... params) {
		List<String> ret = new ArrayList<String>();
		for (Object param : params) {
			if(param!=null) {
				ret.add(param.toString());
			}
		}
		return "["+String.join(",",ret)+"]";
	}
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public List<RichiestaScarico> findByRequetID(String requestId) {
		logger.info(RICERCA_PER_REQUEST_ID);
		logger.info(requestId);
		return jdbcTemplate.query(RICERCA_PER_REQUEST_ID,new RichiestaScaricoMapper(),requestId);
	}	

	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean updateStato(RichiestaScarico model) {
		logger.info(SQL_UPDATE_STATO);
		logger.info( model.toString());
		return jdbcTemplate.update(SQL_UPDATE_STATO, model.getStatorichiesta(), model.getRequestid()) > 0;
	}
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean updateStatoNotificato(RichiestaScarico model) {
		logger.info(SQL_UPDATE_STATO);
		logger.info( model.toString());
		return jdbcTemplate.update(SQL_UPDATE_STATO_NOTIFICATO, model.getStatorichiesta(), model.getDatacreazionepac(), model.getRequestid()) > 0;
	}
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean updateStatoConErrore(RichiestaScarico model) {
		logger.info(RICERCA_PER_REQUEST_ID);
		logger.info( model.toString());
		return jdbcTemplate.update(SQL_UPDATE_STATO_ERRORE, model.getStatorichiesta(), model.getErrore(), model.getRequestid()) > 0;
	}
		
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean update(RichiestaScarico model) {
		logger.info(SQL_UPDATE);
		logger.info( model.toString());
		return jdbcTemplate.update(SQL_UPDATE, model.getZipname(), model.getDirectory(), model.getJobuid(), model.getChecksum(), model.getRequestid()) > 0;
	}

	@Override
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean insert(RichiestaScarico model) {
		logger.info(SQL_INSERT);
		logger.info( model.toString());
		return jdbcTemplate.update(SQL_INSERT, model.getCodicefiscale(), model.getDatainsrichiesta(), model.getDatainviomail(), model.getDataoraerrore(), 
				model.getDataultimodownload(), model.getEmail(), model.getErrore(), model.getFuoriregione(), model.getIdreferto(), model.getNumerotentativi(), 
				model.getPeriodoconservazione(), model.getPin(), model.getStatorichiesta(), model.getStrutturasanitaria(), model.getMail_id(), model.getPacchetto_id(), 
				model.getRichiestacancellazione_id(), model.getScaricopacchetto_id(), model.getAsr(), model.getSistemaoperativo(), model.getAccession_numbers(), 
				model.getRequestid(), model.getZipname(), model.getDirectory(), model.getJobuid(), model.getChecksum(),model.getDatacreazionepac()) > 0;		
		
	}
	
}
