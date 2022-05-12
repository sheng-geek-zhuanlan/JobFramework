package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.utility.DataSubscriber;

import com.sheng.jobframework.jobdom.ACElement;

import java.util.ArrayList;
import java.util.Properties;


public class ACJobAppendController extends ACElement {
    ArrayList appenderList = new ArrayList(100);

    public ACJobAppendController() {
    }

    public ArrayList getAppenderList() {
        return appenderList;
    }

    public void addJobAppender(ACJobAppender append) {
        appenderList.add(append);
    }

    public void removeJobAppender(String appenderType) {
        int isize = appenderList.size();
        for (int i = 0; i < isize; i++) {
            ACJobAppender appender = (ACJobAppender)appenderList.get(i);
            if (appender.getClass().getName().equalsIgnoreCase(appenderType)) {
                appenderList.remove(appender);
                return;
            }
        }
    }

    public int getCtrlListSize() {
        return appenderList.size();
    }

    public ACJobAppender getAppenderByIndex(int i) {
        return (ACJobAppender)appenderList.get(i);
    }

    public ACJobAppender getAppenderInstance(String appenderType) {
        int isize = appenderList.size();
        for (int i = 0; i < isize; i++) {
            ACJobAppender appender = (ACJobAppender)appenderList.get(i);
            if (appender.getClass().getName().equalsIgnoreCase(appenderType)) {
                return appender;
            }
        }
        //this was useful if the appender was a new created
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            ACJobAppender appender =
                (ACJobAppender)loader.loadClass(appenderType).newInstance();
            appenderList.add(appender);
            return appender;
        } catch (Exception e) {
            outputFrameLog("Error: Exception when trying to load class" +
                           appenderType + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void parseAllAppenderToRealValue(DataSubscriber channel) {
        ACTestEnv testenv =
            (ACTestEnv)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        ACTestConfig testconf =
            (ACTestConfig)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
        //ACJobOutput testoutput = (ACJobOutput)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_OUTPUT);
        //Properties outputProp = testoutput.getProp();
        Properties envProp = testenv.getEnvSetting();
        String envFileName = testenv.getEnvFileName();
        //System.out.println("in ACJobAppenderCtrl the envFileName is "+envFileName);
        envProp = ACJobParaParser.formatProp2Para(envFileName, envProp);
        //envProp.list(System.out);
        Properties confProp = testconf.getConfigSetting();
        String confFileName = testconf.getConFileName();
        //System.out.println("in ACJobAppenderCtrl the confFileName is "+confFileName);
        confProp = ACJobParaParser.formatProp2Para(confFileName, confProp);
        //confProp.list(System.out);
        Properties channelProp = channel.getServiceSubscribedDataProp();
        int isize = appenderList.size();
        for (int i = 0; i < isize; i++) {
            ACJobAppender appender = (ACJobAppender)appenderList.get(i);
            //System.out.println("overwriting env");
            appender.parseAppenderVariableToRealValue(envProp);
            //System.out.println("overwriting conf");
            appender.parseAppenderVariableToRealValue(confProp);
            //System.out.println("overwriting channel");
            appender.parseAppenderVariableToRealValue(channelProp);
            //appender.parseAppenderVariableToRealValue(outputProp);
        }
        //shining : global data channel will be outputted to testenv
        testenv.addEnvSetting(channel);
    }

    public void initializeAllAppender() {
        int isize = appenderList.size();
        for (int i = 0; i < isize; i++) {
            ACJobAppender appender = (ACJobAppender)appenderList.get(i);
            //shining added for #HOSTNAME# 04-12
            appender.selfParse();
            //end of shining
            appender.initialize();
        }
    }

    public void overridedBy(ACJobAppendController appendCtrl) {
        int isize = appendCtrl.getCtrlListSize();
        for (int i = 0; i < isize; i++) {
            ACJobAppender parentAppender =
                (ACJobAppender)appendCtrl.getAppenderByIndex(i);
            String parentAppenderType = parentAppender.getClass().getName();
            /*NOTES: now only three types of appender will be inherit from parent job
             * 1. ACLibSet
             * 2. ACDataSet
             * 3. ACTestEnv
             * 4. ACTestConfig
             */
            if ((parentAppenderType.equalsIgnoreCase(ACJobAppender.APPENDER_CLASS_NAME_INPUT)) ||
                (parentAppenderType.equalsIgnoreCase(ACJobAppender.APPENDER_CLASS_NAME_CONFIG)) ||
                (parentAppenderType.equalsIgnoreCase(ACJobAppender.APPENDER_CLASS_NAME_DATA)) ||
                (parentAppenderType.equalsIgnoreCase(ACJobAppender.APPENDER_CLASS_NAME_LIB) ||
                 (parentAppenderType.equalsIgnoreCase(ACJobAppender.APPENDER_CLASS_NAME_PRESTATE)) ||
                 (parentAppenderType.equalsIgnoreCase(ACJobAppender.APPENDER_CLASS_NAME_POSTSTATE)) ||
                 (parentAppenderType.equalsIgnoreCase(ACJobAppender.APPENDER_CLASS_NAME_ENV)))) {
                ACJobAppender currentAppender =
                    getAppenderInstance(parentAppenderType);
                currentAppender.addMore(parentAppender);
            }


        }
    }

}
