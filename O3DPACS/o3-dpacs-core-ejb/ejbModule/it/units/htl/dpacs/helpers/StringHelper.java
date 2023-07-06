/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

public class StringHelper {
    public static String convertUniqueField(String field) {
        String newField = "";
        for (int i = 0; i < field.length(); i++) {
            try {
                Integer.parseInt(field.charAt(i) + "");
                newField += field.charAt(i);
            } catch (Exception e) {
                if (!newField.endsWith(".")) {
                    newField += ".";
                }
            }
        }
        if (newField.endsWith(".")) {
            newField = newField.substring(0, newField.length() - 1);
        }
        return newField;
    }
}
