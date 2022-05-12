package com.sheng.jobframework;


import com.sheng.jobframework.utility.DataSubscriber;
import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.utility.OSCmdUtil;

import com.sheng.jobframework.annotation.ReportInfo;

import com.sheng.jobframework.jobdom.ACElement;

import com.sheng.jobframework.jobdef.ACTestConfig;

import com.sheng.jobframework.observer.FrameLogObserver;
import com.sheng.jobframework.observer.Log4jPrintStream;
import com.sheng.jobframework.observer.ObserverSubscriber;

import java.io.File;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;


public class Styler {
    public static boolean initstatus = false;
    private static String reportLang = ReportInfo.report_lang_en;
    public static boolean licensed = false;
    public static boolean rtcStyle = true;
    private static String timezone = "GMT+8:00";
    private static boolean forceTimezone = true;
    public static String jobfile = "TestJobFile.xml";
    public static String job_type = "JOB_TYPE";
    public static String MINI_JOBFILE = "TestJobFile_Mini.xml";
    public static String conffile = "config.xml";
    public static String jobhistoryDir = "jobHistory";
    public static Properties confProp = new Properties();
    public static String REGRESSION_TIME = "REGRESSION_TIME";
    public static String REGRESSION_CYCLE_MODE = "REGRESSION_CYCLE_MODE";
    public static String REGRESSION_RUN_HOST = "REGRESSION_RUN_HOST";
    public static String SCHEMA_VALID = "SCHEMA_VALID";
    public static String DTE_SEND_JOB = "DTE_SEND_JOB";
    public static String JOB_TO_RUN = "COMP";
    public static String FTP_BASE_PATH = "HTTP_BASE";
    public static String HOOK_ENABLE = "HOOK_ENABLE";
    //the time out for a remote job
    public static String DEFAULT_REMOTE_TIMEOUT = "DEFAULT_REMOTE_TIMEOUT";
    //the time out for a QTP running factory mode
    public static String TEST_TIMEOUT = "TEST_TIMEOUT";
    public static int TimeOut_TestRun = 18000;
    public static int JTCPServicePort = 9988;
    public static int LocalServicePort = 6688;
    public static int AgentPort = 30000;
    public static long WAIT_BETWEEN_JOB = 5000;
    public static long WAIT_BETWEEN_JOBA_WRAPP_JOBB = 3000;
    //the frequency is not so aften because there is possible synchronization of Q, linkedList.
    public static long FREQUENCY_CHECK_RUNNED_Q = 10000;
    public static long FREQUENCY_CHECK_TORUN_Q = 20000;
    public static long TIME_STAY_RUNNED_Q = 30000;
    public static long FREQUENCY_JOM_CHECKJOB = 2000;

    public Styler() {
    }

    public static void initialize() {
        if (!initstatus) {
            initLogStyle();
            initTZStyle();
            initPresentStyle();
            //initACStyle();
            initConfStyle();
            initHookStyle();
            //sleep(2000);
            initstatus = true;
        }
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("XXXX-Exception when sleep in the styler");
        }
    }

    public static void initConfStyle() {
        ACTestConfig configInfo = new ACTestConfig();
        configInfo.loadConfigSetting(conffile);
        confProp = configInfo.getConfigSetting();
    }

    public static void initLogStyle() {
        ObserverSubscriber subscriber = ObserverSubscriber.getInstance();
        Log4jPrintStream.getInstance();
        /*
        Log4jPrintStream.setObserver(subscriber);
        Log4jPrintStream.redirectSystemOut();
        */
        subscriber.subscribeObserver(FrameLogObserver.getInstance());
        //subscriber.subscribeObserver(consoleLogObserver.getInstance());

    }

    public static void initPresentStyle() {
        ReportInfo.setACStyle(reportLang, licensed);
    }

    public static void initTZStyle() {
        System.out.println("-----Current OS timezone is " +
                           System.getProperty("user.timezone"));
        if (System.getProperty("user.timezone").equalsIgnoreCase("") ||
            forceTimezone) {
            System.out.println("-----Current OS timezone is blank or forcedTimezone enabled, will be set to " +
                               timezone);
            System.setProperty("user.timezone", timezone);
            TimeZone.setDefault(TimeZone.getTimeZone(timezone));
            System.out.println("-----After set, OS timezone is " +
                               System.getProperty("user.timezone"));
        }
    }

    public static void initACStyle() {

        ACElement.setCommChannel(DataSubscriber.getInstance());
        ACElement.setObserver(ObserverSubscriber.getInstance());
        //JOM.setObserver( ObserverSubscriber.getInstance());
    }

    public static void initHookStyle() {
        if (confProp.getProperty(HOOK_ENABLE,
                                 "false").equalsIgnoreCase("false")) {
            System.out.println("---hook disabled--");
            return;
        }
        System.out.println("-----registering system.exit() hook");
        Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        System.out.println("!!!!-hook the system.exit event!");
                        String batFilePath =
                            FileUtil.getCurrentDir() + File.separator +
                            OSCmdUtil.getTestAntExecutable();
                        if (!FileUtil.isFileExists(batFilePath)) {
                            System.out.println("!!!!! the restart execute file missing" +
                                               batFilePath);
                            return;
                        }
                        Runtime.getRuntime().exec(batFilePath);
                        System.out.println("---restart framework " +
                                           batFilePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    public static void main(String[] args) {
        try {
            System.out.println(Locale.CHINA);
            Locale al = new Locale("fr", "CA");
            //Locale bl = Locale.forLanguageTag("ja-JP-u-ca-japanese");
            System.out.println(al.getDisplayName());
            System.out.println(al.toString());
            System.out.println(Locale.getDefault());
            //Locale.setDefault(al);
            System.out.println(al.getDisplayName());
            System.out.println(al.getLanguage());
            Locale al2 = new Locale("zh", "TW");
            String res = "oracle.bee.obio.res";
            ResourceBundle bundle = ResourceBundle.getBundle(res, al2);
            // ResourceBundle.


            /*
            ActiveXComponent qtpInstance = new ActiveXComponent("CLSID:4AC609CE-3B0B-4F12-BBAE-C90C7CD2DB08");
            Dispatch qtpApp = qtpInstance.getObject();
            Dispatch.call(qtpApp,"SetIPAndPort","127.0.0.1","4321");
            Dispatch.call(qtpApp,"SetIPAndPort","127.0.0.1","4321");
            */
            //Dispatch.call(qtpApp,"launch");
            //Dispatch.put(qtpApp,"visible",true);
            System.out.println("the library path is " +
                               System.getProperty("java.library.path"));
            /*
        String str_1 = MimeUtility.decodeText("=?gb2312?B?emhfQ063vbHjxPo/Zi56aF9DTre9sePE+j9n?=");
        String str_2 = MimeUtility.decodeText("=?gb2312?B?emhfQ063vbHjxPrUxGYuemhfQ063vbHjxPrUxGc=?=");
        System.out.println(str_1);
        System.out.println(str_2);
        */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
