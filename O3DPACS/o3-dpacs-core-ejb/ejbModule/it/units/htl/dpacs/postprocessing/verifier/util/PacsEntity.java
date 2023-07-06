/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier.util;

import java.io.Serializable;

public class PacsEntity implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String remoteIp;
    private Integer remotePort;
    private String remoteAeTitle;
    private String localAeTitle;
    
    public PacsEntity(String remoteIp, Integer remotePort, String remoteAeTitle, String localAeTitle){
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.remoteAeTitle = remoteAeTitle;
        this.localAeTitle = localAeTitle;
    }
    
    public PacsEntity(){
        
    }
    public String getDicomUrl(){
        return remoteAeTitle+"@"+remoteIp+":"+remotePort;
    }
    
    public String getRemoteIp() {
        return remoteIp;
    }
    public Integer getRemotePort() {
        return remotePort;
    }
    public String getRemoteAeTitle() {
        return remoteAeTitle;
    }
    public String getLocalAeTitle() {
        return localAeTitle;
    }
    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }
    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }
    public void setRemoteAeTitle(String remoteAeTitle) {
        this.remoteAeTitle = remoteAeTitle;
    }
    public void setLocalAeTitle(String localAeTitle) {
        this.localAeTitle = localAeTitle;
    }
}
