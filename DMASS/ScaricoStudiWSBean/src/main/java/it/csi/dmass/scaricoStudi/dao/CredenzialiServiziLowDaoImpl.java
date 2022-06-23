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

import it.csi.dmass.scaricoStudi.model.CredenzialiServiziLowDto;
import it.csi.dmass.scaricoStudi.util.LogUtil;

/**
 * @generated
 */
@Repository
public class CredenzialiServiziLowDaoImpl implements CredenzialiServiziLowDao {


	protected LogUtil log = new LogUtil(this.getClass());
	/**
	 * @generated
	 */
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	Environment environment;	

	/**
	 * @generated
	 */
	@Autowired
	public CredenzialiServiziLowDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}			

	/**
	 * Method 'getTableName'
	 * 
	 * @return String
	 * @generated
	 */
	public String getTableName() {
		return "DMACC_D_CREDENZIALI_SERVIZI";
	}

	/**
	 * Implementazione del finder ByIdPaziente
	 * 
	 * @generated
	 */
	public List<CredenzialiServiziLowDto> findByCodiceServizioUserPassword(
    		CredenzialiServiziLowDto input)
			throws CredenzialiServiziLowDaoException {
		StringBuilder sql = new StringBuilder();				

		sql.append("SELECT"
				+ "	ID,CODICE_SERVIZIO,USERNAME,(pgp_sym_decrypt(PASSWORD::bytea, '"+environment.getProperty("encrypt_password")+"'))::varchar AS PASSWORD,"
				+ "DATA_INIZIO_VALIDITA,DATA_FINE_VALIDITA, "
				+ "DATA_INSERIMENTO");

		sql.append(" FROM DMACC_D_CREDENZIALI_SERVIZI");

		sql.append(" WHERE ");

		sql.append("CODICE_SERVIZIO = ?");
		sql.append(" AND USERNAME = ? ");
		sql.append(" AND (pgp_sym_decrypt(PASSWORD::bytea, '"+environment.getProperty("encrypt_password")+"'))::varchar = ? ");
		
		sql.append(" AND DATA_INIZIO_VALIDITA <= now()  ");
		sql.append(" AND (DATA_FINE_VALIDITA >= now() or DATA_FINE_VALIDITA is null)");			

		List<CredenzialiServiziLowDto> list = null;						
		list = jdbcTemplate.query(sql.toString(), new Object [] {input.getCodiceServizio(), input.getUsername(), input.getPassword()}, new RowMapper<CredenzialiServiziLowDto>() {

			@Override
			public CredenzialiServiziLowDto mapRow(ResultSet rs, int rowNum) throws SQLException {

				CredenzialiServiziLowDto dto = new CredenzialiServiziLowDto();

				dto.setId((Long) rs.getObject("ID"));
				dto.setCodiceServizio(rs.getString("CODICE_SERVIZIO"));
				dto.setUsername(rs.getString("USERNAME"));
				dto.setPassword(rs.getString("PASSWORD"));
				dto.setDataInizioValidita(rs.getTimestamp("DATA_INIZIO_VALIDITA"));
				dto.setDataFineValidita(rs.getTimestamp("DATA_FINE_VALIDITA"));
				dto.setDataInserimento(rs.getTimestamp("DATA_INSERIMENTO"));

				return dto;
			}
			
		});
		return list;
	}	
	
}
