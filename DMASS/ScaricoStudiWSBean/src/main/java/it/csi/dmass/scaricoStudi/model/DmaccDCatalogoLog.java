package it.csi.dmass.scaricoStudi.model;

public class DmaccDCatalogoLog {

	private String codice;
	private String descerizione_errore;
	
	public String getCodice() {
		return codice;
	}
	public void setCodice(String codice) {
		this.codice = codice;
	}
	public String getDescerizione_errore() {
		return descerizione_errore;
	}
	public void setDescerizione_errore(String descerizione_errore) {
		this.descerizione_errore = descerizione_errore;
	}
	@Override
	public String toString() {
		return "DmaccDCatalogoLog [codice=" + codice + ", descerizione_errore=" + descerizione_errore + "]";
	}
	
	
}
