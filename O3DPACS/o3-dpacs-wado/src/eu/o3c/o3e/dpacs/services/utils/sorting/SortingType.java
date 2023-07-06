/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.dpacs.services.utils.sorting;


public enum SortingType {
    CREATION_TIME("Creation Time"),
    INSTANCE_NUMBER("Instance Number"),
    X_AXIS("X Axis"),
    Y_AXIS("Y Axis"),
    Z_AXIS("Z Axis"),
    ECHO_TIME("Echo Time");

    private String rapr;

    private SortingType(String r){
        rapr = r;
    }

    @Override
    public String toString(){
        return rapr;
    }
}
