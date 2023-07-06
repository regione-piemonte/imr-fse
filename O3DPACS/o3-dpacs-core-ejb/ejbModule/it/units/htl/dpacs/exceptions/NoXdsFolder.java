/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.exceptions;

public class NoXdsFolder extends ContinueException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NoXdsFolder(String reason) {
        super(reason);
    }
}
