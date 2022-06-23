package it.csi.dmass.service.rest.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(value="dmacctransactionManager",rollbackFor=Exception.class,propagation=Propagation.REQUIRES_NEW)
public class BaseService {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
}
