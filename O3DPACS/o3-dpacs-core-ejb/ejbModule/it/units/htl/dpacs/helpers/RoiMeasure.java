/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.awt.geom.Point2D;


public class RoiMeasure {
    
    private Point2D p1=null;
    private Point2D p2=null;
    
    public RoiMeasure() {
    }
    
    public Point2D getP1() {
        return p1;
    }
    
    public void setP1(Point2D p1) {
        this.p1 = p1;
    }
    
    public Point2D getP2() {
        return p2;
    }
    
    public void setSize(int width, int height){
    	p2=new java.awt.Point((int)p1.getX()+width, (int)p1.getY()+height);
    }
    
    
}
