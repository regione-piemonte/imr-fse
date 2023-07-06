/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.kos;

import it.units.htl.web.Study.ObjectList;
import it.units.htl.web.Study.SerieListItem;

public class FilteredSeriesListItem extends SerieListItem{
    private ObjectList filteredObject = new ObjectList();
    private boolean isInWarning = false;
    
    
    public void setFilteredObject(ObjectList filtered){
        filteredObject = filtered;
    }
    
    public ObjectList getFilteredObject(){
        return filteredObject;
    }

    public boolean getInWarning() {
        return isInWarning;
    }

    public void setInWarning(boolean isInWarning) {
        this.isInWarning = isInWarning;
    }
}
