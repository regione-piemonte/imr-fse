/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.accessors.implementations;


import it.units.htl.dpacs.accessors.Accessor;
import it.units.htl.dpacs.valueObjects.Credentials;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.filepool.fplibrary.FPClip;
import com.filepool.fplibrary.FPLibraryConstants;
import com.filepool.fplibrary.FPLibraryException;
import com.filepool.fplibrary.FPPool;
import com.filepool.fplibrary.FPTag;


public class CenteraCasAccessor extends Accessor {
	
	private FPPool retrievalPool;
	
	public CenteraCasAccessor(String deviceUrl, Credentials credentials) throws Throwable{
		super(deviceUrl, credentials);
		try{
			retrievalPool=new FPPool(deviceUrl);
		}catch(Throwable ex){
			retrievalPool=null;
			throw ex;
		}
	}

	@Override
	public InputStream getFile(String directUrl) throws IOException {
		
		
		FPClip rClip=null;
		FPTag rTopTag=null;
		ByteArrayOutputStream out=null;
		InputStream in=null;
		
		try{
			
			rClip=new FPClip(retrievalPool, directUrl, FPLibraryConstants.FP_OPEN_FLAT);
			rTopTag=rClip.getTopTag();

			out = new ByteArrayOutputStream();
			rTopTag.BlobRead(out);

			in=new ByteArrayInputStream(out.toByteArray());			// this needs to be closed in lower levels
	
		}catch(Throwable ex){
			throw new IOException(ex);
		}finally{
			try {
				rTopTag.Close();
			} catch (FPLibraryException fplex) {}
			try {
				rClip.Close();
			} catch (FPLibraryException fplex) {}
			if(out!=null){
				try{
					out.close();		// this throws an exception caught in the lower layer
				}catch(IOException ioex){}
			}
			
		}
		
		return in;
	}
	
	public void close(){
		if(retrievalPool!=null){
			try {
				retrievalPool.Close();
			} catch (FPLibraryException fplex) {}
		}
	}
	
	protected void finalize() throws Throwable {
	    try {
	        close();
	    } finally {
	        super.finalize();
	    }
	}


	
}
