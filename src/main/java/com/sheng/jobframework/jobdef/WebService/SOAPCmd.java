package com.sheng.jobframework.jobdef.WebService;

import com.sheng.jobframework.jobdom.ACElement;

import java.util.Properties;


public class SOAPCmd extends ACElement {
    private String soapURI = "";
    private String soapParas = "";
    private String soapHeaders = "";
    private String soapType = "";
    private String soapName = "";
    private String soapXML = "";
    private Properties httpHeaders = new Properties();
    private Properties httpURLParas = new Properties();
    /*
     * for capture command, will extend in future
     */
    private String paraname = "";
    private String LB = "";
    private String RB = "";
    private int OCCURENCE = 0;
    //end specific attribute for capture command
    public static String CMD_TYPE_PUT = "PUT";
    public static String CMD_TYPE_GET = "GET";
    public static String CMD_TYPE_POST = "POST";
    public static String CMD_TYPE_DEL = "DELETE";
    public static String CMD_TYPE_PARA = "SAVEPARA";
    public static String CMD_TYPE_SOAP = "SOAP";

    public SOAPCmd() {
        super();
    }

    public void setName(String name) {
        soapName = name;
    }

    public String getName() {
        return soapName;
    }

    public void setBody(String body) {
        soapXML = body;
    }

    public String getBody() {
        return soapXML;
    }

    public void setURI(String uri) {
        soapURI = uri;
    }

    public String getURI() {

        if (soapURI.endsWith("/")) {
            soapURI = soapURI.substring(0, soapURI.length() - 1);
        }
        return soapURI;
    }

    public void addParas(String parakey, String paravalue) {
        httpURLParas.setProperty(parakey, paravalue);
    }

    public Properties getParas() {
        return httpURLParas;
    }

    public void addHeaders(String headername, String headervalue) {
        httpHeaders.setProperty(headername, headervalue);
    }

    public Properties getHeaders() {
        return httpHeaders;
    }

    public void setType(String type) {
        soapType = type;
    }

    public String getType() {
        return soapType;
    }
    /*
   * for capture CMD temporary, could be sub class in future
   */

    public void setParaname(String name) {
        paraname = name;
    }

    public String getParaname() {
        return paraname;
    }

    public void setLB(String lb) {
        LB = lb;
    }

    public String getLB() {
        return LB;
    }

    public void setRB(String rb) {
        RB = rb;
    }

    public String getRB() {
        return RB;
    }

    public void setOcurrence(String occurence) {

        OCCURENCE = Integer.parseInt(occurence);
    }

    public int getOcurrence() {
        return OCCURENCE;
    }
}
