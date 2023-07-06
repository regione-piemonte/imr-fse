/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.core.services;

import java.util.Collection;
import java.util.HashMap;

/**
 * @author sangalli
 */
public class ServicesManager {
    private HashMap<String, Service> dpacsServices = new HashMap<String, Service>();
    private HashMap<Integer, String> servicesID = new HashMap<Integer, String>();
    private static ServicesManager INSTANCE = null;

    public static ServicesManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServicesManager();
        }
        return INSTANCE;
    }

    /*
     * 
     * DicomServer 1
     * HL7Server 2
     * QueryRetrieve 3
     * StorageSCp 4
     * Mpps Service 5
     * Compression Service 6
     * Image Masking Service 7
     * Forwarder Service 8
     * WorkList Service 9
     * StudiesVerifier Service 10
     * AuditLogServer 11
     * StudyEraser	12
     * StudyMovement 13
     * StudyMove 14
     * Hl7CommunicationService 15
     */
    
    
    private ServicesManager() {
        // DicomServer Service
        Service genServ = new Service();
        genServ.setId(1);
        genServ.setMBeanName("it.units.htl.dpacs.servers:type=DicomServer,index=3");
        genServ.setName("DicomServer");
        servicesID.put(1, "DicomServer");
        dpacsServices.put("DicomServer", genServ);
        // HL7Server service
        genServ = new Service();
        genServ.setId(2);
        genServ.setMBeanName("it.units.htl.dpacs.servers:type=HL7Server,index=2");
        genServ.setName("HL7Server");
        servicesID.put(2, "HL7Server");
        dpacsServices.put("HL7Server", genServ);
        // QueryRetrieveSCP service
        genServ = new Service();
        genServ.setId(3);
        genServ.setMBeanName("it.units.htl.dpacs.servers.queryRetrieve:type=QueryRetrieveSCP,index=4");
        genServ.setName("QueryRetrieveSCP");
        // Dependencies
        genServ.setDependency(new Integer[] { 1, 6 });
        servicesID.put(3, "QueryRetrieveSCP");
        dpacsServices.put("QueryRetrieveSCP", genServ);
        // StorageSCP Service
        genServ = new Service();
        genServ.setId(4);
        genServ.setMBeanName("it.units.htl.dpacs.servers.storage:type=StorageSCP,index=5");
        genServ.setName("StorageSCP");
        genServ.setDependency(new Integer[] { 1, 6, 7 });
        servicesID.put(4, "StorageSCP");
        dpacsServices.put("StorageSCP", genServ);
        // Mpps Service
        genServ = new Service();
        genServ.setId(5);
        genServ.setMBeanName("it.units.htl.dpacs.servers.mpps:type=MPPSSCP,index=8");
        genServ.setName("MPPSSCP");
        servicesID.put(5, "MPPSSCP");
        dpacsServices.put("MPPSSCP", genServ);
        // Compression Service (also provide DPACS Temp dir)
        genServ = new Service();
        genServ.setId(6);
        genServ.setMBeanName("it.units.htl.dpacs.helpers:type=CompressionSCP,index=9");
        genServ.setName("CompressionSCP");
        servicesID.put(6, "CompressionSCP");
        dpacsServices.put("CompressionSCP", genServ);
        // Image Masking Service (for Anonymization)
        genServ = new Service();
        genServ.setId(7);
        genServ.setMBeanName("it.units.htl.dpacs.helpers:type=ImageMaskingSCP,index=10");
        genServ.setName("ImageMaskingSCP");
        servicesID.put(7, "ImageMaskingSCP");
        dpacsServices.put("ImageMaskingSCP", genServ);
        // Forwarder Service
        genServ = new Service();
        genServ.setId(8);
        genServ.setMBeanName("it.units.htl.dpacs.forwarder:type=Forwarder,index=11");
        genServ.setName("Forwarder");
        servicesID.put(8, "Forwarder");
        dpacsServices.put("Forwarder", genServ);
        // Worklist Service
        genServ = new Service();
        genServ.setId(9);
        genServ.setMBeanName("it.units.htl.dpacs.servers.worklist:type=WorklistService,index=12");
        genServ.setName("WorkListService");
        servicesID.put(9, "WorkListService");
        dpacsServices.put("WorkListService", genServ);
        
        genServ = new Service();
        genServ.setId(10);
        genServ.setMBeanName("it.units.htl.dpacs.postprocessing.verifier:type=StudiesVerifierBean,index=13");
        genServ.setName("StudiesVerifier");
        servicesID.put(10, "StudiesVerifier");
        dpacsServices.put("StudiesVerifier", genServ);
        
        genServ = new Service();
        genServ.setId(11);
        genServ.setMBeanName("it.units.htl.atna:type=AuditLogService,index=14");
        genServ.setName("AuditLogServer");
        servicesID.put(11, "AuditLogServer");
        dpacsServices.put("AuditLogServer", genServ);
        
        genServ = new Service();
        genServ.setId(12);
        genServ.setMBeanName("it.units.htl.dpacs.deletion:type=StudyEraserBean,index=15");
        genServ.setName("StudyEraser");
        servicesID.put(12, "StudyEraser");
        dpacsServices.put("StudyEraser", genServ);
        
        genServ = new Service();
        genServ.setId(13);
        genServ.setMBeanName("it.units.htl.dpacs.movement:type=StudyMovement,index=16");
        genServ.setName("StudyMovement");
        servicesID.put(13, "StudyMovement");
        dpacsServices.put("StudyMovement", genServ);
        
        genServ = new Service();
        genServ.setId(13);
        genServ.setMBeanName("it.units.htl.dpacs.postprocessing.studymove:type=StudyMoveBean,index=17");
        genServ.setName("StudyMove");
        servicesID.put(13, "StudyMove");
        dpacsServices.put("StudyMove", genServ);
        
        genServ = new Service();
        genServ.setId(14);
        genServ.setMBeanName("it.units.htl.dpacs.servers.hl7.comunication:type=Hl7CommunicationServer,index=18");
        genServ.setName("Hl7CommunicationServer");
        servicesID.put(14, "Hl7CommunicationServer");
        dpacsServices.put("Hl7CommunicationServer", genServ);
    }

    public Collection<Service> getServices() {
        dpacsServices.values();
        return dpacsServices.values();
    }

    public Service getServiceByName(String servName) {
        return dpacsServices.get(servName);
    }

    public Service getServiceById(Integer id) {
        return dpacsServices.get(servicesID.get(id));
    }
}
