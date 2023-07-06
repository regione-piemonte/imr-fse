/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import it.units.htl.dpacs.dao.UserManager;
import it.units.htl.dpacs.helpers.ConfigurationSettings;

import java.util.ArrayList;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.dict.UIDs;
/**
 * 
 * @author Sara
 * 
 */
public class ObjectListItem implements Comparable<ObjectListItem>{
	private static Log log = LogFactory.getLog(ObjectListItem.class);
	private String sopInstanceUid;
	private String objectInstanceUid;
	private String transferSyntax;
	private String instanceNumber;
	private String wadoUrl;
	private String size;
	private String rows;
	private String columns;
	private String viewedCols;
	private String viewedRows;
	private String wwidth;
	private String wcenter;
	private String photmetricInterpretation;
	private String pixelRepresentation;
	private String isMultiframe = "null";
	private String[] ImagePosition = null;
	private String modality;	
	private ArrayList<String> unsupModality = new ArrayList<String>();
	
	
	
	public String getTransferSyntax() {
        return transferSyntax;
    }

    public void setTransferSyntax(String transferSyntax) {
        this.transferSyntax = transferSyntax;
    }

    public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	public String[] getImagePosition() {
		return ImagePosition;
	}

	public void setImagePosition(String[] imageZPosition) {
		ImagePosition = imageZPosition;
	}

	// return the correct viewer for this type of object
	public String getViewer() {
		unsupModality.add("SR");
		unsupModality.add("PR");
		unsupModality.add("KO");
		unsupModality.add("ECG");
		if(unsupModality.contains(modality)){
			return "noPanel";
		}
		if(transferSyntax.equals(UIDs.MPEG2)){
            return "mpegVideoPanel";
        }
		if (isMultiframe.equals("null")){
	        return "imagePanel";
		}else if(Integer.parseInt(isMultiframe) <= 1){
		    return "imagePanel";
		} else {
			return "videoPanel";
		}
		
	}

	public String getIsMultiframe() {
		return isMultiframe;
	}

	public void setIsMultiframe(String isMultiframe) {
		this.isMultiframe = isMultiframe;
	}
	
	public boolean getIsStructRep() {
		if(modality != null){
			if (modality.equals("SR"))return true;
			else return false;
		}
		else return false;
	}
	
	public void setIsStructRep(boolean isStructRep) {
		
	}

	public String get_minCenterPoint() {
		if (pixelRepresentation.equals("1")) {
			return Double.toString(-Math.pow(2, Integer.valueOf(size)) / 2);
		} else {
			return "0";
		}
	}

	public String get_maxCenterPoint() {
		if (pixelRepresentation.equals("1")) {
			return Double.toString(Math.pow(2, Integer.valueOf(size)) / 2);
		} else {
			return Double.toString(Math.pow(2, Integer.valueOf(size)));
		}
	}

	public String get_pixelRepresentation() {
		return pixelRepresentation;
	}

	public void set_pixelRepresentation(String representation) {
		pixelRepresentation = representation;
	}

	public String get_photmetricInterpretation() {
		return photmetricInterpretation;
	}

	public void set_photmetricInterpretation(String interpretation) {
		photmetricInterpretation = interpretation;
	}

	public String get_maxWindowSize() {
		return Double.toString(Math.pow(2, Integer.valueOf(size)));
	}

	public String getViewedRows() {
		return viewedRows;
	}

	public void setViewedRows(String viewedRows) {
		this.viewedRows = viewedRows;
	}

	public String getWcenter() {
		return wcenter;
	}

	public void setWcenter(String wcenter) {
		this.wcenter = wcenter;
	}

	public String getWwidth() {
		return wwidth;
	}

	public void setWwidth(String wwidth) {
		this.wwidth = wwidth;
	}

	public void setSOPinstance(String _SOPinstance) {
		this.sopInstanceUid = _SOPinstance;
	}

	public void setObjectInstanceUid(String _objectInstanceUid) {
		this.instanceNumber = _objectInstanceUid;

	}

	public void setInstanceNumber(String _instanceNumber) {
		this.instanceNumber = _instanceNumber;

	}

