/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.Compression;
import it.units.htl.dpacs.helpers.FileHasher;
import it.units.htl.dpacs.helpers.ImageMasker;
import it.units.htl.dpacs.helpers.MBeanDealer;
import it.units.htl.dpacs.helpers.MBeanServerUtils;
import it.units.htl.dpacs.helpers.RoiMeasure;
import it.units.htl.dpacs.servers.storage.StorageSCP;
import it.units.htl.dpacs.statistics.Timer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.imageio.stream.ImageInputStream;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.DcmServiceException;

public class StoragePerformer {
    static final Log log = LogFactory.getLog(StoragePerformer.class);
    Compression compress = null;
    private String hash;
    
    private static final String FILE_SEPARATOR="/";

    public StoragePerformer() {
    	MBeanServer mbs = null;
    	if (compress == null) {
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
    			
    		} catch (Exception e) {
    		    log.error("No compressionSCP found!!",e);
    		}
    	}
    }

    public File writeDataToDisk(Dataset ds, DcmParser parser, DcmEncodeParam decParam, String basicPath, StorageSCP scp, String forcedTs, boolean isImage, String callingAE, Vector<RoiMeasure> total) throws DcmServiceException {
        File file = null;
        Timer cronoWrite = new Timer();
        try {
            cronoWrite.restart();
            file = toFile(ds, basicPath);
            storeToFile(parser, ds, file, decParam, scp, forcedTs, isImage, callingAE, total);
            cronoWrite.stop();
        } catch (IOException ioex) {
            throw new DcmServiceException(org.dcm4che.dict.Status.ProcessingFailure, ioex);
        }
        log.debug(callingAE + ":File write in: " + cronoWrite.getMeasure() + "ms.");
        return file;
    }

    private File toFile(Dataset ds, String whereToStore) throws DcmServiceException {

        StringBuilder path= new StringBuilder(whereToStore);
        path.append(FILE_SEPARATOR).append(ds.getString(Tags.StudyInstanceUID));
        path.append(FILE_SEPARATOR).append(ds.getString(Tags.SeriesInstanceUID));
        path.append(FILE_SEPARATOR).append(ds.getString(Tags.SOPInstanceUID));
        return new File(path.toString());
        
    }


    private void storeToFile(DcmParser parser, Dataset ds, File file, DcmEncodeParam encParam, StorageSCP scp, String forcedTs, boolean isImage, String callingAE, Vector<RoiMeasure> total) throws IOException {
        file.getParentFile().mkdirs();
        FileOutputStream fout = new FileOutputStream(file);
        BufferedOutputStream bout = new BufferedOutputStream(fout);
        boolean excp = false;
        try {
            ds.writeFile(bout, encParam);
            bout.flush();
            fout.flush();
        } catch (Exception e) {
            log.error(callingAE + ": Cannot write to disk", e);
            excp = true;
            throw new IOException("Cannot write Dataset to file");
        } finally {
            try {
                bout.close();
                fout.close();
            } catch (IOException ioex) {
                log.error(callingAE + ":When closing storeToFile's buffer", ioex);
            }
        }
        if (!excp && isImage) {
            if ((total!=null)&&(total.size() != 0)) {
                log.info(callingAE + ": Elaborating Images...");
                ImageMasker.graphAnonymize(file, total, file);
                log.info(callingAE + ": Image Elaboration Done");
            }
        }
        

        if (!excp && forcedTs != null && !forcedTs.equals("null")) {
            String ts = ds.getFileMetaInfo().getTransferSyntaxUID();
            log.info(callingAE + ": ts forced for storage with: " + ts + ", isImage " + isImage);
            try {
                if (isImage) {
                    if ((ts.equalsIgnoreCase(UIDs.ExplicitVRLittleEndian)) || (ts.equalsIgnoreCase(UIDs.ImplicitVRLittleEndian))) {
                        // check if my compression level area accepted
                        log.info(callingAE + ": Receving an uncompressed image");
                        log.debug(callingAE + ": Transfer syntax of this file is " + ts + "; going to check if compression is available...");
                        log.debug(callingAE + ": Compression is forced with this transfer syntax: " + forcedTs);
                        log.debug(callingAE + ": renaming image as temporary uncompressed image");
                        File fileToCompress = new File(file.getAbsolutePath().concat("-toCompress"));
                        boolean done = file.renameTo(fileToCompress);
                        log.debug(callingAE + ": Rename operation correctly done: " + done);
                        
                        File f = compress.compressImage(fileToCompress, forcedTs, file.getAbsolutePath(), ds.get(Tags.PixelData).vr());
                        if (f==null){
                        	throw new IOException("cannot compress the image");
                        }
                        
                        fileToCompress.delete();
                        log.debug(callingAE + ": temporary file should have been deleted");
                        // you should not save it two times
                    } else {
                        log.error("You forced for this AE title a wrong trasfer syntax. The image the AE is sending could not be stored to the forced TS");
                        log.error("Maybe u're sending a compressed image, ignoring constraints and trying to save anyway");
                        log.error(callingAE + ": object class" + ds.getFileMetaInfo().getMediaStorageSOPClassUID());
                    }
                } else {
                    log.warn("the DICOM object is not an image..avoiding compression despite being forced.");
                }
            } catch (Exception ioe) {
                throw new IOException("Cannot save the file, it's an image", ioe);
                // ioe.printStackTrace();
            }
        }
        
        if (!excp) {
            FileHasher fh = new FileHasher();
            File f = new File(file.getCanonicalPath());
            String hashAlgorithm = null;
			try {
				//hashAlgorithm = MBeanDealer.getPropertyFromMbean(new ObjectName("it.units.htl.dpacs.servers:type=DicomServer,index=3"), "getHashAlgorithm"); //GDC
				hashAlgorithm = MBeanDealer.getPropertyFromMbean(new ObjectName("it.units.htl.dpacs.servers:type=DicomServer,index=3"), "HashAlgorithm");
			} catch (MalformedObjectNameException monex) {
				log.error("Error retrieving Hash Algorithm ",monex);
			} catch (NullPointerException npex) {
				log.error("Error retrieving Hash Algorithm ",npex);
			}        
			if(hashAlgorithm == null){
				 throw new IOException("Hash Algorithm is not specified");
			}
			hash = fh.doHash(f, hashAlgorithm);
            log.debug(f.getCanonicalPath() + " have hash: " + hash);
        }
        
    }

    @SuppressWarnings("unused")
    private OutputStream openOutputStream(File file, StorageSCP scp, String callingAE) throws IOException {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Could not create " + parent);
                // scp.getLog().info("M-WRITE " + parent);
            }
        }
        log.info(callingAE + ": M-WRITE " + file);
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    @SuppressWarnings("unused")
    private void copyRight(ImageInputStream in, OutputStream out, int len) throws IOException {
        byte[] buffer = new byte[512];
        int c;
        int remain = len;
        while (remain > 0) {
            c = in.read(buffer, 0, Math.min(buffer.length, remain));
            System.out.println("c " + c);
            out.write(buffer, 0, c);
            remain -= c;
        }
    }

    @SuppressWarnings("unused")
    private void copy(InputStream in, OutputStream out) throws IOException {
        int c;
        byte[] buffer = new byte[512];
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
    }

    @SuppressWarnings("unused")
    private void delete(String fileName) {
        try {
            // Construct a File object for the file to be deleted.
            File target = new File(fileName);
            if (!target.exists()) {
                System.err.println("File " + fileName + " not present to begin with!");
                return;
            }
            // Quick, now, delete it immediately:
            if (target.delete()) {
                System.err.println("** Deleted " + fileName + " **");
            } else {
                System.err.println("Failed to delete " + fileName);
            }
        } catch (SecurityException e) {
            System.err.println("Unable to delete " + fileName + "(" + e.getMessage() + ")");
        }
    }

    public String getHash() {
        return hash;
    }
}
