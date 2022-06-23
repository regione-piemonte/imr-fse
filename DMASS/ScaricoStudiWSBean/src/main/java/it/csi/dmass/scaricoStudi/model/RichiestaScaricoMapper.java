package it.csi.dmass.scaricoStudi.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class RichiestaScaricoMapper implements RowMapper<RichiestaScarico>{

	@Override
	public RichiestaScarico mapRow(ResultSet rs, int rowNum) throws SQLException {

		RichiestaScarico model = new RichiestaScarico();
		model.setCodicefiscale(rs.getString("codicefiscale"));
		model.setEmail(rs.getString("email"));
		model.setErrore(rs.getString("errore"));
		model.setIdreferto(rs.getString("idreferto"));
		model.setPin(rs.getString("pin"));
		model.setStatorichiesta(rs.getString("statorichiesta"));
		model.setStrutturasanitaria(rs.getString("strutturasanitaria"));
		model.setAsr(rs.getString("asr"));
		model.setSistemaoperativo(rs.getString("sistemaoperativo"));
		model.setAccession_numbers(rs.getString("accession_numbers"));
		model.setDatainsrichiesta(rs.getTimestamp("datainsrichiesta"));
		model.setDatainviomail(rs.getTimestamp("datainviomail"));
		model.setDataoraerrore(rs.getTimestamp("dataoraerrore"));
		model.setDataultimodownload(rs.getTimestamp("dataultimodownload"));
		model.setIdrichiestascarico(rs.getLong("idrichiestascarico"));
		model.setNumerotentativi(rs.getLong("numerotentativi"));
		model.setPeriodoconservazione(rs.getLong("periodoconservazione"));
		model.setMail_id(rs.getLong("mail_id"));
		model.setPacchetto_id(rs.getLong("pacchetto_id"));
		model.setRichiestacancellazione_id(rs.getLong("richiestacancellazione_id"));
		model.setScaricopacchetto_id(rs.getLong("scaricopacchetto_id"));
		model.setFuoriregione(rs.getBoolean("fuoriregione"));
		model.setChecksum(rs.getString("checksum"));
		model.setDirectory(rs.getString("directory"));
		model.setZipname(rs.getString("zipname"));
		
		
		return model;
	}

	
}
