package com.sheng.jobframework.utility.HttpClient;


import com.sun.xml.internal.ws.api.message.Headers;
import com.sun.xml.internal.ws.developer.WSBindingProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.DetailEntry;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;




/**
 * Class to provide SOAP client utility methods that allow easy access to BDK SOAP Web Services
 */
public class SoapClientUtils {


    private String m_authName = null;
    private String m_authPassword = null;
    private String m_user = null;
    private boolean m_runAs = false;

    private boolean m_dumpSoap = false;

    private SoapServerResponse m_ssr = null;

    private boolean m_throwSoapError = true;

    private static Map<String, String> serviceNameMethodMap = null;

    // private static SecurityPolicyFeature[] securityFeature = null;

    private static final int SEND_SOAP_MAX_RETRY = 2;


    static {
        serviceNameMethodMap = new HashMap<String, String>();
        serviceNameMethodMap.put("AccessControl",
                                 "getAccessControlServiceSoap");
        serviceNameMethodMap.put("AddressBook", "getAddressBookServiceSoap");
        serviceNameMethodMap.put("Calendar", "getCalendarServiceSoap");
        serviceNameMethodMap.put("Community", "getCommunityServiceSoap");
        serviceNameMethodMap.put("Conferencing", "getConferencingServiceSoap");
        serviceNameMethodMap.put("ContentManagement",
                                 "getContentManagementServiceSoap");
        serviceNameMethodMap.put("Directory", "getDirectoryServiceSoap");
        serviceNameMethodMap.put("Discussions", "getDiscussionsServiceSoap");
        serviceNameMethodMap.put("IntelligentCollaboration",
                                 "getIntelligentCollaborationServiceSoap");
        serviceNameMethodMap.put("Me", "getMeServiceSoap");
        serviceNameMethodMap.put("Messaging", "getMessagingServiceSoap");
        serviceNameMethodMap.put("MetadataAndRelations",
                                 "getMetadataAndRelationsServiceSoap");
        serviceNameMethodMap.put("Presence", "getPresenceServiceSoap");
        serviceNameMethodMap.put("SocialNetwork",
                                 "getSocialNetworkServiceSoap");
        serviceNameMethodMap.put("Subscriptions",
                                 "getSubscriptionsServiceSoap");
        serviceNameMethodMap.put("TaskManagement",
                                 "getTaskManagementServiceSoap");
        serviceNameMethodMap.put("WorkspacesAndFolders",
                                 "getWorkspacesAndFoldersServiceSoap");
        /*
      securityFeature = new SecurityPolicyFeature[] {
      new SecurityPolicyFeature("policy:oracle/wss_username_token_client_policy") };*/
    }

    public SoapClientUtils(String authName, String authPassword) {
        m_authName = authName;
        m_authPassword = authPassword;
        m_user = null;
        m_runAs = false;
    }


    public SoapClientUtils(String trustedServiceName,
                           String trustedServicePswd, String runas) {
        m_authName = trustedServiceName;
        m_authPassword = trustedServicePswd;
        m_user = runas;
        if (m_user != null) {
            m_runAs = true;
        }
    }


