/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.units.htl.dpacs.core.ServerBean;

/**
 * 
 * @author sangalli.matteo
 */
public class LoginFilter implements Filter {
	private String homePage;
	private List<String> areas;
	private Log log = LogFactory.getLog(LoginFilter.class);

	/** Creates a new instance of LoginFilter */
	public LoginFilter() {

	}

	@SuppressWarnings("unchecked")
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpSession session = ((HttpServletRequest) request).getSession(false);
		String requestURI = ((HttpServletRequest) request).getRequestURI();
		String[] url = requestURI.split("[/]");

		String loginPage = ((HttpServletRequest) request).getContextPath().concat("/" + this.homePage);

		if (checkLoginState(request)) {
			//queste sono le policy dell'utente correntemente loggato
			ArrayList<String> patterns = (ArrayList<String>) session.getAttribute("secPolicies");
			if (patterns == null) {
				log.error("Couldn't find policies configuration for this user");
				((HttpServletResponse) response).sendRedirect(loginPage);
			}
			//queste sono le aree su cui ci interessa "vegliare"
			areas = Arrays.asList((String[]) session.getAttribute("areas"));
			if (areas == null) {
				log.error("Couldn't find policies configuration");
				((HttpServletResponse) response).sendRedirect(loginPage);
			}
			boolean authorized = false;
			//se l'url richiesto fa parte delle aree protette
			if (areas.contains(url[url.length - 2])) {
				for (String p : patterns) {
					Pattern patToVerify = Pattern.compile(p);
					Matcher matcher = patToVerify.matcher(url[url.length - 2]);
					//controllo che l'utente possa effettivamente guardare quest'area
					if (matcher.find()) {
						authorized = true;
					}
				}
				//se l'utente e' autorizzato oppure ho gia' verificato la sua autorizzazione posso andare avanti con il chain do filter.
				if (authorized || request.getAttribute("auth") != null) {
					request.setAttribute("auth", true);
					chain.doFilter(request, response);
				} else {
					((HttpServletResponse) response).sendRedirect(loginPage);
				}
			} else {
				chain.doFilter(request, response);
			}
		} else if (checkIfViewer(session)) {
			areas = Arrays.asList((String[]) session.getAttribute("vwAreas"));
			if (areas == null) {
				((HttpServletResponse) response).sendRedirect(loginPage);
			}
			if (areas.contains(url[url.length - 2])) {
				if ("studies".equals(url[url.length - 2])) {
					chain.doFilter(request, response);
				} else {
					((HttpServletResponse) response).sendRedirect(loginPage);
				}
			} else {
				try {
					chain.doFilter(request, response);
				} catch (Exception e) {
					((HttpServletResponse) response).sendRedirect(loginPage);
				}
			}
		} else {
			if (!requestURI.equals(loginPage)) {
				((HttpServletResponse) response).sendRedirect(loginPage);
			} else {
				try {
					chain.doFilter(request, response);
				} catch (Exception e) {
					((HttpServletResponse) response).sendRedirect(loginPage);
				}
			}
		}
	}

	private boolean checkIfViewer(HttpSession session) {

		if (session != null) {
			if (session.getAttribute("isViewer") != null) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public static boolean checkLoginState(Object request) throws IOException, ServletException {
		boolean isLoggedIn = false;
		HttpSession session = ((HttpServletRequest) request).getSession(false);
		UserBean managedUserBean = null;
		if (null != session && (null != (managedUserBean = (UserBean) session.getAttribute("userBean")))) {
			if (managedUserBean.isLoggedIn()) {
				isLoggedIn = true;
			}
		}
		return isLoggedIn;
	}

	/* Destroy method for this filter */
	public void destroy() {

	}

	/* Init method for this filter */
	public void init(FilterConfig filterConfig) {
		if (filterConfig != null) {
			this.homePage = filterConfig.getInitParameter("home_page");
			new ServerBean(); //GDC: task: 312586 Bug:36178
		}
	}
}
