package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.jobq.ProcessQ;
import com.sheng.jobframework.utility.OSCmdUtil;
import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.jobdom.ACElement;

import java.util.Enumeration;
import java.util.Properties;

import org.w3c.dom.Node;


public abstract class ACJobAppender extends ACElement {
    //MUST BE NOTED: if new class inherited from ACJobAppender, must added the class name here
    public static String APPENDER_CLASS_NAME_LIB = "framework.JobDef.ACLibSet";
    public static String APPENDER_CLASS_NAME_RUN = "framework.JobDef.ACRunSet";
    public static String APPENDER_CLASS_NAME_INPUT =
        "framework.JobDef.ACJobInput";
    public static String APPENDER_CLASS_NAME_OUTPUT =
        "framework.JobDef.ACJobOutput";
    public static String APPENDER_CLASS_NAME_DATA =
        "framework.JobDef.ACTestDataSet";
    public static String APPENDER_CLASS_NAME_ENV =
        "framework.JobDef.ACTestEnv";
    public static String APPENDER_CLASS_NAME_CONFIG =
        "framework.JobDef.ACTestConfig";
    public static String APPENDER_CLASS_NAME_CLASS =
        "framework.JobDef.ACJavaClassSet";
    public static String APPENDER_CLASS_NAME_ANT =
        "framework.JobDef.Ant.ACAntSet";
    //modified 2010-11-24, the testCase will be a global set for all jobEngine, java engine, web service engine, and so on
    //public static String APPENDER_CLASS_NAME_SEL="framework.JobDef.Selenium.ACSelCaseSet";
    public static String APPENDER_CLASS_NAME_SEL =
        "framework.JobDef.ACTestCaseSet";
    public static String APPENDER_CLASS_NAME_DEAMON =
        "framework.JobDef.Deamon.ACDeamonSet";
    public static String APPENDER_CLASS_NAME_JDBC =
        "framework.JobDef.JDBC.ACJDBCSet";
    public static String APPENDER_CLASS_NAME_PREKILL =
        "framework.JobDef.ACPreKillPro";
    public static String APPENDER_CLASS_NAME_WSSIMPLECMD =
        "framework.JobDef.WebService.SimpleSOAPCmdSet";
    public static String APPENDER_CLASS_NAME_SCRIPT =
        "framework.JobDef.Script.ACScriptSet";
    public static String APPENDER_CLASS_NAME_JMETER =
        "framework.JobDef.Jmeter.ACJmeterSet";
    public static String APPENDER_CLASS_NAME_PRESTATE =
        "framework.JobDef.ACPreState";
    public static String APPENDER_CLASS_NAME_POSTSTATE =
        "framework.JobDef.ACPostState";
    public static String APPENDER_CLASS_NAME_PERFDATA =
        "framework.JobDef.ACPerfCollection";
    public Properties prop = new Properties();
    private static String OS_DEFINED_PARAM_HOSTNAME = "#HOSTNAME#";
    private static String OS_DEFINED_PARAM_RESULTLOCATION = "#RESULTLOCATION#";

    public ACJobAppender() {
    }

    public abstract void initialize();

    public abstract void parseNode(Node node);

    public Properties getProp() {
        return prop;
    }

    public void selfParse() {
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String textvalue = prop.getProperty(strKey);
            if (textvalue.contains(OS_DEFINED_PARAM_HOSTNAME)) {
                textvalue =
                        textvalue.replaceAll(OS_DEFINED_PARAM_HOSTNAME, OSCmdUtil.getHostName());
                prop.put(strKey, textvalue);
            }
            if (textvalue.contains(OS_DEFINED_PARAM_RESULTLOCATION)) {
                String baselocation =
                    ProcessQ.getCurrentRunningJob().getLocationPath();
                textvalue =
                        textvalue.replaceAll(OS_DEFINED_PARAM_RESULTLOCATION,
                                             baselocation);
                prop.put(strKey, textvalue);
            }
        }
    }

    public void addMore(ACJobAppender objAppender) {
        //System.out.println("current properties list");
        //prop.list(System.out);
        Properties propToadd = objAppender.getProp();
        //System.out.println("new properties list");
        //prop.list(System.out);
        prop = Utility.prop1OveridedByProp2(prop, propToadd);
        //System.out.println("after merged properties list");
        //prop.list(System.out);
    }

    public void overridedBy(ACJobAppender appender) {
        addMore(appender);
    }

    public int getSize() {
        return prop.size();
    }

    public String getContent(String key) {
        return prop.getProperty(key, "");
    }

    public String parseAppenderVariableToRealValue(Properties channelProp) {
        //System.out.println("---------------in acjobappender parse chcannel");


        Properties dataFromChannel = channelProp;

        //dataFromChannel.list(System.out);
        //System.out.println("---------------in acjobappender parse chcannel");

        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String textvalue = prop.getProperty(strKey);
            //shining 2012-03-20, most text value store the path, here should reset the path to current OS format

            //end shining
            if (textvalue instanceof String) {
                //System.out.println("in acjobappender  to parse "+strKey+textvalue);
                Enumeration parakeys = dataFromChannel.keys();
                while (parakeys.hasMoreElements()) {
                    String paraName = (String)parakeys.nextElement();

                    if (textvalue.contains(paraName)) {
                        if (!dataFromChannel.getProperty(paraName).equalsIgnoreCase("")) {
                            textvalue =
                                    textvalue.replace(paraName, dataFromChannel.getProperty(paraName));
                            //System.out.println("in jobappenderParse1 :is to put key/value"+paraName+"="+textvalue);
                            //the overwrite purpose is like  path=$TESTVERSION/common/lib
                            //System.out.println("content has the paraname:"+paraName+" putting the new key value "+strKey+":"+textvalue);
                            prop.put(strKey, textvalue);
                        } else {
                            String errMsg =
                                "Error: the paraName <" + paraName +
                                "> is not ready in AC parameter channel!";
                            return errMsg;
                        }

                    } else if (strKey.equalsIgnoreCase(paraName) ||
                               ((strKey.startsWith("$") &&
                                 strKey.contains(paraName)))) {
                        if (!dataFromChannel.getProperty(paraName).equalsIgnoreCase("")) {
                            String paraValue =
                                dataFromChannel.getProperty(paraName);
                            //System.out.println("in jobappenderParse2 :is to put key/value"+paraName+"="+paraValue);
                            //format is $DATA=sssssss
                            // System.out.println("key has the paraname:"+paraName+" putting the new key value "+strKey+":"+paraValue);
                            prop.put(strKey, paraValue);
                        } else {
                            String errMsg =
                                "Warning: the paraName <" + paraName +
                                "> is not ready in AC parameter channel!";
                            System.out.println(errMsg);
                            return errMsg;
                        }
                    }

                }
            }

        }
        //System.out.println("after parsed, in acjobappender  to parse ");
        //prop.list(System.out);
        return "";
    }
}
