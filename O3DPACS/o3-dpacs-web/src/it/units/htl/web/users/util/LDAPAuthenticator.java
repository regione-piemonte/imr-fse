/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.users.util;

import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LDAPAuthenticator {
    Hashtable<String, String> env = new Hashtable<String, String>();
    private static final String LdapServiceKey = "LdapConfiguration";
    private Log log = LogFactory.getLog(LDAPAuthenticator.class);
    
    
    public LDAPAuthenticator() throws Exception {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, GlobalConfigurationLoader.getConfigParam(LdapServiceKey));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
    }

    public SearchResult doLogin(String username, String password) {
        DirContext ctx = null;
        try {
            env.put(Context.SECURITY_PRINCIPAL, username);
            env.put(Context.SECURITY_CREDENTIALS, password);
            ctx = new InitialDirContext(env);
            NamingEnumeration<SearchResult> answer = ctx.search(env.get(Context.PROVIDER_URL), "(cn=" + username + ")", null);
            if (answer.hasMoreElements()) {
                return answer.next();
            }else
                return null;
        } catch (javax.naming.AuthenticationException e) {
            log.debug("User o pwd non validi");
            return null;
        } catch (Exception e) {
            log.debug("Unable to authenticate user");
            return null;
        } finally {
            if (ctx != null)
                try {
                    ctx.close();
                } catch (NamingException e) {
                }
        }
    }
    
    
}
