/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.queryRetrieve;

//--deletedAudit.AuditLoggerFactory;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4che.util.DcmURL;


class MoveForwarder implements DcmServiceBase.MultiDimseRsp, DimseListener{

	private static String loggersConfFile=null;
	private static String loggingDir=null;
	private static final String[] TRANSFER_SYNTAXES={UIDs.ImplicitVRLittleEndian};	// DTODO: use a reference to the scp!!!
	private static final int acTimeout=25000;		// DTODO: Take it from the .properties file, for instance!!!
	private static final int dimseTimeout=0;		// DTODO: Take it from the .properties file, for instance!!!
	private static final int soCloseDelay=500;		// DTODO: Take it from the .properties file, for instance!!!
	private static final boolean packPDVs=false;		// DTODO: Take it from the .properties file, for instance!!!

	private final static AuditLoggerFactory alf = AuditLoggerFactory.getInstance();
    private final static AssociationFactory assocFact = AssociationFactory.getInstance();
    private final static DcmParserFactory parserFact = DcmParserFactory.getInstance();
    private final static DcmObjectFactory objFact = DcmObjectFactory.getInstance();
    private final static UIDDictionary uidDict = DictionaryFactory.getInstance().getDefaultUIDDictionary();
    private final static Dataset mergeKeys = objFact.newDataset();

	private int currentStatus=-1;
    private boolean cancel = false;
    private final DcmURL forwardTo;			
//    private final String moveOrigAET;
//    private final int moveOrigMsgID;
    private int curIndex = 0;
    private ActiveAssociation activeAssoc;		// Toward the Move requester
    private ActiveAssociation toScp;		// Toward the actual Move SCP
    private int connectTO = 5000;
	private boolean waitingForResponse=false;
	private Object aMonitor=new Object();		// To synchronize the Threads!!!
	private Dimse newDimse=null;		// This is the one which I got from the Move SCP!
	private Dataset data=null;
	private static Log log = LogFactory.getLog("MoveForwarder");

    public MoveForwarder(ActiveAssociation assoc, Dimse req, Command rspCmd, DcmURL forwardTo) throws DcmServiceException{
		// DTODO: this should just prepare the association and forward the Dataset and Command, with the called AET caught from the URL, or better, passed as argument
        // DTODO: to prepare a connection, look at RouterModuleMDBean.java
        this.activeAssoc = assoc;
        this.forwardTo = forwardTo;
		try{
			data=req.getDataset();	
                        //log.debug("uno");// The original one
			Command cmd=req.getCommand();		// The original one
                        //log.debug("due");
                        //log.debug("due e mezzo" + data.getString(Tags.SOPClassUID));
                        log  .debug("forward to: " + forwardTo.toString());
                        
			toScp=openAssoc(forwardTo, req.getCommand().getAffectedSOPClassUID()/*, cmd*/);		// Now I have an open connection: I can forward data!
                        //log.debug("tre");
			actuallySend(toScp, data, cmd);
                        log.debug("request forwarding completed, now listening");
		}catch(IOException ioex){
			ioex.printStackTrace();
		}catch(InterruptedException iex){
			iex.printStackTrace();
		}catch(Exception e){e.printStackTrace();}


//        this.moveOrigAET = moveOrigAET;
//        this.moveOrigMsgID = moveOrigMsgID;
    }

	private ActiveAssociation openAssoc(DcmURL url, String sopClass) throws IOException{
		Association assoc = assocFact.newRequestor(newSocket(url.getHost(), url.getPort()));	// DTODO: what to support TLS?
		AAssociateRQ assocRQ = assocFact.newAAssociateRQ();
		assocRQ.setCalledAET(url.getCalledAET());
		assocRQ.setCallingAET(url.getCallingAET());
		assocRQ.addPresContext(assocFact.newPresContext(assocRQ.nextPCID(), sopClass, TRANSFER_SYNTAXES));
		assoc.setAcTimeout(acTimeout);
		assoc.setDimseTimeout(dimseTimeout);
		assoc.setSoCloseDelay(soCloseDelay);
		assoc.setPackPDVs(packPDVs);
		PDU assocAC = assoc.connect(assocRQ);
		if (!(assocAC instanceof AAssociateAC)) {
			return null;
		}
		ActiveAssociation retval = assocFact.newActiveAssociation(assoc, null);
		retval.start();
		return retval;
	}

    private java.net.Socket newSocket(String host, int port) throws IOException{		// DTODO: TLS is not yet taken into account!!!
		return new java.net.Socket(host, port);
    }

