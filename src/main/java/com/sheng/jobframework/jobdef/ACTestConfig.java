package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.utility.OSCmdUtil;
import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.annotation.TestJobDOM;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class ACTestConfig extends ACJobAppender {
    //Properties configProp = new Properties();
    public static String confFileName = "";
    public ArrayList confFileArr = new ArrayList(100);

    public ACTestConfig() {
    }

    public boolean loadConfigSetting(String instanceFile) {
        Properties confProp = new Properties();
        //load the config xml from absolute path
        //instanceFile = FileUtil.getAbsolutePath(instanceFile);
        //System.out.println("load config info xml "+instanceFile);
        confProp = Utility.loadProperties(instanceFile);
        prop = Utility.mergeTwoProperties(prop, confProp);
        if (prop == null)
            return false;
        else
            //driverLogger.fatal("Driver logging: Fail to read instance info file!!!");
            //kickoffByAgent=false;
            return true;
    }

    public boolean loadSelConfigSetting(String instanceFile) {
        Properties confProp = new Properties();
        confProp = Utility.loadParaFile(instanceFile);
        prop = Utility.mergeTwoProperties(prop, confProp);
        //prop.list(System.out);
        if (prop == null)
            return false;
        else
            //driverLogger.fatal("Driver logging: Fail to read instance info file!!!");
            //kickoffByAgent=false;
            return true;
    }

    public boolean loadConfigSetting(String name, String instanceFile) {
        prop = Utility.loadProperties(instanceFile);
        Properties prop2 = new Properties();
        int paraNum = prop.size();
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String value = prop.getProperty(strKey);
            prop2.put(name + "." + strKey, value);
        }
        prop = Utility.mergeTwoProperties(prop, prop2);
        if (prop == null)
            return false;
        else
            //driverLogger.fatal("Driver logging: Fail to read instance info file!!!");
            //kickoffByAgent=false;
            return true;
    }

    public Properties getConfigSetting() {
        return prop;
    }

    public String getConfigPara(String paraname) {
        return getContent(paraname);
    }

    public void parseConfPropertyNode(Node node) {
        Element ele = (Element)node;
        String name = ele.getAttribute(TestJobDOM.node_attribute_name);
        String value = ele.getAttribute(TestJobDOM.node_attribute_value);
        addConfProperty(name, value);
    }

    public void addConfProperty(String key, String value) {
        prop.put(key, value);
    }

    public void initialize() {
        int isize = confFileArr.size();
        for (int i = 0; i < isize; i++) {
            String filepath = (String)confFileArr.get(i);
            if (filepath.startsWith("PARAMS")) {
                String[] parafile = filepath.split("##");
                filepath = parafile[1];
                loadSelConfigSetting(filepath);
            } else {
                //shining 2012-03-21
                filepath = OSCmdUtil.pathReSettle2OS(filepath);
                //end
                loadConfigSetting(filepath);
            }

        }
    }

    public void addConfSetting(ACTestConfig conf) {
        Properties prop2 = conf.getConfigSetting();
        int paraNum = prop2.size();
        Enumeration keys = prop2.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            prop.put(strKey, prop2.getProperty(strKey));
        }
    }

    public void parseNode(Node node) {

    }

    public void parseConfFileNode(Node node) {

        Element ele = (Element)node;
        String envfilepath =
            ele.getAttribute(TestJobDOM.node_attribute_location);
        String name = ele.getAttribute(TestJobDOM.node_attribute_name);
        try {
            //comment loadConfigSetting at 2011-06-28. make the config file loaded at run time, not at the loadJOM phase, this is done for remote job.
            //loadConfigSetting(envfilepath);
            confFileName = name;
            confFileArr.add(envfilepath);
        } catch (Exception e) {
            outputFrameLog("XXXX-Eexception when parse config file " +
                           envfilepath + " with name: " + name);
            e.printStackTrace();
        }
    }

    public void parseSelConfFileNode(Node node) {
        Element ele = (Element)node;
        String envfilepath =
            ele.getAttribute(TestJobDOM.node_attribute_location);
        String name = ele.getAttribute(TestJobDOM.node_attribute_name);
        try {
            //loadSelConfigSetting(envfilepath);
            if (!name.equalsIgnoreCase("")) {
                confFileName = name;
                confFileArr.add("PARAMS##" + envfilepath);
            }
        } catch (Exception e) {
            outputFrameLog("XXXX-Exception when parse param file " +
                           envfilepath + " with name: " + name);
        }
    }

    public String getConFileName() {
        return confFileName;
    }
}
