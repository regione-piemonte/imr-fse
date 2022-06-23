package it.csi.dmass.scaricoStudi.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.dmass.scaricoStudi.util.LogUtil;



@Repository
public class DmaccidxTDocumentoDaoImpl implements DmaccidxTDocumentoDao {
	
	
	protected LogUtil log = new LogUtil(this.getClass());
	
	@Autowired
	Environment environment;

	JdbcTemplate jdbcTemplate;			

	@Autowired
	public DmaccidxTDocumentoDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}
	
	@Override
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public Long getIdDocumentoIlecByCodiceDipartimentale(String codiceDocumentoDipartimentale, String codiceCl) {
		log.info("getDescrizioneErroreByCodice",RICERCA_ID_DOCUMENTO_ILEC_BY_CODICE_DOCUMENTO_DIPARTIMENTALE);								
		log.info("getDescrizioneErroreByCodice","codiceDocumentoDipartimentale: "+codiceDocumentoDipartimentale);
		List<Long> idDocumentoIlec = jdbcTemplate.query(RICERCA_ID_DOCUMENTO_ILEC_BY_CODICE_DOCUMENTO_DIPARTIMENTALE,
				new Object[] {codiceDocumentoDipartimentale,codiceCl},new GetIdDocumentoIlecByCodiceDipartimentale());
		
		if(idDocumentoIlec!=null && idDocumentoIlec.size()>0) {
			return idDocumentoIlec.get(0);
		}
		return null;
	}
	
	@Override
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public String getCodiceDocumentoDipartimentaleByIdDocumentoIlec(Long idDocumentoIlec, String codiceCl) {
		log.info("getDescrizioneErroreByCodice",RICERCA_CODICE_DOCUMENTO_DIPARTIMENTALE_BY_ID_DOCUMENTO_ILEC);								
		log.info("getDescrizioneErroreByCodice","idDocumentoIlec: "+idDocumentoIlec);
		List<String> codiceDocumentoDipartimentale =  jdbcTemplate.query(RICERCA_CODICE_DOCUMENTO_DIPARTIMENTALE_BY_ID_DOCUMENTO_ILEC,
				new Object[] {idDocumentoIlec,codiceCl},new GetCodiceDocumentoDipartimentaleByIdDocumentoIlec());
		
		if(codiceDocumentoDipartimentale!=null && codiceDocumentoDipartimentale.size()>0) {
			return codiceDocumentoDipartimentale.get(0);
		}
		return null;
	}
	
	private class GetIdDocumentoIlecByCodiceDipartimentale implements RowMapper<Long>{

		@Override
		public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			return rs.getLong("id_documento_ilec");
		}
		
	}
	
	private class GetCodiceDocumentoDipartimentaleByIdDocumentoIlec implements RowMapper<String>{

		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.getString("codice_documento_dipartimentale");
		}
		
	}

	
}
