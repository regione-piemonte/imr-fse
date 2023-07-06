/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.io.Serializable;

/**
 * The class is a data object for DICOM code sequence
 * @author Cicuta
 */
public class CodeSequence implements HtlVo, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String codeValue=null;					
    private String codingSchemeDesignator=null;		
    private String codingSchemeVersion=null;		
    private String codeMeaning=null;		
        	
	public CodeSequence(){}
	public CodeSequence(String cv, String csd, String csv, String cm){
		codeValue = prepareString(cv, 16);
		codingSchemeDesignator = prepareString(csd, 16);
		codingSchemeVersion = prepareString(csv, 16);
		codeMeaning = prepareString(cm, 64);
	}

	
	public void setCodeValue(String cv){
		codeValue=prepareString(cv, 16);
	}
	public void setCodingSchemeDesignator(String csd){
		codingSchemeDesignator=prepareString(csd, 16);
	}
	public void setCodingSchemeVersion(String csv){
		codingSchemeVersion=prepareString(csv, 16);
	}
	public void setCodeMeaning(String cm){
		codeMeaning=prepareString(cm, 64);
	}
	public String getCodeValue(){
		return codeValue;
	}
	public String getCodingSchemeDesignator(){
		return codingSchemeDesignator;
	}
	public String getCodingSchemeVersion(){
		return codingSchemeVersion;
	}
	public String getCodeMeaning(){
		return codeMeaning;
	}

	public String prepareString(String arg, int len){
		if (arg==null) return null;
		String temp=arg.trim();
		return ((temp.length()>len)? temp.substring(0, len) : temp);
	}
	
	public String prepareLong(String arg){return null;}
	public String prepareInt(String arg){return null;}
	public void setToPerform(char arg){}
	public char getToPerform(){return ' ';}


	public void reset(){
		codeValue=null;				
		codingSchemeDesignator=null;
		codingSchemeVersion=null;	
		codeMeaning=null;			
	
	}
	public boolean equals(CodeSequence o) {
//		verifying codeMeaning
		if(codeMeaning != null && o.getCodeMeaning() != null){
			if(!codeMeaning.equals(o.getCodeMeaning())){
				return false;
			}
		}else if(codeMeaning != null || o.getCodeMeaning() != null){
			return false;
		}
//		verifying codeValue
		if(codeValue != null && o.getCodeValue() != null){
			if(!codeValue.equals(o.getCodeValue())){
				return false;
			}				
		}else if(codeValue != null || o.getCodeValue() != null){
			return false;
		}
//		 verifying codingSchemeDesignator
		if(codingSchemeDesignator != null && o.getCodingSchemeDesignator() != null){
			if(!codingSchemeDesignator.equals(o.getCodingSchemeDesignator())){
				return false;
			}
		}else if(codingSchemeDesignator != null || o.getCodingSchemeDesignator() != null){
			return false;
		}
//		verifying  codingSchemeVersion
		if(codingSchemeVersion != null && o.getCodingSchemeVersion() != null){
			if(!codingSchemeVersion.equals(o.getCodingSchemeVersion())){
				return false;
			}
		}else if(codingSchemeVersion != null || o.getCodingSchemeVersion() != null){
			return false;
		}
		return true;
	}
}
