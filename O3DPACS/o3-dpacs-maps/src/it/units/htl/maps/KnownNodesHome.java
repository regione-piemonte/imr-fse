/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.exception.ConstraintViolationException;

public class KnownNodesHome {
    private static final Log log = LogFactory.getLog(KnownNodesHome.class);

    public List findByAETitle(String aeTitle, Session s) {
        log.debug("getting Knownnodes instance with AETitle: " + aeTitle);
        try {
            List results = null;
            results = s.createCriteria(KnownNodes.class).add(
                    Property.forName("aeTitle").eq(aeTitle)).list();
            log.debug("find by example successful, result size: "
                    + results.size());
            return results;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }

    public List findAll(Session s) {
        log.debug("finding all Knownnodes");
        try {
            List results = null;
            results = s.createCriteria(KnownNodes.class).addOrder(
                    Order.asc("aeTitle")).list();
            log.debug("findAll successful, result size: " + results.size());
            return results;
        } catch (RuntimeException re) {
            log.error("find by example failed", re);
            throw re;
        }
    }

    public List<KnownNodes> findSourcesForTarget(
            NodesForwardMappingId targetId, Session s) {
        try {
            List<KnownNodes> results = null;
            String hql = "SELECT kn FROM KnownNodes kn, NodesForwardMapping nfm WHERE nfm.id.sourceNodeFk=kn.pk AND nfm.id.targetNodeFk=? ORDER BY kn.aeTitle ASC";
            Query query = s.createQuery(hql);
            results = query.setLong(0, targetId.getTargetNodeFk()).list();
            return results;
        } catch (RuntimeException re) {
            log.error("findSourcesForTarget failed", re);
            throw re;
        }
    }

    public List<KnownNodes> findTargetsForSource(
            NodesForwardMappingId sourceId, Session s) {
        try {
            List<KnownNodes> results = null;
            String hql = "SELECT kn FROM KnownNodes kn, NodesForwardMapping nfm WHERE nfm.id.targetNodeFk=kn.pk AND nfm.id.sourceNodeFk=? ORDER BY sortOrder ASC";
            Query query = s.createQuery(hql);
            results = query.setLong(0, sourceId.getSourceNodeFk()).list();
            return results;
        } catch (RuntimeException re) {
            log.error("findTargetsForSource failed", re);
            throw re;
        }
    }

    public boolean addNodeForwardMapping(NodesForwardMappingId id,
            Byte sortOrder, Session s) {
        boolean result = true;
        try {
            NodesForwardMapping nfm = new NodesForwardMapping();
            NodesForwardMapping present = (NodesForwardMapping) s.get(
                    NodesForwardMapping.class, id);
            if (present == null) {
                s.beginTransaction();
                NodesForwardMappingId toAdd = new NodesForwardMappingId(id
                        .getSourceNodeFk(), id.getTargetNodeFk());
                nfm.setId(toAdd);
                nfm.setSortOrder(sortOrder);
                s.persist(nfm);
                s.getTransaction().commit();
            } else
                result = false;
        } catch (ConstraintViolationException cvex) {
            try {
                s.getTransaction().rollback();
            } catch (Exception ex) {
            }
            log.info("Could not add NodesForwardMapping: " + cvex.getMessage());
            // No need to set result to false: no action should've been taken
            // anyway
        } catch (Exception ex) {
            log.error("Couldn't add NodeForwardMapping" + ex.getMessage());
            try {
                s.getTransaction().rollback();
            } catch (Exception iex) {
            }
            result = false;
        }
        return result;
    }

    public boolean deleteNodeForwardMapping(NodesForwardMappingId id, Session s) {
        boolean result = true;
        try {
            s.beginTransaction();
            NodesForwardMapping nfm = (NodesForwardMapping) s.get(
                    NodesForwardMapping.class, id);
            s.delete(nfm);
            s.getTransaction().commit();
        } catch (Exception ex) {
            log.error("Couldn't delete NodeForwardMapping", ex);
            s.getTransaction().rollback();
            result = false;
        }
        return result;
    }
}
