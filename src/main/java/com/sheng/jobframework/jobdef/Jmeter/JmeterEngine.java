package com.sheng.jobframework.jobdef.Jmeter;

import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.utility.Utility;
import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;
import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.Styler;
import com.sheng.jobframework.types.JOMAttach;

import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobEngine;
import com.sheng.jobframework.jobdef.ACRunSet;
import com.sheng.jobframework.jobdef.ACTestConfig;
import com.sheng.jobframework.jobdef.ACTestEnv;
import com.sheng.jobframework.jobdef.Jmeter.utils.JMX;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.jmeter.JMeterDriver;


public class JmeterEngine extends ACJobEngine {
    public static String LIB_BASE =
        "common" + File.separator + "lib" + File.separator + "perl";
    public static String IMPORT_FILE_NAME = "import.txt";
    public static String JMETER_HOME = "JMeter_Home";
    public static String JTL_XSL_NAME = "jmeter-results-detail-report_21.xsl";
    public static String MIN_TIME = "MIN_TIME";
    public static String MAX_TIME = "MAX_TIME";
    public static String AVG_TIME = "AVG_TIME";
    public static String PASS_TRANS = "PASS_TRANS";
    public static String FAIL_TRANS = "FAIL_TRANS";
    public static String TOTAL_THREADS = "THREADS RUN";
    public static String PER_LOOPS = "LOOPS RUN";
    public static String PERCENT_PASS = "PASS%";
    public static String PERCENT_FAIL = "FAIL%";
    public static String TOTAL_TRANS = "TOTAL_TRANS";
    public static String JMETER_DEFINED_THREADS = "num_threads";
    public static String JMETER_DEFINED_LOOPS = "loops";
    public static String JMETER_DEFINED_RAMP_TIME = "ramp_time";
    public static String JMETER_TEMPLATE_JMX = "Template.jmx";

    public int startsCounterOfServiceData = 0;
    public int endCounterOfServiceData = 0;
    public String JMXPath = "";
    public int threads = 0;
    public int loops = 0;
    public int rampup = 0;
    public int rampdown = 0;
    public int ramp_time = 60;
    public String JTLResultPath = "";
    public String JLog = "";
    public JMX jmxUtil;

    public JmeterEngine() {
    }

