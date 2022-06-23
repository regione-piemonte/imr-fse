/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.run;

import it.csi.dmass.dmassbatchcancpacc.env.Context;
import it.csi.dmass.dmassbatchcancpacc.run.batch.Batch;


public class MAIN
{
    
    public static void main(final String[] args) {
        try {        	
            init(args);   
            final Batch batch = Context.getBean(Batch.class); 
            batch.init();
            batch.run();
        }
        catch (Exception e) {  
            System.exit(-1);
        }        
        System.exit(0);
    }
    
    private static void init(final String[] args) throws Exception {
       
        try {
            Context.init();           
        }
        catch (Exception t) {
            System.out.print(t + "\n");
            throw t;
        }
    }
}
