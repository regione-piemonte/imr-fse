/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(value="dmasstransactionManager",rollbackFor=Exception.class,propagation=Propagation.REQUIRED)
public class BaseService {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
}
