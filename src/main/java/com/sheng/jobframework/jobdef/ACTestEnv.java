package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.utility.DataSubscriber;
import com.sheng.jobframework.utility.OSCmdUtil;
import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.annotation.TestJobDOM;

import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class ACTestEnv extends ACJobAppender {
    // Properties envProp = new Properties();
    public static String envFileName = "";
    public ArrayList propFileArr = new ArrayList(100);

    public ACTestEnv() {
    }

    public boolean loadEnvSetting(String instanceFile) {
        Properties envProp = new Properties();
        //load the instanceinfoxml from absolute path
        //instanceFile = FileUtil.getAbsolutePath(instanceFile);
        //System.out.println("load instrance info xml "+instanceFile);
        envProp = Utility.loadProperties(instanceFile);
        envProp.put(TestJobDOM.DEFINED_ENV_KEY_TESTLANG,
                    Utility.getSystemInfo().getProperty("userLocale"));

        //envProp.put("jobID","kickOffByLocal");
        prop = Utility.mergeTwoProperties(prop, envProp);
        //envProp.put("driverType",driverType);
        if (prop == null)
            return false;
        else
            return true;
    }

    public boolean loadEnvSetting(String name, String instanceFile) {
        prop = Utility.loadProperties(instanceFile);
        prop.put(TestJobDOM.DEFINED_ENV_KEY_TESTLANG,
                 Utility.getSystemInfo().getProperty("userLocale"));
        //prop.put("jobID","kickOffByLocal");
        Properties prop2 = new Properties();
        int paraNum = prop.size();
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String value = prop.getProperty(strKey);
            prop2.put(name + "." + strKey, value);
        }
        //prop2 will override prop1
        prop = Utility.mergeTwoProperties(prop, prop2);
        if (prop == null)
            return false;
        else
            return true;
    }

    public void addEnvSetting(ACTestEnv env) {
        Properties prop2 = env.getEnvSetting();
        int paraNum = prop2.size();
        Enumeration keys = prop2.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            prop.put(strKey, prop2.getProperty(strKey));
        }
    }

    public void addEnvSetting(DataSubscriber subscriber) {
        Properties prop2 = subscriber.getServiceSubscribedDataProp();
        int paraNum = prop2.size();
        Enumeration keys = prop2.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            //changed 2011-05-12, GDC will overwrite local env, exclude "COMP", every job need to have his own COMP
            //if(!prop.containsKey(strKey)){
            if (!strKey.equalsIgnoreCase("COMP"))
                prop.put(strKey, prop2.getProperty(strKey));
            //}

        }
    }

    public void parseDTECommand(String cmdList, String jobid,
                                String driverType) {
        //will write the DTE command to global data channel to make it as a global data, then the envrionment will rehresh the job's appenders in jobPrepare()
        //omitted, if two jobs are crossed, will modify the GDC with conflicted
        outputFrameLog("---the cmdList got from agent is " + cmdList);
        Properties prop2 = new Properties();
        String[] cmdArr = cmdList.split(";");
        int arrSize = cmdArr.length;
        for (int i = 0; i < arrSize; i++) {
            String[] propArr = cmdArr[i].split("=");
            String key = "";
            String value = "";
            if (propArr.length < 2) {
                System.out.println("XXXX-Blank property with key:" +
                                   propArr[0]);
                continue;
            }
            key = propArr[0];
            int k = cmdArr[i].indexOf("=");

            key = key.toUpperCase();
            //value=propArr[1];
            value = cmdArr[i].substring(k + 1);
            if (propArr[1].equalsIgnoreCase("none") ||
                propArr[1].equalsIgnoreCase("80")) {
                value = "";
            }
            //write the command into comm channel
            //writeIntoComChannel(key,value);
            prop2.put(key, value);
        }
        prop = prop2;
        prop.put(TestJobDOM.DEFINED_ENV_KEY_TESTLANG,
                 Utility.getSystemInfo().getProperty("userLocale"));
        //prop.put("jobID",jobid);
        prop.put(TestJobDOM.DEFINED_ENV_KEY_DRIVERTYPE, driverType);
    }

    public Properties getEnvSetting() {
        return prop;
    }

    public void outputToPropFile(String filePath) {
        try {
            FileOutputStream os = new FileOutputStream(filePath);
            prop.storeToXML(os,
                            "this is a env record for round of automation run");
        } catch (Exception e) {
            outputFrameLog("XXX--Exception thrown when outputPropFile to path: " +
                           filePath);
            e.printStackTrace();
        }

    }

    public void addEnvProperty(String key, String value) {
        prop.put(key, value);
    }

    public String getEnvProperty(String key) {
        return prop.getProperty(key, "");
        /*
        int paraNum = envProp.size();
         Enumeration keys = envProp.keys();
         while (keys.hasMoreElements()){
             String strKey = (String)keys.nextElement();
             if(strKey.equalsIgnoreCase(key)){
                 return envProp.getProperty(strKey);
             }
         }
         return "";
    */
    }

    public void parseNode(Node node) {

    }

    public void parseEnvFileNode(Node node) {

        Element ele = (Element)node;
        String name = ele.getAttribute(TestJobDOM.node_attribute_name);
        //System.out.println("in ACTestEnv the file name is  "+name);

        String envfilepath =
            ele.getAttribute(TestJobDOM.node_attribute_location);
        try {
            //comment loadEnvSetting at 2011-06-28. make the env file loaded at run time, not at the loadJOM phase, this is done for remote job.
            //comment it back, since COMP parameter is needed before JOB starts
            //loadEnvSetting(envfilepath);
            envFileName = name;
            propFileArr.add(envfilepath);
        } catch (Exception e) {
            outputFrameLog("XXXX-Exception when try to parse env file " +
                           envfilepath + " with name: " + name);
            e.printStackTrace();
        }
        //System.out.println("in ACTestEnv the envFileName is "+envFileName);
        //System.out.println("in ACTestEnv the get envFileName is "+getEnvFileName());
        //Test
        //Properties prop2 = getEnvSetting();
        //prop.list(System.out);
    }

    public String getEnvFileName() {
        return envFileName;
    }

    public void parseEnvPropertyNode(Node node) {
        Element ele = (Element)node;
        String name = ele.getAttribute(TestJobDOM.node_attribute_name);
        String value = ele.getAttribute(TestJobDOM.node_attribute_value);
        addEnvProperty(name, value);
    }

    public void initialize() {
        int isize = propFileArr.size();
        for (int i = 0; i < isize; i++) {
            String filepath = (String)propFileArr.get(i);
            //shining 2012-03-21
            filepath = OSCmdUtil.pathReSettle2OS(filepath);
            //end
            loadEnvSetting(filepath);
        }
    }
}
