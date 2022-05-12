package com.sheng.jobframework.utility.HttpClient;

//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;


import com.sheng.jobframework.jobdom.ACElement;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import java.lang.reflect.Method;

import java.net.URLEncoder;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

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


public class HttpClientUtils extends ACElement {
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
    private boolean payloadIsStream = true;

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
    private static java.util.Hashtable<String, RestClientUtils> rcuCache =
        new java.util.Hashtable<String, RestClientUtils>();
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

    HttpServerResponse rsr = null;
    Exception ex = null;
    Object errorObject = null;

    private boolean runAsSoap = false;
    private boolean restAuthenticated = false;
    SoapClientUtils scu = null;

    private Map<String, Map<String, Object>> uploadedContent =
        new HashMap<String, Map<String, Object>>();
    Properties global_headerParams = new Properties();


    public HttpClientUtils() {
        super();
    }

    public HttpClientUtils(String authUsername,
                           String authPassword) throws Exception {
        init(authUsername, authPassword, "");
    }

    public HttpClientUtils(String authUsername, String authPassword,
                           String urlprefix) throws Exception {
        init(authUsername, authPassword, urlprefix);
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
        outputFrameLog("Authorizing using : " + authUsername + "/" +
                       authPassword);
        //shining added
        client.getParams().setAuthenticationPreemptive(true);
        client.getState().setCredentials(AuthScope.ANY,
                                         new UsernamePasswordCredentials(authUsername,
                                                                         authPassword));
    }

    /**
     * Log in to the HTTP session
     */
    public void login() throws Exception {
        if (!runAsSoap && !restAuthenticated) {
            Object ret = null;
            //Object ret = post("session/auth/login",null);
            Class c = Class.forName("com.oracle.beehive.rest.AntiCSRF");
            if (ret != null && c.getName().equals(ret.getClass().getName())) {
                Method m = c.getMethod("getToken");
                antiCSRFtoken = (String)m.invoke(ret);
                System.out.println("CSRF token = " + antiCSRFtoken);
                restAuthenticated = true;

            } else {
                System.out.println("login returned unexpected object of type = " +
                                   ret);

            }
        }
    }

    public void logout() throws Exception {
        if (restAuthenticated) {
            System.out.println("________MTB: logging out ...");
            post("session/logout", null);
            restAuthenticated = false;
        }
    }

    private void addHttpHeader(String key, String value) {
        global_headerParams.setProperty(key, value);
    }

    private void clieanHttpHeader() {
        global_headerParams = new Properties();
    }

    public void setContentType(String type) {
        global_headerParams.setProperty(CONTENT_TYPE_HEADER, type);
    }

