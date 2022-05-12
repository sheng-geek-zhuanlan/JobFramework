package com.sheng.jobframework.jobdom;


import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;


//import oracle.ocs.qa.framework.junit.ConfigurationError;

public abstract class ACSeleniumJob extends ACUITestJob {

    public Selenium selenium;

    public ACSeleniumJob() {
    }

    public ACSeleniumJob(String str) {

    }

    public ACSeleniumJob(String str, long l) {

    }

    public String getCfgParam(String key) {
        //return getDataProperty(key);
        return getConfProperty(key);
    }

    public String getCfgParam(String key, String defaultvalue) {
        //return getDataProperty(key);
        String configvalue = getConfProperty(key, defaultvalue);
        return configvalue;

    }

    public void setUp(String url, String browser) {
        selenium = new DefaultSelenium("localhost", 4444, browser, url);
        selenium.start();
    }

    public void setUp(String host, int port, String url, String browser) {
        selenium = new DefaultSelenium(host, port, browser, url);
        selenium.start();
    }

    public void tearDown() throws Exception {
        selenium.stop();
    }

    public int getCfgIntParam(String key) {
        int res = -1;
        try {
            res = Integer.parseInt(getCfgParam(key));
        } catch (NumberFormatException nf_ex) {
        }
        return res;
    }

    public String getCustCfgParam(String paraname) {
        return getDataProperty(paraname);
    }

    public int getCustCfgIntParam(String paraname) {
        int res = -1;
        try {
            res = Integer.parseInt(getCustCfgParam(paraname));
        } catch (NumberFormatException nf_ex) {

        }

        return res;
    }
}
