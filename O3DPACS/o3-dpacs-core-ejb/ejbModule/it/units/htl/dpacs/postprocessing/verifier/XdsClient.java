/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XdsClient {

	Log log = LogFactory.getLog(XdsClient.class);

	private EndpointReference targetEPR;
	private String wsAction;
	private boolean isMtomEnabled = false;
	private int timeoutInSeconds = 120;
	private int maxConnectionsPerHost = 20;

	public XdsClient(String endpointUrl) {
		this.targetEPR = new EndpointReference(endpointUrl);
	}

	public OMElement send(OMElement payload) throws Exception {

		if (this.wsAction == null) {
			throw new IllegalStateException("wsAction must be specified");
		}

		String soapVersion = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
		String transportProtocol = Constants.TRANSPORT_HTTP;

		log.debug("SOAP message target: " + this.targetEPR.toString());
		log.debug("WS Action: " + this.wsAction);
		log.debug("Soap Version: " + soapVersion);
		log.debug("Transport Protocol: " + transportProtocol);

		OMElement response = null;
		ServiceClient sender = null;

		try {

			Options options = new Options();
			options.setTo(this.targetEPR);
			options.setTransportInProtocol(transportProtocol);
			options.setAction(this.wsAction);
			options.setSoapVersionURI(soapVersion);
			options.setTimeOutInMilliSeconds(this.timeoutInSeconds * 1000);
			if (this.isMtomEnabled) {
				options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
			}

			ConfigurationContext configurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();
			MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
			HttpConnectionManagerParams params = new HttpConnectionManagerParams();
			params.setDefaultMaxConnectionsPerHost(this.maxConnectionsPerHost);
			multiThreadedHttpConnectionManager.setParams(params);

			HttpClient httpClient = new HttpClient(multiThreadedHttpConnectionManager);
			configurationContext.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);

			sender = new ServiceClient(null, null);

			ConfigurationContext context = sender.getServiceContext().getConfigurationContext();
			context.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);
			context.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
			context.setProperty(HTTPConstants.AUTO_RELEASE_CONNECTION, true);
			sender.setOptions(options);

			sender.engageModule("addressing"); // engage WS-Addressing module

			log.debug("Sending SOAP message...");
			response = sender.sendReceive(payload);

		} finally {
			if (sender != null) {
				try {
					sender.cleanupTransport();
				} catch (Exception e) {
					log.error("", e);
				}

				try {
					sender.cleanup();
				} catch (Exception e) {
				    log.error("", e);
				}
			}
		}

		return response;
	}

	public void setWsAction(String wsAction) {
		this.wsAction = wsAction;
	}

	/**
	 * Specify if MTOM/XOP is to be used. By default it is disabled.
	 * 
	 * @param isEnabled
	 */
	public void setMtomEnabled(boolean isEnabled) {
		this.isMtomEnabled = isEnabled;
	}
	
	public void setMaxConnectionsPerHost(int maxConnectionsPerHost){
		this.maxConnectionsPerHost = maxConnectionsPerHost;
	}

	public static final String WS_ACTION_PROVIDE_AND_REGISTER_DOCUMENT_SET = "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b";
	public static final String WS_ACTION_REGISTRY_STORED_QUERY = "urn:ihe:iti:2007:RegistryStoredQuery";
}
