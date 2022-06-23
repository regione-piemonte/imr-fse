/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.spring;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import it.csi.dmass.dmassbatchcancpacc.client.ScaricoStudiWSBean.ScaricoStudiWSBean;
import it.csi.dmass.dmassbatchcancpacc.logging.DmassBatchCancpaccLogger;
import it.csi.dmass.dmassbatchcancpacc.run.batch.Batch;

@Configuration
@ComponentScan("it.csi.dmass.dmassbatchcancpacc")
@PropertySource("classpath:environment.properties")
@EnableTransactionManagement
public class AppConfig {

	@Autowired
	Environment environment;

	private final String DMASSURL = "dmassurl";
	private final String DMASSUSER = "dmassdbuser";
	private final String DMASSDRIVER = "driver";
	private final String DMASSPASSWORD = "dmassdbpassword";

	public static final String FS_LOG_PATH = "fs.log.path";
	public static final String LOG4J_APPENDER_THRESHOLD = "log4j.appender.dmassbatchcancpacc.Threshold";
		
	public static final String CC_ENCRYPT_PASSWORD = "ccencryptionkey";
	
	@Value("${client.scaricoStudiWSBean.address}")
	private String scaricoStudiWSBeanAddress;

	private Date AUDIT;	
	
	@Value("${userScaricoStudi}")
	private String userScaricoStudi;
	
	@Value("${passScaricoStudi}")
	private String passScaricoStudi;

	@Bean
	DmassBatchCancpaccLogger dmassBatchCancpaccLogger() {		
		AUDIT = new Date(System.currentTimeMillis());
		DmassBatchCancpaccLogger LOG = new DmassBatchCancpaccLogger(Logger.getLogger(Batch.class));
		final FileAppender fa = new FileAppender();
		fa.setName(LOG.getName());
		fa.setFile(environment.getProperty("fs.log.path").concat(String.valueOf(File.separator)
				+ new SimpleDateFormat("yyyyMMddHHmmss").format(AUDIT) + "dmassbatchcancpacc.log"));
		fa.setLayout((Layout) new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold((Priority) Level.toLevel(environment.getProperty("log4j.appender.dmassbatchcancpacc.Threshold"),
				Level.ERROR));
		fa.setAppend(true);
		fa.activateOptions();
		LOG.addAppender(fa);

		final ConsoleAppender console = new ConsoleAppender();
		console.setName(LOG.getName());
		console.setLayout((Layout) new PatternLayout("%d %-5p [%c{1}] %m%n"));
		console.setThreshold((Priority) Level.toLevel(environment.getProperty("log4j.appender.dmassbatchcancpacc.Threshold"),
				Level.ERROR));
		console.activateOptions();
		LOG.addAppender(console);

		return LOG;
	}
	
	@Bean
	Properties config() {
		//1) Lettura Configurazione				
		Properties proerties = new Properties();	
		proerties.setProperty(CC_ENCRYPT_PASSWORD, environment.getProperty("ccencryptionkey"));
		proerties.setProperty("fs.log.path", environment.getProperty("fs.log.path"));
		proerties.setProperty("AUDIT", new SimpleDateFormat("yyyyMMddHHmmss").format(AUDIT));						
		return proerties;
	}

	@Bean
	DataSource dmassdataSource() {
		DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
		driverManagerDataSource.setUrl(environment.getProperty(DMASSURL));
		driverManagerDataSource.setUsername(environment.getProperty(DMASSUSER));
		driverManagerDataSource.setPassword(environment.getProperty(DMASSPASSWORD));
		driverManagerDataSource.setDriverClassName(environment.getProperty(DMASSDRIVER));
		return driverManagerDataSource;
	}
	
	@Bean
	public PlatformTransactionManager dmasstransactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(dmassdataSource());
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
				
		clientPasswordCallback.setUserScaricoStudi(userScaricoStudi);				
		clientPasswordCallback.setPassScaricoStudi(passScaricoStudi);
		
		return clientPasswordCallback;
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
		jaxWsProxyFactoryBean.setOutInterceptors(outInterceptors);

		return (ScaricoStudiWSBean) jaxWsProxyFactoryBean.create();
	}
	

}
