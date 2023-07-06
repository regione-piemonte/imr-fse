/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.queryRetrieve;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DataSource;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.PresContext;
import org.dcm4che2.data.Tag;

import it.units.htl.dpacs.accessors.Accessor;
import it.units.htl.dpacs.accessors.AccessorFactory;
import it.units.htl.dpacs.helpers.AEData;
import it.units.htl.dpacs.helpers.Anonymizer;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.helpers.MBeanServerUtils;
import it.units.htl.dpacs.helpers.RetrieveData;
import it.units.htl.dpacs.helpers.Compression;
import it.units.htl.dpacs.helpers.CompressionSCP;
import it.units.htl.dpacs.helpers.TransferSyntax;
import it.units.htl.dpacs.statistics.Timer;
import it.units.htl.dpacs.valueObjects.InstancesAction;
import it.units.htl.dpacs.valueObjects.NearlineData;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

class StorageSCU implements DcmServiceBase.MultiDimseRsp, DimseListener {

	private Compression compress = null;
	private static Log log = LogFactory.getLog(StorageSCU.class);
	private static final AssociationFactory assocFact = AssociationFactory.getInstance();
	private static final DcmParserFactory parserFact = DcmParserFactory.getInstance();
	private static final DcmObjectFactory objFact = DcmObjectFactory.getInstance();
	private static final UIDDictionary uidDict = DictionaryFactory.getInstance().getDefaultUIDDictionary();
	private static final Dataset mergeKeys = objFact.newDataset();
	private static final String MOVE_TEMP_SOURCE = ".MOVE.tmp.src";
	private static final String MOVE_TEMP_DIST = ".MOVE.temp";

	static {
		mergeKeys.putCS(Tags.SpecificCharacterSet);
		mergeKeys.putLO(Tags.PatientID);
		mergeKeys.putLO(Tags.IssuerOfPatientID);
		mergeKeys.putPN(Tags.PatientName);
		mergeKeys.putDA(Tags.PatientBirthDate);
		mergeKeys.putCS(Tags.PatientSex);
	}
	private final QueryRetrieveSCP scp;
	private final InstancesAction action;
	private boolean cancel = false;
	private final AEData moveDest;
	private final RetrieveData[] toMove;
	private final String moveOrigAET;
	private final int moveOrigMsgID;
	private int curIndex = 0;
	private ActiveAssociation activeAssoc;
	// private final RemoteNode rnode;
	// private int connectTO = 5000;
	private Association origAssociation;
	private Accessor accessor;
	private String lastAccessorType;
	
	private String _originalPatientID = "";
	private String _originalPatientName = "";
	

	// Constructors --------------------------------------------------
	public StorageSCU(QueryRetrieveSCP scp, ActiveAssociation activeAssoc, Association origAssoc, 
	        RetrieveData[] toMove, AEData moveDest, String moveOrigAET, int moveOrigMsgID, InstancesAction action)
			throws DcmServiceException {
	    
		this.accessor = null;
		this.origAssociation = origAssoc;
		this.scp = scp;
		this.activeAssoc = activeAssoc;
		// this.rnode = activeAssoc != null ?
		// toRemoteNode(activeAssoc.getAssociation()) : null;
		this.toMove = toMove;
		this.moveDest = moveDest;
		this.moveOrigAET = moveOrigAET;
		this.moveOrigMsgID = moveOrigMsgID;
		this.action = action;
		
		if (action != null) {

			String studyUid = "Affected studies in retrieve: ";
			String[] studyList = action.listStudyInstanceUIDs();
			for (int i = 0; i < studyList.length; i++) {
				studyUid.concat(studyList[i] + " - ");
			}
			studyUid.concat(".");
			action.setNumberOfInstances(0);
			action.clearSOPClassUIDs();
			if (compress == null) {
				MBeanServer mbs = null;
				mbs = MBeanServerUtils.getDpacsMBeanServer();
				try {
					//compress = (Compression) mbs.invoke(new ObjectName("it.units.htl.dpacs.helpers" + ":type=CompressionSCP,index=9"), "getInstance", new Object[] {}, new String[] {});
	    			ObjectName objectName = new ObjectName("it.units.htl.dpacs.helpers" + ":type=CompressionSCP,index=9");
	    			MBeanInfo mbi = mbs.getMBeanInfo(objectName);

	                for (int i = 0; i < mbi.getAttributes().length; i++) {
	                    String attributeName = mbi.getAttributes()[i].getName();
	                    if(attributeName.equals("Instance"))
	                    {
	                    	compress = (Compression) mbs.getAttribute(objectName, attributeName);
	                    	break;
	                    }
	                } 
	                log.debug(moveOrigAET + ": Compressor loaded ");
				} catch (Exception e) {
				    log.error("No compressionSCP found!",e);
				}
			}
		}
	}