		private void actuallySend(ActiveAssociation a, Dataset d, Command cmd) throws IOException, InterruptedException{
            FileMetaInfo fmi = d.getFileMetaInfo();
            //String classUID = fmi.getMediaStorageSOPClassUID();
            String classUID = cmd.getAffectedSOPClassUID();
            String destination= cmd.getString(Tags.MoveDestination);
            List<PresContext> pcs = a.getAssociation().listAcceptedPresContext(classUID);
            if (pcs.isEmpty()) {
                return;
            }
			PresContext pc = pcs.get(0);
			//cmd.set(); // DTODO: Set what needs to be changed
            cmd.initCMoveRQ(a.getAssociation().nextMsgID(), classUID, Command.MEDIUM, a.getAssociation().getCalledAET());
            //c-move is created to self destination
            //adding destination
            cmd.putAE(Tags.MoveDestination,destination);
          //log.debug("destionation is C: " + cmd.getString(Tags.MoveDestination));
          //log.debug("destionation is D: " + d.getString(Tags.MoveDestination));
            a.invoke(assocFact.newDimse(pc.pcid(), cmd, data)/*The old dataset*/, 
					new DimseListener(){	// DTODO: deal with threads!!!
						public void dimseReceived(Association assoc, Dimse dimse){
							synchronized(aMonitor){
								log.debug("FORWARDER Inside the Listener!");
								newDimse=dimse;
								waitingForResponse=false;	// Now next() can go on and forward the response
								currentStatus=newDimse.getCommand().getStatus();
								aMonitor.notifyAll();
							}	// end sync
						}
					}
				);
			waitingForResponse=true;	// Now I am waiting for a CMoveResponse from the SCP!
			//DTODO??? if(rsp.get().getCommand().getStatus()!=Status.Success)...
	}

    public void dimseReceived(Association assoc, Dimse dimse){	// Called if a CANCEL is received
		cancel=true;	
		log.debug("FORWARDER HEY I'm in DimseReceived!!! Why did this happen???");
    }


    public DimseListener getCancelListener(){	// DTODO: look at StorageSCU
        return this;
    }


    public Dataset next(ActiveAssociation assoc, Dimse rq, Command rspCmd)throws DcmServiceException{
		// DTODO: look at StorageSCU and deal with cancel
		boolean toRelease=false;
		Dataset data=null;
		// DTODO: This should receive() a move response and send it to the original requestor while rspCmd.status==pending
		synchronized(aMonitor){
			try{
				while(waitingForResponse)aMonitor.wait();	// Don't do anything till a response arrives form the SCP and the DimseListener sets waitingForResponse to false!!!
			// Now write newDimse and newCom to the original CMove SCU, setting appropriate states!
			// Use activeAssoc
			// Set the status of the respsonse to that of the received one!
					if(!waitingForResponse){	// If I am free to write...
                                                log.debug("I've already received a response");
						//if((newDimse.getCommand().getStatus()!=Status.Pending))toRelease=true;
						if (currentStatus!=65280)toRelease=true;
                                                data=newDimse.getDataset();
						//rq=newDimse;
						//log.debug("The  old Status is: "+rspCmd.getStatus());
					//	rspCmd=newDimse.getCommand();
						waitingForResponse=true;
						//log.debug("The new Status is: "+rspCmd.getStatus());
					}
					rspCmd.putUS(Tags.Status, currentStatus);
					log.debug("The Status is: "+currentStatus+" - "+rspCmd.getStatus());
		            //rspCmd.remove(Tags.NumberOfRemainingSubOperations);

				if(toRelease){
                                    log.debug("I should release associations");
                                    //log.debug("association to MoveSCU");
                                    //activeAssoc.release(true);
                                    log.debug("association to StoreSCP");
                                    toScp.release(true);
                                   // log.debug("In this version, others should close associations");
				}
			}catch(IOException ioex){
				ioex.printStackTrace();
			}catch(InterruptedException iex){
				iex.printStackTrace();
			}
		}
                log.debug("Outside the monitor");
		return data;		// DTODO: what about sync?
		// At the end (status=cancel or failure or success) release both associations!!!
	}


   public void release(){			// DTODO: check whether it's correct
    /*  if (activeAssoc != null) {
            try {/**/
				log.debug("Closing a connection!");
                //activeAssoc.release(true);
            /*} catch (Exception e) {
                log.debug();
				e.printStackTrace();
			}
            activeAssoc = null;
        }/**/
    }

}	// end class*/

