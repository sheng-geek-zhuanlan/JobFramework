package com.sheng.jobframework.utility.HttpClient;

import com.sheng.jobframework.jobdom.ACElement;

import java.io.InputStream;


public class HttpServerResponse extends ACElement {
    public HttpServerResponse() {
        super();
    }
    InputStream stream = null;

    /** Get  response payload as InputStream
     * @return stream returned by server
     */
    public InputStream getStream() {
        return XmlJsonUtils.getStreamFromString(payloadStr);
    }

    String payloadStr = null;

    /** Get response payload in String format
     * @return response payload in String format
     */
    public String getResponsePayloadAsString() {
        return payloadStr;
    }

    int returnCode = -1;

    /** Get return code
     * @return code returned by server */
    public int getReturnCode() {
        return (returnCode == -1 ? exceptionCode : returnCode);
    }

    Throwable exceptionCause = null;

    /** Get exception cause (if any)
     * @return exception cause returned by server */
    public Throwable getExceptionCause() {
        return exceptionCause;
    }

    int exceptionCode = -1;

    /** Get exception code (default is -1)
     * @return exception code returned by server */
    public int getExceptionCode() {
        return exceptionCode;
    }

    String exceptionMessage = null;

    /** Get exception message (if any)
     * @return exception message returned by server */
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    String exceptionResponseMessage = null;

    /** Get exception response message (if any)
     * @return exception response message returned by server */
    public String getExceptionResponseMessage() {
        return exceptionResponseMessage;
    }

    String url = null;

    /** Get URL actually sent to server
     * @return actual url */
    public String getURL() {
        return url;
    }

    String payload = null;

    /** Get request payload actually sent to server
     * @return actual payload */
    public String getPayload() {
        return payload;
    }

    public RestClientUtils rcu = null;
    public HttpClientUtils hcu = null;
    String repsonseContentType = "";

    public RestClientUtils getRCU() {
        return rcu;
    }

    public static void main(String[] args) {
        HttpServerResponse httpServerResponse = new HttpServerResponse();
    }
}
