package it.csi.dmass.scaricoStudi.dao;

public interface DmaccTDocumentoScaricabileDao
{    	
			
	public Long getIdDocumentoIlecByCodiceDipartimentale(String codiceDocumentoDipartimentale,String codiceCl, String codiceFiscale);
	
	public String getCodiceDocumentoDipartimentaleByIdDocumentoIlec(Long IdDocumentoIlec,String codiceCl, String codiceFiscale);
	
}
