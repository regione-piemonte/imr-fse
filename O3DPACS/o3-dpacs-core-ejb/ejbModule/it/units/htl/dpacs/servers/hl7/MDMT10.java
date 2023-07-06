/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import it.units.htl.dpacs.servers.HL7Server;
import it.units.htl.dpacs.valueObjects.MoveStudyHistory;

import java.util.Timer;


import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.util.Terser;

public class MDMT10 extends HL7Dealer {
	
	protected String structASR;  //ZM 20121115
	private String management;
	private MoveStudyHistory moveSH;
	
	public MDMT10(HL7Server hl7) {
		super(hl7);
		daysToEmptyCache=Integer.parseInt(hl7.getDaysToEmptyHL7Cache());
		management=hl7.getMdmService();
	}

	private int daysToEmptyCache=-1;
	private static Timer emptyCacheTimer = null;

	protected void parse(Message hm) throws HL7Exception{
		
//		if(MDM_MGMT_STUDYMOVE.equals(management)){		// Reflection would be better, instantiating the right separate class 
//		
//			moveSH = new MoveStudyHistory();		// parse() and run() are always called from a synchronized processMessage()
//			String messageControlId=null;
//			Segment pid = null; // I can have one PID segment
//			Segment pv1 = null; // I can have one PV1 segment
//			Segment obx = null; // I can have one OBX segment
//	
//	
//			Terser terser=new Terser(hm);
//	
//	        try{
//	        	pid=terser.getSegment(TERSER_PID);
//	        }catch (HL7Exception hex){
//	        	throw new HL7SegmentException("No PID segment");		// Must generate an AR
//	        }
//	        try{
//	        	pv1=terser.getSegment(TERSER_PV1);
//	        }catch (HL7Exception hex){
//	        	throw new HL7SegmentException("No PV1 segment");		// Must generate an AR
//	        }
//		        
//        
//	        try{
//	        	String currentRepetition=null;
//	        	int i=0;
//	        	do{
//	        		currentRepetition=terser.get("/.OBXNTE("+i+")/OBX-2");	// Get the one with RP as OBX-2
//	        		if("RP".equals(currentRepetition))
//	        			obx=terser.getSegment("/.OBXNTE("+i+")/OBX");				
//	        		i++;
//	        	}while(currentRepetition!=null && obx==null);
//	        	if(obx==null)
//	        		throw new HL7Exception("Required OBX segment missing (OBX-2=RP)");
//	        }catch (HL7Exception hex){
//	        	throw new HL7SegmentException(hex.getMessage());		// Must generate an AR
//	        }
//	        
//	        try{
//	        	messageControlId=terser.get("/.MSH-10");
//	        }catch (HL7Exception hex){
//	        	throw new HL7SegmentException("No Message Control ID segment");		// Must generate an AR
//	        }
//	        
//	        
//
//	       	moveSH.setAccessionnumber(get(obx,5,1,1));
//	       	moveSH.setRis("---");
//	       	String pid3 = get(pid,3);
//	       	String arPid3[] = pid3.split("~");
//	       	
//	       	for (int kk=0;kk<arPid3.length;kk++){
//	       		if ((arPid3[kk].indexOf("^RIS")>=0)){
//	       			String vRis[] = arPid3[kk].split("\\^");
//	       			moveSH.setRis(vRis[vRis.length-1]);
//	       		}
//	       	}
//	       	
//	       	
//	       	moveSH.setMessageId(messageControlId);
//	       	String aa[]=get(pv1,3,1,4).split("&");
//	       	structASR = aa[0];
//
//		}
        
	}

	@Override
	protected void run() throws Exception{ 
		log.info("Received MDM^T10 skip hl7" );
        //bean.insertMoveStudyHistory(moveSH, structASR);
        if (emptyCacheTimer == null) {
            emptyCacheTimer = new Timer();
            emptyCacheTimer.schedule(new TransferEraser(), System.currentTimeMillis() + (daysToEmptyCache * MILLIS_PER_DAY), daysToEmptyCache * MILLIS_PER_DAY);
        }

	}
	
	private class TransferEraser extends java.util.TimerTask {
        public void run() {
//            try {
//                Context jndiCon = new InitialContext();
//                HL7BeforeTransferELocalHome homeTrans = (HL7BeforeTransferELocalHome) jndiCon.lookup("java:comp/env/ejb/BeforeTransferData");
//                jndiCon.close();
//                
//                Collection<HL7BeforeTransferELocal> ct = homeTrans.findOlderThan(System.currentTimeMillis() - (daysToEmptyCache * MILLIS_PER_DAY));
//                // Now iterate through each item and call remove() on it
//                for (HL7BeforeTransferELocal item : ct) {
//                    item.remove();
//                }
//            } catch (Exception ex) {
//                // Nothing particular should be done when in production!
//				log.error("", ex);
//            } // end try...catch
        }
    }

}
