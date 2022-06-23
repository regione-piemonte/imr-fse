package it.csi.dmass.scaricoStudi.dao;

import java.util.List;

import it.csi.dmass.scaricoStudi.model.CredenzialiServiziLowDto;

/**
 * @generated
 */
public interface CredenzialiServiziLowDao {
  
    /**
     * Implementazione del finder ByIdPaziente
     * 
     * @generated
     */   
    public List<CredenzialiServiziLowDto> findByCodiceServizioUserPassword(
    		CredenzialiServiziLowDto input)
            throws CredenzialiServiziLowDaoException;
            
    
	

}
