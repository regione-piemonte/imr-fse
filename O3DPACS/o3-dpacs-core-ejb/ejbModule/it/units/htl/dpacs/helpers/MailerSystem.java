/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import it.units.htl.dpacs.dao.Dbms;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.log4j.Logger;

/**
 * This MailerSystem offers static methods to easily send email messages. The simplest way
 * to send an email is the following:<br>
 * <code>MailerSystem.send(ConfigurationSetting.EMAIL_EVENT_XY);</code> <br>
 * This would result in sending an email to every member of the group specified in the
 * EmailMessages table for the email with id EMAIL_EVENT_XY (default is administrator
 * group). The message subject and content are taken from the same row of the
 * EmailMessages table. The default locale is 'en'.<br>
 * <br>
 * It is however possible to specify custom recipients, as well as the language. <br>
 * The last parameter is always the token array used to substitute any token in the
 * message body.<br>
 * <br>
 * <h3>Special characters in body text and subject</h3> The body text and subject are
 * taken from the EmailMessages table. If a newline is required, it is necessary to use
 * the following: "\n".<br>
 * 
 * @author giacomo petronio
 * 
 */
public class MailerSystem {

	private static Logger log = Logger.getLogger(MailerSystem.class);

	/**
	 * Same as calling MailerSystem.send(emailMessageId, null, null, tokens);
	 * 
	 * @param emailMessageId
	 * @param tokens
	 */
	public static void send(String emailMessageId, String[] tokens) {
		MailerSystem.send(emailMessageId, null, null, tokens);
	}

	/**
	 * Same as calling MailerSystem.send(emailMessageId, language, null, tokens);
	 * 
	 * @param emailMessageId
	 * @param language
	 * @param tokens
	 */
	public static void send(String emailMessageId, String language, String[] tokens) {
		MailerSystem.send(emailMessageId, language, null, tokens);
	}

	/**
	 * Same as calling MailerSystem.send(emailMessageId, null, recipients, tokens);
	 * 
	 * @param emailMessageId
	 * @param recipients
	 * @param tokens
	 */
	public static void send(String emailMessageId, String[] recipients, String[] tokens) {
		MailerSystem.send(emailMessageId, null, recipients, tokens);
	}

	/**
	 * This method sends an email to the specified recipients. The body message is taken
	 * from the EmailMessage table with the specified emailMessageId and language. If
	 * present, tokens are used to substitute any token in the body message.<br>
	 * The only required parameter is the emailMessageId, the other parameters can be
	 * null. <br>
	 * If the recipients parameter is null, the email addresses will be taken from those
	 * users belonging to the targetRole found in the email message row (default 1,
	 * meaning administrator role)
	 * 
	 * @param emailMessageId
	 * @param language
	 *            can be null, default is 'en'
	 * @param recipients
	 *            can be null, default will send email to every user with administrator
	 *            role
	 * @param tokens
	 *            can be null
	 */
	public static void send(String emailMessageId, String language, String[] recipients, Object[] tokens) {
		log.debug("entering send(emailMessageId=" + emailMessageId + ", language=" + language + ", recipients=" + recipients + ", tokens=" + tokens + ")");

		if (language == null) {
			language = "en";
		}

		EmailMessage emailMessage = getEmailMessage(emailMessageId, language);
		if (emailMessage != null) {
			if (recipients == null) {
				// get Recipients from the targetRole of the EmailMessage
				recipients = getEmailsForRole(emailMessage.getTargetRole());
			}
			emailMessage.setRecipients(recipients);

			if (tokens != null) {
				String output = MessageFormat.format(emailMessage.getBodyText(), tokens);
				emailMessage.setBodyText(output);
			}

			sendEmail(emailMessage);
		} else {
			log.warn("Unable to retrieve the email message with id '" + emailMessageId + "' and language '" + language + "', cannot send email");
		}
		log.debug("leaving send()");
	}

	private static void sendEmail(EmailMessage emailMessage) {
		log.debug("entering sendEmail(emailMessage=" + emailMessage + ")");
		try {
			Context initCtx = new InitialContext();
			Session session = (Session) initCtx.lookup("java:/Mail");
			Message message = new MimeMessage(session);
			List<InternetAddress> addressList = new ArrayList<InternetAddress>();

			String[] recipients = emailMessage.getRecipients();

			for (int i = 0; i < recipients.length; i++) {
				try {
					InternetAddress address = new InternetAddress(recipients[i]);
					addressList.add(address);
				} catch (AddressException e) {
					log.warn("The following user email address is invalid: " + recipients[i], e);
				}
			}

			InternetAddress addresses[] = new InternetAddress[addressList.size()];
			addressList.toArray(addresses);
			message.setRecipients(Message.RecipientType.TO, addresses);
			message.setSubject(emailMessage.getSubject());
			message.setText(emailMessage.getBodyText());
			Transport.send(message);
			log.debug("email sent");
		} catch (NamingException ex) {
			log.error("Error while trying to retrieve the JBoss Mail Service", ex);
		} catch (MessagingException ex) {
			log.error("Error while trying to send email", ex);
		}
		log.debug("leaving sendEmail()");
	}

