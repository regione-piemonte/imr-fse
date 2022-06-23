package it.csi.dmass.service.dao;

public interface DmaccDCatalogoLogDao
{    	
	
	public static final String DESCRIZIONE_ERRORE = "select descrizione_errore from dmacc_d_catalogo_log WHERE codice = ?";
	
	
	
	public String getDescrizioneErroreByCodice(String codice);	
	
	
}
