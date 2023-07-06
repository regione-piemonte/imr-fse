/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.users;

import it.units.htl.maps.Users;
import it.units.htl.maps.UsersHome;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.Connector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 *
 * @author francesco.feront
 */

public class UserQuery {
	private static Log log = LogFactory.getLog(UserQuery.class);
	
    public static UserBean findUser(String _username, String _password) {
    	UserBean currentUser = null;
        try {            
            Users u = new Users();
            u.setUserName(_username);
            u.setPassword(_password);
            final Session s = SessionManager.getInstance().openSession();  
            UsersHome UH = new UsersHome();
            List<Users> rsUser =  UH.findByExample(u, s);
            s.close();
            if(rsUser.size() == 1){
            	u = rsUser.get(0);
            	currentUser = new UserBean();
                currentUser.setFirstName(u.getFirstName());
                currentUser.setLastName(u.getLastName());
                currentUser.setUserName(u.getUserName());
                currentUser.setPassword(u.getPassword());
                currentUser.setEmail(u.getEmail());
                currentUser.setRole(u.getRoleFk());
                currentUser.setAccountNo(u.getPk());
                currentUser.setRealLifeRole(u.getRealLifeRole());
                if (u.getLastLoginDate()!=null){
                    currentUser.setLastLoginDate(u.getLastLoginDate().toString());
                }else{
                    currentUser.setLastLoginDate("");
                }
                if (u.getLastLoginTime()!=null){
                    currentUser.setLastLoginTime(u.getLastLoginTime().toString());
                }else{
                    currentUser.setLastLoginTime("");
                }
                Date pwdExpirationDate=u.getPwdExpirationDate();
                
                currentUser.setPwdExpirationDate(pwdExpirationDate);
                // if currentUser.isLoggedIn() == false -> credentials were right, but expired!!!
                if(pwdExpirationDate==null)
                	currentUser.setLoggedIn(false);
                else
                	currentUser.setLoggedIn((pwdExpirationDate.after(new Date()))?true:false);
            }            
            
        } catch(Exception e){
            log.error("Error during login:", e);

        }
        return currentUser;
    }
    
    public static boolean updateUserField(String _username, String _nameOfField, Object _value) {
        try {
            Connection connection = Connector.getInstance().getConnection();
            PreparedStatement pstmt = connection.prepareStatement("UPDATE Users SET "+_nameOfField+" = ? WHERE userName = ?");
            pstmt.setObject(1, _value);
            pstmt.setString(2, _username);
            pstmt.execute();
            pstmt.close();
            connection.close();
            return true;
        } catch(Exception e){
        	log.error("Error during update web user:", e);
            return false;
        }
    }
}