	/**
	 * Replace any occurrence of "/n" in a new line.<br>
	 * Fix any single quote character "'" in order to avoid problems in the
	 * MessageFormat.format method (a "'" becomes a "'''")
	 * 
	 * @return
	 */
	private static String replaceSpecialCharacters(String text) {
		String parsedText = null;
		parsedText = text.replace("\\n", "\n");
		parsedText = parsedText.replace("'", "''");
		return parsedText;

	}

	private static Connection getDBConnection() throws SQLException {
		Connection dbConn = null;
		try {
			Context jndiContext = new InitialContext();
			DataSource dataSource = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
			dbConn = dataSource.getConnection();
		} catch (NamingException ex) {
			log.error("Unable to retrieve a connection from dbDS", ex);
		}

		return dbConn;
	}

	private static EmailMessage getEmailMessage(String id, String language) {
		log.debug("entering getEmailMessage(id=" + id + ", language=" + language + ")");
		EmailMessage result = null;
		Connection connection = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		try {
			connection = getDBConnection();
			boolean isOracle = Dbms.isOracle(connection);
			if (isOracle) {
				cs = connection.prepareCall("{call getEventEmail(?,?,?)}");
				cs.registerOutParameter(3, OracleTypes.CURSOR);
			} else {
				cs = connection.prepareCall("{call getEventEmail(?,?)}");
			}
			cs.setString(1, id);
			cs.setString(2, language);
			if (isOracle) {
				cs.execute();
				rs = (ResultSet) cs.getObject(3);
			} else {
				rs = cs.executeQuery();
			}
			if ((rs != null) && (rs.next())) {
				String subject = replaceSpecialCharacters(rs.getString(1));
				String bodyText = replaceSpecialCharacters(rs.getString(2));
				result = new EmailMessage(id, language, subject, bodyText, rs.getLong(3));
			}
		} catch (Exception ex) {
			log.error("Error retrieving email message for " + id + " " + language, ex);
		} finally {
			try {
				rs.close();
			} catch (Exception ex) {
			}
			try {
				cs.close();
			} catch (Exception ex) {
			}
			try {
				connection.close();
			} catch (Exception ex) {
			}
		}
		log.debug("Leaving getEmailMessage(): " + result);
		return result;
	}

	/**
	 * Retrieve the email of every user that belongs to the specified role
	 * 
	 * @param roleId
	 * @return
	 */
	private static String[] getEmailsForRole(long roleId) {
		log.debug("entering getEmailsForRole(roleId=" + roleId + ")");
		ArrayList<String> result = null;
		String[] res = null;
		Connection connection = null;
		CallableStatement cs = null;
		ResultSet rs = null;

		try {
			connection = getDBConnection();
			boolean isOracle = Dbms.isOracle(connection);
			if (isOracle) {
				cs = connection.prepareCall("{call getEmailFromRoleFk(?,?)}");
				cs.registerOutParameter(2, OracleTypes.CURSOR);
			} else {
				cs = connection.prepareCall("{call getEmailFromRoleFk(?)}");
			}
			cs.setLong(1, roleId);
			if (isOracle) {
				cs.execute();
				rs = (ResultSet) cs.getObject(2);
			} else {
				rs = cs.executeQuery();
			}

			if (rs != null) {
				while (rs.next()) {
					if (result == null)
						result = new ArrayList<String>();
					result.add(rs.getString(1));
				}
			}
		} catch (SQLException ex) {
			log.error("An error occurred retrieving emails from role", ex);
		} finally {
			try {
				rs.close();
			} catch (Exception ex) {
			}
			try {
				cs.close();
			} catch (Exception ex) {
			}
			try {
				connection.close();
			} catch (Exception ex) {
			}
		}

		if ((result != null) && (result.size() > 0)) {
			res = new String[result.size()];
			result.toArray(res);
		}
		log.debug("leaving getEmailsForRole(): " + res);
		return res;
	}
}