	public void dimseReceived(Association assoc, Dimse dimse) {
		if (dimse.getCommand().getCommandField() == Command.C_CANCEL_RQ) {
			cancel = true;
		}
	}

	public DimseListener getCancelListener() {
		return this;
	}

	long tot = 0;
	
	public Dataset next(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws DcmServiceException {
		Timer t = new Timer();
		t.start();
		if (cancel) {
			log.debug("cancel received, stopping to send images");
			release();
			// do not write the response to the association, it is done by
			// DcmServiceBase(dcm4che) at the end of each next method
			rspCmd.putUS(Tags.Status, Status.Cancel);
			t.stop();
			return null;
		}
		if (origAssociation == null) {
			log.debug("original MOVE association is null");
		} else {
			log.debug("Continuing to send images, original move association from : " + origAssociation.getCallingAET());
		}
		if (curIndex == toMove.length) {
			rspCmd.putUS(Tags.Status, rspCmd.getInt(Tags.NumberOfFailedSubOperations, 0) == 0 ? Status.Success : Status.SubOpsOneOrMoreFailures);
			rspCmd.remove(Tags.NumberOfRemainingSubOperations);
			release();
			t.stop();
			log.info(assoc.getAssociation().getCallingAET() + ": file transfer to " + moveDest.getTitle() + " ended in " + tot + "ms.");
			return null;
		}

		sendNext(toMove[curIndex++], rspCmd);
		tot += t.getMeasure();
		log.debug(moveOrigAET + ": sended " + curIndex + "/" + toMove.length);
		return null;
	}

	public void release() {
		if (activeAssoc != null) {
			try {
			    if(cancel){
                    activeAssoc.release(false);
                }else{
                    activeAssoc.release(true);
                }
			} catch (Exception e) {
				log.error("Release failed:", e);
			}
			activeAssoc = null;
		}
		if (accessor != null)
			accessor.close();
	}

	private void updateRsp(Command rspCmd, int tag) {
		rspCmd.putUS(Tags.NumberOfRemainingSubOperations, rspCmd.getInt(Tags.NumberOfRemainingSubOperations, 0) - 1);
		rspCmd.putUS(tag, rspCmd.getInt(tag, 0) + 1);
	}

	protected void finalize() throws Throwable {
		try {
			if (accessor != null)
				accessor.close();
		} finally {
			super.finalize();
		}

	}

	@SuppressWarnings("unchecked")
	private void sendNext(RetrieveData retrieveData, final Command rspCmd) {
		InputStream in = null;
		File temp = null;
		File temp2 = null;
		String newUrl = null;
		NearlineData nd = retrieveData.getNearlineData();
		if (nd != null)
			newUrl = retrieveData.getNearlineData().getDirectUrl();
		else
			newUrl = retrieveData.getURL().replace("file://", "");
		log.debug(moveOrigAET + ": StorageSCU moving to " + newUrl);
		String tempUrlDir = CompressionSCP.TempDir;
		File tempUrlDirFile = new File(tempUrlDir);
		log.debug(moveOrigAET + ": temporary directory is: " + tempUrlDirFile.getAbsolutePath());
		String instUID = "";
		try {
			if (nd != null) {
				// I need to access the nearline media
				if (accessor == null) { // First time I need the accessor
					accessor = AccessorFactory.getAccessor(nd.getDeviceType(), nd.getDeviceUrl(), nd.getCredentials());
					lastAccessorType = nd.getDeviceType();
				} else {
					// I already used one accessor
					if ((!lastAccessorType.equals(nd.getDeviceType())) || (!accessor.getDeviceUrl().equals(nd.getDeviceUrl()))) {
						// If it's not the same as above
						accessor.close();
						accessor = AccessorFactory.getAccessor(nd.getDeviceType(), nd.getDeviceUrl(), nd.getCredentials());
						lastAccessorType = nd.getDeviceType();
					}
				}
				in = accessor.getFile(newUrl);
			} else {
				in = new FileInputStream(new File(newUrl));
			}
			// Parsing original file
			Dataset ds = objFact.newDataset();
			DcmParser parser = parserFact.newDcmParser(in);
			parser.setDcmHandler(ds.getDcmHandler());
			parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
			FileMetaInfo fmi = ds.getFileMetaInfo();
			String classUID = fmi.getMediaStorageSOPClassUID();
			instUID = fmi.getMediaStorageSOPInstanceUID();
			String tempUrl = tempUrlDir + instUID + MOVE_TEMP_DIST;
			log.debug("Original file has been parsed");
			log.debug("COUNTACCEPTEDPRESCONTEXT: " + activeAssoc.getAssociation().countAcceptedPresContext());
			List<PresContext> pcs = activeAssoc.getAssociation().listAcceptedPresContext(classUID);
			log.debug("COUNTACCEPTEDPRESCONTEXT: " + activeAssoc.getAssociation().countAcceptedPresContext());
			if (pcs.isEmpty()) {
				log.info("Cannot move " + uidDict.toString(classUID) + " to " + moveDest + " (service rejected)");
				updateRsp(rspCmd, Tags.NumberOfFailedSubOperations);
				return;
			}
			ds.putAll(retrieveData.getDataset().subSet(mergeKeys));
			// the transfer syntax of the instance
			TransferSyntax instanceTransferSyntax = TransferSyntax.getTransferSyntaxByName(fmi.getTransferSyntaxUID());
			PresContext pc = (PresContext) getPresContextByTSUID(pcs, instanceTransferSyntax);
			// Define if the compression on outcoming image is forced
			String forcedTsUidString = CompressionSCP.getCompressionTransferSyntax(moveDest.getTitle());
			Dataset compressedDataset = null;
			try {
				if ("Images".equalsIgnoreCase(retrieveData.getType())) {
					// if pacs has to compress outgoing images for this node,
					// and this node can receive compressed images and
					// the image is ImplicitVRLittleEndian or
					// ExplicitVRLittleEndian i can compress that!
					if ((forcedTsUidString != null) && (getPresContextByTSUID(pcs, TransferSyntax.getTransferSyntaxByName(forcedTsUidString)) != null)
							&& ((instanceTransferSyntax.equals(TransferSyntax.ImplicitVRLittleEndian) || instanceTransferSyntax.equals(TransferSyntax.ExplicitVRLittleEndian)))) {
						log.debug(moveOrigAET + ": have to compress image, and i can do it!");
						TransferSyntax forcedTsUid = TransferSyntax.getTransferSyntaxByName(forcedTsUidString);
						pc = getPresContextByTSUID(pcs, forcedTsUid);
						boolean created = tempUrlDirFile.exists();
						if (!created) {
							created = tempUrlDirFile.mkdir();
							if (!created) {
								log.fatal(moveOrigAET + "Cannot create temp directory!: " + tempUrlDirFile.getAbsolutePath());
								return;
							} else
								log.debug(moveOrigAET + "Tempdir cretated");
						}
						log.debug(moveOrigAET + ": Compression is forced with transfer syntax " + forcedTsUid);
						log.debug("Destination path of compressed file: " + tempUrl);
						if (nd != null) // This does not write the file locally
							temp = compress.compressImage(accessor.getFile(newUrl), forcedTsUid.getValue(), tempUrl, VRs.OB);
						else
							temp = compress.compressImage(new File(newUrl), forcedTsUid.getValue(), tempUrl, VRs.OB);
						if (temp != null) {
							compressedDataset = readImage(tempUrl, forcedTsUid);
							ds = compressedDataset;
							in = new FileInputStream(tempUrl);
							parser = parserFact.newDcmParser(in);
							parser.setDcmHandler(ds.getDcmHandler());
							parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
							fmi = ds.getFileMetaInfo();
							log.debug("Compression done.");
						}
					}
					// if transfer syntax of the original image is not supported
					else if ((pc == null) && ((!instanceTransferSyntax.equals(TransferSyntax.ImplicitVRLittleEndian)) && (!instanceTransferSyntax.equals(TransferSyntax.ExplicitVRLittleEndian)))) {
						// if i haven't compress but instanceTS is not supported
						log.info(moveOrigAET + ": File has TS: " + instanceTransferSyntax + "...not supported by destination!");
						boolean created = tempUrlDirFile.exists();
						if (!created) {
							created = tempUrlDirFile.mkdir();
							if (!created) {
								log.fatal("Cannot create temp dir: " + tempUrlDirFile.getAbsolutePath());
								return;
							} else
								log.debug("Created temp dir " + tempUrlDirFile.getAbsolutePath());
						}
						log.debug("temp = " + tempUrl);
						File sourceFile = null;
						boolean toDelete = false;
						if (nd != null) {
							// If study is nearline, bring the file to the temp
							// dir, since needed by implementation of
							// decompressImage!!!
							FileOutputStream outFile = new FileOutputStream(tempUrlDir + instUID + MOVE_TEMP_SOURCE + moveDest.getTitle());
							InputStream is = accessor.getFile(newUrl);
							int nextChar = -1;
							while ((nextChar = is.read()) != -1) {
								outFile.write(nextChar);
							}
							try {
								outFile.close();
							} catch (Exception ex) {
							}
							try {
								is.close();
							} catch (Exception ex) {
							}

							sourceFile = new File(tempUrlDir + instUID + MOVE_TEMP_SOURCE + moveDest.getTitle());
							toDelete = true;
						} else {
							sourceFile = new File(newUrl);
						}
						log.debug(moveOrigAET + ": Taking the first deflated presentation context...");
						pc = (PresContext) pcs.get(0);
						TransferSyntax tsDec = TransferSyntax.getTransferSyntaxByName(pc.getTransferSyntaxUID());
						log.debug(moveOrigAET + ": Decompressing with transfer syntax " + tsDec);
						temp = compress.decompressImage(sourceFile, pc.getTransferSyntaxUID(), tempUrl);
						log.debug(moveOrigAET + ": Decompression ended!");
						in = new FileInputStream(temp);
						parser = parserFact.newDcmParser(in);
						parser.setDcmHandler(ds.getDcmHandler());
						parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
						fmi = ds.getFileMetaInfo();
						if (toDelete) {
							sourceFile.delete(); // Delete the newly created
							// file
						}
					} else if (pc == null) {
						log.warn("Basic (Implicit&Explicit VR LittleEndian) TS is not supported by " + moveDest);
						return;
					}
				} else {
					log.debug(moveOrigAET + ": This object isn't an image, don't needs compression.");
				}
			} catch (IOException ioe) {
				log.error("", ioe);
				return;
			}

			Command cmd = objFact.newCommand();
			cmd.initCStoreRQ(activeAssoc.getAssociation().nextMsgID(), classUID, instUID, Command.MEDIUM);
			cmd.setMoveOriginator(moveOrigAET, moveOrigMsgID);
			
			
//          modify the dataset before the sending if the nod is anon
			Anonymizer _anonimizer = new Anonymizer();
			if(moveDest.isAnonymized()){
				boolean removePatId=Anonymizer.hasToRemovePatientId(moveDest.getTitle());
                ds = _anonimizer.anonymize(ds, false, removePatId);
            }else if(scp.isPartiallyAnonymized() && !moveDest.canDeanonymized()){
                ds = _anonimizer.removeNameAndPatientId(ds);
            }		
			
			activeAssoc.invoke(assocFact.newDimse(pc.pcid(), cmd, new MyDataSource(parser, ds)), new DimseListener() {
				public void dimseReceived(Association assoc, Dimse dimse) {
					switch (dimse.getCommand().getStatus()) {
					case Status.Success:
						updateRsp(rspCmd, Tags.NumberOfCompletedSubOperations);
						break;
					case Status.CoercionOfDataElements:
					case Status.DataSetDoesNotMatchSOPClassWarning:
					case Status.ElementsDiscarded:
						updateRsp(rspCmd, Tags.NumberOfWarningSubOperations);
						break;
					default:
						log.fatal(moveOrigAET + ": Move failed: " + dimse.getCommand());
						rspCmd.putUS(Tags.Status, Status.UnableToPerformSuboperations);
						updateRsp(rspCmd, Tags.NumberOfFailedSubOperations);
						break;
					}
				}
			});
			action.incNumberOfInstances(1);
			action.addSOPClassUID(classUID);
		} catch (Exception e) {
			log.error(moveOrigAET + ": move of this instance failed: " + instUID, e);
			rspCmd.putUS(Tags.Status, Status.UnableToPerformSuboperations);
			updateRsp(rspCmd, Tags.NumberOfFailedSubOperations);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error("", e);
				}
			}
			if (temp != null) {
				if (!temp.delete()) {
					log.warn("Cannot delete temporary file!");
				}
			}
			if (temp2 != null) {
				if (!temp2.delete()) {
					log.warn("Cannot delete temporary file!");
				}
			}

		}
	}

	private static final class MyDataSource implements DataSource {

		final DcmParser parser;
		final Dataset ds;

		MyDataSource(DcmParser parser, Dataset ds) {
			this.parser = parser;
			this.ds = ds;
		}

		public void writeTo(OutputStream out, String tsUID) throws IOException {
			DcmEncodeParam netParam = (DcmEncodeParam) DcmDecodeParam.valueOf(tsUID);
			ds.writeDataset(out, netParam);
			if (parser.getReadTag() == Tags.PixelData) {
				DcmDecodeParam fileParam = parser.getDcmDecodeParam();
				ds.writeHeader(out, netParam, parser.getReadTag(), parser.getReadVR(), parser.getReadLength());
				// if
				// (
				// !UIDs.JPEG2000Lossless.equalsIgnoreCase(tsUID)
				// &&
				// !UIDs.JPEGLossless.equalsIgnoreCase(tsUID)
				// )
				// copy(parser.getInputStream(), out, parser.getReadLength());
				copyall(parser.getInputStream(), out);
				ds.clear();
				parser.parseDataset(fileParam, Tags.PixelData);
				ds.writeDataset(out, netParam);
			}
		}
	}

	// private static void copy(InputStream in, OutputStream out, int len)
	// throws IOException {
	// byte[] buffer = new byte[512];
	// int c;
	// int remain = len;
	// while (remain > 0) {
	// c = in.read(buffer, 0, Math.min(buffer.length, remain));
	// out.write(buffer, 0, c);
	// remain -= c;
	// }
	// }

	private static void copyall(InputStream in, OutputStream out) throws IOException {
		int c;
		byte[] buffer = new byte[512];
		while ((c = in.read(buffer)) != -1) {
			out.write(buffer, 0, c);
		}
	}

	private Dataset readImage(String path, TransferSyntax tsuid) {
		Dataset data = objFact.newDataset();
		InputStream in = null;
		DcmParser parser = null;
		try {
			in = new FileInputStream(new File(path));

			log.debug(moveOrigAET + ": Reading compressed (eventually temporary) file...");

			parser = parserFact.newDcmParser(in);
			parser.setDcmHandler(data.getDcmHandler());
			parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
			FileMetaInfo fmi = objFact.newFileMetaInfo(data, tsuid.getValue());
			data.setFileMetaInfo(fmi);
			in.close();
		} catch (IOException ioe) {
			log.fatal("Cannot read the temporary file: ", ioe);

		}

		return data;
	}

	@SuppressWarnings("unchecked")
	private PresContext getPresContextByTSUID(List<PresContext> pcs, TransferSyntax ts) {
		PresContext pc = null;
		for (int i = 0; i < pcs.size(); i++) {
			PresContext pCon = (PresContext) pcs.get(i);
			List<String> tsUIDs = pCon.getTransferSyntaxUIDs();
			for (int j = 0; j < tsUIDs.size(); j++) {
				TransferSyntax tsuid = TransferSyntax.getTransferSyntaxByName(tsUIDs.get(j));
				if (tsuid.equals(ts)) {
					pc = pCon;
					log.debug("Taken Pres Context: " + pc.pcid());
				}
			}
		}
		return pc;
	}
}
