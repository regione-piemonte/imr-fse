/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.exceptions;

public class IncorrectPatientIdException extends BlockingException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public IncorrectPatientIdException(String reason) {
        super(reason);
    }
}
