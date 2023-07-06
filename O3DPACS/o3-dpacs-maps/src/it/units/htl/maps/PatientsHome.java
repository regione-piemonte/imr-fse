/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;

import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;

import it.units.htl.maps.util.SessionManager;

public class PatientsHome {
    private static final Log log = LogFactory.getLog(PatientsHome.class);

    protected SessionFactory getSessionFactory() {
        try {
        		return SessionManager.getInstance();
//            return (SessionFactory) InitialContext.doLookup("DpacsSessionFactory");
        } catch (Exception e) {
            log.error("Could not locate SessionFactory in JNDI", e);
            throw new IllegalStateException(
                    "Could not locate SessionFactory in JNDI");
        }
    }

    @SuppressWarnings("unchecked")
    public List<Patients> find(Patients instance, Session s) {
        try {
            List<Patients> results = null;
            Criteria c = s.createCriteria(Patients.class).createAlias("studies", "st").setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
            Example ex = Example.create(instance).excludeNone().excludeZeroes().enableLike().ignoreCase().excludeProperty("st.series");
            c.add(ex);
            if(instance.getStudies() != null){
                
            
            if ( ((Studies) instance.getStudies().toArray()[0]).getAccessionNumber()!= null &&  !"".equals(((Studies) instance.getStudies().toArray()[0]).getAccessionNumber()))
                c.add(Restrictions.eq("st.accessionNumber", ((Studies) instance.getStudies().toArray()[0]).getAccessionNumber()));
            if (((Studies) instance.getStudies().toArray()[0]).getStudyInstanceUid() != null && !"".equals(((Studies) instance.getStudies().toArray()[0]).getStudyInstanceUid()))
                c.add(Restrictions.eq("st.studyInstanceUid", ((Studies) instance.getStudies().toArray()[0]).getStudyInstanceUid()));
            if (null != ((Studies) instance.getStudies().toArray()[0]).getStudyDate())
                c.add(Restrictions.eq("st.studyDate", ((Studies) instance.getStudies().toArray()[0]).getStudyDate()));
//            if (null != ((Studies) instance.getStudies().toArray()[0]).getReconcile())
//                c.add(Restrictions.eq("st.reconcile", ((Studies) instance.getStudies().toArray()[0]).getReconcile()));
            }
            c.setMaxResults(100);
            results = c.list();
            log.debug("find by example successful, result size: " + results.size());
            if (results.isEmpty())
                return new ArrayList<Patients>();
            return results;
        } catch (RuntimeException re) {
            log.error("Find by example failed", re);
            throw re;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Patients> findByExample(Patients instance, Session s) {
        try {
            List<Patients> results = null;
            Criteria c = s.createCriteria(Patients.class);
            Example ex = Example.create(instance).enableLike().ignoreCase();
            c.add(ex);
            c.setMaxResults(100);
            results = c.list();
            log.debug("find by example successful, result size: " + results.size());
            if (results.isEmpty())
                return new ArrayList<Patients>();
            return results;
        } catch (RuntimeException re) {
            log.error("Find by example failed", re);
            throw re;
        }
    }
}
