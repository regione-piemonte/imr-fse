/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.users;

import it.units.htl.web.utils.XmlConfigLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * @author francesco.feront
 */
public class UserUtil {
    private static final Log log = LogFactory.getLog(UserUtil.class);
    
    public static void copyUserProperties(UserBean userFrom, UserBean userTo) {
        userTo.setAccountNo(userFrom.getAccountNo());
        userTo.setFirstName(userFrom.getFirstName());
        userTo.setLastName(userFrom.getLastName());
        userTo.setUserName(userFrom.getUserName());
        userTo.setPassword(userFrom.getPassword());
        userTo.setEmail(userFrom.getEmail());
        userTo.setRole(userFrom.getRole());
        userTo.setRealLifeRole(userFrom.getRealLifeRole());
        userTo.setLastLoginTime(userFrom.getLastLoginTime());
        userTo.setLastLoginDate(userFrom.getLastLoginDate());
        userTo.setLoggedIn(userFrom.isLoggedIn());
        userTo.setPwdExpirationDate(userFrom.getPwdExpirationDate());
        userTo.setLdap(userFrom.isLdap());
    }

    public static void copyUserProperties(SearchResult sr, UserBean userTo) throws NamingException {
        userTo.setAccountNo(1);
        Attribute attr = sr.getAttributes().get("sn");
        if (attr != null) {
            for (Enumeration e1 = attr.getAll(); e1.hasMoreElements();) {
                String unprocessedGroupDN = e1.nextElement().toString();
                userTo.setLastName(unprocessedGroupDN);
                break;
            }
        }
        attr = sr.getAttributes().get("givenName");
        if (attr != null) {
            for (Enumeration e1 = attr.getAll(); e1.hasMoreElements();) {
                String unprocessedGroupDN = e1.nextElement().toString();
                userTo.setFirstName(unprocessedGroupDN);
                break;
            }
        }
        attr = sr.getAttributes().get("cn");
        if (attr != null) {
            for (Enumeration e1 = attr.getAll(); e1.hasMoreElements();) {
                String unprocessedGroupDN = e1.nextElement().toString();
                userTo.setUserName(unprocessedGroupDN);
                break;
            }
        }
        attr = sr.getAttributes().get("cn");
        if (attr != null) {
            for (Enumeration e1 = attr.getAll(); e1.hasMoreElements();) {
                String unprocessedGroupDN = e1.nextElement().toString();
                userTo.setUserName(unprocessedGroupDN);
                break;
            }
        }
        userTo.setPassword("userFromLDAP");
        attr = sr.getAttributes().get("email");
        if (attr != null) {
            for (Enumeration e1 = attr.getAll(); e1.hasMoreElements();) {
                String unprocessedGroupDN = e1.nextElement().toString();
                userTo.setEmail(unprocessedGroupDN);
                break;
            }
        }
        userTo.setPassword("userFromLDAP");
        ArrayList<String> groups = new ArrayList<String>();
        attr = sr.getAttributes().get("memberOf");
        if (attr != null) {
            for (Enumeration e1 = attr.getAll(); e1.hasMoreElements();) {
                String unprocessedGroupDN = e1.nextElement().toString();
                String[] segmenti = unprocessedGroupDN.split(",");
                if (segmenti.length > 0) {
                    String group = segmenti[0].replace("CN=", "");
                    groups.add(group);
                }
            }
        }
        userTo.setRole(getRoleFromGroupName(groups));
        attr = sr.getAttributes().get("department");
        if (attr != null) {
            for (Enumeration e1 = attr.getAll(); e1.hasMoreElements();) {
                String unprocessedGroupDN = e1.nextElement().toString();
                userTo.setRealLifeRole(unprocessedGroupDN);
                break;
            }
        }
        userTo.setLastLoginTime("");
        userTo.setLastLoginDate("");
        userTo.setLoggedIn(true);
        Calendar c = new GregorianCalendar();
        c.set(2100, 12, 31);
        userTo.setPwdExpirationDate(c.getTime());
        userTo.setLdap(true);
    }

    private static Integer  getRoleFromGroupName(ArrayList<String> groups) {
        if(groups.size() ==0)return 0;
        Document config = XmlConfigLoader.getConfigurationFromDB("WebSecurity");
        if(config == null){
            log.error("Unable to load security policy of web!");
            return 0;
        }
        NodeList groupsNode = config.getElementsByTagName("group");
        for(int i = 0; i < groupsNode.getLength(); i++){
            String groupName = groupsNode.item(i).getAttributes().getNamedItem("descr").getTextContent();
            if(groups.contains(groupName)){
                return Integer.parseInt(groupsNode.item(i).getAttributes().getNamedItem("id").getTextContent());
            }
        }
        
        
        
        
        
        return 0;
    }
}
