/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.MaskingParameter;

import javax.ejb.Local;

import java.util.Hashtable;
import java.util.ArrayList;

@Local
public interface DicomDbDealer {
    // No Exceptions need to be thrown, except for application ones
    public Hashtable<String, String> loadMappingCache();

    public Hashtable<String, String> loadStorageCache();

    public Hashtable<String, String> loadEquipmentCache();

    public ArrayList<String> loadAbstractSyntaxes(int type);

    public String getCompressionTransferSyntaxUID(String aetitle);

    public String getExistingAEs();

    boolean isAnonymized(String aetitle);

    public boolean isImageMaskingEnabled(String aeTitle);

    public MaskingParameter[] getMaskTags(String aeTitle, String modality);

    public boolean isToBeVerified(String aeTitle);
    
    public boolean hasToRemovePatientId(String aeTitle);

    // return if the PACS is an PARTIALLY ANONYMIZED installation
    public boolean isPartiallyAnonymizedInstallation();
    
    public String[] getInstancesInProgress();
    
    public int clearInstancesInProgress();
}