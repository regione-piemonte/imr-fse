/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

public class BeansName {
//  if the field starts with L is the Local interface of the bean, if R is the remote interface
//    public static final String LDicomDbDealer = "o3-dpacs/DicomDbDealerBean/local";
    public static final String LDicomDbDealer = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/DicomDbDealerBean!it.units.htl.dpacs.dao.DicomDbDealer";
//    public static final String LDeletionDealer = "o3-dpacs/DeletionDealerBean/local";
    public static final String LDeletionDealer = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/DeletionDealerBean!it.units.htl.dpacs.dao.DeletionDealer"; 
//    public static final String LMoveDealer = "o3-dpacs/DicomMoveDealerBean/local";
    public static final String LMoveDealer = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/DicomMoveDealerBean!it.units.htl.dpacs.dao.DicomMoveDealerLocal";
//    public static final String RMoveDealer = "o3-dpacs/DicomMoveDealerBean/remote";
    public static final String RMoveDealer = "java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DicomMoveDealerBean!it.units.htl.dpacs.dao.DicomMoveDealerRemote";
//    public static final String LMppsDealer = "o3-dpacs/DicomMppsDealerBean/local";
    public static final String LMppsDealer = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/DicomMppsDealerBean!it.units.htl.dpacs.dao.DicomMppsDealerLocal";
//    public static final String LQueryDealer = "o3-dpacs/DicomQueryDealerBean/local";
    public static final String LQueryDealer = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/DicomQueryDealerBean!it.units.htl.dpacs.dao.DicomQueryDealerLocal";
//    public static final String RQueryDealer = "o3-dpacs/DicomQueryDealerBean/remote";
    public static final String RQueryDealer = "java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DicomQueryDealerBean!it.units.htl.dpacs.dao.DicomQueryDealerRemote";
//    public static final String LDicomStorageCommitmentDealer = "o3-dpacs/DicomStgCmtDealerBean/local";
    public static final String LDicomStorageCommitmentDealer = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/DicomStgCmtDealerBean!it.units.htl.dpacs.dao.DicomStgCmtDealerLocal";
//    public static final String LDicomStorageDealer = "o3-dpacs/DicomStorageDealerBean/local";
    public static final String LDicomStorageDealer = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/DicomStorageDealerBean!it.units.htl.dpacs.dao.DicomStorageDealerLocal";
//    public static final String LHl7Dealer = "o3-dpacs/HL7DealerBean/local";
    public static final String LHl7Dealer = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/HL7DealerBean!it.units.htl.dpacs.dao.HL7DealerLocal";
//    public static final String RHl7Dealer = "o3-dpacs/HL7DealerBean/remote";
    public static final String RHl7Dealer = "java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/HL7DealerBean!it.units.htl.dpacs.dao.HL7DealerRemote";    
//    public static final String LImageAvailability = "o3-dpacs/ImageAvailabilityBean/local";
    public static final String LImageAvailability = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/ImageAvailabilityBean!it.units.htl.dpacs.dao.ImageAvailabilityLocal";
//    public static final String StudiesVerifierToolsL = "o3-dpacs/StudiesVerifierTools/local";
    public static final String StudiesVerifierToolsL = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/StudiesVerifierTools!it.units.htl.dpacs.postprocessing.verifier.bean.StudiesVerifierToolsLocal";
//    public static final String StudiesVerifierToolsR = "o3-dpacs/StudiesVerifierTools/remote";
    public static final String StudiesVerifierToolsR = "java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/StudiesVerifierTools!it.units.htl.dpacs.postprocessing.verifier.bean.StudiesVerifierToolsRemote";
//    public static final String LStudyMove = "o3-dpacs/StudyMoveBean/local";
    public static final String LStudyMove = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/StudyMoveBean!it.units.htl.dpacs.dao.StudyMoveLocal";
//    public static final String RStudyMove = "o3-dpacs/StudyMoveBean/remote";
    public static final String RStudyMove = "java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/StudyMoveBean!it.units.htl.dpacs.dao.StudyMoveRemote";
//    public static final String LHl7ComDbDealer= "o3-dpacs/Hl7ComDbDealerBean/local";
    public static final String LHl7ComDbDealer= "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/Hl7ComDbDealerBean!it.units.htl.dpacs.servers.hl7.comunication.dao.Hl7ComDbDealerLocal";
//    public static final String LHl7Publisher = "o3-dpacs/Hl7PublisherBean/local";
    public static final String LHl7Publisher = "java:global/o3-dpacs-ear/o3-dpacs-core-ejb/Hl7PublisherBean!it.units.htl.dpacs.dao.Hl7PublisherLocal";
//    public static final String RHl7Publisher = "o3-dpacs/Hl7PublisherBean/remote";
    public static final String RHl7Publisher = "java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/Hl7PublisherBean!it.units.htl.dpacs.dao.Hl7PublisherRemote";
}
