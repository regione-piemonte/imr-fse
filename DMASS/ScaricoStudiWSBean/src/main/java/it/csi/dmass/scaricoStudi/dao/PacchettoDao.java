package it.csi.dmass.scaricoStudi.dao;

import java.math.BigDecimal;

public interface PacchettoDao
{    	
	
	public static final String DIMENSIONE_PACCHETTO = "select dimensione from pacchetto WHERE id = ?";
	
	
	
	public BigDecimal getDimensionePacchetto(BigDecimal id);	
	
	
}
