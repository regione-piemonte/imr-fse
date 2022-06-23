/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import it.csi.dmass.client.CCConsensoINIExtService.CCConsensoINIExtServicePortType;
import it.csi.dmass.client.ScaricoStudiWSBean.ScaricoStudiWSBean;
import it.csi.dmass.client.delegaService.dmacc.DelegaService;
import it.csi.dmass.client.utilityService.UtilityService;
import it.csi.dmass.client.verificaService.VerificaService;
import it.csi.dmass.service.ScaricoPacchettoCitAutService;
import it.csi.dmass.service.ScaricoPacchettoCitNoAutService;
import it.csi.dmass.service.ScaricoPacchettoOperatoreSanitarioService;





@Configuration
@EnableTransactionManagement
public class AppConfig {

	@Autowired
	Environment environment;		

	private final String DMASSJINDINAME = "dmassjndiName";
	
	@Value("${client.utilityService.address}")
	private String utilityServiceAddress;
	
	@Value("${client.verificaService.address}")
	private String verificaServiceAddress;
	
	@Value("${client.scaricoStudiWSBean.address}")
	private String scaricoStudiWSBeanAddress;
	
	@Value("${client.consensoINIExtServices.address}")
	private String consensoINIExtServicesAddress;
	
	@Value("${client.delegaService.address}")
	private String delegaAddress;
	
	@Value("${userUtility}")
	private String userUtility;		
	
	@Value("${passUtility}")
	private String passUtility;
	
	@Value("${userDma}")
	private String userDma;
	
	@Value("${userConsensoIni}")
	private String userConsensoIni;
	
	@Value("${passConsensoIni}")
	private String passConsensoIni;
	
	@Value("${userVerifica}")
	private String userVerifica;		
	
	@Value("${passVerifica}")
	private String passVerifica;
	
	@Value("${userDelega}")
	private String userDelega;
	
	@Value("${passDelega}")
	private String passDelega;
	
	@Value("${userScaricoStudi}")
	private String userScaricoStudi;
	
	@Value("${passScaricoStudi}")
	private String passScaricoStudi;
	
	//public static final String CC_ENCRYPT_PASSWORD = "ccencryptionkey";
	
	@Bean
	public ScaricoPacchettoOperatoreSanitarioService scaricoPacchettoOperatoreSanitarioService() {
		return new ScaricoPacchettoOperatoreSanitarioService();
	}
	
	@Bean
	public ScaricoPacchettoCitAutService scaricoPacchettoCitAutService() {
		return new ScaricoPacchettoCitAutService();
	}
	
	@Bean
	public ScaricoPacchettoCitNoAutService scaricoPacchettoCitNoAutService() {
		return new ScaricoPacchettoCitNoAutService();
	}
			
	@Bean
	JndiObjectFactoryBean dmassdataSource() {		
		JndiObjectFactoryBean jndiObjectFactoryBean =new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName(environment.getProperty(DMASSJINDINAME));
        jndiObjectFactoryBean.setResourceRef(true);
        jndiObjectFactoryBean.setProxyInterface(DataSource.class);
        return jndiObjectFactoryBean;		
	}

