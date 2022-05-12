package com.sheng.jobframework.jobdef.WebService;


import com.sheng.jobframework.jobdom.ACJavaJob;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACTestConfig;

import java.util.Hashtable;


public class ACWSJob extends ACJavaJob {
    public ACWSJob() {
        super();
    }
    //for web service constructor

    public ACWSJob(String name) {
        super();
    }

    public ACWSJob(String name, long timeout) {
        super();
    }
    //end for web service

    public void Init() {

    }

    public void Run() {

    }

    public void End() {

    }


    public String getCfgParam(String key) {
        //return getDataProperty(key);
        if (getConfProperty(key).equalsIgnoreCase("")) {
            if (key.equalsIgnoreCase("wls.host")) {
                String server_fromAC = getEnvProperty("SERVER");
                String[] servername = server_fromAC.split("\\.");
                return servername[0];
            } else if (key.equalsIgnoreCase("host.fqdn")) {
                String server_fromAC = getEnvProperty("SERVER");
                int i = server_fromAC.indexOf(".");
                return server_fromAC.substring(i + 1);
            } else if (key.equalsIgnoreCase("wls.ohs.port")) {
                return getEnvProperty("WEBDAV_PORT");
            } else
                return getEnvProperty(key);
        } else
            return getConfProperty(key);
    }
    //steup and teardown is for oracle selenium jobs

    protected void setUp() throws Exception {
    }

    public void setUp(String url, String browser) {

    }

    public void setUp(String host, int port, String url, String browser) {

    }
    //shining modified for radvision soultion, from protected to public

    public void tearDown() throws Exception {


    }
    //for oracle only

    public Hashtable<String, String> getAllParams() {
        ACTestConfig testconf =
            (ACTestConfig)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
        Hashtable hash = (Hashtable)testconf.getConfigSetting();
        return hash;
    }

    public int getCfgIntParam(String key) {
        int res = 0;
        try {
            res = Integer.parseInt(getCfgParam(key));
        } catch (NumberFormatException nf_ex) {
        }
        return res;
    }

    public void fail(String msg) {
        reportFail(msg);

    }

    public void wait(int isecs) {
        long ms = isecs * 1000;
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            outputFrameLog("Exception when calling thread.sleep....");
            e.printStackTrace();
        }

    }

    public void pass(String msg) {
        reportPass(msg);

    }

    public void log(String msg) {
        outputFrameLog(msg);

    }

    public String getCustCfgParam(String paraname) {
        return getDataProperty(paraname);
    }

    public boolean getCfgBoolParam(String key) {
        String value = getConfProperty(key);
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        } else {
            outputFrameLog("XXXX- the config value for " + key +
                           " is not boolean: " + value +
                           " will return false as default!");
            return false;
        }
    }

    public int getCustCfgIntParam(String paraname) {
        int res = 0;
        try {
            res = Integer.parseInt(getCustCfgParam(paraname));
        } catch (NumberFormatException nf_ex) {

        }

        return res;
    }
}
