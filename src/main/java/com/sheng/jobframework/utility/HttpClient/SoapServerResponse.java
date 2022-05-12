

package com.sheng.jobframework.utility.HttpClient;

import javax.xml.ws.soap.SOAPFaultException;


/**
 * Used by SoapClientUtils to return server response details when request is done via "sendSoapRequest" call only
 *
 */
public class SoapServerResponse {
    private Object m_responseObject = null;
    private String m_serverException = null;
    private SOAPFaultException m_soapException = null;

    public Object getResponseObject() {
        return m_responseObject;
    }

    public String getServerException() {
        return m_serverException;
    }

    public SOAPFaultException getSoapException() {
        return m_soapException;
    }

    protected void setResponseObject(Object o) {
        m_responseObject = o;
    }

    protected void setServerException(String e) {
        m_serverException = e;
    }

    protected void setSoapException(SOAPFaultException s) {
        m_soapException = s;
    }

}


