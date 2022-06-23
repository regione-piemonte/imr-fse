package it.csi.dmass.scaricoStudi.model;

import java.io.Serializable;

/**
 * @generated
 */
public class CredenzialiServiziLowPk implements Serializable {

    /**
     * @generated
     */
    private Long id;

    /**
     * @generated
     */
     public final void setIdConsenso(Long val) {
        this.id = val;
    }

    /**
     * @generated
     */
     public final Long getId() {
        return this.id;
    }

    /**
     * @generated
     */
    public CredenzialiServiziLowPk() {
    }

    /**
     * @generated
     */
    public CredenzialiServiziLowPk(

    final Long id

    ) {

        this.id = id;

    }

    /**
     * Method 'equals'
     * 
     * @param other
     * @return boolean
     */
    public final boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (other == this) {
            return true;
        }

        if (!(other instanceof CredenzialiServiziLowPk)) {
            return false;
        }

        final CredenzialiServiziLowPk cast = (CredenzialiServiziLowPk) other;

        if (id == null ? cast.getId() != id
                : !id.equals(cast.getId())) {
            return false;
        }

        return true;
    }

    /**
     * Method 'hashCode'
     * 
     * @return int
     */
    public final int hashCode() {
        int hashCode = 0;

        if (id != null) {
            hashCode = 29 * hashCode + id.hashCode();
        }

        return hashCode;
    }

    /**
     * Method 'toString'
     * 
     * @return String
     */
    public final String toString() {
        StringBuffer ret = new StringBuffer();

        ret.append("it.csi.dma.dmaccbl.business.dao.dmaccbl.dto.CredenzialiServiziLowPk: ");
        ret.append("idConsenso=" + id);

        return ret.toString();
    }
}
