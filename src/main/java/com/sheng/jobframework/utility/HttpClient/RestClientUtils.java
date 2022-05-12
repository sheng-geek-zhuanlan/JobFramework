package com.sheng.jobframework.utility.HttpClient;

import com.sheng.jobframework.jobdom.ACElement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.Method;

import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.MarshalException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpClientParams;


public class RestClientUtils extends ACElement {
    public static final int DEF_MIN_OK_CODE = 200;

    /** HTTP max OK code */
    public static final int DEF_MAX_OK_CODE = 299;

    /** HTTP GET verb */
    public static final String HTTP_GET = "GET";

    /** HTTP POST verb */
    public static final String HTTP_POST = "POST";

    /** HTTP PUT verb */
    public static final String HTTP_PUT = "PUT";

    /** HTTP DELETE verb */
    public static final String HTTP_DELETE = "DELETE";


    /** XML content type */
    public static final String CONTENT_TYPE_XML = "application/xml";

    /** JSON content type */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /** All content type */
    public static final String CONTENT_TYPE_ALL = "*/*";


    /** STRICT request mode means that all errors returned from server are treated as exceptions */
    public static final String REQ_MODE_STRICT = "STRICT";

    /** NORMAL request mode (default) means that all errors returned from server are part of returned response */
    public static final String REQ_MODE_NORMAL = "NORMAL";

    /** DEBUG request mode means that all errors returned from server are part of returned response and extra param "debug=true' is set to help improve server response details */
    public static final String REQ_MODE_DEBUG = "DEBUG";


    /** Accept Header */
    public static final String ACCEPT_HEADER = "Accept";


    /** Content-Type Header */
    public static final String CONTENT_TYPE_HEADER = "Content-Type";


    /** use this as payload to work around empty payload requirements for puts and posts */
    public static final Object EMPTY_PAYLOAD =
        XmlJsonUtils.getStreamFromString(" ");


    /** Payload content type */
    String payloadContentType = CONTENT_TYPE_XML;
    boolean payloadContentTypeSet = false;

    /** if true, show payload when verbose NOTE:  this may mangle binary payloads, so turn this off as needed */
    private boolean showPayload = true;

    private boolean payloadIsStream = false;

    /** if true, show raw payload EVEN when NOT verbose NOTE:  this may mangle binary payloads, so turn this off as needed */
    private boolean showRawPayload = true;

    /** if true, show formatted payload NOTE:  this may mangle some payloads, so turn this off as needed */
    private boolean formatPayload = true;

    /** if true, show response when verbose NOTE:  this may mangle binary responses, so turn this off as needed */
    private boolean showResponse = true;

    /** if true, show JSON equivalent of response when verbose NOTE:  this may mangle binary responses, so turn this off as needed */
    private boolean showJSONResponse = false;

    /** if needed, set to false to see output on console */
    private boolean formatForHtml = true;

    /** if needed, set to true to suppress all outputs from this class */
    private boolean quiet = false;

    /** AntiCSRF token - needed to authenticate sessions */
    private String antiCSRFtoken = null;

    /** HttpClient */
    // HttpClient client = new DefaultHttpClient();
    HttpClient client = new HttpClient();
    //BasicHttpRequest client2 = new BasicHttpRequest("","");
    HttpMethod httpmethod = null;

    /** response content type */
    String responseContentType = null;

    private String PROP_SEPARATOR = "&";

    private String authUsername;
    private String authPassword;
    private String trustedServiceName = null;
    private String trustedServicePswd = null;

    private boolean suppress20xCode = false;

    private static Hashtable<String, RestClientUtils> rcuCache =
        new Hashtable<String, RestClientUtils>();

    private String verboseString = "";
    boolean forceVerbose = false; // override for debugging via test.params
    boolean forceQuiet = false; // override as needed via test.params
    boolean verbose = false; // set to true to see  verbose meesages

    int minRetCode = DEF_MIN_OK_CODE;
    int maxRetCode = DEF_MAX_OK_CODE;

    boolean checkRetCode =
        true; // set to true to assert failures on ret code not in range

    String reqMode = REQ_MODE_NORMAL;
    String accept = CONTENT_TYPE_XML;
    boolean acceptSet = false;
    String urlPrefix = null;

    RestServerResponse rsr = null;
    Exception ex = null;
    Object errorObject = null;

    private boolean runAsSoap = false;
    private boolean restAuthenticated = false;
    SoapClientUtils scu = null;

    private Map<String, Map<String, Object>> uploadedContent =
        new HashMap<String, Map<String, Object>>();

    public RestClientUtils() {
        super();
    }

    public RestClientUtils(String authUsername,
                           String authPassword) throws Exception {
        init(authUsername, authPassword, "");
    }

    /**
     * @param authUsername user name for authorization
     * @param authPassword password for authorization
     * @param urlPrefix prefix to use for all URLs (example: "http://my.example.com:7777/comb/v1/d/")
     */
    public RestClientUtils(String authUsername, String authPassword,
                           String urlPrefix) throws Exception {
        init(authUsername, authPassword, urlPrefix);
    }

    /**
     * @param authUsername user name for authorization
     * @param authPassword password for authorization
     * @param urlPrefix prefix to use for all URLs (example: "http://my.example.com:7777/comb/v1/d/")
     */
    protected void init(String authUsername, String authPassword,
                        String urlPrefix) throws Exception {
        this.authUsername = authUsername;
        this.authPassword = authPassword;
        setUrlPrefix(urlPrefix);
        System.out.println();
        print("Authorizing using : " + authUsername + "/" + authPassword);
        //shining added
        client.getParams().setAuthenticationPreemptive(true);

        client.getState().setCredentials(AuthScope.ANY,
                                         new UsernamePasswordCredentials(authUsername,
                                                                         authPassword));

    }


    public static RestClientUtils getInstance(String authUsername,
                                              String authPassword,
                                              String urlPrefix) throws Exception {
        String key = authUsername + ":" + authPassword + ":" + urlPrefix;
        RestClientUtils rcu = rcuCache.get(key);
        if (rcu == null) {
            rcu = new RestClientUtils(authUsername, authPassword, urlPrefix);
            rcuCache.put(key, rcu);
        }
        return rcu;
    }

