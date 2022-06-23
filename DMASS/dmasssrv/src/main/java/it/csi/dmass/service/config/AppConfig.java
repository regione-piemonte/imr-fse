package it.csi.dmass.service.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class AppConfig {

	@Autowired
	Environment environment;		

	private final static String DMACCJINDINAME = "dmaccjndiName";
			
	public final static String UserNameFSE = "UserNameFSE";
	public final static String PasswordFSE = "PasswordFSE";
			
	@Bean
	JndiObjectFactoryBean dmaccdataSource() {		
		JndiObjectFactoryBean jndiObjectFactoryBean =new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName(environment.getProperty(DMACCJINDINAME));
        jndiObjectFactoryBean.setResourceRef(true);
        jndiObjectFactoryBean.setProxyInterface(DataSource.class);
        return jndiObjectFactoryBean;		
	}
		
	@Bean
	Map<String,String> credentilas() {		
		Map<String,String>  credentilas = new HashMap<String,String>();
		credentilas.put(UserNameFSE,environment.getProperty(UserNameFSE));
		credentilas.put(PasswordFSE,environment.getProperty(PasswordFSE));
        return credentilas;		
	}

	@Bean
	public PlatformTransactionManager dmacctransactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource((DataSource)dmaccdataSource().getObject());
		return transactionManager;
	}	

 
		
}