    public boolean initJmeterParas() {
        try {
            ACTestEnv testenv =
                (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
            ACJmeterSet jmeterset =
                (ACJmeterSet)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_JMETER);
            ACRunSet runset =
                (ACRunSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_RUN);
            String class2run = "";
            JMXPath = jmeterset.getJMXPath();
            ArrayList runArr = runset.getRunSetArr();
            if (runArr.size() > 1) {
                outputFrameLog("XXXX-more than 2 <run> appender defined in JMeter Job: " +
                               runArr.size());
                return false;
            } else if (runArr.size() == 0) {
                if (JMXPath.equalsIgnoreCase("")) {
                    outputFrameLog("XXXX-no <run> defined and no <JMX> tag defined ");
                    return false;
                }
            } else {
                class2run = (String)runArr.get(0);
            }


            Properties envProp = testenv.getEnvSetting();
            String location = getLocationPath();
            String newJmxPath = location + File.separator + "ac_jmeter.jmx";


            if (envProp.getProperty(TestJobDOM.node_tag_JMX.toUpperCase(),
                                    "").equalsIgnoreCase("")) {

                if (JMXPath.equalsIgnoreCase("")) {
                    String defaultJMXPath =
                        JMETER_HOME + File.separator + JMETER_TEMPLATE_JMX;
                    outputFrameLog("****-the JMX file defined in <JMX> Appender: does not exist: " +
                                   JMXPath + " will use default JMXPath: " +
                                   defaultJMXPath);
                    JMXPath = defaultJMXPath;
                }
                if (!FileUtil.isFileExists(JMXPath)) {
                    outputFrameLog("XXXX-the JMX file does not exist: " +
                                   JMXPath);
                    return false;
                }
                FileUtil.copyFile(JMXPath, newJmxPath, false);
                JMXPath = newJmxPath;
            } else {
                JMXPath =
                        envProp.getProperty(TestJobDOM.node_tag_JMX.toUpperCase());
                if (!FileUtil.isFileExists(JMXPath)) {
                    outputFrameLog("XXXX-the JMX file defined in env property <JMX>: does not exist: " +
                                   JMXPath);
                    return false;
                }
            }
            outputFrameLog("---loading JMX file " + JMXPath);

            jmxUtil = new JMX(JMXPath);
            /*
        System.out.println("num_threads:" + jmxUtil.getThreadGroupProp("num_threads"));
        System.out.println("loops:" + jmxUtil.getThreadGroupProp("loops"));
        System.out.println("JavaSamplerClass:" + jmxUtil.getJavaSamplerClass());*/

            if (envProp.getProperty(TestJobDOM.node_tag_threads.toUpperCase(),
                                    "").equalsIgnoreCase("")) {
                threads = jmeterset.getThreads();
            } else {
                threads =
                        Integer.parseInt(envProp.getProperty(TestJobDOM.node_tag_threads.toUpperCase()));
                //System.out.println("000debug, the threads read from env is "+threads);

            }
            String strThreads = Integer.toString(threads);
            // System.out.println("000debug, the threads finnaly is "+strThreads);
            jmxUtil.setThreadGroupProp(JMETER_DEFINED_THREADS, strThreads);
            if (envProp.getProperty(TestJobDOM.node_tag_loops.toUpperCase(),
                                    "").equalsIgnoreCase("")) {
                loops = jmeterset.getLoops();
            } else {
                loops =
                        Integer.parseInt(envProp.getProperty(TestJobDOM.node_tag_loops.toUpperCase()));
            }
            if (loops == 0) {
                //if loops parameter not set, will use default 1
                outputFrameLog("****-loops set to 0, will use default 1");
                loops = 1;
            }
            String strLoops = Integer.toString(loops);
            jmxUtil.setThreadGroupProp(JMETER_DEFINED_LOOPS, strLoops);
            if (!class2run.equalsIgnoreCase("")) {
                jmxUtil.setJavaSamplerClass(class2run);
            }
            if (envProp.getProperty(TestJobDOM.node_tag_rampup.toUpperCase(),
                                    "").equalsIgnoreCase("")) {
                rampup = jmeterset.getRampup();
            } else {
                rampup =
                        Integer.parseInt(envProp.getProperty((TestJobDOM.node_tag_rampup.toUpperCase())));
            }

            if (envProp.getProperty(TestJobDOM.node_tag_rampdown.toUpperCase(),
                                    "").equalsIgnoreCase("")) {
                rampdown = jmeterset.getRampdown();
            } else {
                rampdown =
                        Integer.parseInt(envProp.getProperty((TestJobDOM.node_tag_rampdown.toUpperCase())));
            }
            if (envProp.getProperty(TestJobDOM.node_tag_ramptime.toUpperCase(),
                                    "").equalsIgnoreCase("")) {
                ramp_time = jmeterset.getRamptime();
            } else {
                ramp_time =
                        Integer.parseInt(envProp.getProperty((TestJobDOM.node_tag_ramptime.toUpperCase())));
            }
            String strRamp_time = Integer.toString(ramp_time);
            jmxUtil.setThreadGroupProp(JMETER_DEFINED_RAMP_TIME, strRamp_time);
            jmxUtil.save();
            if (JMXPath.equalsIgnoreCase("") || threads == 0) {
                outputFrameLog("XXXX-failed to intiliaze Jmeter Paras due JMXPath: " +
                               JMXPath + " threads: " + threads);
                return false;
            }
            //copy the xsl to results directory
            String xslPath =
                FileUtil.getAbsolutePath(JMETER_HOME + File.separator + "xsl" +
                                         File.separator + JTL_XSL_NAME);
            if (FileUtil.isFileExists(xslPath)) {
                //copy the xsl to results dir
                String newXSLPath =
                    location + File.separator + "xsl" + File.separator +
                    JTL_XSL_NAME;
                FileUtil.copyFile(xslPath, newXSLPath, false);
            } else {
                outputFrameLog("XXXX-WARNING: jtl xsl file missing at path: " +
                               xslPath);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void runEntityJob() {
        if (!initJmeterParas()) {
            JOM.restruct2StatusJob(this, "FAIL_INIT_PARAMETERS",
                                   "failed to init jmeter job parameters",
                                   TestResult.FAIL);
            return;
        }
        try {
            ACTestEnv testenv =
                (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
            Properties envProp = testenv.getEnvSetting();

            TestJobElement jmeterjob = new TestJobElement(TestJobType.CASE);
            jmeterjob.setName(getName());
            jmeterjob.setBeginTime(System.currentTimeMillis());
            addChildJob(jmeterjob);
            jmeterjob.setBeginTime(System.currentTimeMillis());
            //Build up the JMeter command
            //TODO, pass these args from config file

            //Properties argProp = runset.getProp();
            TestJobElement scripTestJob = new TestJobElement(TestJobType.TEST);


            scripTestJob.setName("Jmeter_test_job");
            jmeterjob.addChildJob(scripTestJob);


            scripTestJob.setBeginTime(System.currentTimeMillis());
            String location = getLocationPath();
            String JTLResultPath = location + File.separator + "ac_jmeter.jtl";
            String JLog = location + File.separator + "ac_jmeter.log";
            outputFrameLog("-----JMeter Engine is to run JMX file " + JMXPath);
            outputFrameLog("-----JMeter Engine JTL Path is set to  " +
                           JTLResultPath);
            outputFrameLog("-----JMeter Engine JLog patch is set to  " + JLog);

            String[] run_commandArgs =
            { "-n", "-t", JMXPath, "-l", JTLResultPath, "-j", JLog };
            String[] commandArgs =
                new String[run_commandArgs.length + envProp.size() * 2];
            //initialize the command args
            int i = 0;
            for (; i < run_commandArgs.length; i++) {
                String vector = run_commandArgs[i];
                commandArgs[i] = vector;
            }
            Enumeration keys = envProp.keys();
            while (keys.hasMoreElements()) {
                String strKey = (String)keys.nextElement();
                String value = envProp.getProperty(strKey);
                commandArgs[i] = "-J";
                commandArgs[i + 1] = strKey + "=" + value;
                i++;
                i++;
            }
            //
            System.out.println(Arrays.asList(commandArgs));
            System.setProperty("jmeter.home", JMETER_HOME);
            outputFrameLog("JMeter Driver begin to start...");
            //NewDriver JMeterDriver = new JMeterDriver();
            JMeterDriver.start(commandArgs);

            Thread.sleep(2000);
            TestJobElement steps = new TestJobElement(TestJobType.STEP);
            steps.addProperty("done", "script task is being executed!");
            scripTestJob.addChildJob(steps);
            TestJobElement step2 = new TestJobElement(TestJobType.STEP);
            step2.addProperty("done",
                              "concurrent starting" + threads + " threads with " +
                              loops);
            scripTestJob.addChildJob(step2);
            ACTestConfig testconf =
                (ACTestConfig)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
            String testTimeOut = testconf.getConfigPara(Styler.TEST_TIMEOUT);
            int timeoutSecs =
                Utility.strToInt(testTimeOut, Styler.TimeOut_TestRun);
            //int timeoutSecs = 3600;
            if (!waitUntilFinished(JTLResultPath, timeoutSecs)) {
                outputFrameLog("XXXX-Jmeter job time out with " + timeoutSecs +
                               " seconds!");
                JOM.restruct2StatusJob(this, "JMETER_JOB_TIMEOUT",
                                       "Jmeter job " + timeoutSecs +
                                       " time out ", TestResult.FAIL);
                return;
            }
            scripTestJob.setEndTime(System.currentTimeMillis());
            jmeterjob.setEndTime(System.currentTimeMillis());

            if (FileUtil.isFileExists(JTLResultPath)) {
                JmeterResultParser jra = new JmeterResultParser(JTLResultPath);
                //to be integrated with Jmeter JOM model report
                System.out.println("minTime of the transaction: " +
                                   jra.getMinTime());
                System.out.println("avgTime of the transaction: " +
                                   jra.getAvgTime());
                System.out.println("maxTime of the transaction: " +
                                   jra.getMaxTime());
                System.out.println("suceessful transaction: " +
                                   jra.getSuccessSampleCount());
                System.out.println("failure transaction: " +
                                   jra.getFailureSampleCount());
                JOMAttach attach = new JOMAttach();
                attach.setJobType(getDriverType());
                if (getDriverType().equalsIgnoreCase("")) {
                    outputFrameLog("XXXX-Error when try to get DriverType in Jmeter engine, will set to " +
                                   TestJobDOM.job_engine_jmeter);
                    attach.setJobType(TestJobDOM.job_engine_jmeter);
                }
                int total_threads =
                    Integer.parseInt(jmxUtil.getThreadGroupProp(JMETER_DEFINED_THREADS));
                int total_loops =
                    Integer.parseInt(jmxUtil.getThreadGroupProp(JMETER_DEFINED_LOOPS));
                int total_trans = total_threads * total_loops;
                int pass_trans = jra.getSuccessSampleCount();
                int fail_trans = jra.getFailureSampleCount();
                String percent_pass = "N/A";
                String percent_fail = "N/A";
                if (total_trans != 0) {
                    percent_pass =
                            "%" + Integer.toString(100 * pass_trans / total_trans);
                    percent_fail =
                            "%" + Integer.toString(100 * fail_trans / total_trans);
                }


                attach.addJobProp(MIN_TIME,
                                  Integer.toString(jra.getMinTime()));
                attach.addJobProp(MAX_TIME,
                                  Integer.toString(jra.getMaxTime()));
                attach.addJobProp(AVG_TIME,
                                  Integer.toString(jra.getAvgTime()));
                attach.addJobProp(PASS_TRANS, Integer.toString(pass_trans));
                attach.addJobProp(FAIL_TRANS, Integer.toString(fail_trans));
                attach.addJobProp(TOTAL_THREADS,
                                  Integer.toString(total_threads));
                attach.addJobProp(PER_LOOPS, Integer.toString(total_loops));
                attach.addJobProp(TOTAL_TRANS, Integer.toString(total_trans));
                attach.addJobProp(PERCENT_PASS, percent_pass);
                attach.addJobProp(PERCENT_FAIL, percent_fail);
                attachOwnJOM(attach);
                scripTestJob.setResults(TestResult.PASS);
            } else {
                outputFrameLog("XXXX-jtl not found at path " + JTLResultPath);
                JOM.restruct2StatusJob(this, "JMETER_JTL_NOTFOUND",
                                       "jtl file is missing", TestResult.FAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOM.restruct2StatusJob(this, "FAIL_RUN_JMETER_JOB",
                                   "failed to run jmeter job",
                                   TestResult.FAIL);
        }
    }

    public boolean waitUntilFinished(String jtlPath, int timeoutSecs) {
        try {
            boolean runflag = true;
            int itimeout = timeoutSecs;
            int toWaitTime = (itimeout) * 1000;
            ACElement.outputLog("Use " + timeoutSecs +
                      "s time out as default, Will wait for " + itimeout +
                      " seconds");
            long startTimeInMillis = Utility.getCurrentTimeInMillis();
            long currentTimeInMillis = Utility.getCurrentTimeInMillis();
            while (currentTimeInMillis < startTimeInMillis + toWaitTime) {
                if (JMeterDriver.isFinished(jtlPath)) {
                    return true;
                } else {
                    //refresh current job, since it has been replaced by the remote job when returned, while current job will be set to finished status when remote job returned(in ACJobEngine), once retunred, current
                    //job will be replaced by remoted job, current job will be a lonely node, so it is to be refreshed.
                    Thread.sleep(2000);
                    currentTimeInMillis = Utility.getCurrentTimeInMillis();
                }
            }
            return false;
        } catch (Exception e) {
            ACElement.outputLog("XXXXX-Exception when query job running status cycle");
            e.printStackTrace();
            return false;
        }

    }
    /*
     * potential risk: if the script output log or pass/fail with same key, then the key will be overwritten with only ONE pamater remained finnaly in the data
     */

    public void parseScriptJobs(TestJobElement testjob) {
        Properties channelProp = ACElement.subscriber.getServiceSubscribedDataProp();
        if (endCounterOfServiceData > startsCounterOfServiceData) {
            outputFrameLog("There has some service data to be parsed...");
            Enumeration keys = channelProp.keys();
            int i = 0;
            while (keys.hasMoreElements()) {
                String strKey = (String)keys.nextElement();
                i++;
                //System.out.println(i+" the key is "+strKey);
                if (i > startsCounterOfServiceData) {
                    String value = channelProp.getProperty(strKey);
                    TestJobElement steps =
                        new TestJobElement(TestJobType.STEP);
                    if (strKey.contains("pass")) {
                        if (!testjob.getResults().equalsIgnoreCase(TestResult.FAIL)) {
                            testjob.setResults(TestResult.PASS);
                            steps.addProperty(JobStatus.PASSED, value);
                        }
                    } else if (strKey.contains("fail")) {
                        testjob.setResults(TestResult.FAIL);
                        steps.addProperty(JobStatus.FAILED, value);
                    } else {
                        steps.addProperty(strKey, value);
                    }
                    testjob.addChildJob(steps);
                }
            }

        }

    }
}
