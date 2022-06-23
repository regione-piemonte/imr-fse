/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.spring;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;



public class ClientPasswordCallback implements CallbackHandler {	
	String userScaricoStudi;
	String passScaricoStudi;
	
	public String getUserScaricoStudi() {
		return userScaricoStudi;
	}


	public void setUserScaricoStudi(String userScaricoStudi) {
		this.userScaricoStudi = userScaricoStudi;
	}

	public String getPassScaricoStudi() {
		return passScaricoStudi;
	}

	public void setPassScaricoStudi(String passScaricoStudi) {
		this.passScaricoStudi = passScaricoStudi;
	}		

	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

		for (int i = 0; i < callbacks.length; i++)
		{
			WSPasswordCallback pc = (WSPasswordCallback)callbacks[i];
			
			if(pc.getIdentifier().equals(userScaricoStudi)) {
				pc.setPassword(passScaricoStudi);
			}

		}
	}


	
	
}