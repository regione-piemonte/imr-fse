/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.accessors;

import it.units.htl.dpacs.accessors.implementations.CenteraCasAccessor;
import it.units.htl.dpacs.valueObjects.Credentials;



public class AccessorFactory {
	
	public static final String TYPE_HARDDISK="Hard Disk";
	public static final String TYPE_CENTERACAS="CenteraCAS";

	
	public static synchronized Accessor getAccessor(String deviceType, String deviceUrl, Credentials credentials) throws NoSuchAccessorException{
		Accessor accessor=null;
		try{
			/*if(TYPE_HARDDISK.equals(deviceType)){
				accessor=new HardDiskAccessor(deviceUrl, credentials);
			}else*/ if(TYPE_CENTERACAS.equals(deviceType)){
				accessor=new CenteraCasAccessor(deviceUrl, credentials);
			}else{
				throw new NoSuchAccessorException("Not a supported archiver: "+deviceType);
			}
		}catch(Throwable ex){
			if(deviceType!=null)
				throw new NoSuchAccessorException("Unable to instantiate accessor of type: "+deviceType+" - "+ex.getMessage());
			else
				throw new NoSuchAccessorException("Unable to instantiate requested accessor, maybe of type null - "+ex.getMessage());
		}
		return accessor;
	} 
}
