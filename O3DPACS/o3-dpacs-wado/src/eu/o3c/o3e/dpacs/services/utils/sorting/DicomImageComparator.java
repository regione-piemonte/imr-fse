/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package eu.o3c.o3e.dpacs.services.utils.sorting;

import eu.o3c.o3e.dpacs.services.utils.objects.Instance;

import java.util.Comparator;
import java.util.Iterator;
    
public class DicomImageComparator implements Comparator<Instance>{
    private SortingCriteria criteria;

    public DicomImageComparator(SortingCriteria criteria){
        this.criteria = criteria;
    }
    
    public int compare(Instance o1, Instance o2) {
        Iterator<SortingType> types = criteria.getUsedSortingTypes().iterator();

        while(types.hasNext()){
            SortingType st = types.next();
            SortingDirection direction = criteria.getSortingDirection(st);

            int eval = compare(o1, o2, st, direction);

            if(eval != 0){
                return eval;
            }
        }
        return 0;
    }

    private int compare(Instance o1,
            Instance o2, SortingType st, SortingDirection sd){
        switch(st){
            case CREATION_TIME:
                return compareCreationTime(o1, o2, sd);
            case INSTANCE_NUMBER:
                return compareInstanceNumber(o1, o2, sd);
            case X_AXIS:
                return compareX(o1, o2, sd);
            case Y_AXIS:
                return compareY(o1, o2, sd);
            case Z_AXIS:
                return compareZ(o1, o2, sd);
            case ECHO_TIME:
                return compareEchoTime(o1, o2, sd);
        }
        return 0;
    }

    private int compareCreationTime(Instance o1, Instance o2, SortingDirection sd){
        String other_idt = o2.getInstanceDateTime();
        String idt = o1.getInstanceDateTime();

        if (idt.equals("") || other_idt.equals("") || idt.equals(other_idt)) {
            return 0;
        } else {
            try {
                double dt1 = Double.parseDouble(idt);
                double dt2 = Double.parseDouble(other_idt);
                double diff = dt1 - dt2;

                if (sd == SortingDirection.DESCENDING) {
                    if (diff < 0) {
                        return 1;
                    } else if (diff > 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                } else {
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            } catch (Exception e) {
                return 0;
            }
        }
    }

    private int compareInstanceNumber(Instance o1, Instance o2, SortingDirection sd){
        String stringInstanceNumber = o1.getInstanceNumber();
        if (stringInstanceNumber == null) {
            return 0;
        }

        if (stringInstanceNumber.length() == 0) {
            return 0;
        }

        String stringSecondInstanceNumber = o2.getInstanceNumber();
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
            return 0;
        }

        try {
            secondInstanceNumber = Integer.parseInt(stringSecondInstanceNumber);
        } catch (Exception e) {
            return 0;
        }

        if (sd == SortingDirection.DESCENDING) {
            return secondInstanceNumber - firstInstanceNumber;
        } else {
            return firstInstanceNumber - secondInstanceNumber;
        }
    }

    private int compareX(Instance o1, Instance o2, SortingDirection sd){
        if(o1.getImagePositionPatient()== null ||
                o2.getImagePositionPatient() == null){
            return 0;
        }

        double d1 = Double.parseDouble(o1.getImagePositionPatient()[0]);
        double d2 =  Double.parseDouble(o2.getImagePositionPatient()[0]);

        if (sd == SortingDirection.DESCENDING) {
            if (d1 - d2 > 0) {
                return -1;
            }

            if (d1 - d2 < 0) {
                return 1;
            }

            return 0;
        } else {
            if (d1 - d2 > 0) {
                return 1;
            }

            if (d1 - d2 < 0) {
                return -1;
            }

            return 0;
        }
    }

    private int compareY(Instance o1, Instance o2, SortingDirection sd){
        if(o1.getImagePositionPatient()== null ||
                o2.getImagePositionPatient() == null){
            return 0;
        }

        double d1 = Double.parseDouble(o1.getImagePositionPatient()[1]);
        double d2 =  Double.parseDouble(o2.getImagePositionPatient()[1]);
       
        if (sd == SortingDirection.DESCENDING) {
            if (d1 - d2 > 0) {
                return -1;
            }

            if (d1 - d2 < 0) {
                return 1;
            }

            return 0;
        } else {
            if (d1 - d2 > 0) {
                return 1;
            }

            if (d1 - d2 < 0) {
                return -1;
            }

            return 0;
        }
    }

    private int compareZ(Instance o1, Instance o2, SortingDirection sd){
        if(o1.getImagePositionPatient()== null ||
                o2.getImagePositionPatient() == null){
            return 0;
        }

        double d1 = Double.parseDouble(o1.getImagePositionPatient()[2]);
        double d2 =  Double.parseDouble(o2.getImagePositionPatient()[2]);

        if (sd == SortingDirection.DESCENDING) {
            if (d1 - d2 > 0) {
                return -1;
            }

            if (d1 - d2 < 0) {
                return 1;
            }

            return 0;
        } else {
            if (d1 - d2 > 0) {
                return 1;
            }

            if (d1 - d2 < 0) {
                return -1;
            }

            return 0;
        }
    }
    
    private int compareEchoTime(Instance o1, Instance o2, SortingDirection sd){
        String other_idt = o2.getEchoTime();
        String idt = o1.getEchoTime();

        if (idt.equals("") || other_idt.equals("") || idt.equals(other_idt)) {
            return 0;
        } else {
            try {
                double dt1 = Double.parseDouble(idt);
                double dt2 = Double.parseDouble(other_idt);
                double diff = dt1 - dt2;

                if (sd == SortingDirection.DESCENDING) {
                    if (diff < 0) {
                        return 1;
                    } else if (diff > 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                } else {
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