	@Bean
	public PlatformTransactionManager dmasstransactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource((DataSource)dmassdataSource().getObject());
		return transactionManager;
	}
	
	@Bean(name = "logOut")
	public LoggingOutInterceptor logOut(){
		return new LoggingOutInterceptor();
	}
	
	@Bean(name = "logIn")
	public LoggingInInterceptor logIn(){
		return new LoggingInInterceptor();
	}
	
	@Bean(name = "passwordCallbac")
	public ClientPasswordCallback passwordCallbac(){
		ClientPasswordCallback clientPasswordCallback = new ClientPasswordCallback();
		
		clientPasswordCallback.setUserUtility(userUtility);
		clientPasswordCallback.setPassUtility(passUtility);
		
		clientPasswordCallback.setUserConsensoIni(userConsensoIni);
		clientPasswordCallback.setPassConsensoIni(passConsensoIni);
		
		clientPasswordCallback.setUserDelega(userDelega);
		clientPasswordCallback.setPassDelega(passDelega);
				
		clientPasswordCallback.setUserScaricoStudi(userScaricoStudi);				
		clientPasswordCallback.setPassScaricoStudi(passScaricoStudi);
		
		return clientPasswordCallback;
	}

	@Bean(name = "wss4jOutSecuredDma")
	public WSS4JOutInterceptor wss4jOutSecuredDma(){
		
		WSS4JOutInterceptor wSS4JOutInterceptor = new WSS4JOutInterceptor();
		Map<String, Object> properties = new HashMap<String, Object>(); 
		properties.put("mustUnderstand", "false");
		properties.put("action",WSHandlerConstants.USERNAME_TOKEN);
		properties.put("user", userDma);
		properties.put("passwordCallbackRef", passwordCallbac());	
		properties.put("passwordType","PasswordText");
		wSS4JOutInterceptor.setProperties(properties);
		
		return wSS4JOutInterceptor;
	}
      
	@Bean(name = "utilityServiceProxy")
	public UtilityService utilityServiceProxy() {
		JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
		jaxWsProxyFactoryBean.setServiceClass(UtilityService.class);
		jaxWsProxyFactoryBean.setAddress(utilityServiceAddress);		
		
		List<Interceptor<? extends Message>> inInterceptors = new ArrayList<Interceptor<? extends Message>>();
		inInterceptors.add(logIn());
		jaxWsProxyFactoryBean.setInInterceptors(inInterceptors);
		  
		List<Interceptor<? extends Message>> outInterceptors = new ArrayList<Interceptor<? extends Message>>();
		outInterceptors.add(logOut());
		outInterceptors.add(wss4jOutSecuredDma());
		jaxWsProxyFactoryBean.setOutInterceptors(outInterceptors);

		return (UtilityService) jaxWsProxyFactoryBean.create();
	}
	
	@Bean(name = "wss4jOutSecuredVerifica")
	public WSS4JOutInterceptor wss4jOutSecuredVerifica(){
		
		WSS4JOutInterceptor wSS4JOutInterceptor = new WSS4JOutInterceptor();
		Map<String, Object> properties = new HashMap<String, Object>(); 
		
		properties.put("action",WSHandlerConstants.USERNAME_TOKEN);
		properties.put("user", userVerifica);
		properties.put("passwordCallbackRef", passwordCallbac());	
		properties.put("passwordType","PasswordText");
		properties.put("mustUnderstand","false");
		wSS4JOutInterceptor.setProperties(properties);
		
		return wSS4JOutInterceptor;
	}		
	
	@Bean(name = "verificaServiceProxy")
	public VerificaService verificaServiceProxy() {
		JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
		jaxWsProxyFactoryBean.setServiceClass(VerificaService.class);
		jaxWsProxyFactoryBean.setAddress(verificaServiceAddress);		
		
		List<Interceptor<? extends Message>> inInterceptors = new ArrayList<Interceptor<? extends Message>>();
		inInterceptors.add(logIn());
		jaxWsProxyFactoryBean.setInInterceptors(inInterceptors);
		  
		List<Interceptor<? extends Message>> outInterceptors = new ArrayList<Interceptor<? extends Message>>();
		outInterceptors.add(logOut());
		outInterceptors.add(wss4jOutSecuredVerifica());
		jaxWsProxyFactoryBean.setOutInterceptors(outInterceptors);

		return (VerificaService) jaxWsProxyFactoryBean.create();
	}
	
	@Bean(name = "wss4jOutSecuredScaricoStudi")
	public WSS4JOutInterceptor wss4jOutSecuredScaricoStudi(){
		
		WSS4JOutInterceptor wSS4JOutInterceptor = new WSS4JOutInterceptor();
		Map<String, Object> properties = new HashMap<String, Object>(); 
				
		properties.put("mustUnderstand","false");
		properties.put("action",WSHandlerConstants.USERNAME_TOKEN);
		properties.put("user", userScaricoStudi);
		properties.put("passwordCallbackRef", passwordCallbac());	
		properties.put("passwordType","PasswordText");
		wSS4JOutInterceptor.setProperties(properties);
		
		return wSS4JOutInterceptor;
	}
	
	@Bean(name = "scaricoStudiWSBeanProxy")
	public ScaricoStudiWSBean scaricoStudiWSBeanProxy() {
		JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
		jaxWsProxyFactoryBean.setServiceClass(ScaricoStudiWSBean.class);
		jaxWsProxyFactoryBean.setAddress(scaricoStudiWSBeanAddress);		
		
		List<Interceptor<? extends Message>> inInterceptors = new ArrayList<Interceptor<? extends Message>>();
		inInterceptors.add(logIn());
		jaxWsProxyFactoryBean.setInInterceptors(inInterceptors);
		  
		List<Interceptor<? extends Message>> outInterceptors = new ArrayList<Interceptor<? extends Message>>();
		outInterceptors.add(logOut());		
		outInterceptors.add(wss4jOutSecuredScaricoStudi());
		jaxWsProxyFactoryBean.setOutInterceptors(outInterceptors);

		return (ScaricoStudiWSBean) jaxWsProxyFactoryBean.create();
	}
	
	@Bean(name = "wss4jOutSecuredConsenso")
	public WSS4JOutInterceptor wss4jOutSecuredConsenso(){
		
		WSS4JOutInterceptor wSS4JOutInterceptor = new WSS4JOutInterceptor();
		Map<String, Object> properties = new HashMap<String, Object>(); 
		
		properties.put("action",WSHandlerConstants.USERNAME_TOKEN);
		properties.put("user", userConsensoIni);
		properties.put("passwordCallbackRef", passwordCallbac());	
		properties.put("passwordType","PasswordDigest");
		wSS4JOutInterceptor.setProperties(properties);
		
		return wSS4JOutInterceptor;
	}
	
	
	@Bean(name = "consensoINIExtServicesProxy")
	public CCConsensoINIExtServicePortType consensoINIExtServicesProxy() {
		JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
		jaxWsProxyFactoryBean.setServiceClass(CCConsensoINIExtServicePortType.class);
		jaxWsProxyFactoryBean.setAddress(consensoINIExtServicesAddress);		
		
		List<Interceptor<? extends Message>> inInterceptors = new ArrayList<Interceptor<? extends Message>>();
		inInterceptors.add(logIn());
		jaxWsProxyFactoryBean.setInInterceptors(inInterceptors);
		  
		List<Interceptor<? extends Message>> outInterceptors = new ArrayList<Interceptor<? extends Message>>();
		outInterceptors.add(logOut());
		outInterceptors.add(wss4jOutSecuredConsenso());
		jaxWsProxyFactoryBean.setOutInterceptors(outInterceptors);

		return (CCConsensoINIExtServicePortType) jaxWsProxyFactoryBean.create();
	}
	
	@Bean(name = "wss4jOutSecuredDelega")
	public WSS4JOutInterceptor wss4jOutSecuredDelega(){
		
		WSS4JOutInterceptor wSS4JOutInterceptor = new WSS4JOutInterceptor();
		Map<String, Object> properties = new HashMap<String, Object>(); 
				
		properties.put("mustUnderstand","false");
		properties.put("action",WSHandlerConstants.USERNAME_TOKEN);
		properties.put("user", userDelega);
		properties.put("passwordCallbackRef", passwordCallbac());	
		properties.put("passwordType","PasswordText");
		wSS4JOutInterceptor.setProperties(properties);
		
		return wSS4JOutInterceptor;
	}
	
	
	@Bean(name = "delegaServicesProxy")
	public DelegaService delegaServicesProxy() {
		JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
		jaxWsProxyFactoryBean.setServiceClass(DelegaService.class);
		jaxWsProxyFactoryBean.setAddress(delegaAddress);		
		
		List<Interceptor<? extends Message>> inInterceptors = new ArrayList<Interceptor<? extends Message>>();
		inInterceptors.add(logIn());
		jaxWsProxyFactoryBean.setInInterceptors(inInterceptors);
		  
		List<Interceptor<? extends Message>> outInterceptors = new ArrayList<Interceptor<? extends Message>>();
		outInterceptors.add(logOut());
		outInterceptors.add(wss4jOutSecuredDelega());
		jaxWsProxyFactoryBean.setOutInterceptors(outInterceptors);

		return (DelegaService) jaxWsProxyFactoryBean.create();
	}
		
}