    public HttpClient getClient() {
        return client;
    }


    /**
     * Hack to prevent username/pswd from being sent in to each call
     */
    private void removeAuthorization() throws Exception {
        HttpClientParams params = client.getParams();
        params.setAuthenticationPreemptive(false);
    }

    private void addAuthorization() throws Exception {
        HttpClientParams params = client.getParams();
        params.setAuthenticationPreemptive(true);
    }


    public void login(String authUsername,
                      String authPassword) throws Exception {
        this.authUsername = authUsername;
        this.authPassword = authPassword;
        System.out.println();
        print("Authorizing using : " + authUsername + "/" + authPassword);
        client.getState().setCredentials(AuthScope.ANY,
                                         new UsernamePasswordCredentials(authUsername,
                                                                         authPassword));
        login();
    }

    /**
     * Set trusted service credentials.  If these are set, all calls are made using
     * trusted creds for user/pswd and query param runas=&lt;user&gt;
     * @param trustedServiceName user name for authorization
     * @param trustedServicePswd password for authorization
     */
    public void setTrustedCredentials(String trustedServiceName,
                                      String trustedServicePswd) throws Exception {
        if (trustedServiceName != null && trustedServicePswd != null) {
            this.trustedServiceName = trustedServiceName;
            this.trustedServicePswd = trustedServicePswd;
            print("Authorizing trusted service using : " + trustedServiceName +
                  "/" + trustedServicePswd);
            client.getState().setCredentials(AuthScope.ANY,
                                             new UsernamePasswordCredentials(this.trustedServiceName,
                                                                             this.trustedServicePswd));
        }
    }


    /**
     * Log in to the HTTP session
     */
    public void login() throws Exception {
        if (!runAsSoap && !restAuthenticated) {
            addAuthorization();
            Object ret = sendPost("session/auth/login", null, null, null);
            Class c = Class.forName("com.oracle.beehive.rest.AntiCSRF");
            if (ret != null && c.getName().equals(ret.getClass().getName())) {
                Method m = c.getMethod("getToken");
                antiCSRFtoken = (String)m.invoke(ret);
                printv("CSRF token = " + antiCSRFtoken);
                restAuthenticated = true;
                removeAuthorization();
            } else {
                print("login returned unexpected object of type = " + ret);
                showNullError();
            }
        }
    }

    public void logout() throws Exception {
        if (restAuthenticated) {
            System.out.println("________MTB: logging out ...");
            sendPost("session/logout", null, null, null);
            restAuthenticated = false;
            addAuthorization();
        }
    }

    /**
     * returns true if "authenticated via REST
     */
    public boolean isRestAuthenticated() {
        return this.restAuthenticated;
    }

    /** set verbose flag
     * @param verbose set to true for verbose output (if set to false, only URIs will be displayed, unless quiet is also set to true)
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** set quiet flag
     * @param quiet set to true to suppress all output
     */
    public void setQuiet(boolean quiet) {
        this.verbose = !quiet;
        this.quiet = quiet;
    }

    /** set force verbose flag
     * @param forceVerbose set to true for all verbose output (if set to false, normal behavior based on verbose/quiet settings)
     */
    public void setForceVerbose(boolean forceVerbose) {
        this.forceVerbose = forceVerbose;
    }

    /** set force quiet flag
     * @param forceQuiet set to true for all quiet output (if set to false, normal behavior based on verbose/quiet settings)
     */
    public void setForceQuiet(boolean forceQuiet) {
        this.forceQuiet = forceQuiet;
    }

    /** get verbose flag
     * @return verbose flag
     */
    public boolean getVerbose() {
        return verbose;
    }

    /** set min OK return code
     * @param minRetCode min return code for asserts
     */
    public void setMinRetCode(int minRetCode) {
        this.minRetCode = minRetCode;
    }

    /** set max OK return code
     * @param maxRetCode max return code for asserts
     */
    public void setMaxRetCode(int maxRetCode) {
        this.maxRetCode = maxRetCode;
    }

    /** set check ret code flag
     * @param checkRetCode set to false to suppress failing on incorrect ret code
     */
    public void setCheckRetCode(boolean checkRetCode) {
        this.checkRetCode = checkRetCode;
        if (scu != null) {
            this.scu.throwSoapError(checkRetCode);
        }
    }

