package it.csi.dmass.scaricoStudi.dao;

public interface DmaccDCatalogoLogDao
{    	
	
	public static final String RICERCA_RICHIESTE = "select descrizione_errore from dmacc_d_catalogo_log WHERE codice = ?";
	
	
	
	public String getDescrizioneErroreByCodice(String codice);	
	
	
}