	private String initWadoUrl(HttpSession s){
		HttpSession session = null;
		if(s==null){
			HttpServletRequest request = (HttpServletRequest) javax.faces.context.FacesContext.getCurrentInstance().getExternalContext().getRequest();
			session = request.getSession();
		}else{
			session=s;
		}
		
		String wUrl="";
		if(session.getAttribute(ConfigurationSettings.CONFIG_WADOURL)==null){
			try {
				wUrl=new UserManager().getConfigParam(ConfigurationSettings.CONFIG_WADOURL);
			} catch (NamingException nex) {
				log.error("Error retrieving WadoUrl",nex);
				wUrl="";
			}
			session.setAttribute(ConfigurationSettings.CONFIG_WADOURL, wUrl);
		}else{
			wUrl=(String)session.getAttribute(ConfigurationSettings.CONFIG_WADOURL);
		}
		return wUrl;
	}
	
	public void setWadoUrl(String studyInstanceUid, String seriesInstanceUid,String sopInstanceUid) {
		
		String wUrl=initWadoUrl(null);
		this.wadoUrl=wUrl+"?requestType=WADO&studyUID=" + studyInstanceUid + "&seriesUID=" + seriesInstanceUid+ "&objectUID=" + sopInstanceUid;
	}
	
	
	public void setWadoUrl(String studyInstanceUid, String seriesInstanceUid,String sopInstanceUid, HttpSession session) {
		String wUrl=initWadoUrl(session);
		this.wadoUrl=wUrl+"?requestType=WADO&studyUID=" + studyInstanceUid + "&seriesUID=" + seriesInstanceUid+ "&objectUID=" + sopInstanceUid;
    }

	public void setSize(String size) {
		this.size = size;
	}

	public String getObjectInstanceUid() {
		return objectInstanceUid;
	}

	public String getInstanceNumber() {
		return instanceNumber;
	}

	public String getSOPinstance() {
		return sopInstanceUid;
	}

	public String getWadoUrl() {
		return wadoUrl;
	}

	public void setRows(String _rows) {
		this.rows = _rows;
	}

	public void setColumns(String _columns) {

		this.columns = _columns;

	}

	public String getRows() {
		return rows;
	}

	public String getColumns() {
		return columns;
	}

	public String getSize() {
		return size;
	}

	public String getViewedCols() {
		return viewedCols;
	}

	public void setViewedCols(String viewedCols) {
		this.viewedCols = viewedCols;
	}

	// return the url for video/quicktime representation
	public String getWadoUrlForMov() {
		return wadoUrl + "&contentType=video/quicktime";
	}

	// return the url for text/xml representation
	public String getWadoUrlForXML() {
		return wadoUrl + "&contentType=text/xml";
	}
	
	public String getWadoUrlForMpeg(){
	    return wadoUrl + "&contentType=video/mpeg";
	}

	// return the url to get the object in image/jpeg format
	public String getWadoUrlForImage() {
		String tempUrl = wadoUrl;
		tempUrl += "&contentType=image/jpeg";
		tempUrl += "&rows=" + viewedRows + "&columns=" + viewedCols;
//		this informations mean something only if both are != null
		if ((wwidth != null) && (wcenter != null)) {
			tempUrl += "&windowWidth=" + wwidth;
			tempUrl += "&windowCenter=" + wcenter;
		}
		return tempUrl;
	}	
	
	public String getWadoUrlForPDF() {
		String tempUrl = wadoUrl;
		tempUrl += "&contentType=application/pdf";
		return tempUrl;
	}
	
