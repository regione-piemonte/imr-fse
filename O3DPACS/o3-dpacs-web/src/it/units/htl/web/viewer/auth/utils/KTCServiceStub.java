/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.viewer.auth.utils;

/*
 *  KTCServiceStub java implementation
 */
public class KTCServiceStub extends org.apache.axis2.client.Stub {
    protected org.apache.axis2.description.AxisOperation[] _operations;
    // hashmaps to keep the fault mapping
    private java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
    private java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
    private java.util.HashMap faultMessageMap = new java.util.HashMap();
    private static int counter = 0;

    private static synchronized java.lang.String getUniqueSuffix() {
        // reset the counter if it is greater than 99999
        if (counter > 99999) {
            counter = 0;
        }
        counter = counter + 1;
        return java.lang.Long.toString(System.currentTimeMillis()) + "_" + counter;
    }

    private void populateAxisService() throws org.apache.axis2.AxisFault {
        // creating the Service with a unique name
        _service = new org.apache.axis2.description.AxisService("KTCService" + getUniqueSuffix());
        addAnonymousOperations();
        // creating the operations
        org.apache.axis2.description.AxisOperation __operation;
        _operations = new org.apache.axis2.description.AxisOperation[2];
        __operation = new org.apache.axis2.description.OutInAxisOperation();
        __operation.setName(new javax.xml.namespace.QName("http://tempuri.org", "getSessionInfo"));
        _service.addOperation(__operation);
        _operations[0] = __operation;
        __operation = new org.apache.axis2.description.OutInAxisOperation();
        __operation.setName(new javax.xml.namespace.QName("http://tempuri.org", "echo"));
        _service.addOperation(__operation);
        _operations[1] = __operation;
    }

    // populates the faults
    private void populateFaults() {
    }

