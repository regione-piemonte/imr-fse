package it.csi.dmass.scaricoStudi.dao;

public interface DmaccidxTDocumentoDao
{    	
	
	public static final String RICERCA_ID_DOCUMENTO_ILEC_BY_CODICE_DOCUMENTO_DIPARTIMENTALE = "select id_documento_ilec from dmaccidx_t_documento WHERE "
			+ " codice_documento_dipartimentale = ? and cod_cl = ? and data_annullamento is null";
	
	public static final String RICERCA_CODICE_DOCUMENTO_DIPARTIMENTALE_BY_ID_DOCUMENTO_ILEC	= "select codice_documento_dipartimentale from dmaccidx_t_documento WHERE "
			+ " id_documento_ilec = ? and cod_cl = ? and data_annullamento is null";
	
		
	public Long getIdDocumentoIlecByCodiceDipartimentale(String codiceDocumentoDipartimentale,String codiceCl);
	
	public String getCodiceDocumentoDipartimentaleByIdDocumentoIlec(Long IdDocumentoIlec,String codiceCl);
	
}
