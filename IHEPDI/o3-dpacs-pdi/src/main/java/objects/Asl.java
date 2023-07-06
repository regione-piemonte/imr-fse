/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package objects;

import java.io.Serializable;

import com.google.gson.Gson;

/**
 * Classe che contiene le informazioni dell'ASL
 */
public class Asl implements Serializable {

	private static final long serialVersionUID = -915758272273525546L;
	
	/**
	 * ID dell'ASL
	 */
	private String idAsl = null;
    
	/**
	 * Nome dell'ASL
	 */
	private String nome = null;
	
	/**
	 * Endpoint per l'invocazione dei servizi dell'ASL
	 */
	private String endpoint = null;
	
	/**
	 * Numero tentativi per l'invocazione dei servizi dell'ASL
	 */
	private String numeRetry = null;
    
	/**
	 * Directory di destinazione per il contenuto PDI
	 */
	private String dist = null;
    
	/**
	 * Percorso dell'xslt dell'ASL riferimento per il job
	 */
	private String xslt = null;
    
	/**
	 * Percorso del CSS dell'ASL riferimento per il job
	 */
	private String css = null;
	
	/**
	 * Percorso del JS dell'ASL riferimento per il job
	 */
	private String js = null;
	
	/**
	 * Comando per la move
	 */
    private String commandMove = null;
    
	/**
	 * Percorso alla directory contenente il file zip del viewer
	 */
    private String viewerFolder = null;
    
    /**
     * Tempo di attesa per l'invocazione dei servizi dell'ASL
     */
    private String sleep = null;
    
    /**
     * Tempo di attesa per l'invocazione della MOVE
     */
    private String sleepMove = null;
        
    public Asl() {}
    
	/**
	 * Fornisce l'id dell'ASl
	 * @return l'id del'ASL
	 */
    public String getIdAsl() {
		return idAsl;
	}

	/**
	 * Permette di impostare l'id dell'ASL
	 * @param idAsl l'id dell'ASL
	 */
	public void setIdAsl(String idAsl) {
		this.idAsl = idAsl;
	}

	/**
	 * Fornisce il nome dell'ASl
	 * @return il nome del'ASL
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Permette di impostare il nome dell'ASL
	 * @param nome il nome dell'ASL
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * Fornisce l'endpoint per l'invocazione dei servizi 
	 * @return l'endpoint per l'invocazione dei servizi 
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * Permette di impostare l'endpoint per l'invocazione dei servizi 
	 * @param endpoint l'endpoint per l'invocazione dei servizi 
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * Permette di ottenere il numero dei tentativi per l'invocazione dei servizi dell'ASL
	 * @return il numero dei tentativi per l'invocazione dei servizi dell'ASL
	 */
	public String getNumeRetry() {
		return numeRetry;
	}

	/**
	 * Permette di impostare il numero dei tentativi per l'invocazione dei servizi dell'ASL
	 * @param numeRetry il numero dei tentativi per l'invocazione dei servizi dell'ASL
	 */
	public void setNumeRetry(String numeRetry) {
		this.numeRetry = numeRetry;
	}

	/**
	 * Permette di ottenere la directory destinazione del contenuto PDI
	 * @return la directory destinazione del contenuto PDI
	 */
    public String getDist() {
		return dist;
	}
    
	/**
	 * Permette di impostare la directory destinazione del contenuto PDI
	 * @param dist la directory destinazione del contenuto PDI
	 */
	public void setDist(String dist) {
		this.dist = dist;
	}

	/**
	 * Fornisce il percorso all'XSLT dell'ASL
	 * @return il percorso all'XSLT dell'ASL
	 */
	public String getXslt() {
		return xslt;
	}

	/**
	 * Permette di impostare il percorso all'XSLT dell'ASL
	 * @param xslt il percorso all'XSLT dell'ASL
	 */
	public void setXslt(String xslt) {
		this.xslt = xslt;
	}

	/**
	 * Fornisce il percorso al CSS dell'ASL
	 * @return il percorso al CSS dell'ASL
	 */
	public String getCss() {
		return css;
	}

	/**
	 * Permette di impostare il percorso al CSSdell'ASL
	 * @param css il percorso al CSS dell'ASL
	 */
	public void setCss(String css) {
		this.css = css;
	}

	/**
	 * Permette di ottenere il comando da eseguire per l'operazione move
	 * @return il comando da eseguire per l'operazione move
	 */
	public String getCommandMove() {
		return commandMove;
	}

	/**
	 * Permette di impostare il comando da eseguire per l'operazione move
	 * @param commandMove il comando da eseguire per l'operazione move
	 */
	public void setCommandMove(String commandMove) {
		this.commandMove = commandMove;
	}
	
	/**
	 * Permette di ottenere la directory del viewer
	 * @return il percorso alla directory del viewer
	 */
	public String getViewerFolder() {
		return viewerFolder;
	}

	/**
	 * Permette di impostare il percorso della directory del viewer
	 * @param viewerFolder il percorso alla directory del viewer
	 */
	public void setViewerFolder(String viewerFolder) {
		this.viewerFolder = viewerFolder;
	}

	/**
	 * Permette di ottenere il tempo di attesa per l'invocazione dei servizi dell'ASL
	 * @return il tempo di attesa  per l'invocazione dei servizi dell'ASL
	 */
	public String getSleep() {
		return sleep;
	}

	/**
	 * Permette di impostare il tempo di attesa per l'invocazione dei servizi dell'ASL
	 * @param sleep il tempo di attesa per l'invocazione dei servizi dell'ASL
	 */
	public void setSleep(String sleep) {
		this.sleep = sleep;
	}
	
	public String getSleepMove() {
		return sleepMove;
	}

	public void setSleepMove(String sleepMove) {
		this.sleepMove = sleepMove;
	}

	/**
	 * Fornisce il percorso al JS dell'ASL
	 * @return il percorso al JS dell'ASL
	 */
	public String getJs() {
		return js;
	}

	/**
	 * Permette di impostare il percorso al JS dell'ASL
	 * @param js il percorso al CSS dell'ASL
	 */
	public void setJs(String js) {
		this.js = js;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
	
}