    private void setAuthentication(BindingProvider bp) {


        bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY,
                                   m_authName);
        bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,
                                   m_authPassword);
        //  bp.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY,true);

        if (m_runAs) {
            setRunAs((WSBindingProvider)bp, m_user);
        }
    }


    private static void printHttpResponseCode(BindingProvider bp) {
        Map responseContext = bp.getResponseContext();
        Integer responseCode =
            (Integer)(responseContext.get(MessageContext.HTTP_RESPONSE_CODE));
        System.out.println("HTTP Response Return Code: " + responseCode);
    }

    /**
     * Add the user context to the SOAP header using the WSBinding
     * @param wsb
     * @param username
     */
    private void setRunAs(WSBindingProvider wsb, String username) {
        wsb.setOutboundHeaders(Headers.create(new QName("http://www.oracle.com/beehive",
                                                        "runAs"), username));
    }


    /**
     * Send a soap request passing service, method, and parameter values.
     * Returns a beehive object if call is successful, null otherwise.  If SOAP call failed, call getSoapServerResponse to retrieve exception thrown.
     * @param service
     * @param method
     * @param paramValues
     */
    public Object sendSoapRequest(String service, String method,
                                  Object[] paramValues) throws Exception {

        if (paramValues == null) {
            throw new SoapClientUtilsException("Null value passed for paramValues");
        }

        int paramNum = paramValues.length;
        Class[] paramTypes = new Class[paramNum];
        for (int i = 0; i < paramNum; i++) {
            paramTypes[i] = paramValues[0].getClass();
        }

        return sendSoapRequest(service, method, paramTypes, paramValues);
    }


    /**
     * Send a soap request passing service, method, parameter types, and parameter values.
     * Returns a beehive object if call is successful, null otherwise.  If SOAP call failed, call getSoapServerResponse to retrieve exception thrown.
     * @param service
     * @param method
     * @param paramTypes
     * @param paramValues
     */
    public Object sendSoapRequest(String service, String method,
                                  Class[] paramTypes,
                                  Object[] paramValues) throws Exception {
        m_ssr = new SoapServerResponse();
        Object entity = null;
        boolean send_success = false;
        int retryNum = 0;
        Object serviceSoap = null;
        Class serviceClass = null;
        Method m = null;


        do {
            serviceSoap = this.getServiceSoap(service);
            serviceClass = serviceSoap.getClass();
            m = serviceClass.getMethod(method, paramTypes);

            System.out.println("\nSOAP call: " + service + "Service " +
                               m.getName());

            try {
                entity = m.invoke(serviceSoap, paramValues);
                send_success = true;
                m_ssr.setResponseObject(entity);
                m_ssr.setServerException(null);
                m_ssr.setSoapException(null);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof SOAPFaultException) {
                    send_success = true;
                    setSoapServerResponseException((SOAPFaultException)(e.getCause()));
                    if (m_throwSoapError) {
                        throw (SOAPFaultException)(e.getCause());
                    }
                } else {
                    e.getCause().printStackTrace(System.out);
                    retryNum++;
                    Thread.sleep(4000);
                    //throw new SoapClientUtilsException(e.getCause().getMessage());
                }
            }
        } while ((!send_success) &&
                 (retryNum <= SEND_SOAP_MAX_RETRY)); // temporary workaround for: java.lang.AssertionError: the tube must call the add(...) method to register itself before start copying other pipes, but com.sun.xml.ws.handler.ClientLogicalHandlerTube@7d0e06bf hasn't done so


        if (!send_success) {
            throw new SoapClientUtilsException("Failed to send SOAP Request");
        }

        /* // for debugging Bug9837283
      System.out.println("Entity class: " + entity.getClass().getName());
      if (entity instanceof com.oracle.beehive.ListResult) {
        System.out.println("ListResult size from SoapClientUtils: " + ((com.oracle.beehive.ListResult)entity).getElements().size());
        CommunityServiceSoap css = getCommunityServiceSoap();
        com.oracle.beehive.ListResult lr = css.listEnterprises(new com.oracle.beehive.PredicateAndSortListParameters());
        System.out.println("ListResult size from SoapClientUtils direct call: " + lr.getElements().size());
      }
*/

        return entity;
    }


    /**
     * Set to true to throw SOAPFaulException for failed call, false to suppress throwing this Exception
     * @param throwError
     */
    public void throwSoapError(boolean throwError) {
        m_throwSoapError = throwError;
    }


    public Object getServiceSoap(String service) throws SoapClientUtilsException,
                                                        IllegalAccessException,
                                                        NoSuchMethodException,
                                                        InvocationTargetException {
        //Class containClass = Class.forName("oracle.ocs.qa.utils.soap_client.SoapClientUtils");
        Class containClass = this.getClass();
        String serviceMethod = serviceNameMethodMap.get(service);
        if (serviceMethod == null) {
            throw new SoapClientUtilsException("Method name to get service soap port is missing in SoapClientUtils's serviceNameMethodMap: " +
                                               service);
        }
        Method m = containClass.getMethod(serviceNameMethodMap.get(service));
        //System.out.println("\ngetServiceSoap: " + service + "Service "  + m.getName());
        Object o = m.invoke(this);
        return o;

    }


    /**
     * Get SoapServerResponse from the most recent SOAP call
     *
     */
    public SoapServerResponse getSoapServerResponse() {
        return m_ssr;
    }


    private void setSoapServerResponse(SoapServerResponse ssr) {
        m_ssr = ssr;
    }


    /**
     * Do not call this method - this is to be used by REST2SOAP utility only!
     *
     */
    public void resetSoapServerResponse() {
        m_ssr = null;
    }

    private void setSoapServerResponseException(SOAPFaultException e) {
        m_ssr.setSoapException(e);
        e.printStackTrace(System.out);
        if (e.getFault().hasDetail()) {
            Iterator i = e.getFault().getDetail().getDetailEntries();
            while (i.hasNext()) {
                DetailEntry de = (DetailEntry)(i.next());
                String value = de.getValue();
                String element = de.getElementName().getLocalName();
                //System.out.println("Element: " + element);
                //System.out.println("Value: " + value);
                m_ssr.setResponseObject(null);
                if (element.trim().equals("exceptionType")) {
                    m_ssr.setServerException(value);
                    break;
                } else {
                    m_ssr.setServerException(null);
                }
            }
        }
    }


    /**
     * Do not call this method - this is set only one time at MauiTestBase
     *
     */
    public static void setDumpSoap(boolean b) {
        if (b) {
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
                               "true");
            //System.out.println("Debug turned on, value: " + System.getProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump"));
        } else {
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
                               "false");
            //System.out.println("Debug turned off, value: " + System.getProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump"));
        }
    }


    /**
     * Helper method to create an array of Class passing comma separated values
     *
     */
    public static Class[] createParamTypes(Class... paramTypes) {
        return paramTypes;
    }


    /**
     * Helper method to create an array of Object passing comma separated values
     *
     */
    public static Object[] createParamValues(Object... paramValues) {
        if (paramValues == null) {
            paramValues = new Object[1];
            paramValues[0] = null;
        }

        return paramValues;
    }


    /**
     * Do not call this method - this is to be used by REST2SOAP utility only!
     *
     */
    /*
    public void replaceArrayListWithBeeIdList(ArrayList al) {
      if (m_ssr.getResponseObject() instanceof ArrayList) {
        BeeIdList bil = new BeeIdList();
        bil.getBeeId().addAll(al);
        m_ssr.setResponseObject(bil);
      }
    }
*/

    /**
     * Do not call this method - this is to be used by REST2SOAP utility only!
     *
     */
    /*
    public void replaceArrayListWithBeeList(ArrayList al) {
      if (m_ssr.getResponseObject() instanceof ArrayList) {
        BeeList bl = new BeeList();
        List list = bl.getElements();
        list.addAll(al);
        m_ssr.setResponseObject(bl);
      }
    }
*/
}