    /**
     *Constructor that takes in a configContext
     */
    public KTCServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext,
            java.lang.String targetEndpoint)
            throws org.apache.axis2.AxisFault {
        this(configurationContext, targetEndpoint, false);
    }

    /**
     * Constructor that takes in a configContext and useseperate listner
     */
    public KTCServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext,
            java.lang.String targetEndpoint, boolean useSeparateListener)
            throws org.apache.axis2.AxisFault {
        // To populate AxisService
        populateAxisService();
        populateFaults();
        _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext, _service);
        _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(
                targetEndpoint));
        _serviceClient.getOptions().setUseSeparateListener(useSeparateListener);
    }

    /**
     * Default Constructor
     */
    public KTCServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext) throws org.apache.axis2.AxisFault {
        this(configurationContext, "http://localhost/webapp/Custom.KS.KTCService.cls");
    }

    /**
     * Default Constructor
     */
    public KTCServiceStub() throws org.apache.axis2.AxisFault {
        this("http://localhost/webapp/Custom.KS.KTCService.cls");
    }

    /**
     * Constructor taking the target endpoint
     */
    public KTCServiceStub(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(null, targetEndpoint);
    }

    /**
     * Auto generated method signature
     * 
     * @see org.tempuri.KTCService#getSessionInfo
     * @param getSessionInfo
     */
    public it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfoResponse getSessionInfo(
            it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfo getSessionInfo)
    throws java.rmi.RemoteException
    {
        org.apache.axis2.context.MessageContext _messageContext = null;
        try {
            org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
            _operationClient.getOptions().setAction("http://tempuri.org/Custom.KS.KTCService.GetSessionInfo");
            _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
            addPropertyToOperationClient(_operationClient, org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");
            // create a message context
            _messageContext = new org.apache.axis2.context.MessageContext();
            // create SOAP envelope with that payload
            org.apache.axiom.soap.SOAPEnvelope env = null;
            env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    getSessionInfo,
                                                    optimizeContent(new javax.xml.namespace.QName("http://tempuri.org",
                                                            "getSessionInfo")));
            // adding SOAP soap_headers
            _serviceClient.addHeadersToEnvelope(env);
            // set the message context with that soap envelope
            _messageContext.setEnvelope(env);
            // add the message contxt to the operation client
            _operationClient.addMessageContext(_messageContext);
            // execute the operation client
            _operationClient.execute(true);
            org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
            java.lang.Object object = fromOM(
                                             _returnEnv.getBody().getFirstElement(),
                                             it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfoResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));
            return (it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfoResponse) object;
        } catch (org.apache.axis2.AxisFault f) {
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt != null) {
                if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
                    // make the fault by reflection
                    try {
                        java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex =
                                (java.lang.Exception) exceptionClass.newInstance();
                        // message class
                        java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt, messageClass, null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[] { messageClass });
                        m.invoke(ex, new java.lang.Object[] { messageObject });
                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    } catch (java.lang.ClassCastException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                } else {
                    throw f;
                }
            } else {
                throw f;
            }
        } finally {
            _messageContext.getTransportOut().getSender().cleanup(_messageContext);
        }
    }

    /**
     * Auto generated method signature
     * 
     * @see org.tempuri.KTCService#echo
     * @param echo
     */
    public it.units.htl.web.viewer.auth.utils.KTCServiceStub.EchoResponse echo(
            it.units.htl.web.viewer.auth.utils.KTCServiceStub.Echo echo)
    throws java.rmi.RemoteException
    {
        org.apache.axis2.context.MessageContext _messageContext = null;
        try {
            org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
            _operationClient.getOptions().setAction("http://tempuri.org/Custom.KS.KTCService.echo");
            _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
            addPropertyToOperationClient(_operationClient, org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");
            // create a message context
            _messageContext = new org.apache.axis2.context.MessageContext();
            // create SOAP envelope with that payload
            org.apache.axiom.soap.SOAPEnvelope env = null;
            env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    echo,
                                                    optimizeContent(new javax.xml.namespace.QName("http://tempuri.org",
                                                            "echo")));
            // adding SOAP soap_headers
            _serviceClient.addHeadersToEnvelope(env);
            // set the message context with that soap envelope
            _messageContext.setEnvelope(env);
            // add the message contxt to the operation client
            _operationClient.addMessageContext(_messageContext);
            // execute the operation client
            _operationClient.execute(true);
            org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
            java.lang.Object object = fromOM(
                                             _returnEnv.getBody().getFirstElement(),
                                             it.units.htl.web.viewer.auth.utils.KTCServiceStub.EchoResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));
            return (it.units.htl.web.viewer.auth.utils.KTCServiceStub.EchoResponse) object;
        } catch (org.apache.axis2.AxisFault f) {
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt != null) {
                if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
                    // make the fault by reflection
                    try {
                        java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex =
                                (java.lang.Exception) exceptionClass.newInstance();
                        // message class
                        java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt, messageClass, null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[] { messageClass });
                        m.invoke(ex, new java.lang.Object[] { messageObject });
                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    } catch (java.lang.ClassCastException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                } else {
                    throw f;
                }
            } else {
                throw f;
            }
        } finally {
            _messageContext.getTransportOut().getSender().cleanup(_messageContext);
        }
    }

    /**
     * A utility method that copies the namepaces from the SOAPEnvelope
     */
    private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env) {
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
        }
        return returnMap;
    }

    private javax.xml.namespace.QName[] opNameArray = null;

    private boolean optimizeContent(javax.xml.namespace.QName opName) {
        if (opNameArray == null) {
            return false;
        }
        for (int i = 0; i < opNameArray.length; i++) {
            if (opName.equals(opNameArray[i])) {
                return true;
            }
        }
        return false;
    }

    // http://w2k3ksvmtdf/ktc/Custom.KS.KTCService.cls
    public static class GetSessionInfo
            implements org.apache.axis2.databinding.ADBBean {
        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://tempuri.org",
                "GetSessionInfo",
                "ns1");

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if (namespace.equals("http://tempuri.org")) {
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * field for Server
         */
        protected java.lang.String localServer;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localServerTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getServer() {
            return localServer;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            Server
         */
        public void setServer(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localServerTracker = true;
            } else {
                localServerTracker = false;
            }
            this.localServer = param;
        }

        /**
         * field for User
         */
        protected java.lang.String localUser;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localUserTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getUser() {
            return localUser;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            User
         */
        public void setUser(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localUserTracker = true;
            } else {
                localUserTracker = false;
            }
            this.localUser = param;
        }

        /**
         * field for SessionId
         */
        protected java.lang.String localSessionId;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localSessionIdTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getSessionId() {
            return localSessionId;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            SessionId
         */
        public void setSessionId(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localSessionIdTracker = true;
            } else {
                localSessionIdTracker = false;
            }
            this.localSessionId = param;
        }

        /**
         * isReaderMTOMAware
         * 
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;
            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
                isReaderMTOMAware = false;
            }
            return isReaderMTOMAware;
        }

        /**
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement(
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {
                           public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                               GetSessionInfo.this.serialize(MY_QNAME, factory, xmlWriter);
                           }
                       };
            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
                    MY_QNAME, factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            serialize(parentQName, factory, xmlWriter, false);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;
            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();
            if ((namespace != null) && (namespace.trim().length() > 0)) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }
                    xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }
            if (serializeType) {
                java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://tempuri.org");
                if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                            namespacePrefix + ":GetSessionInfo",
                            xmlWriter);
                } else {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                            "GetSessionInfo",
                            xmlWriter);
                }
            }
            if (localServerTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "server", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "server");
                    }
                } else {
                    xmlWriter.writeStartElement("server");
                }
                if (localServer == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("server cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localServer);
                }
                xmlWriter.writeEndElement();
            }
            if (localUserTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "user", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "user");
                    }
                } else {
                    xmlWriter.writeStartElement("user");
                }
                if (localUser == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("user cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localUser);
                }
                xmlWriter.writeEndElement();
            }
            if (localSessionIdTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "sessionId", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "sessionId");
                    }
                } else {
                    xmlWriter.writeStartElement("sessionId");
                }
                if (localSessionId == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("sessionId cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localSessionId);
                }
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                      java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace, java.lang.String attName,
                                      java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String attributeNamespace = qname.getNamespaceURI();
            java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
            if (attributePrefix == null) {
                attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
            }
            java.lang.String attributeValue;
            if (attributePrefix.trim().length() > 0) {
                attributeValue = attributePrefix + ":" + qname.getLocalPart();
            } else {
                attributeValue = qname.getLocalPart();
            }
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attributeValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attributeValue);
            }
        }

        /**
         * method to handle Qnames
         */
        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }
                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;
                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }
                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }

        /**
         * databinding method to get an XML representation of this object
         */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();
            if (localServerTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "server"));
                if (localServer != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localServer));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("server cannot be null!!");
                }
            }
            if (localUserTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "user"));
                if (localUser != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUser));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("user cannot be null!!");
                }
            }
            if (localSessionIdTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "sessionId"));
                if (localSessionId != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("sessionId cannot be null!!");
                }
            }
            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
        }

        /**
         * Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static GetSessionInfo parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
                GetSessionInfo object =
                        new GetSessionInfo();
                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";
                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");
                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;
                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                            }
                            nsPrefix = nsPrefix == null ? "" : nsPrefix;
                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);
                            if (!"GetSessionInfo".equals(type)) {
                                // find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (GetSessionInfo) ExtensionMapper.getTypeObject(
                                        nsUri, type, reader);
                            }
                        }
                    }
                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();
                    reader.next();
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "server").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setServer(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "user").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setUser(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "sessionId").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setSessionId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement())
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }
                return object;
            }
        }// end of factory class
    }

    public static class GetSessionInfoResponse
            implements org.apache.axis2.databinding.ADBBean {
        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://tempuri.org",
                "GetSessionInfoResponse",
                "ns1");

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if (namespace.equals("http://tempuri.org")) {
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * field for GetSessionInfoResult
         */
        protected SessionInfo localGetSessionInfoResult;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localGetSessionInfoResultTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return SessionInfo
         */
        public SessionInfo getGetSessionInfoResult() {
            return localGetSessionInfoResult;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            GetSessionInfoResult
         */
        public void setGetSessionInfoResult(SessionInfo param) {
            if (param != null) {
                // update the setting tracker
                localGetSessionInfoResultTracker = true;
            } else {
                localGetSessionInfoResultTracker = false;
            }
            this.localGetSessionInfoResult = param;
        }

        /**
         * isReaderMTOMAware
         * 
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;
            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
                isReaderMTOMAware = false;
            }
            return isReaderMTOMAware;
        }

        /**
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement(
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {
                           public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                               GetSessionInfoResponse.this.serialize(MY_QNAME, factory, xmlWriter);
                           }
                       };
            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
                    MY_QNAME, factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            serialize(parentQName, factory, xmlWriter, false);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;
            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();
            if ((namespace != null) && (namespace.trim().length() > 0)) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }
                    xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }
            if (serializeType) {
                java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://tempuri.org");
                if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                            namespacePrefix + ":GetSessionInfoResponse",
                            xmlWriter);
                } else {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                            "GetSessionInfoResponse",
                            xmlWriter);
                }
            }
            if (localGetSessionInfoResultTracker) {
                if (localGetSessionInfoResult == null) {
                    throw new org.apache.axis2.databinding.ADBException("GetSessionInfoResult cannot be null!!");
                }
                localGetSessionInfoResult.serialize(new javax.xml.namespace.QName("http://tempuri.org", "GetSessionInfoResult"),
                                               factory, xmlWriter);
            }
            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                      java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace, java.lang.String attName,
                                      java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String attributeNamespace = qname.getNamespaceURI();
            java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
            if (attributePrefix == null) {
                attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
            }
            java.lang.String attributeValue;
            if (attributePrefix.trim().length() > 0) {
                attributeValue = attributePrefix + ":" + qname.getLocalPart();
            } else {
                attributeValue = qname.getLocalPart();
            }
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attributeValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attributeValue);
            }
        }

        /**
         * method to handle Qnames
         */
        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }
                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;
                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }
                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }

        /**
         * databinding method to get an XML representation of this object
         */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();
            if (localGetSessionInfoResultTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "GetSessionInfoResult"));
                if (localGetSessionInfoResult == null) {
                    throw new org.apache.axis2.databinding.ADBException("GetSessionInfoResult cannot be null!!");
                }
                elementList.add(localGetSessionInfoResult);
            }
            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
        }

        /**
         * Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static GetSessionInfoResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
                GetSessionInfoResponse object =
                        new GetSessionInfoResponse();
                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";
                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");
                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;
                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                            }
                            nsPrefix = nsPrefix == null ? "" : nsPrefix;
                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);
                            if (!"GetSessionInfoResponse".equals(type)) {
                                // find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (GetSessionInfoResponse) ExtensionMapper.getTypeObject(
                                        nsUri, type, reader);
                            }
                        }
                    }
                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();
                    reader.next();
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "GetSessionInfoResult").equals(reader.getName())) {
                        object.setGetSessionInfoResult(SessionInfo.Factory.parse(reader));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement())
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }
                return object;
            }
        }// end of factory class
    }

    public static class Echo
            implements org.apache.axis2.databinding.ADBBean {
        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://tempuri.org",
                "echo",
                "ns1");

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if (namespace.equals("http://tempuri.org")) {
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * field for Str
         */
        protected java.lang.String localStr;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localStrTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getStr() {
            return localStr;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            Str
         */
        public void setStr(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localStrTracker = true;
            } else {
                localStrTracker = false;
            }
            this.localStr = param;
        }

        /**
         * isReaderMTOMAware
         * 
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;
            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
                isReaderMTOMAware = false;
            }
            return isReaderMTOMAware;
        }

        /**
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement(
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {
                           public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                               Echo.this.serialize(MY_QNAME, factory, xmlWriter);
                           }
                       };
            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
                    MY_QNAME, factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            serialize(parentQName, factory, xmlWriter, false);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;
            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();
            if ((namespace != null) && (namespace.trim().length() > 0)) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }
                    xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }
            if (serializeType) {
                java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://tempuri.org");
                if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                            namespacePrefix + ":echo",
                            xmlWriter);
                } else {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                            "echo",
                            xmlWriter);
                }
            }
            if (localStrTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "str", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "str");
                    }
                } else {
                    xmlWriter.writeStartElement("str");
                }
                if (localStr == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("str cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localStr);
                }
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                      java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace, java.lang.String attName,
                                      java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String attributeNamespace = qname.getNamespaceURI();
            java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
            if (attributePrefix == null) {
                attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
            }
            java.lang.String attributeValue;
            if (attributePrefix.trim().length() > 0) {
                attributeValue = attributePrefix + ":" + qname.getLocalPart();
            } else {
                attributeValue = qname.getLocalPart();
            }
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attributeValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attributeValue);
            }
        }

        /**
         * method to handle Qnames
         */
        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }
                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;
                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }
                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }

        /**
         * databinding method to get an XML representation of this object
         */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();
            if (localStrTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "str"));
                if (localStr != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStr));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("str cannot be null!!");
                }
            }
            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
        }

        /**
         * Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static Echo parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
                Echo object =
                        new Echo();
                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";
                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");
                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;
                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                            }
                            nsPrefix = nsPrefix == null ? "" : nsPrefix;
                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);
                            if (!"echo".equals(type)) {
                                // find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (Echo) ExtensionMapper.getTypeObject(
                                        nsUri, type, reader);
                            }
                        }
                    }
                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();
                    reader.next();
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "str").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setStr(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement())
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }
                return object;
            }
        }// end of factory class
    }

    public static class ExtensionMapper {
        public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
            if ("http://tempuri.org".equals(namespaceURI) &&
                    "SessionInfo".equals(typeName)) {
                return SessionInfo.Factory.parse(reader);
            }
            throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
        }
    }

    public static class EchoResponse
            implements org.apache.axis2.databinding.ADBBean {
        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://tempuri.org",
                "echoResponse",
                "ns1");

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if (namespace.equals("http://tempuri.org")) {
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * field for EchoResult
         */
        protected java.lang.String localEchoResult;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localEchoResultTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getEchoResult() {
            return localEchoResult;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            EchoResult
         */
        public void setEchoResult(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localEchoResultTracker = true;
            } else {
                localEchoResultTracker = false;
            }
            this.localEchoResult = param;
        }

        /**
         * isReaderMTOMAware
         * 
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;
            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
                isReaderMTOMAware = false;
            }
            return isReaderMTOMAware;
        }

        /**
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement(
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {
                           public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                               EchoResponse.this.serialize(MY_QNAME, factory, xmlWriter);
                           }
                       };
            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
                    MY_QNAME, factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            serialize(parentQName, factory, xmlWriter, false);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;
            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();
            if ((namespace != null) && (namespace.trim().length() > 0)) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }
                    xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }
            if (serializeType) {
                java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://tempuri.org");
                if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                            namespacePrefix + ":echoResponse",
                            xmlWriter);
                } else {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                            "echoResponse",
                            xmlWriter);
                }
            }
            if (localEchoResultTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "echoResult", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "echoResult");
                    }
                } else {
                    xmlWriter.writeStartElement("echoResult");
                }
                if (localEchoResult == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("echoResult cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localEchoResult);
                }
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                      java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace, java.lang.String attName,
                                      java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String attributeNamespace = qname.getNamespaceURI();
            java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
            if (attributePrefix == null) {
                attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
            }
            java.lang.String attributeValue;
            if (attributePrefix.trim().length() > 0) {
                attributeValue = attributePrefix + ":" + qname.getLocalPart();
            } else {
                attributeValue = qname.getLocalPart();
            }
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attributeValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attributeValue);
            }
        }

        /**
         * method to handle Qnames
         */
        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }
                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;
                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }
                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }

        /**
         * databinding method to get an XML representation of this object
         */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();
            if (localEchoResultTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "echoResult"));
                if (localEchoResult != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEchoResult));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("echoResult cannot be null!!");
                }
            }
            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
        }

        /**
         * Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static EchoResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
                EchoResponse object =
                        new EchoResponse();
                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";
                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");
                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;
                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                            }
                            nsPrefix = nsPrefix == null ? "" : nsPrefix;
                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);
                            if (!"echoResponse".equals(type)) {
                                // find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (EchoResponse) ExtensionMapper.getTypeObject(
                                        nsUri, type, reader);
                            }
                        }
                    }
                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();
                    reader.next();
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "echoResult").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setEchoResult(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement())
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }
                return object;
            }
        }// end of factory class
    }

    public static class SessionInfo
            implements org.apache.axis2.databinding.ADBBean {
        /*
         * This type was generated from the piece of schema that had name = SessionInfo Namespace URI = http://tempuri.org Namespace Prefix = ns1
         */
        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if (namespace.equals("http://tempuri.org")) {
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * field for SessionCookie
         */
        protected java.lang.String localSessionCookie;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localSessionCookieTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getSessionCookie() {
            return localSessionCookie;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            SessionCookie
         */
        public void setSessionCookie(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localSessionCookieTracker = true;
            } else {
                localSessionCookieTracker = false;
            }
            this.localSessionCookie = param;
        }

        /**
         * field for LogonSiteCode
         */
        protected java.lang.String localLogonSiteCode;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localLogonSiteCodeTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getLogonSiteCode() {
            return localLogonSiteCode;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            LogonSiteCode
         */
        public void setLogonSiteCode(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localLogonSiteCodeTracker = true;
            } else {
                localLogonSiteCodeTracker = false;
            }
            this.localLogonSiteCode = param;
        }

        /**
         * field for LogonUserId
         */
        protected java.lang.String localLogonUserId;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localLogonUserIdTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getLogonUserId() {
            return localLogonUserId;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            LogonUserId
         */
        public void setLogonUserId(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localLogonUserIdTracker = true;
            } else {
                localLogonUserIdTracker = false;
            }
            this.localLogonUserId = param;
        }

        /**
         * field for LogonUserCode
         */
        protected java.lang.String localLogonUserCode;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localLogonUserCodeTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getLogonUserCode() {
            return localLogonUserCode;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            LogonUserCode
         */
        public void setLogonUserCode(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localLogonUserCodeTracker = true;
            } else {
                localLogonUserCodeTracker = false;
            }
            this.localLogonUserCode = param;
        }

        /**
         * field for LogonUserName
         */
        protected java.lang.String localLogonUserName;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localLogonUserNameTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getLogonUserName() {
            return localLogonUserName;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            LogonUserName
         */
        public void setLogonUserName(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localLogonUserNameTracker = true;
            } else {
                localLogonUserNameTracker = false;
            }
            this.localLogonUserName = param;
        }

        /**
         * field for LogonGroupId
         */
        protected java.lang.String localLogonGroupId;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localLogonGroupIdTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getLogonGroupId() {
            return localLogonGroupId;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            LogonGroupId
         */
        public void setLogonGroupId(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localLogonGroupIdTracker = true;
            } else {
                localLogonGroupIdTracker = false;
            }
            this.localLogonGroupId = param;
        }

        /**
         * field for LogonGroupDesc
         */
        protected java.lang.String localLogonGroupDesc;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localLogonGroupDescTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getLogonGroupDesc() {
            return localLogonGroupDesc;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            LogonGroupDesc
         */
        public void setLogonGroupDesc(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localLogonGroupDescTracker = true;
            } else {
                localLogonGroupDescTracker = false;
            }
            this.localLogonGroupDesc = param;
        }

        /**
         * field for LogonLangId
         */
        protected java.lang.String localLogonLangId;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localLogonLangIdTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getLogonLangId() {
            return localLogonLangId;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            LogonLangId
         */
        public void setLogonLangId(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localLogonLangIdTracker = true;
            } else {
                localLogonLangIdTracker = false;
            }
            this.localLogonLangId = param;
        }

        /**
         * field for LogonCTLocId
         */
        protected java.lang.String localLogonCTLocId;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localLogonCTLocIdTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getLogonCTLocId() {
            return localLogonCTLocId;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            LogonCTLocId
         */
        public void setLogonCTLocId(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localLogonCTLocIdTracker = true;
            } else {
                localLogonCTLocIdTracker = false;
            }
            this.localLogonCTLocId = param;
        }

        /**
         * field for Context
         */
        protected java.lang.String localContext;
        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be used to determine whether to include this field in the serialized XML
         */
        protected boolean localContextTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getContext() {
            return localContext;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            Context
         */
        public void setContext(java.lang.String param) {
            if (param != null) {
                // update the setting tracker
                localContextTracker = true;
            } else {
                localContextTracker = false;
            }
            this.localContext = param;
        }

        /**
         * isReaderMTOMAware
         * 
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;
            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
                isReaderMTOMAware = false;
            }
            return isReaderMTOMAware;
        }

        /**
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement(
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this, parentQName) {
                           public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                               SessionInfo.this.serialize(parentQName, factory, xmlWriter);
                           }
                       };
            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
                    parentQName, factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            serialize(parentQName, factory, xmlWriter, false);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;
            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();
            if ((namespace != null) && (namespace.trim().length() > 0)) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }
                    xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }
            if (serializeType) {
                java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://tempuri.org");
                if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                            namespacePrefix + ":SessionInfo",
                            xmlWriter);
                } else {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                            "SessionInfo",
                            xmlWriter);
                }
            }
            if (localSessionCookieTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "SessionCookie", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "SessionCookie");
                    }
                } else {
                    xmlWriter.writeStartElement("SessionCookie");
                }
                if (localSessionCookie == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("SessionCookie cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localSessionCookie);
                }
                xmlWriter.writeEndElement();
            }
            if (localLogonSiteCodeTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "LogonSiteCode", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "LogonSiteCode");
                    }
                } else {
                    xmlWriter.writeStartElement("LogonSiteCode");
                }
                if (localLogonSiteCode == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("LogonSiteCode cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localLogonSiteCode);
                }
                xmlWriter.writeEndElement();
            }
            if (localLogonUserIdTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "LogonUserId", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "LogonUserId");
                    }
                } else {
                    xmlWriter.writeStartElement("LogonUserId");
                }
                if (localLogonUserId == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("LogonUserId cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localLogonUserId);
                }
                xmlWriter.writeEndElement();
            }
            if (localLogonUserCodeTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "LogonUserCode", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "LogonUserCode");
                    }
                } else {
                    xmlWriter.writeStartElement("LogonUserCode");
                }
                if (localLogonUserCode == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("LogonUserCode cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localLogonUserCode);
                }
                xmlWriter.writeEndElement();
            }
            if (localLogonUserNameTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "LogonUserName", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "LogonUserName");
                    }
                } else {
                    xmlWriter.writeStartElement("LogonUserName");
                }
                if (localLogonUserName == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("LogonUserName cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localLogonUserName);
                }
                xmlWriter.writeEndElement();
            }
            if (localLogonGroupIdTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "LogonGroupId", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "LogonGroupId");
                    }
                } else {
                    xmlWriter.writeStartElement("LogonGroupId");
                }
                if (localLogonGroupId == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("LogonGroupId cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localLogonGroupId);
                }
                xmlWriter.writeEndElement();
            }
            if (localLogonGroupDescTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "LogonGroupDesc", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "LogonGroupDesc");
                    }
                } else {
                    xmlWriter.writeStartElement("LogonGroupDesc");
                }
                if (localLogonGroupDesc == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("LogonGroupDesc cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localLogonGroupDesc);
                }
                xmlWriter.writeEndElement();
            }
            if (localLogonLangIdTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "LogonLangId", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "LogonLangId");
                    }
                } else {
                    xmlWriter.writeStartElement("LogonLangId");
                }
                if (localLogonLangId == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("LogonLangId cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localLogonLangId);
                }
                xmlWriter.writeEndElement();
            }
            if (localLogonCTLocIdTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "LogonCTLocId", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "LogonCTLocId");
                    }
                } else {
                    xmlWriter.writeStartElement("LogonCTLocId");
                }
                if (localLogonCTLocId == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("LogonCTLocId cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localLogonCTLocId);
                }
                xmlWriter.writeEndElement();
            }
            if (localContextTracker) {
                namespace = "http://tempuri.org";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                        xmlWriter.writeStartElement(prefix, "Context", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "Context");
                    }
                } else {
                    xmlWriter.writeStartElement("Context");
                }
                if (localContext == null) {
                    // write the nil attribute
                    throw new org.apache.axis2.databinding.ADBException("Context cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(localContext);
                }
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                      java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace, java.lang.String attName,
                                      java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String attributeNamespace = qname.getNamespaceURI();
            java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
            if (attributePrefix == null) {
                attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
            }
            java.lang.String attributeValue;
            if (attributePrefix.trim().length() > 0) {
                attributeValue = attributePrefix + ":" + qname.getLocalPart();
            } else {
                attributeValue = qname.getLocalPart();
            }
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attributeValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attributeValue);
            }
        }

        /**
         * method to handle Qnames
         */
        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }
                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;
                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }
                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }

        /**
         * databinding method to get an XML representation of this object
         */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();
            if (localSessionCookieTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "SessionCookie"));
                if (localSessionCookie != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionCookie));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("SessionCookie cannot be null!!");
                }
            }
            if (localLogonSiteCodeTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "LogonSiteCode"));
                if (localLogonSiteCode != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLogonSiteCode));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("LogonSiteCode cannot be null!!");
                }
            }
            if (localLogonUserIdTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "LogonUserId"));
                if (localLogonUserId != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLogonUserId));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("LogonUserId cannot be null!!");
                }
            }
            if (localLogonUserCodeTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "LogonUserCode"));
                if (localLogonUserCode != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLogonUserCode));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("LogonUserCode cannot be null!!");
                }
            }
            if (localLogonUserNameTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "LogonUserName"));
                if (localLogonUserName != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLogonUserName));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("LogonUserName cannot be null!!");
                }
            }
            if (localLogonGroupIdTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "LogonGroupId"));
                if (localLogonGroupId != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLogonGroupId));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("LogonGroupId cannot be null!!");
                }
            }
            if (localLogonGroupDescTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "LogonGroupDesc"));
                if (localLogonGroupDesc != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLogonGroupDesc));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("LogonGroupDesc cannot be null!!");
                }
            }
            if (localLogonLangIdTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "LogonLangId"));
                if (localLogonLangId != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLogonLangId));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("LogonLangId cannot be null!!");
                }
            }
            if (localLogonCTLocIdTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "LogonCTLocId"));
                if (localLogonCTLocId != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLogonCTLocId));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("LogonCTLocId cannot be null!!");
                }
            }
            if (localContextTracker) {
                elementList.add(new javax.xml.namespace.QName("http://tempuri.org",
                                                                      "Context"));
                if (localContext != null) {
                    elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localContext));
                } else {
                    throw new org.apache.axis2.databinding.ADBException("Context cannot be null!!");
                }
            }
            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
        }

        /**
         * Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object Precondition: If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable If this object is not an element, it is a complex type and the reader is at the event just after the outer start element Postcondition: If this object is an element, the reader is positioned at its end element If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static SessionInfo parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
                SessionInfo object =
                        new SessionInfo();
                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";
                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");
                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;
                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                            }
                            nsPrefix = nsPrefix == null ? "" : nsPrefix;
                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);
                            if (!"SessionInfo".equals(type)) {
                                // find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (SessionInfo) ExtensionMapper.getTypeObject(
                                        nsUri, type, reader);
                            }
                        }
                    }
                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();
                    reader.next();
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "SessionCookie").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setSessionCookie(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "LogonSiteCode").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setLogonSiteCode(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "LogonUserId").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setLogonUserId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "LogonUserCode").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setLogonUserCode(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "LogonUserName").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setLogonUserName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "LogonGroupId").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setLogonGroupId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "LogonGroupDesc").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setLogonGroupDesc(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "LogonLangId").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setLogonLangId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "LogonCTLocId").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setLogonCTLocId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://tempuri.org", "Context").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();
                        object.setContext(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                        reader.next();
                    } // End of if for expected property start element
                    else {
                    }
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();
                    if (reader.isStartElement())
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }
                return object;
            }
        }// end of factory class
    }

    private org.apache.axiom.om.OMElement toOM(it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfo param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {
        try {
            return param.getOMElement(it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfo.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    private org.apache.axiom.om.OMElement toOM(it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfoResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {
        try {
            return param.getOMElement(it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfoResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    private org.apache.axiom.om.OMElement toOM(it.units.htl.web.viewer.auth.utils.KTCServiceStub.Echo param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {
        try {
            return param.getOMElement(it.units.htl.web.viewer.auth.utils.KTCServiceStub.Echo.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    private org.apache.axiom.om.OMElement toOM(it.units.htl.web.viewer.auth.utils.KTCServiceStub.EchoResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {
        try {
            return param.getOMElement(it.units.htl.web.viewer.auth.utils.KTCServiceStub.EchoResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfo param, boolean optimizeContent)
                                        throws org.apache.axis2.AxisFault {
        try {
            org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
            emptyEnvelope.getBody().addChild(param.getOMElement(it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfo.MY_QNAME, factory));
            return emptyEnvelope;
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    /* methods to provide back word compatibility */
    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, it.units.htl.web.viewer.auth.utils.KTCServiceStub.Echo param, boolean optimizeContent)
                                        throws org.apache.axis2.AxisFault {
        try {
            org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
            emptyEnvelope.getBody().addChild(param.getOMElement(it.units.htl.web.viewer.auth.utils.KTCServiceStub.Echo.MY_QNAME, factory));
            return emptyEnvelope;
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    /* methods to provide back word compatibility */
    /**
     * get the default envelope
     */
    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory) {
        return factory.getDefaultEnvelope();
    }

    private java.lang.Object fromOM(
            org.apache.axiom.om.OMElement param,
            java.lang.Class type,
            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault {
        try {
            if (it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfo.class.equals(type)) {
                return it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfo.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }
            if (it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfoResponse.class.equals(type)) {
                return it.units.htl.web.viewer.auth.utils.KTCServiceStub.GetSessionInfoResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }
            if (it.units.htl.web.viewer.auth.utils.KTCServiceStub.Echo.class.equals(type)) {
                return it.units.htl.web.viewer.auth.utils.KTCServiceStub.Echo.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }
            if (it.units.htl.web.viewer.auth.utils.KTCServiceStub.EchoResponse.class.equals(type)) {
                return it.units.htl.web.viewer.auth.utils.KTCServiceStub.EchoResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }
        } catch (java.lang.Exception e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
        return null;
    }
}