	public int compareTo(ObjectListItem o) {
		if ("MG".equals(getModality())) {
            String stringInstanceNumber = this.getInstanceNumber();
            if (stringInstanceNumber == null) {
                return 0;
            }
            if (stringInstanceNumber.length() == 0) {
                return 0;
            }

            String stringSecondInstanceNumber = o.getInstanceNumber();
            if (stringSecondInstanceNumber == null) {
                return 0;
            }
            if (stringSecondInstanceNumber.length() == 0) {
                return 0;
            }

            int firstInstanceNumber = 0;
            int secondInstanceNumber = 0;



            try {
                firstInstanceNumber = Integer.parseInt(stringInstanceNumber);
            } catch (Exception e) {
                log.warn("Number exception for: " + stringInstanceNumber);
                return 0;
            }

            try {
                secondInstanceNumber = Integer.parseInt(stringSecondInstanceNumber);
            } catch (Exception e) {
                log.warn("Number exception for: " + stringSecondInstanceNumber);
                return 0;
            }

            /*__*__*/ //if (firstInstanceNumber>secondInstanceNumber) return 1;
            /*__*__*/ //if (firstInstanceNumber<secondInstanceNumber) return -1;
            return firstInstanceNumber - secondInstanceNumber;
        } else if ("MR".equals(getModality())) {

            if (getImagePosition() == null) {
                return 0;
            }

            if (o.getImagePosition() == null) {
                return 0;
            }


            String[] myvalues = getImagePosition();
            String[] othervalues = o.getImagePosition();

            if (myvalues.length != 3) {
                return 0;
            }
            if (othervalues.length != 3) {
                return 0;
            }

            double mydvals[] = new double[3];
            double secondvals[] = new double[3];

            try {
                mydvals[0] = Double.parseDouble(myvalues[0]);
                mydvals[1] = Double.parseDouble(myvalues[1]);
                mydvals[2] = Double.parseDouble(myvalues[2]);
                secondvals[0] = Double.parseDouble(othervalues[0]);
                secondvals[1] = Double.parseDouble(othervalues[1]);
                secondvals[2] = Double.parseDouble(othervalues[2]);
            } catch (Exception e) {
                return 0;
            }


            double dx, dy, dz;
            dx = secondvals[0] - mydvals[0];
            dy = secondvals[1] - mydvals[1];
            dz = secondvals[2] - mydvals[2];


            if (dx == 0) {
                if (dy == 0.0) {
                    /*__*__*/ //System.out.println("return dz1");
                    if (dz > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                if (Math.min(Math.abs(dy), Math.abs(dz)) == Math.abs(dz)) {
                    /*__*__*/ //System.out.println("return dy111");
                    if (dy > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    /*__*__*/ //System.out.println("return dz11");
                    if (dz > 0) {
                        return 1;
                    } else {
                        return -1;
                    }


                }
            }
            if (dy == 0) {
                if (dx == 0.0) {
                    /*__*__*/ //System.out.println("return dz2");
                    if (dz > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                if (Math.min(Math.abs(dx), Math.abs(dz)) == Math.abs(dz)) {

                    /*__*__*/ //System.out.println("return dx222");
                    if (dx > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    /*__*__*/ // System.out.println("return dz22");
                    if (dz > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
            if (dz == 0) {
                if (dx == 0.0) {
                    /*__*__*/ //System.out.println("return dy3");
                    if (dy > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                if (Math.min(Math.abs(dx), Math.abs(dy)) == Math.abs(dy)) {
                    /*__*__*/ //System.out.println("return dx333");
                    if (dx > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    /*__*__*/ // System.out.println("return dy33");
                    if (dy > 0) {
                        return 1;
                    } else {
                        return -1;
                    }

                }
            }

            double a = Math.abs(dx);
            double b = Math.abs(dy);
            double c = Math.abs(dz);

            if (a > b && a < c) {
                /*__*__*/ //  System.out.println("return dx4");
                if (dx > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
            if (a > c && a < b) {
                /*__*__*/ // System.out.println("return dx44");
                if (dx > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }

            if (b > a && b < c) {
                /*__*__*/ // System.out.println("return dy5");
                if (dy > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
            if (b > c && b < a) {
                /*__*__*/ //  System.out.println("return dy55");
                if (dy > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }

            if (c > b && c < a) {
                /*__*__*/ // System.out.println("return dz8");
                if (dz > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
            if (c > a && c < b) {
                /*__*__*/ //  System.out.println("return dz88");
                if (dz > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
            return 0;

        } else {
        	double d1 = 0;
        	if(getImagePosition() != null && getImagePosition().length == 3){
        		if(getImagePosition()[2] != null){
            		d1 = Double.parseDouble(getImagePosition()[2]);
            	}
        	}
        	
        	double d2 = 0;
        	if(o.getImagePosition() == null && o.getImagePosition().length == 3){
        		if(o.getImagePosition()[2] != null){
            		d2 = Double.parseDouble(o.getImagePosition()[2]);
                }
        	}
        	
             

//            double diff = d1 - d2;

            if (d1 - d2 > 0) {
                return -1;
            }
            if (d1 - d2 < 0) {
                return 1;
            }
            return 0;
        }
	}
}