    /** set url prefix (not required)
     * @param urlPrefix if set, will be used as a prefix so that complete URLs do not need to be set for each request
     */
    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
        if (!this.urlPrefix.endsWith("/")) {
            urlPrefix += "/";
        }
    }

    /** set request mode flag
     * @param reqMode set to one of REQ_MODE_STRICT,REQ_MODE_NORMAL, or REQ_MODE_DEBUG
     */
    public void setRequestMode(String reqMode) {
        this.reqMode = reqMode;
    }


    /** get reqMode flag
     * @return reqMode flag
     */
    public String getRequestMode() {
        return reqMode;
    }

    /** set accept mode
     *  @param accept value for response content type  (must be one of RestClientUtils.CONTENT_TYPE_*)
     */
    public void setAccept(String accept) {
        this.accept = accept;
        acceptSet = true;
    }

    /** get last server response
     * @return RestServerResponse from last call
     */
    public RestServerResponse getServerResponse() {
        return rsr;
    }

    /** get last exception
     * @return exception from last call
     */
    public Exception getException() {
        return ex;
    }

    public Object getErrorObject() {
        return errorObject;
    }

    /** set payload content type
     * @param contentType set payload content type (must be one of RestClientUtils.CONTENT_TYPE_*)
     */
    public void setPayloadContentType(String contentType) {
        payloadContentType = contentType;
        payloadContentTypeSet = true;
    }

    public String getPayloadContentType() {
        return payloadContentType;
    }

    /** set show payload flag
     * @param showPayload set to true to show payload in verbose mode.  set to false when payload contains binary data or special characters that might get mangled by XML formatting
     */
    public void setShowPayload(boolean showPayload) {
        this.showPayload = showPayload;
    }

    /** set show raw payload flag
     * @param showRawPayload set to true to show raw payload even in non verbose mode.  set to false as needed
     */
    public void setShowRawPayload(boolean showRawPayload) {
        this.showRawPayload = showRawPayload;
    }

    /** set show response flag
     * @param showResponse set to true to show response in verbose mode.  set to false when response contains binary data or special characters that might get mangled by XML formatting
     */
    public void setShowResponse(boolean showResponse) {
        this.showResponse = showResponse;
    }

    /** set format for html flag
     * @param formatForHtml set to false when displaying on console
     */
    public void setFormatForHtml(boolean formatForHtml) {
        this.formatForHtml = formatForHtml;
    }

    /** set show JSON response flag
     * @param showJSONResponse set to true to show JSON equivalent response in verbose mode.  set to false when response contains binary data or special characters that might get mangled by XML formatting
     */
    public void setShowJSONResponse(boolean showJSONResponse) {
        this.showJSONResponse = showJSONResponse;
    }

    /** set format payload flag
     * @param formatPayload set to true to format payload in verbose mode.  set to false when payload contains binary data or special characters that might get mangled by XML formatting
     */
    public void setFormatPayload(boolean formatPayload) {
        this.formatPayload = formatPayload;
    }

    /** set suppress 20x code  flag
     * @param suppress20xCode set to true to suppress codes between 200 and 210
     */
    public void setSuppress20xCode(boolean suppress20xCode) {
        this.suppress20xCode = suppress20xCode;
    }

    private void resetVString() {
        verboseString = "";
    }

    private void printv(String msg) {
        if (forceVerbose || verbose)
            print(msg);
        verboseString += msg + "\n";
    }

    private void print(String msg) {
        if ((!(forceQuiet || quiet)) && (msg != null) &&
            (!(msg.trim().equals("")))) {
            System.out.println("____[" +
                               (trustedServiceName != null ? trustedServiceName :
                                authUsername) + "|" + hashCode() + "] " + msg);
        }
    }

    private Hashtable<String, String> getHeaderParams(String headerParams) {
        return (headerParams == null ? null : stringToHashtable(headerParams));
    }

    private Hashtable<String, String> getQueryParams(String queryParams) {
        return (queryParams == null ? null : stringToHashtable(queryParams));
    }

    /** this will be removed : DO NOT USE */
    public static Object rsrToObject(RestServerResponse rsr) throws Exception {
        return rsr.getRCU().getResponseObject(rsr.payloadStr);
    }

    /**
     * Interpret response stream as an Object
     * @param respStr response from server
     * @return object (unmarshalled from XML or JSON or raw input stream)
     */
    Object getResponseObject(String respStr) throws Exception {
        if (respStr == null) {
            respStr = "";
        }
        Object ret = null;
        boolean isXmlResp = false;
        Exception xmlE = null;
        boolean isJsonResp = false;
        Exception jsonE = null;
        // try interpreting XML or JSON and if that fails, return InputStream
        respStr = respStr.trim();
        try {
            // process xml response
            ret =
QAJAXBPool.unmarshallXML(XmlJsonUtils.getStreamFromString(respStr));
            isXmlResp = (ret != null);
        } catch (Exception xmle) {
            xmlE = xmle;
            xmle.printStackTrace(System.out);
        }
        try {
            if (ret == null) {
                // process json response
                ret = (new JSONReader()).parse(respStr);
                isJsonResp = (ret != null);
            }
        } catch (Exception jsone) {
            jsonE = jsone;
            jsone.printStackTrace(System.out);
        }
        if (isJsonResp && showResponse && (verbose || forceVerbose))
            print("application/json response: \n" +
                    (new JSONWriter(formatForHtml)).write(ret) + "\n");
        if (!isXmlResp && !isJsonResp) {
            if (respStr.length() > 0)
                print("____failed to convert response payload to a Java object, returning response payload\n'" +
                      respStr + "'");
            ret = XmlJsonUtils.getStreamFromString(respStr);
        }
        if (ret instanceof InputStream)
            return ret;
        if (ret != null &&
            ret.getClass().getName().startsWith("com.oracle.beehive.rest") &&
            (ret.getClass().getName().endsWith("Failure") ||
             ret.getClass().getName().endsWith("Mismatch"))) {
            errorObject = ret;
            return null;
        }
        return ret;
    }

    /* DO NOT USE
   * @deprecated will be removed
   */

    public void objectToStream(Object payload,
                               ByteArrayOutputStream out) throws Exception {
        InputStream in = getPayloadStream(payload);
        String input = XmlJsonUtils.getStreamContent(in);
        out.reset();
        out.write(input.getBytes());
    }

    private InputStream objectToXMLStream(Object payload,
                                          boolean showRawPayload) throws Exception {
        InputStream in = null;
        try {
            in = QAJAXBPool.marshallXML(payload, showRawPayload);
        } catch (MarshalException marExcp) {
            // will only happen for XML payloads
            Class c = Class.forName("com.oracle.beehive.ObjectFactory");
            String cname = payload.getClass().getName();
            String[] names = cname.split("\\.");
            String basename = names[names.length - 1];
            Method m = c.getMethod("create" + basename, payload.getClass());
            JAXBElement jo = (JAXBElement)(m.invoke(c.newInstance(), payload));
            in = QAJAXBPool.marshallXML(jo, showRawPayload);
        }
        return in;
    }

    private InputStream getPayloadStream(Object payload) throws Exception {
        InputStream in = null;
        if (payload != null) {
            // if payload is com.oracle.beehive.* then marshal into XML or JSON
            // else if InputStream, return stream (for uploads)
            // else fail("Bad payload!");

            if (payload.getClass().getName().startsWith("com.oracle.beehive")) {
                payloadIsStream = false;
                if (payloadContentType.equals(CONTENT_TYPE_XML)) {
                    in = objectToXMLStream(payload, showRawPayload);
                } else {
                    String jpayload =
                        (new JSONWriter(formatForHtml)).write(payload);
                    in = XmlJsonUtils.getStreamFromString(jpayload);
                }
            } else if (payload instanceof InputStream) {
                payloadIsStream = true;
                in = (InputStream)payload;
            } else {
                System.err.println("Payload is neither com.oracle.beehive.* object nor InputStream");
            }
        }
        return in;
    }

    /**
     * set to run as SOAP
     * @param runAsSoap set to true to run as SOAP, false to run as REST
     */

    public void setRunAsSoap(boolean runAsSoap) throws Exception {
        this.runAsSoap = runAsSoap;
        if (runAsSoap) {
            if (scu == null) {
                if (trustedServiceName != null &&
                    trustedServiceName.trim().length() > 0) {
                    scu =
new SoapClientUtils(trustedServiceName, trustedServicePswd, authUsername);
                    scu.throwSoapError(checkRetCode);
                } else {
                    scu =
new SoapClientUtils(authUsername, authPassword, null);
                    scu.throwSoapError(checkRetCode);
                }
            }
        } else {
            login();
        }
    }

    /**
     *  returns true if "run as SOAP"
     */
    public boolean getRunAsSoap() {
        return this.runAsSoap;
    }

    /**
     * get last server response
     * @return SoapServerResponse from last call, null if last call is a REST call
     */
    public SoapServerResponse getSoapServerResponse() {
        return scu.getSoapServerResponse();
    }

    /** @return response content type */
    public String getResponseContentType() {
        return responseContentType;
    }


    /**
     *  Send GET request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @return unmarshalled object or input stream from HTTP response.
     *  If null, invoke showNullError()
     */
    public Object sendGet(String url) throws Exception {
        return sendGet(url, null, null);
    }

    /**
     *  Send GET request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @param headerParams header params (in addition to accept and content-type) can be null - of the form 'k1=v1&amp;k2=v2&amp;k3=v3 ...'
     *  @param queryParams can be null - query params of the form 'k1=v1&amp;k2=v2&amp;k3=v3 ...'
     *  @return unmarshalled object or input stream from HTTP response.
     *  If null, invoke showNullError()
     */
    public Object sendGet(String url, String headerParams,
                          String queryParams) throws Exception {
        Object ret = null;
        if (!runAsSoap) {
            if (scu != null) {
                scu.resetSoapServerResponse();
            }
            resetVString();
            try {
                get(url, null, getHeaderParams(headerParams),
                    getQueryParams(queryParams));
                ret = getResponseObject(rsr.payloadStr);
            } catch (Exception e) {
                ex = e;
                e.printStackTrace(System.out);
            }
            if (ret == null) {
                showNullError();
            }
        } else {
            rsr = null;
            Rest2Soap.r2s(scu, "GET", url, queryParams, null);
            ret = scu.getSoapServerResponse().getResponseObject();
        }
        return ret;
    }

    /**
     *  Send GET request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @param accept value for response content type  (must be null or one of RestClientUtils.CONTENT_TYPE_*)
     *  @param headerParams hash table <String, String>  for additional request headers (can be null)
     *  @param queryParams hash table <String, String>  for request queryParams (can be null)
     *
     *  @return REST server response (@see RestServerResponse)
     */
    public RestServerResponse get(String url, String accept,
                                  Hashtable<String, String> headerParams,
                                  Hashtable<String, String> queryParams) throws Exception {
        RestServerResponse rsr =
            sendRequest(url, HTTP_GET, accept, headerParams, null,
                        queryParams);
        assertCode(minRetCode, maxRetCode, "Failed: executing GET : " + url);
        return rsr;
    }


    /**
     *  Send DELETE request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     */
    public void sendDelete(String url) throws Exception {
        sendDelete(url, null, null);
    }

    /**
     *  Send DELETE request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @param headerParams header params (in addition to accept and content-type) can be null - of the form 'k1=v1&amp;k2=v2&amp;k3=v3 ...'
     *  @param queryParams can be null - query params of the form 'k1=v1&amp;k2=v2&amp;k3=v3 ...'
     */
    public void sendDelete(String url, String headerParams,
                           String queryParams) throws Exception {
        if (!runAsSoap) {
            if (scu != null) {
                scu.resetSoapServerResponse();
            }

            resetVString();
            delete(url, null, getHeaderParams(headerParams),
                   getQueryParams(queryParams));
        } else {
            rsr = null;
            Rest2Soap.r2s(scu, "DELETE", url, queryParams, null);
        }
    }


    /**
     *  Send DELETE request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @param accept value for response content type  (must be null or one of RestClientUtils.CONTENT_TYPE_*)
     *  @param headerParams hash table <String, String>  for additional request headers (can be null)
     *  @param queryParams hash table <String, String>  for request queryParams (can be null)
     *
     *  @return REST server response (@see RestServerResponse)
     */
    public RestServerResponse delete(String url, String accept,
                                     Hashtable<String, String> headerParams,
                                     Hashtable<String, String> queryParams) throws Exception {
        RestServerResponse rsr =
            sendRequest(url, HTTP_DELETE, accept, headerParams, null,
                        queryParams);
        assertCode(minRetCode, maxRetCode,
                   "Failed: executing DELETE : " + url);
        return rsr;
    }

    /**
     *  Send upload request to web server
     *  @param stream data stream
     *  @param uploadScope upload scope to use (see BDK API)
     *  @param contentId content ID to use (see BDK API)
     *  @return unmarshalled object or input stream from HTTP response.
     *  If null, invoke showNullError()
     */
    public Object sendUpload(InputStream stream, String uploadScope,
                             String contentId) throws Exception {
        return sendPost("session/upload", stream, null,
                        "uploadscope=" + uploadScope + "&content_id=" +
                        contentId);
    }


    /**
     *  Send POST request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @param payload object to be sent (must be com.oracle.beehive.* object, InputStream, or EMPTY_PAYLOAD (only if needed))
     *  @return unmarshalled object or input stream from HTTP response.
     *  If null, invoke showNullError()
     */
    public Object sendPost(String url, Object payload) throws Exception {
        return sendPost(url, payload, null, null);
    }

    /**
     *  Send POST request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @param queryParams can be null - query params of the form 'k1=v1i&amp;k2=v2&amp;k3=v3 ...'
     *  @param headerParams header params (in addition to accept and content-type) can be null - of the form 'k1=v1&amp;k2=v2&amp;k3=v3 ...'
     *  @param payload object to be sent (must be com.oracle.beehive.* object, InputStream, or EMPTY_PAYLOAD (only if needed))
     *  @return unmarshalled object or input stream from HTTP response.
     *  If null, invoke showNullError()
     */
    public Object sendPost(String url, Object payload, String headerParams,
                           String queryParams) throws Exception {
        Object ret = null;
        if (!runAsSoap) {
            System.out.println("in sendPost: the runAsSoap is " + runAsSoap);
            if (scu != null) {
                scu.resetSoapServerResponse();
            }
            System.out.println("in sendPost: REST mode, will convert payload");
            resetVString();
            InputStream in = getPayloadStream(payload);
            //String body = inputStream2String(in);
            //System.out.println("Debug: the string after getPayloadStream is: "+body);
            try {
                post(url, null, getHeaderParams(headerParams), in,
                     getQueryParams(queryParams));
                ret = getResponseObject(rsr.payloadStr);
            } catch (Exception e) {
                ex = e;
                e.printStackTrace(System.out);
            }
        } else {
            rsr = null;
            if (url.toLowerCase().equals("session/upload")) {
                InputStream in = copyInputStream((InputStream)payload);
                String uploadScopeId =
                    Rest2Soap.getParamValue(queryParams, "uploadscope");
                String contentId =
                    Rest2Soap.getParamValue(queryParams, "content_id");
                if (!(uploadedContent.containsKey(uploadScopeId))) {
                    Map<String, Object> contentIdMap =
                        new HashMap<String, Object>();
                    contentIdMap.put(contentId, in);
                    uploadedContent.put(uploadScopeId, contentIdMap);
                    //System.out.println("UploadScopeId: " + uploadScopeId + " contentId " + contentId + " added to uploadedContent Map");
                } else {
                    Map<String, Object> contentIdMap =
                        uploadedContent.get(uploadScopeId);
                    if (!(contentIdMap.containsKey(contentId))) {
                        contentIdMap.put(contentId, in);
                        //System.out.println("UploadScopeId: " + uploadScopeId + " contentId " + contentId + " added to uploadedContent Map");
                    } // else ignore
                }
            } else {
                String uploadScopeId =
                    Rest2Soap.getParamValue(queryParams, "uploadscope");
                //System.out.println("uploadscope: " + uploadScopeId);
                if (uploadScopeId != null) {
                    Rest2Soap.r2s(scu, "POST", url, queryParams, payload,
                                  uploadScopeId, uploadedContent);
                } else {
                    Rest2Soap.r2s(scu, "POST", url, queryParams, payload);
                }
                ret = scu.getSoapServerResponse().getResponseObject();
            }
        }
        return ret;
    }


    /**
     *  Send POST request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @param accept value for response content type  (must be one of RestClientUtils.CONTENT_TYPE_*)
     *  @param headerParams hash table <String, String>  for additional request headers (can be null)
     *  @param input input stream (payload).  Use setPayloadContentType() to set payload type
     *  @param queryParams hash table <String, String>  for request queryParams (can be null)
     *
     *  @return REST server response (@see RestServerResponse)
     */
    public RestServerResponse post(String url, String accept,
                                   Hashtable<String, String> headerParams,
                                   InputStream input,
                                   Hashtable<String, String> queryParams) throws Exception {
        RestServerResponse rsr =
            sendRequest(url, HTTP_POST, accept, headerParams, input,
                        queryParams);
        assertCode(minRetCode, maxRetCode, "Failed: executing POST : " + url);
        return rsr;
    }

    /**
     *  Send PUT request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @param queryParams can be null - query params of the form 'k1=v1i&amp;k2=v2&amp;k3=v3 ...'
     *  @param headerParams header params (in addition to accept and content-type) can be null - of the form 'k1=v1&amp;k2=v2&amp;k3=v3 ...'
     *  @param payload object to be sent (must be com.oracle.beehive.* object, InputStream, or EMPTY_PAYLOAD (only if needed))
     *  @return unmarshalled object or input stream from HTTP response.
     *  If null, invoke showNullError()
     */
    public Object sendPut(String url, Object payload, String headerParams,
                          String queryParams) throws Exception {
        Object ret = null;
        if (!runAsSoap) {
            if (scu != null) {
                scu.resetSoapServerResponse();
            }

            resetVString();
            InputStream in = getPayloadStream(payload);
            try {
                put(url, null, getHeaderParams(headerParams), in,
                    getQueryParams(queryParams));
                ret = getResponseObject(rsr.payloadStr);
            } catch (Exception e) {
                ex = e;
                e.printStackTrace(System.out);
            }
        } else {
            rsr = null;
            String uploadScopeId =
                Rest2Soap.getParamValue(queryParams, "uploadscope");
            if (uploadScopeId != null) {
                Rest2Soap.r2s(scu, "PUT", url, queryParams, payload,
                              uploadScopeId, uploadedContent);
            } else {
                Rest2Soap.r2s(scu, "PUT", url, queryParams, payload);
            }
            ret = scu.getSoapServerResponse().getResponseObject();
        }
        return ret;
    }


    /**
     *  Send PUT request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @param payload object to be sent (must be com.oracle.beehive.* object, InputStream, or EMPTY_PAYLOAD (only if needed))
     *  @return unmarshalled object or input stream from HTTP response.
     *  If null, invoke showNullError()
     */
    public Object sendPut(String url, Object payload) throws Exception {
        return sendPut(url, payload, null, null);
    }

    /**
     *  Send PUT request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @param accept value for response content type  (must be null or one of RestClientUtils.CONTENT_TYPE_*)
     *  @param input input stream (payload).  Use setPayloadContentType() to set payload type
     *  @param headerParams hash table <String, String>  for additional request headers (can be null)
     *  @param queryParams hash table <String, String>  for request queryParams (can be null)
     *
     *  @return REST server response (@see RestServerResponse)
     */
    public RestServerResponse put(String url, String accept,
                                  Hashtable<String, String> headerParams,
                                  InputStream input,
                                  Hashtable<String, String> queryParams) throws Exception {
        RestServerResponse rsr =
            sendRequest(url, HTTP_PUT, accept, headerParams, input,
                        queryParams);
        assertCode(minRetCode, maxRetCode, "Failed: executing PUT : " + url);
        return rsr;
    }


    /**
     * Show error(s) if null object returned unexpectedly
     */
    public void showNullError() {
        try {
            if (!checkRetCode)
                return;
            if (ex != null) {
                print("Exception : " + ex);
                StackTraceElement[] stes = ex.getStackTrace();
                for (int i = 0; i < stes.length; i++) {
                    StackTraceElement ste = stes[i];
                    print("" + ste);
                    if (("" + ste).indexOf("RestClientUtils") != -1)
                        break;
                }
            }
            showRespDetails(true, true);
        } catch (Exception e) {
            print("Exception encountered displaying error : " + e);
            e.printStackTrace(System.out);
        }
    }


    Hashtable<String, String> setUrlParam(Hashtable<String, String> queryParams,
                                          String key,
                                          String value) throws Exception {
        //  create params if needed
        if (queryParams == null) {
            queryParams = new Hashtable<String, String>();
        }
        // put value in only if needed (otherwise tests that test for bogus values will fail)
        if (queryParams.get(key) == null) {
            queryParams.put(key, value);
        }
        return queryParams;
    }


    /** Set query params in the url */
    void setUrlParams(Hashtable<String, String> queryParams,
                      String url) throws Exception {
        if (reqMode.equals(REQ_MODE_DEBUG) &&
            url.toLowerCase().indexOf("debug=true") == -1) {
            queryParams = setUrlParam(queryParams, "debug", "true");
        }
        if (trustedServiceName != null &&
            trustedServiceName.trim().length() > 0) {
            queryParams = setUrlParam(queryParams, "runas", authUsername);
        }
        // antiCSRFtoken not really needed for GETs but no harm in sending ...
        System.out.println("URL: " + url);
        if (!(url.contains("login"))) {
            if (antiCSRFtoken != null && antiCSRFtoken.trim().length() > 0) {
                queryParams =
                        setUrlParam(queryParams, "anticsrf", antiCSRFtoken);
            }
        }
        if (suppress20xCode) {
            queryParams =
                    setUrlParam(queryParams, "suppress_20x_code", suppress20xCode +
                                "");
        }
        if (queryParams != null) {
            boolean appendedParam = false;
            for (Enumeration<String> keys = queryParams.keys();
                 keys.hasMoreElements(); ) {
                String k = keys.nextElement();
                String val = queryParams.get(k);
                rsr.url +=
                        (appendedParam ? "&" : "?") + k + "=" + URLEncoder.encode(val,
                                                                                  "UTF-8");
                appendedParam = true;
            }
        }
    }

    /**
     * Assert return codes for http requests
     * @param min minimum return code (usually 200)
     * @param max max return code (usually 299)
     * @param failMsg can be null
     */
    public void assertCode(int min, int max, String failMsg) throws Exception {
        int retCode = getServerResponse().getReturnCode();
        if (suppress20xCode &&
            (retCode > DEF_MIN_OK_CODE && retCode < DEF_MAX_OK_CODE)) {
            System.err.println("suppress_20x_code failed: " + retCode +
                               " within (201,299)");
        }
        if (checkRetCode && (retCode < min || retCode > max)) {
            System.err.println((failMsg != null ? failMsg : "Failed:") + " " +
                               retCode + " not within (" + min + "," + max +
                               ")");
            if (!verbose && !forceVerbose)
                print(verboseString);
            resetVString();
        }
    }

    private String formatHTML(String s) {
        if (formatForHtml) {
            s = s.replace("<", "&lt;").replace(">", "&gt;");
            s =
  s.replace("&lt;", "<b><font color=\"#007700\">&lt;").replace("&gt;",
                                                               "&gt;</font></b>");
        }
        return s;
    }

    /**
     *  Send request to web server
     *
     *  @param url url to send to (example: http://stadt26.us.oracle.com:7777/comb/v1/d/my/inbox)
     *  @param method method to use (must be one of RestClientUtils.HTTP_*)
     *  @param accept value for response content type  (must be one of RestClientUtils.CONTENT_TYPE_*)
     *  @param headerParams hash table for request header Params (in addition to accept and content-type)
     *  @param input input stream for Post and Put requests
     *  @param queryParams hash table for request queryParams
     *
     *  @return REST server response (@see RestServerResponse)
     */
    private RestServerResponse sendRequest(String url, String method,
                                           String accept,
                                           Hashtable<String, String> headerParams,
                                           InputStream input,
                                           Hashtable<String, String> queryParams) throws Exception {

        rsr = new RestServerResponse();
        rsr.rcu = this;

        // fix url prefix (if needed)
        if (!url.startsWith("http:") && urlPrefix != null &&
            urlPrefix.length() > 0) {
            url = urlPrefix + (url.startsWith("/") ? url.substring(1) : url);
        }
        rsr.url = url;
        setUrlParams(queryParams, url);
        String sendURL = rsr.url;
        print("\n\t in SendRequest, is to run " + method + " to URL: " +
              sendURL);
        //for debug purpose
        /*
       if(input!=null){
         InputStream copy =  new ByteArrayInputStream(getInputStreamByteArrayContent(input));
         print("\n\t in SendRequest, is to send body: "+inputStream2String(copy));
       }*/
        //debug end
        InputStream newin = input;
        String payloadDisp = null;
        if (input != null && !payloadIsStream) {
            System.out.println("in SendRequest, input!=null, payloadIsStream is false");
            if (payloadContentType.equals(CONTENT_TYPE_XML)) {
                if (formatPayload) {
                    Object[] res = XmlJsonUtils.parseXML(input);
                    payloadDisp = (String)res[0];
                    //print("\n\t in SendRequest, is to send body: "+payloadDisp);
                    newin = XmlJsonUtils.getStreamFromString(payloadDisp);
                } else {
                    payloadDisp = XmlJsonUtils.getStreamContent(input);
                }
                /*
           if(payloadDisp!=null){
             System.out.println("in SendRequest, is to send body: "+payloadDisp);
           }*/
                //shining commeted out
                //payloadDisp = formatHTML(payloadDisp);
            } else {
                //retrim the input stream
                payloadDisp = XmlJsonUtils.getStreamContent(input);
                String payloadDisp2 = JSONWriter.stripFormat(payloadDisp);
                System.out.println("in SendRequest, payloadDisp2 to be sent is  " +
                                   payloadDisp2);
                newin = XmlJsonUtils.getStreamFromString(payloadDisp2);
            }

            System.out.println("in SendRequest, showPayload is " +
                               showPayload);
            if (showPayload)
                printv("payload (" + payloadContentType + ") = \n" +
                        payloadDisp);
        }
        if (input != null && !showPayload && (verbose || forceVerbose)) {
            printv("show payload = " + showPayload);
        }
        if (method.equals(HTTP_GET)) {
            httpmethod = new GetMethod(sendURL);
        } else if (method.equals(HTTP_DELETE)) {
            httpmethod = new DeleteMethod(sendURL);
        } else if (method.equals(HTTP_PUT)) {
            httpmethod = new PutMethod(sendURL);
            ((PutMethod)httpmethod).setRequestEntity(new InputStreamRequestEntity(newin));
        } else if (method.equals(HTTP_POST)) {
            httpmethod = new PostMethod(sendURL);
            if (newin != null) {
                //String inputStreamDisp = XmlJsonUtils.getStreamContent(newin);
                //System.out.println("In sendRequest: before setRequestEntity, the inputstream is "+inputStreamDisp);
                ((PostMethod)httpmethod).setRequestEntity(new InputStreamRequestEntity(newin));
            }
        }

        if (acceptSet && (accept != null || this.accept != null) &&
            (headerParams == null ||
             (headerParams.get(ACCEPT_HEADER) == null &&
              headerParams.get(ACCEPT_HEADER.toLowerCase()) == null))) {
            System.out.println("set header  httpmethod.setRequestHeader(ACCEPT_HEADER");
            System.out.println("ACCEPT_HEADER is " + ACCEPT_HEADER +
                               "//playload is " + this.accept);
            httpmethod.setRequestHeader(ACCEPT_HEADER,
                                        (accept == null ? this.accept :
                                         accept));

        }
        if (payloadContentTypeSet && payloadContentType != null &&
            (headerParams == null ||
             (headerParams.get(CONTENT_TYPE_HEADER) == null &&
              headerParams.get(CONTENT_TYPE_HEADER.toLowerCase()) == null))) {
            System.out.println("shining: set header  httpmethod.setRequestHeader(CONTENT_TYPE_HEADER");
            System.out.println("shining: CONTENT_TYPE_HEADER is " +
                               CONTENT_TYPE_HEADER + "//playload is " +
                               payloadContentType);
            //httpmethod.setRequestHeader(CONTENT_TYPE_HEADER, payloadContentType);
            httpmethod.addRequestHeader(CONTENT_TYPE_HEADER,
                                        payloadContentType);
        }

        if (headerParams != null) {
            for (Enumeration<String> keys = headerParams.keys();
                 keys.hasMoreElements(); ) {
                String k = keys.nextElement();
                String val = headerParams.get(k);
                httpmethod.setRequestHeader(k, val);
            }
        }

        // add code here to print headers using printv
        /*
       Header accept1 = new Header();
           accept1.setName("Accept");
           accept1.setValue("application/xml");
      */
        //httpmethod.addRequestHeader(accept1);
        int statusCode = client.executeMethod(httpmethod);
        //System.out.println("In rest call returned: "+inputStream2String(httpmethod.getResponseBodyAsStream()));
        Header ctHdr = httpmethod.getResponseHeader("Content-Type");
        if (ctHdr != null) {
            responseContentType = ctHdr.toString();
        } else {
            responseContentType = "";
        }
        rsr.returnCode = statusCode;
        rsr.stream = httpmethod.getResponseBodyAsStream();
        if (rsr.stream != null) {
            rsr.payloadStr = XmlJsonUtils.getStreamContent(rsr.stream);
        } else {
            rsr.payloadStr = "";
        }
        showRespDetails(forceVerbose || verbose ||
                        (rsr.returnCode > DEF_MAX_OK_CODE),
                        ((forceVerbose || verbose) && showResponse) ||
                        (rsr.returnCode > DEF_MAX_OK_CODE));


        return rsr;
    }

    private void showRespDetails(boolean verbose,
                                 boolean showResponse) throws Exception {
        if (verbose)
            print("response : " + rsr.returnCode + " : " +
                  responseContentType);
        if (showResponse) {
            // format response
            String svrResp = rsr.payloadStr;
            if (responseContentType.toLowerCase().contains(CONTENT_TYPE_XML)) {
                Object[] res =
                    XmlJsonUtils.parseXML(XmlJsonUtils.getStreamFromString(svrResp));
                if (res[0] != null) {
                    //shining commented out
                    //svrResp = formatHTML((String)res[0]);
                }
            }
            if (verbose)
                print("\n" +
                        svrResp);
        } else {
            if (verbose)
                print("show response = " + showResponse);
        }
    }

    /**
     * Convert hashtable to string
     * @param ht
     * @return string form using "key1=value1"+PROP_SEPARATOR+"key2=value2"+...
     */
    public static String hashTableToString(Hashtable<String, String> ht) {
        String ret = "";
        if (ht != null) {
            for (Enumeration<String> keys = ht.keys(); keys.hasMoreElements();
            ) {
                String k = keys.nextElement();
                String val = ht.get(k);
                ret += ((ret.length() > 0) ? "&" : "") + k + "=" + val;
            }
        }
        return ret;
    }

    /**
     * Convert string to Hashtable
     * @param s string form using "key1=value1"+PROP_SEPARATOR+"key2=value2"+...
     * @return Hashtable
     */
    private Hashtable<String, String> stringToHashtable(String s) {
        Hashtable<String, String> ht = new Hashtable<String, String>();
        String[] lines = s.split(PROP_SEPARATOR);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // line is 'key=value'
            int j = line.indexOf("=");
            if (j != -1) {
                String key = line.substring(0, j);
                String val = line.substring(j + 1);
                ht.put(key, val);
            }
        }
        return ht;
    }

    private InputStream copyInputStream(InputStream is) throws Exception {
        InputStream copy = null;

        if (is != null) {
            copy =
new ByteArrayInputStream(getInputStreamByteArrayContent(is));
        }

        return copy;
    }


    // Retrieve content in byte array for passed InputStream

    private byte[] getInputStreamByteArrayContent(InputStream is) throws Exception {
        byte[] bs = null;
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        byte[] barr = new byte[16384];

        int read = 0;
        do {
            read = is.read(barr);
            if (read > 0) {
                bais.write(barr, 0, read);
            }
        } while (read >= 0);
        is.close();
        bs = bais.toByteArray();

        return bs;

    }

    public String inputStream2String(InputStream in) throws IOException {
        try {
            if (in == null)
                return "blank body";
            InputStream newin = copyInputStream(in);
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[4096];
            for (int n; (n = newin.read(b)) != -1; ) {
                out.append(new String(b, 0, n));
            }
            return out.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "exception body";
        }
    }

    public List<EmailRecipient> createEmailRecipientCollection(OrganizationUser... orgUsers) {
        List<EmailRecipient> rcptList = new ArrayList<EmailRecipient>();
        for (OrganizationUser o : orgUsers) {
            EmailRecipient emlRcpt = new EmailRecipient();
            emlRcpt.setParticipantHandle(o.getCollabId());
            rcptList.add(emlRcpt);
        }
        return rcptList;
    }

    public void setToRecipients(List<EmailRecipient> toRcvrs,
                                EmailMessageContentUpdater emsgCntntUpdtr) {
        if (toRcvrs != null) {
            EmailRecipientListUpdater erlu = new EmailRecipientListUpdater();
            emsgCntntUpdtr.setToReceivers(erlu);
            List<EmailRecipient> toRcpts = erlu.getAdd();
            for (EmailRecipient emlRcpt : toRcvrs) {
                toRcpts.add(emlRcpt);
            }
        }
    }

    public static void main(String[] args) {
        try {


            String host =
                (args.length > 0 ? args[0] : "sgtcnpc48.cn.oracle.com");
            String port = (args.length > 1 ? args[1] : "7777");
            String user = (args.length > 2 ? args[2] : "shining.liu");
            String pswd = (args.length > 3 ? args[3] : "Welcome1");
            RestClientUtils rcu =
                new RestClientUtils(user, pswd, "http://" + "scl58022.us.oracle.com" +
                                    ":" + "7777" + "/comb/v1/d/");


            rcu.setAccept("application/xml");
            rcu.setPayloadContentType("application/xml");
            rcu.setVerbose(true);
            rcu.setFormatForHtml(true);

            //rcu.setPayloadContentType("application/xml");
            //rcu.sendGet("my/user");
            String msgSubject = "subject-from-webservice";
            String textBody = "body-from-webservice";
            String mediaType = "text/plain";
            String uniqueID = Long.toString(System.nanoTime());
            String contentId = Long.toString(System.nanoTime());
            String uploadScope = Long.toString(System.nanoTime());
            //rcu.sendGet("my/user",null,null);

            OrganizationUser user2 =
                (OrganizationUser)rcu.sendGet("my/user", null, null);
            //OrganizationUser user2 = new OrganizationUser();
            // rcu.setPayloadContentType(mtb_defaultContentType);
            rcu.login();
            EmailParticipant emlPcpt = new EmailParticipant();
            emlPcpt.setParticipantHandle(user2.getCollabId());
            //upload email body before send email

            InputStream streamData =
                new ByteArrayInputStream(textBody.getBytes());
            rcu.setPayloadContentType(mediaType);
            System.out.println("Before Upload Data");
            rcu.sendUpload(streamData, uploadScope, contentId);
            System.out.println("After Upload Data");
            streamData.close();

            //set mail header
            rcu.setPayloadContentType("application/xml");
            EmailMessageContentUpdater emsgCntntUpdtr =
                new EmailMessageContentUpdater();
            emsgCntntUpdtr.setSender(emlPcpt);
            RawString subject = new RawString();
            //set subject
            if (msgSubject != null)
                subject.setString(msgSubject);
            emsgCntntUpdtr.setSubject(subject);
            //set sender
            emsgCntntUpdtr.setEnvelopeSender(emlPcpt);
            //set to
            List<EmailRecipient> reclist =
                rcu.createEmailRecipientCollection(user2);
            rcu.setToRecipients(reclist, emsgCntntUpdtr);
            //set body

            StreamedSimpleContentUpdater bodyUpdtr =
                new StreamedSimpleContentUpdater();
            emsgCntntUpdtr.setBodyUpdater(bodyUpdtr);
            bodyUpdtr.setCharacterEncoding("utf-8");

            //Set Media Type
            if (mediaType != null)
                bodyUpdtr.setMediaType(mediaType);
            else
                bodyUpdtr.setMediaType("text/plain");

            //Set ContentStreamId
            if (contentId != null)
                bodyUpdtr.setContentStreamId(contentId);
            else
                bodyUpdtr.setContentStreamId(uniqueID);
            //rcu.setRunAsSoap(true);
            rcu.setRequestMode("DEBUG");
            rcu.sendPost("emsg/send", emsgCntntUpdtr, null,
                         "uploadscope=" + uploadScope);

            //emlPcpt.setParticipantHandle(orgUser.getCollabId());
            /*
        EmailMessageContentUpdater emsgCntntUpdtr = createEmlMsgCntntUpdtr(
              emlPcpt, emlPcpt, emlPcpt, emlPcpt, emlPcpt, emlPcpt,
              "subject-testing", mediaType, contentId);*/
            /*
    HttpClient httpClient = new HttpClient();

        // Set the user name and password.
        String username="beeadmin";
        String password="Welcome1";
        String machine="sgtcnpc48.cn.oracle.com";
        int port= 7777;
        Credentials defaultcreds = new UsernamePasswordCredentials(username,
            password);

        // Set the client to always send the authentication information.
        httpClient.getParams().setAuthenticationPreemptive(true);

        // Set the host name and port number and then apply the credentials.
        httpClient.getHostConfiguration().setHost(machine, port);
        httpClient.getState().setCredentials(
            new AuthScope(machine, port, AuthScope.ANY_REALM), defaultcreds);

        // Create a Accept header to receive the information in XML format
        Header accept = new Header();
        accept.setName("Accept");
        accept.setValue("application/xml");

        // Create a GetMethod accessing the My Resource.
        GetMethod getUser = new GetMethod("/comb/v1/d/my/user");
        getUser.addRequestHeader(accept);
        httpClient.executeMethod(getUser);
         InputStream in = getUser.getResponseBodyAsStream();

        System.out.println(RestClientUtils.inputStream2String(in));
*/
        } catch (Exception e) {
            System.out.println("Exception e");
            e.printStackTrace();
        }
    }
}
