/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.exceptions;

import it.units.htl.dpacs.dao.CannotStoreException;



/**
 * @author sango
 * @category Used to block the dicom CSTORE
 */
public class BlockingException extends CannotStoreException{
    private static final long serialVersionUID = 1L;

    public BlockingException(String reason) {
        super(reason);
    }
}
