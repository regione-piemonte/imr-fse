package it.csi.dmass.scaricoStudi.model;

import java.io.Serializable;

/**
 * @generated
 */
public class CredenzialiServiziLowDto implements Serializable {
	
	
   
    private Long id;  
    private String codiceServizio;  
    private String username;
    private String password;
    private java.sql.Timestamp dataInizioValidita;
    private java.sql.Timestamp dataFineValidita;
    private java.sql.Timestamp dataInserimento;
   
   
	
	
	
   
   

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodiceServizio() {
		return codiceServizio;
	}

	public void setCodiceServizio(String codiceServizio) {
		this.codiceServizio = codiceServizio;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public java.sql.Timestamp getDataInizioValidita() {
		return dataInizioValidita;
	}

	public void setDataInizioValidita(java.sql.Timestamp dataInizioValidita) {
		this.dataInizioValidita = dataInizioValidita;
	}

	public java.sql.Timestamp getDataFineValidita() {
		return dataFineValidita;
	}

	public void setDataFineValidita(java.sql.Timestamp dataFineValidita) {
		this.dataFineValidita = dataFineValidita;
	}

	public java.sql.Timestamp getDataInserimento() {
		return dataInserimento;
	}

	public void setDataInserimento(java.sql.Timestamp dataInserimento) {
		this.dataInserimento = dataInserimento;
	}

	public final boolean equals(Object other) {
        // TODO
        return super.equals(other);
    }

    /**
     * Method 'hashCode'
     * 
     * @return int
     * @generated
     */
    public final int hashCode() {
        // TODO
        return super.hashCode();
    }

    /**
     * Method 'createPk'
     * 
     * @return ConsensoCCPk
     * @generated
     */
     public final CredenzialiServiziLowPk createPk() {
        return new CredenzialiServiziLowPk(id);
    }

    /**
     * Method 'toString'
     * 
     * @return String
     * @generated
     */
    public final String toString() {
        // TODO
        return super.toString();
    }

}
