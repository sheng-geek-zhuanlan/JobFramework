

package com.sheng.jobframework.utility.HttpClient;

public class Rest2SoapParamMap {
    String restParam;
    String restParamType;
    String soapParam;

    protected void printValues() throws Exception {
        System.out.println("REST Parameter: " + this.restParam + " " +
                           this.restParamType);
        System.out.println("SOAP Parameter: " + this.soapParam);
    }

}

