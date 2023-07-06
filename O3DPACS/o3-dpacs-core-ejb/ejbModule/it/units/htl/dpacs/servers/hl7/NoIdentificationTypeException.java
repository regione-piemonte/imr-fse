/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

public class NoIdentificationTypeException extends Exception{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public NoIdentificationTypeException(String mes){
        super(mes);
    }
    
}
