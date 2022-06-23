/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.config;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;



public class ClientPasswordCallback implements CallbackHandler {
	String userRol;
	String passRol;
	String userScaricoStudi;
	String passScaricoStudi;
	String userConsensoIni;
	String passConsensoIni;
	String userAura;
	String passAura;
	String userArruolamento;
	String passArruolamento;
	String userDma;
	String passDma;
	String userUtility;
	String passUtility;
	String userPaziente;
	String passPaziente;
	String userVerifica;
	String passVerifica;
	String userDelega;
	String passDelega;	
	
	public String getUserDelega() {
		return userDelega;
	}



	public void setUserDelega(String userDelega) {
		this.userDelega = userDelega;
	}



	public String getPassDelega() {
		return passDelega;
	}



	public void setPassDelega(String passDelega) {
		this.passDelega = passDelega;
	}



	public String getUserVerifica() {
		return userVerifica;
	}



	public void setUserVerifica(String userVerifica) {
		this.userVerifica = userVerifica;
	}



	public String getPassVerifica() {
		return passVerifica;
	}



	public void setPassVerifica(String passVerifica) {
		this.passVerifica = passVerifica;
	}



	public String getUserConsensoIni() {
		return userConsensoIni;
	}



	public void setUserConsensoIni(String userConsensoIni) {
		this.userConsensoIni = userConsensoIni;
	}



	public String getPassConsensoIni() {
		return passConsensoIni;
	}



	public void setPassConsensoIni(String passConsensoIni) {
		this.passConsensoIni = passConsensoIni;
	}



	public String getUserRol() {
		return userRol;
	}



	public void setUserRol(String userRol) {
		this.userRol = userRol;
	}

	public String getPassRol() {
		return passRol;
	}

	public void setPassRol(String passRol) {
		this.passRol = passRol;
	}

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

	public String getUserAura() {
		return userAura;
	}

	public void setUserAura(String userAura) {
		this.userAura = userAura;
	}

	public String getPassAura() {
		return passAura;
	}

	public void setPassAura(String passAura) {
		this.passAura = passAura;
	}

	


	public String getUserArruolamento() {
		return userArruolamento;
	}



	public void setUserArruolamento(String userArruolamento) {
		this.userArruolamento = userArruolamento;
	}



	public String getPassArruolamento() {
		return passArruolamento;
	}



	public void setPassArruolamento(String passArruolamento) {
		this.passArruolamento = passArruolamento;
	}



	public String getUserDma() {
		return userDma;
	}



	public void setUserDma(String userDma) {
		this.userDma = userDma;
	}



	public String getPassDma() {
		return passDma;
	}



	public void setPassDma(String passDma) {
		this.passDma = passDma;
	}



	public String getUserUtility() {
		return userUtility;
	}



	public void setUserUtility(String userUtility) {
		this.userUtility = userUtility;
	}



	public String getPassUtility() {
		return passUtility;
	}



	public void setPassUtility(String passutility) {
		this.passUtility = passutility;
	}



	public String getUserPaziente() {
		return userPaziente;
	}



	public void setUserPaziente(String userPaziente) {
		this.userPaziente = userPaziente;
	}



	public String getPassPaziente() {
		return passPaziente;
	}



	public void setPassPaziente(String passPaziente) {
		this.passPaziente = passPaziente;
	}



	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

		for (int i = 0; i < callbacks.length; i++)
		{
			WSPasswordCallback pc = (WSPasswordCallback)callbacks[i];

			if(pc.getIdentifier().equals(userUtility)) {
				pc.setPassword(passUtility);
			}
			
			if(pc.getIdentifier().equals(userConsensoIni)) {
				pc.setPassword(passConsensoIni);
			}
			
			if(pc.getIdentifier().equals(userVerifica)) {
				pc.setPassword(passVerifica);
			}
			
			if(pc.getIdentifier().equals(userDelega)) {
				pc.setPassword(passDelega);
			}
			
			if(pc.getIdentifier().equals(userScaricoStudi)) {
				pc.setPassword(passScaricoStudi);
			}

		}
	}


	
	
}