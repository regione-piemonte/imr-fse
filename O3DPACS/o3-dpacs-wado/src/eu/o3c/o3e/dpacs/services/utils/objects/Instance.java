/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.dpacs.services.utils.objects;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Instance implements Comparable<Instance> {
    private Log log = LogFactory.getLog(Instance.class);
    // inner class that provides instances information
    private String sopInstanceUID;
    private String sopClassUID;
    private String instanceNumber;
    private String mimeType;
    private String modality;
    // variables for ordering instances
    private String[] imagePositionPatient;
    private String numberOfFrames;
    private String windowCenter;
    private String windowWidth;
    private String rows;
    private String columns;
    private String photometricInterpretation;
    private String bitPerPixel;
    private String rescaleSlope;
    private String rescaleIntercept;
    private String transferSyntax;
    private String[] pixelSpacing;
    private String[] imagerPixelSpacing;
    private String echoTime;
    private String instanceDateTime;
    private String meaning;
    private List<Instance> referencedInstances;
    private String referencedSeries;
    private String contentDate;
    private String contentTime;

    public String[] getImagerPixelSpacing() {
        return imagerPixelSpacing;
    }

    public void setImagerPixelSpacing(String[] imagerPixelSpacing) {
        this.imagerPixelSpacing = imagerPixelSpacing;
    }

    public String[] getPixelSpacing() {
        return pixelSpacing;
    }

    public void setPixelSpacing(String[] pixelSpacing) {
        this.pixelSpacing = pixelSpacing;
    }

    public String getNumberOfFrames() {
        return numberOfFrames;
    }

    public void setNumberOfFrames(String numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    public String getWindowCenter() {
        return windowCenter;
    }

    public void setWindowCenter(String windowCenter) {
        this.windowCenter = windowCenter;
    }

    public String getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(String windowWidth) {
        this.windowWidth = windowWidth;
    }

    public String getRows() {
        return rows;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getPhotometricInterpretation() {
        return photometricInterpretation;
    }

    public void setPhotometricInterpretation(String fotometricInterpretation) {
        this.photometricInterpretation = fotometricInterpretation;
    }

    public String getBitPerPixel() {
        return bitPerPixel;
    }

    public void setBitPerPixel(String bitPerPixel) {
        this.bitPerPixel = bitPerPixel;
    }

    public String getRescaleSlope() {
        return rescaleSlope;
    }

    public void setRescaleSlope(String rescaleSlope) {
        this.rescaleSlope = rescaleSlope;
    }

    public String getRescaleIntercept() {
        return rescaleIntercept;
    }

    public void setRescaleIntercept(String rescaleIntercept) {
        this.rescaleIntercept = rescaleIntercept;
    }

    public String getTransferSyntax() {
        return transferSyntax;
    }

    public void setTransferSyntax(String transferSyntax) {
        this.transferSyntax = transferSyntax;
    }

    public String getSopInstanceUID() {
        return sopInstanceUID;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getModality() {
        return this.modality;
    }

    public void setSopInstanceUID(String sopInstanceUID) {
        this.sopInstanceUID = sopInstanceUID;
    }

    public String getSopClassUID() {
        return sopClassUID;
    }

    public void setSopClassUID(String sopClassUID) {
        this.sopClassUID = sopClassUID;
    }

    public String getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(String instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String[] getImagePositionPatient() {
        return imagePositionPatient;
    }

    public void setImagePositionPatient(String[] imagePositionPatient) {
    	if(imagePositionPatient!=null){
    		if(imagePositionPatient.length<3){
    			this.imagePositionPatient = null;
    			if(imagePositionPatient.length!=0)		// Something was specified, but VM should've been 3
    				log.warn("Off-standard VM for ImagePositionPatient: should be 3, is "+imagePositionPatient.length);		// Actually there's no reference to the sopInstance, so it can be useful only if sorting problems are notified or DEBUGLOG is active 
    		}else{		// at least three values - currently values beyond the third are ignored
    			if("".equals(imagePositionPatient[0]) || "".equals(imagePositionPatient[1]) || "".equals(imagePositionPatient[2])){
    				this.imagePositionPatient = null;
    			}else{
    				this.imagePositionPatient = imagePositionPatient;
    			}
    		}
    	}else{		// the array is not null
    		this.imagePositionPatient = null;
    	}
    }

    public int compareTo(Instance i) {
        if ("MG".equals(i.getModality())) {
            String stringInstanceNumber = this.getInstanceNumber();
            if (stringInstanceNumber == null) {
                return 0;
            }
            if (stringInstanceNumber.length() == 0) {
                return 0;
            }
            String stringSecondInstanceNumber = i.getInstanceNumber();
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
            /* __*__ */// if (firstInstanceNumber>secondInstanceNumber) return 1;
            /* __*__ */// if (firstInstanceNumber<secondInstanceNumber) return -1;
            return firstInstanceNumber - secondInstanceNumber;
        } else if ("MR".equals(i.getModality())) {
            if (getImagePositionPatient() == null) {
                return 0;
            }
            if (i.getImagePositionPatient() == null) {
                return 0;
            }
            String[] myvalues = getImagePositionPatient();
            String[] othervalues = i.getImagePositionPatient();
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
                    /* __*__ */// System.out.println("return dz1");
                    if (dz > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                if (Math.min(Math.abs(dy), Math.abs(dz)) == Math.abs(dz)) {
                    /* __*__ */// System.out.println("return dy111");
                    if (dy > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    /* __*__ */// System.out.println("return dz11");
                    if (dz > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
            if (dy == 0) {
                if (dx == 0.0) {
                    /* __*__ */// System.out.println("return dz2");
                    if (dz > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                if (Math.min(Math.abs(dx), Math.abs(dz)) == Math.abs(dz)) {
                    /* __*__ */// System.out.println("return dx222");
                    if (dx > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    /* __*__ */// System.out.println("return dz22");
                    if (dz > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
            if (dz == 0) {
                if (dx == 0.0) {
                    /* __*__ */// System.out.println("return dy3");
                    if (dy > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                if (Math.min(Math.abs(dx), Math.abs(dy)) == Math.abs(dy)) {
                    /* __*__ */// System.out.println("return dx333");
                    if (dx > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    /* __*__ */// System.out.println("return dy33");
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
                /* __*__ */// System.out.println("return dx4");
                if (dx > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
            if (a > c && a < b) {
                /* __*__ */// System.out.println("return dx44");
                if (dx > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
            if (b > a && b < c) {
                /* __*__ */// System.out.println("return dy5");
                if (dy > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
            if (b > c && b < a) {
                /* __*__ */// System.out.println("return dy55");
                if (dy > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
            if (c > b && c < a) {
                /* __*__ */// System.out.println("return dz8");
                if (dz > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
            if (c > a && c < b) {
                /* __*__ */// System.out.println("return dz88");
                if (dz > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
            return 0;
        } else {
            double d1 = 0;
            if (getImagePositionPatient() != null && getImagePositionPatient().length == 3) {
                if (getImagePositionPatient()[2] != null) {
                    d1 = Double.parseDouble(getImagePositionPatient()[2]);
                }
            }
            double d2 = 0;
            if (i.getImagePositionPatient() != null && i.getImagePositionPatient().length == 3) {
                if (i.getImagePositionPatient()[2] != null) {
                    d2 = Double.parseDouble(i.getImagePositionPatient()[2]);
                }
            }
            // double diff = d1 - d2;
            if (d1 - d2 > 0) {
                return -1;
            }
            if (d1 - d2 < 0) {
                return 1;
            }
            return 0;
        }
    }

    public String getEchoTime() {
        return echoTime;
    }

    public void setEchoTime(String echoTime) {
        this.echoTime = echoTime;
    }

    public String getInstanceDateTime() {
        return instanceDateTime;
    }

    public void setInstanceDateTime(String instanceDateTime) {
        this.instanceDateTime = instanceDateTime;
    }

	public String getMeaning() {
		return meaning;
	}

	public void setMeaning(String meaning) {
		this.meaning = meaning;
	}

	public List<Instance> getReferencedInstances() {
		return referencedInstances;
	}

	public void setReferencedInstances(List<Instance> referencedInstances) {
		this.referencedInstances = referencedInstances;
	}

	public String getReferencedSeries() {
		return referencedSeries;
	}

	public void setReferencedSeries(String referencedSeries) {
		this.referencedSeries = referencedSeries;
	}

	public String getContentDate() {
		return contentDate;
	}

	public void setContentDate(String contentDate) {
		this.contentDate = contentDate;
	}

	public String getContentTime() {
		return contentTime;
	}

	public void setContentTime(String contentTime) {
		this.contentTime = contentTime;
	}
    
}
