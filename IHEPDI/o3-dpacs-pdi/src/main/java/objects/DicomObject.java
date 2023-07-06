/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package objects;

/**
 * Modella un oggetto DICOM con le informazioni necessarie
 * per l'ottenimento di esso come formato JPEG
 */
public class DicomObject {
	
	public DicomObject() {}
	
	/**
	 * Istanza un oggetto DICOM
	 * @param id id dell'oggetto DICOM
	 * @param frameNumber il numero di frame dell'oggetto DICOM
	 */
	public DicomObject(String id, String frameNumber) {
		this.id = id;
		this.frameNumber = frameNumber;
	}
	

	/**
	 * Istanza un oggetto DICOM
	 * @param id id dell'oggetto DICOM
	 * @param frameNumber il numero di frame dell'oggetto DICOM
	 * @param rows il numero di row dell'oggetto DICOM
	 * @param columns il numero di column dell'oggetto DICOM
	 */
	public DicomObject(String id, String frameNumber, String rows, String columns) {
		this.id = id;
		this.frameNumber = frameNumber;
		this.rows = rows;
		this.columns = columns;
	}

	/**
	 * Fornisce i frame di una DICOM per la richiesta JPEG
	 * @return numero di frame per la DICOM
	 */
	public int getActualFrames() {
		int frames = 1;
		String frameNumber = getFrameNumber();
		if (frameNumber != null && !frameNumber.isEmpty()) {
			int objectFrames = Integer.parseInt(frameNumber);
			if (objectFrames > 3) {
				frames = objectFrames / 3;
			}
		}
		
		return frames;
	}
	
	/**
	 * Fornisce il numero di frame dell'oggetto DICOM
	 * @return frameNumber il numero di frame dell'oggetto DICOM
	 */
	public String getFrameNumber() {
		return frameNumber;
	}
	
	/**
	 * Permette di impostare il numero di frame dell'oggetto DICOM
	 * @param frameNumber il numero di frame dell'oggetto DICOM
	 */
	public void setFrameNumber(String frameNumber) {
		this.frameNumber = frameNumber;
	}
	
	/**
	 * Fornisce l'id dell'oggetto DICOM
	 * @return id l'id dell'oggetto DICOM
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Permette di impostare l'id dell'oggetto DICOM
	 * @param id l'id dell'oggetto DICOM
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Permette di recuperare il # di row dell'oggetto DICOM
	 */
	public String getRows() {
		return rows;
	}

	/**
	 * Permette di impostare il # di rows dell'oggetto DICOM
	 * @param rows # di row dell'oggetto DICOM
	 */
	public void setRows(String rows) {
		this.rows = rows;
	}

	/**
	 * Permette di recuperare il # di column dell'oggetto DICOM
	 */
	public String getColumns() {
		return columns;
	}

	/**
	 * Permette di impostare il # di columns dell'oggetto DICOM
	 * @param columns # di column dell'oggetto DICOM
	 */
	public void setColumns(String columns) {
		this.columns = columns;
	}



	/**
	 * ID dell'oggetto DICOM
	 */
	private String id;
	
	/**
	 * Numero di frame dell'oggetto DICOM
	 */
	private String frameNumber;
	
	/**
	 * #Row dell'oggetto DICOM
	 */
	private String rows;
	
	/**
	 * Columns dell'oggetto DICOM
	 */
	private String columns;
}