    public void setAccept(String accept) {
        global_headerParams.setProperty(ACCEPT_HEADER, accept);
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

    public HttpServerResponse get(String url) {
        try {
            HttpServerResponse hsp =
                sendRequest(url, HTTP_GET, null, null, null, null);
            return hsp;
        } catch (Exception e) {
            outputFrameLog("Exception occurs when performa a GET call: " +
                           e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public HttpServerResponse get(String url,
                                  Hashtable<String, String> headerParams,
                                  Hashtable<String, String> queryParams) {
        try {
            HttpServerResponse hsp =
                sendRequest(url, HTTP_GET, null, headerParams, null,
                            queryParams);
            return hsp;
        } catch (Exception e) {
            outputFrameLog("Exception occurs when performa a GET call: " +
                           e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public HttpServerResponse put(String url,
                                  Hashtable<String, String> headerParams,
                                  Hashtable<String, String> queryParams) {
        try {
            HttpServerResponse hsp =
                sendRequest(url, HTTP_PUT, null, headerParams, null,
                            queryParams);
            return hsp;
        } catch (Exception e) {
            outputFrameLog("Exception occurs when performa a PUT call: " +
                           e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public HttpServerResponse post(String url, String postbody,
                                   Hashtable<String, String> headerParams,
                                   Hashtable<String, String> queryParams) {
        try {
            InputStream input = new StringBufferInputStream(postbody);
            HttpServerResponse hsp =
                sendRequest(url, HTTP_POST, null, headerParams, input,
                            queryParams);
            return hsp;
        } catch (Exception e) {
            outputFrameLog("Exception occurs when performa a POST call: " +
                           e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public HttpServerResponse post(String url, String postbody) {
        try {
            InputStream input = new StringBufferInputStream(postbody);
            HttpServerResponse hsp =
                sendRequest(url, HTTP_POST, null, null, input, null);
            return hsp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
    private HttpServerResponse sendRequest(String url, String method,
                                           String accept,
                                           java.util.Hashtable<String, String> headerParams,
                                           InputStream input,
                                           java.util.Hashtable<String, String> queryParams) throws Exception {

        rsr = new HttpServerResponse();
        rsr.hcu = this;

        // fix url prefix (if needed)
        if (!url.startsWith("http:") && urlPrefix != null &&
            urlPrefix.length() > 0) {
            url = urlPrefix + (url.startsWith("/") ? url.substring(1) : url);
        }
        rsr.url = url;
        setUrlParams(queryParams, url);
        String sendURL = rsr.url;
        outputFrameLog("\n\t" + method + " " + sendURL);
        InputStream newin = getFormatedStream(input);

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
                ((PostMethod)httpmethod).setRequestEntity(new InputStreamRequestEntity(newin));
            }
        }
        setRequestParas(httpmethod, accept, headerParams, queryParams);
        int statusCode = client.executeMethod(httpmethod);
        //System.out.println("In rest call returned: "+inputStream2String(httpmethod.getResponseBodyAsStream()));
        Header ctHdr = httpmethod.getResponseHeader("Content-Type");
        if (ctHdr != null) {
            responseContentType = ctHdr.toString();
            rsr.repsonseContentType = responseContentType;
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
        //showRespDetails(forceVerbose || verbose || (rsr.returnCode > DEF_MAX_OK_CODE),((forceVerbose || verbose) && showResponse) || (rsr.returnCode > DEF_MAX_OK_CODE));


        return rsr;
    }

    private InputStream getFormatedStream(InputStream input) {
        // System.out.println("this payloadisStream beging is  "+payloadIsStream);
        InputStream newin = input;
        String payloadDisp = null;
        try {
            //this branch is for oracle object
            if (input != null && !payloadIsStream) {
                System.out.println("this payloadisStream is  " +
                                   payloadIsStream);
                if (payloadContentType.equals(CONTENT_TYPE_XML)) {
                    if (formatPayload) {
                        Object[] res = XmlJsonUtils.parseXML(input);
                        payloadDisp = (String)res[0];
                        newin = XmlJsonUtils.getStreamFromString(payloadDisp);
                    } else {
                        payloadDisp = XmlJsonUtils.getStreamContent(input);
                    }
                    payloadDisp = formatHTML(payloadDisp);
                } else {
                    //retrim the input stream

                    payloadDisp = XmlJsonUtils.getStreamContent(input);
                    String payloadDisp2 = JSONWriter.stripFormat(payloadDisp);
                    System.out.println("the data will be sent to server: ");
                    System.out.println(payloadDisp2);
                    newin = XmlJsonUtils.getStreamFromString(payloadDisp2);
                }
                if (showPayload)
                    outputFrameLog("payload (" + payloadContentType +
                                   ") = \n" +
                            payloadDisp);
            } else {
                //this branch is for normal post, the object is a normal inputStream
                if (input != null) {
                    payloadDisp = XmlJsonUtils.getStreamContent(input);
                    String payloadDisp2 = JSONWriter.stripFormat(payloadDisp);
                    System.out.println("branch the data will be sent to server : ");
                    System.out.println(payloadDisp2);
                    newin = XmlJsonUtils.getStreamFromString(payloadDisp2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return newin;
    }

    private void setRequestParas(HttpMethod httpmethod, String accept,
                                 Hashtable<String, String> headerParams,
                                 Hashtable<String, String> queryParams) {
        if (accept != null) {
            if (!accept.equalsIgnoreCase("")) {
                httpmethod.setRequestHeader(ACCEPT_HEADER, accept);
            }
        }
        if (headerParams != null) {
            for (Enumeration<String> keys = headerParams.keys();
                 keys.hasMoreElements(); ) {
                String k = keys.nextElement();
                String val = headerParams.get(k);
                httpmethod.setRequestHeader(k, val);
            }
        }
        if (global_headerParams != null) {
            Enumeration elmentInfoKeys = global_headerParams.keys();
            while (elmentInfoKeys.hasMoreElements()) {
                String k = (String)elmentInfoKeys.nextElement();
                String val = global_headerParams.getProperty(k);
                httpmethod.setRequestHeader(k, val);
            }
        }

    }

    public void setPayLoadIsNormalStream(boolean isNormalStream) {
        payloadIsStream = isNormalStream;
    }

    public void setOraclePayLoad(boolean isOraclePayLoad) {
        formatPayload = isOraclePayLoad;
    }

    public void initNormalHttpClient() {
        setPayLoadIsNormalStream(true);
        setOraclePayLoad(false);
    }

    public void initSoapClient() {
        setPayLoadIsNormalStream(true);
        setOraclePayLoad(false);
        setContentType("application/xml");
        setAccept("application/xml");
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
                //System.out.println("query paras key/value is "+k+val);
                rsr.url +=
                        (appendedParam ? "&" : "?") + k + "=" + URLEncoder.encode(val,
                                                                                  "UTF-8");
                appendedParam = true;
            }
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

    public static String inputStream2String(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
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

    private void showRespDetails(boolean verbose,
                                 boolean showResponse) throws Exception {
        if (verbose)
            outputFrameLog("response : " + rsr.returnCode + " : " +
                           responseContentType);
        if (showResponse) {
            // format response
            String svrResp = rsr.payloadStr;
            if (responseContentType.toLowerCase().contains(CONTENT_TYPE_XML)) {
                Object[] res =
                    XmlJsonUtils.parseXML(XmlJsonUtils.getStreamFromString(svrResp));
                if (res[0] != null) {
                    svrResp = formatHTML((String)res[0]);
                }
            }
            if (verbose)
                outputFrameLog("\n" +
                        svrResp);
        } else {
            if (verbose)
                outputFrameLog("show response = " + showResponse);
        }
    }

    public static void main(String[] args) {
        try {


            HttpClientUtils hcu =
                new HttpClientUtils("shining.liu", "Welcome1",
                                    "http://" + "scl58025.us.oracle.com" +
                                    ":" + "7777" + "/comb/v1/d/");
            //hcu.initSoapClient();
            InputStream in = new StringBufferInputStream("test");
            String soapEnvelope =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\"><soap:Header><wsa:Action></wsa:Action><wsa:MessageID>uuid:ca025af7-eaac-468d-ae2b-c988b38fac3b</wsa:MessageID><wsa:ReplyTo><wsa:Address>http://schemas.xmlsoap.org/ws/2004/03/addressing/role/anonymous</wsa:Address></wsa:ReplyTo><wsa:To>http://sgtcnpc48.cn.oracle.com:7777/ws/soap/MessagingService</wsa:To></soap:Header><soap:Body><send xmlns=\"http://www.oracle.com/beehive/2010/01/ws\" /></soap:Body></soap:Envelope>";
            //Attention! the url must not be ended with "/"
            String geturl = "http://scl58025.us.oracle.com:7777/my/user";
            String posturl =
                "http://scl58025.us.oracle.com:7777/ws/soap/MessagingService";
            hcu.setContentType("text/xml; charset=utf-8");
            /*
    hcu.setAccept("text/xml");

    hcu.setContentType("text/xml; charset=utf-8");

    hcu.addHttpHeader("SOAPAction", "");
    hcu.addHttpHeader("User-Agent", "AC");
    hcu.addHttpHeader("Content-Length", Integer.toString(soapEnvelope.length()));
    hcu.addHttpHeader("host", "sgtcnpc48.cn.oracle.com:7777");
   hcu.setContentType("text/xml; charset=utf-8");
   hcu.addHttpHeader("SOAPAction", "");



    String soapHeader = "";
    soapHeader = soapHeader + "POST "+"/ws/soap/MessagingService"+" HTTP/1.1\r\n";
    soapHeader = soapHeader + "Content-Type: text/xml; charset=utf-8"+"\r\n";
    soapHeader = soapHeader + "host:sgtcnpc48.cn.oracle.com:7777"+"\r\n";
    soapHeader = soapHeader + "Content-Length:"+soapEnvelope.length()+"\r\n";
    soapHeader = soapHeader + "Content-Type:"+"text/xml; charset=utf-8\r\n";
    soapHeader = soapHeader + "SOAPAction: \"\""+"\r\n";
    soapHeader = soapHeader + "User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; MS Web Services Client Protocol 1.1.4322.2032)\r\n";
    soapHeader = soapHeader + "Accept: *\r\n";
    soapHeader = soapHeader + "Connection: Keep-Alive\r\rn";
    soapHeader = soapHeader +"\r\n";
        */
            //soapEnvelope = soapHeader+soapEnvelope;

            //System.out.println(soapEnvelope);
            //hcu.post(url, soapHeader);
            String msgSubject = "subject-from-webservice";
            String textBody = "body-from-webservice";
            String mediaType = "text/plain";
            String contentId = "12345";
            String uploadScope = "12345";
            /*
        OrganizationUser ret = (OrganizationUser)hcu.get("my/user");
        EmailParticipant emlPcpt = new EmailParticipant();
            emlPcpt.setParticipantHandle(orgUser.getCollabId());
         */
            hcu.setAccept("application/xml");
            hcu.get("my/user");
            hcu.post(posturl, soapEnvelope);

            //hcu.get("my/user");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
