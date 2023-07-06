/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.exceptions;

import it.units.htl.dpacs.dao.CannotStoreException;

public class ContinueException extends CannotStoreException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ContinueException(String reason) {
        super(reason);
        // TODO Auto-generated constructor stub
    }
}
