/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.dpacs.services.utils.sorting;

/**
 *
 * @author pittau
 */
public enum SortingDirection {
    DESCENDING("Descending"),
    ASCENDING("Ascending");
            
    private String rapr;

    private SortingDirection(String r){
        rapr = r;
    }

    @Override
    public String toString(){
        return rapr;
    }
}
