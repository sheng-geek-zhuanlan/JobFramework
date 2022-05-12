

package com.sheng.jobframework.utility.HttpClient;

import java.util.Enumeration;
import java.util.Vector;

public class Rest2SoapMap {
    String restVerb;
    String restUri;
    String soapApi;
    String service;
    String restOutput;
    Vector<Rest2SoapParamMap> params;

    protected void printValues() throws Exception {
        System.out.println("REST: " + this.restVerb + " " + this.restUri +
                           " Output: " + this.restOutput);
        System.out.println("SOAP: " + this.service + "Service " +
                           this.soapApi);
        Enumeration e = this.params.elements();
        while (e.hasMoreElements()) {
            ((Rest2SoapParamMap)(e.nextElement())).printValues();
        }
    }
}

