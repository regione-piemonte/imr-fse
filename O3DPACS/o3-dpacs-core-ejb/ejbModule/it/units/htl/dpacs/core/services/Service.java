/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.core.services;

/**
 * @author sangalli
 *
 */
public class Service {
	private String serviceName = null;
	private String serviceMBeanName;
	private Integer serviceId;
	private Integer[] serviceDependency = null;
	private boolean enabled;
	private String runFromIp = null;
	
	
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    /**
	 * @return the service name
	 */
	public String getName() {
		return serviceName;
	}
	/**
	 * @param serviceName
	 */
	public void setName(String serviceName) {
		this.serviceName = serviceName;
	}
	/**
	 * @return
	 */
	public Integer getId() {
		return serviceId;
	}
	/**
	 * @param serviceId
	 */
	public void setId(Integer serviceId) {
		this.serviceId = serviceId;
	}
	/**
	 * @return
	 */
	public Integer[] getDependency() {
		return serviceDependency;
	}
	/**
	 * @param serviceDependency
	 */
	public void setDependency(Integer[] serviceDependency) {
		this.serviceDependency = serviceDependency;
	}
	/**
	 * @return the MBean name of the service registered on MBeanServer
	 */
	public String getMBeanName() {
		return serviceMBeanName;
	}
	/**
	 * @param serviceMBeanName set the MBean name of the service registered on MBeanServer
	 */
	public void setMBeanName(String serviceMBeanName) {
		this.serviceMBeanName = serviceMBeanName;
	}
    public String getRunFromIp() {
        return runFromIp;
    }
    public void setRunFromIp(String runFromIp) {
        this.runFromIp = runFromIp;
    }
	
	
}
