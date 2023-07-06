/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import java.util.ArrayList;

/**
 * 
 * @author Sara
 *
 */

public class ObjectList {

	private ArrayList<ObjectListItem> objectsMatched;
	
	public ObjectList(){
		objectsMatched = new ArrayList<ObjectListItem>();
	}

	public void findObjects(String serieInstanceUid) {
		objectsMatched.clear();
		StudyFinder.getObjects(serieInstanceUid);
	}
	
	public ArrayList<ObjectListItem> getObjectsMatched() {
		
        return objectsMatched;
    }
    @SuppressWarnings("unused")
	private void setObjectsMatched(ArrayList<ObjectListItem> arrayList) {
		this.objectsMatched = arrayList;
	}

	public void add(ObjectListItem objectListItem) {
		objectsMatched.add(objectListItem);
	}
    public String getLenght(){
    	return String.valueOf(objectsMatched.size());
    }
}
