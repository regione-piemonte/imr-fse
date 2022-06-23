package it.csi.dmass;

import javax.sql.DataSource;
import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import it.csi.dmass.scaricoStudi.SaricoStudiWSBeanImpl;

@Configuration
@ComponentScan("it.csi.dmass")
public class AppConfig {
	
	@Autowired
	Environment environment;	

	@Autowired
	private Bus bus;	
	
	private final String DMACCJINDINAME = "dmaccjndiName";
	
	@Bean
	JndiObjectFactoryBean dmaccdataSource() {		
		JndiObjectFactoryBean jndiObjectFactoryBean =new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName(environment.getProperty(DMACCJINDINAME));
        jndiObjectFactoryBean.setResourceRef(true);
        jndiObjectFactoryBean.setProxyInterface(DataSource.class);
        return jndiObjectFactoryBean;
		
	}

	@Bean
	public PlatformTransactionManager dmacctransactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource((DataSource)dmaccdataSource().getObject());
		return transactionManager;
	}

	@Bean
	public Endpoint endpoint() {
		EndpointImpl endpoint = new EndpointImpl(bus, new SaricoStudiWSBeanImpl());
		endpoint.publish("/scaricoStudiWSBean");

		return endpoint;
	}

}
