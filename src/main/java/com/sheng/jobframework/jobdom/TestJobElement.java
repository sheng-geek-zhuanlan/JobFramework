package com.sheng.jobframework.jobdom;


import com.sheng.jobframework.Scheduler;
import com.sheng.jobframework.Styler;
import com.sheng.jobframework.types.JOMAttach;
import com.sheng.jobframework.types.RouteTableStack;
import com.sheng.jobframework.utility.DataSubscriber;
import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.utility.OSCmdUtil;
import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.annotation.DriverType;
import com.sheng.jobframework.annotation.JobProperties;
import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;

import com.sheng.jobframework.jobdef.ACJobAppendController;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobInput;
import com.sheng.jobframework.jobdef.ACJobOutput;
import com.sheng.jobframework.jobdef.ACLibSet;
import com.sheng.jobframework.jobdef.ACPerfCollection;
import com.sheng.jobframework.jobdef.ACRunSet;
import com.sheng.jobframework.jobdef.ACTestConfig;
import com.sheng.jobframework.jobdef.ACTestDataSet;
import com.sheng.jobframework.jobdef.ACTestEnv;

import com.sheng.jobframework.observer.FrameLogObserver;

import java.io.File;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.w3c.dom.Node;





public class TestJobElement extends TreeElement implements Cloneable {

    //following is the static defined attributes

    public static boolean debugMode = false;
    //following is append objecto to job
    public static ACTestEnv envInfo = new ACTestEnv();
    public static ACTestConfig configInfo = null;
    public ACJobAppendController appenderCtrl = new ACJobAppendController();
    private ArrayList dependencyArr;
    public Scheduler scheduler;

    //some staus or config variable
    //Notes: don't change the max_rerunNum, it will affect the remote job, since the remote job will be only runned once, after RUNNED, the job will lost host info, since it has been trimed before sent to remote host.
    public static int max_rerunNum = 1;
    public boolean isRunnable = false;
    public boolean isFactoryMode = false;
    public boolean isDeamonJob = false;
    public boolean isRunned = false;
    public boolean isRunning = false;

    private String myElementType = "";
    private String myJobType;
    //ACDriver drv;
    public long begintimeL = 0;
    public long endtimeL = 0;
    public int rerunNum = 0;

    public RouteTableStack routeStack = new RouteTableStack();
    private JOMAttach ownAttach = null;

    public TestJobElement() {
        //myElementType=TestJobType.SUITE;
        constructTestJob();
    }

    /**
     * @param type
     */
    public void setElementType(String type) {
        elementProp.put(TestJobDOM.DEFINED_ELEMENT_TYPE, type);
        myElementType = type;
        constructTestJob();
    }

    public void constructTestJob() {
        dependencyArr = new ArrayList(1000);

    }

    /**
     * @param serviceDataSubscriber
     * @param
     */
    public TestJobElement(DataSubscriber serviceDataSubscriber) {
        subscriber = serviceDataSubscriber;
        constructTestJob();
    }

    public void attachOwnJOM(JOMAttach attachJOM) {
        ownAttach = attachJOM;
    }

    public JOMAttach getOwnJOM() {
        return ownAttach;
    }

    public ACJobAppendController getAppenderCtrl() {
        return appenderCtrl;
    }

    public static void setDebugMode(boolean mode) {
        debugMode = mode;
    }

    public void setScheduler(Scheduler schedule) {
        scheduler = schedule;
        scheduler.setRunJob(this);
    }

    public void runJob() {

        scheduler.run();
    }

    public TestJobElement(String type) {
        setElementType(type);
        myElementType = type;
        constructTestJob();

        if (type.equalsIgnoreCase(TestJobType.TEST)) {
            elementProp.put("testlogpath", "log.txt");
            elementProp.put("screenpath", "screenshot");
        }
    }

    public void addLibSet(ACLibSet lib) {
        //libset.addLib(lib);
    }

    public Properties getTestEnvProp() {
        ACTestEnv testenv =
            (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        return testenv.getEnvSetting();
    }

    public Properties getTestPerfProp() {
        ACPerfCollection testperf =
            (ACPerfCollection)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_PERFDATA);
        return testperf.getProp();
    }

    public Properties getTestConfigProp() {
        ACTestConfig testconfig =
            (ACTestConfig)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
        return testconfig.getConfigSetting();
    }

    public String getCurrTestIndicator() {
        return getEnvProperty(TestJobDOM.DEFINED_VALUE_INDICATOR);
    }

    public void addTestEnv(ACTestEnv env) {
        //envInfo.addEnvSetting(env);
        ACTestEnv testenv =
            (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        testenv.addEnvSetting(env);
    }

    public void addTestConf(ACTestConfig conf) {
        //envInfo.addEnvSetting(env);
        ACTestConfig testconf =
            (ACTestConfig)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
        testconf.addConfSetting(conf);
    }

    public String getEnvProperty(String key) {
        //return envInfo.getEnvProperty(key);
        ACTestEnv testenv =
            (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        return testenv.getEnvProperty(key);
    }

    public String getEnvProperty(String key, String defaultEnv) {
        //return envInfo.getEnvProperty(key);
        ACTestEnv testenv =
            (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        if (testenv.getEnvProperty(key).equalsIgnoreCase(""))
            return defaultEnv;
        return testenv.getEnvProperty(key);
    }

    public void addEnvProperty(String key, String value) {
        //return envInfo.getEnvProperty(key);
        ACTestEnv testenv =
            (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        testenv.addEnvProperty(key, value);
    }
    //shining added to extract the env to global Data channel

    public void extractEnvProperty2GDC() {
        //return envInfo.getEnvProperty(key);
        ACTestEnv testenv =
            (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        Properties envProp = testenv.getEnvSetting();
        Enumeration parakeys = envProp.keys();
        outputFrameLog("---Begin to Extract the environment props to GDC--------");
        while (parakeys.hasMoreElements()) {
            String key = (String)parakeys.nextElement();
            String value = envProp.getProperty(key);
            //temp workaround here:
            if (key.equalsIgnoreCase("CONF_PRESENT_CONTENT") ||
                key.equalsIgnoreCase("SLIDE_PAGE_ID")) {
                //nothing to do to write into global data channel
            } else {
                writeIntoComChannel(key, value);
            }


        }
        outputFrameLog("---End of Extract the environment props to GDC--------");
    }
    /*
     * shining modified 0701, to get data from global data channel, not from jobinput. if jobinput is not defined. then will get nothing from jobinput
     */

    public String getInputValue(String key) {

        Properties prop = subscriber.getServiceSubscribedDataProp();
        return prop.getProperty(key, "");
    }

    public ArrayList getInputValueArr(String key) {

        Properties prop = subscriber.getServiceSubscribedDataProp();
        Enumeration parakeys = prop.keys();
        ArrayList paraArr = new ArrayList(100);
        int i = 0;
        while (parakeys.hasMoreElements()) {
            String fullName = (String)parakeys.nextElement();
            if (fullName.contains(key)) {
                paraArr.add(prop.getProperty(fullName));
                i++;
            }
        }
        //outputInfo(jobinput.getProp());
        return paraArr;
    }

    public String getConfProperty(String key) {
        //return envInfo.getEnvProperty(key);
        ACTestConfig testconf =
            (ACTestConfig)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
        return testconf.getConfigPara(key);
    }

    public String getConfProperty(String key, String defaultvalue) {
        //return envInfo.getEnvProperty(key);
        ACTestConfig testconf =
            (ACTestConfig)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
        String configvalue = testconf.getConfigPara(key);
        if (configvalue.equalsIgnoreCase("")) {
            return defaultvalue;
        } else {
            return configvalue;
        }
    }

    public String getDataProperty(String key) {
        //return envInfo.getEnvProperty(key);
        ACTestDataSet testdataset =
            (ACTestDataSet)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
        //System.out.println("in testjobelement: the data size is "+testdataset.getDataHashArr().size());
        return testdataset.getData(key);
    }

    /*
     * add at 2010-12-01
     */
    //user could add data property during job running

    public void addData(String key, String value) {
        //return envInfo.getEnvProperty(key);
        ACTestDataSet testdataset =
            (ACTestDataSet)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
        testdataset.addData(key, value);
    }
    /* moidified 2010-12-23
     * to sperate appderctl.initialize with checkdependency
     *
     */

    public boolean jobPrepare() {
        outputFrameLog(LOGMSG.JOB_INIT_DEPENDENCY + getName());
        if ((getDriverType().equalsIgnoreCase(DriverType.DRV_QTP)) &&
            (isFactoryMode == true)) {
            //if QTP factory mode is enabled, QTP case will has his own log, will not rewrite it.
            outputJobLog(FrameLogObserver.NONEED_LOG);
        } else {
            outputJobLog(getLocationPath());
        }
        if (rerunNum >= max_rerunNum) {
            outputFrameLog("XXXX-has excceed the max re run number! will cancel the job rerunning");
            return false;
        }
        ArrayList dependArr = getDependencyArr();
        int size = dependArr.size();
        TestJobElement parent = getParentJob();
        for (int i = 0; i < size; i++) {
            String name = (String)dependArr.get(i);
            outputFrameLog(LOGMSG.JOB_INIT_CHECKING_DEPENDENCY + name);
            TestJobElement jobToRun =
                (TestJobElement)getBrotherNodeByName(name);
            if (jobToRun == null) {
                outputFrameLog("Error: dependency job <" + name +
                               "> could not be found in job file, please correct it!");
                continue;
            }
            //isRunning is set to true only when it is a remote job run.
            outputFrameLog("----is to check the status of dependency job <" +
                           jobToRun.getName() + "> ");
            //here to call
            JOM.checkAndWaitRemoteJobFinished(jobToRun);
            //outputFrameLog("*****first time 1 to check the dependency job status "+jobToRun.getResults());
            jobToRun =
                    (TestJobElement)parent.getChildNodeByName(jobToRun.getName());
            //jobToRun.isRunned=true;
            // outputFrameLog("*****first time 2 to check the dependency job status "+jobToRun.getResults());
            if ((!jobToRun.getResults().equalsIgnoreCase(TestResult.PASS)) &&
                (jobToRun.rerunNum < jobToRun.max_rerunNum) &&
                (!jobToRun.getResults().equalsIgnoreCase(TestResult.CNR))) {
                outputFrameLog("--------- <" + name +
                               "> will be run since it's status is " +
                               jobToRun.getResults());
                jobToRun.setToRunMark(true);
                //jobToRun.setJobStatus(JobStatus.RUNNING);
                Scheduler newscheduler = new Scheduler();
                newscheduler.setRunJob(jobToRun);
                newscheduler.run();
                //it is a dependency job, after run completed, setToRunMark to false so that it will not be runned when scheduler meet it in next loop.
                jobToRun.setToRunMark(false);
                try {
                    Thread.sleep(Styler.WAIT_BETWEEN_JOB);
                } catch (Exception e) {
                    outputFrameLog("XXXX-Thread sleep excetion:" +
                                   e.getMessage());
                    e.printStackTrace();
                }
            }
            JOM.checkAndWaitRemoteJobFinished(jobToRun);
            //outputFrameLog("*****second time 1 to check the dependency job status "+jobToRun.getResults());
            jobToRun =
                    (TestJobElement)parent.getChildNodeByName(jobToRun.getName());
            //jobToRun.isRunned=true;
            //outputFrameLog("*****second time 2 to check the dependency job status "+jobToRun.getResults());
            if (!jobToRun.getResults().equalsIgnoreCase(TestResult.PASS)) {
                outputFrameLog(" <" + getName() + " >'s depedency Job <" +
                               jobToRun.getName() +
                               "> NOT succeed to run with results " +
                               jobToRun.getResults());
                return false;
            }
            outputFrameLog("-----Run depedency Job " + jobToRun.getName() +
                           " with status PASS");
            //testJob.setInputFromOutput(jobToRun);
        }
        //put overide here, to make sure all the appender value will be refreshed according to GDC, while is is un suitable to do in jobini(), since we still want to keep the local job has his own values
        //second considerations, if put jobPrepare(), still could not make sure DTE command will overwrite the local file. move to jobInit()
        //third considerations, still need a call here, to make sure the env propfile path to be refreshed when do initialzie
        appenderCtrl.parseAllAppenderToRealValue(subscriber);
        return true;
    }

    public boolean jobInit() {
        if ((getDriverType().equalsIgnoreCase(DriverType.DRV_QTP)) &&
            (isFactoryMode == true)) {
            //if QTP factory mode is enabled, QTP case will has his own log, will not rewrite it.
            outputJobLog(FrameLogObserver.NONEED_LOG);
        } else {
            String locationPath = getLocationPath();
            locationPath = OSCmdUtil.pathReSettle2OS(locationPath);
            outputJobLog(locationPath);
            System.out.println("outputJobLog is " + locationPath);
        }


        outputFrameLog(LOGMSG.JOB_INIT_BEGIN + getName());
        outputFrameLog(LOGMSG.JOB_INIT_DEPENDENCY_DONE + getName());
        outputFrameLog(LOGMSG.JOB_INIT_APPENDERS + getName());

        appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_PERFDATA);
        appenderCtrl.initializeAllAppender();
        outputFrameLog(LOGMSG.JOB_INIT_APPENDERSD_DONE + getName());
        outputFrameLog(LOGMSG.JOB_INIT_DONE + getName());
        //set begin time
        setBeginTime(Utility.getCurrentTimeInMillis());
        setJobStatus(JobStatus.RUNNING);

        ACTestEnv testenv =
            (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        testenv.addEnvSetting(subscriber);
        ACTestConfig config =
            (ACTestConfig)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
        ACTestEnv env =
            (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        outputFrameLog("-------------job <" + getName() +
                       "> initializing------------");
        initToRunList();
        outputFrameLog("-------------job  <" + getName() +
                       ">initialized completed------------");


        recuSetAppenderValues(this);
        //end
        return true;

    }

    public void recuSetAppenderValues(TestJobElement job) {
        if (job.ifHasChildNodes()) {
            int childNumbers = job.getChildNodesNum();
            for (int i = 0; i < childNumbers; i++) {
                TestJobElement childJob =
                    (TestJobElement)job.getChildNodesByIndex(i);
                childJob.appenderCtrl.overridedBy(job.appenderCtrl);
                //outputFrameLog("try to set "+childJob.getName()+"to result"+status);
                recuSetAppenderValues(childJob);
            }
        }
    }

    public void initToRunList() {
        ACTestEnv env =
            (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        String compToRun = env.getEnvProperty(Styler.JOB_TO_RUN);
        int compSize = getChildNodesNum();
        //System.out.println("in shceduler.InitJob(): the chid job size is "+compSize);
        for (int i = 0; i < compSize; i++) {
            TestJobElement compJob = (TestJobElement)getChildNodesByIndex(i);
            if (!compToRun.equalsIgnoreCase("")) {
                if (compToRun.equalsIgnoreCase("ALL")) {
                    compJob.setToRunMark(true);
                } else {
                    compJob.setToRunMark(false);
                    if (compJob.isDeamonJob)
                        compJob.setToRunMark(true);
                    String[] arrtoRun = compToRun.split(",");
                    int iSize = arrtoRun.length;
                    for (int k = 0; k < iSize; k++) {
                        //if(compJob.getName().equalsIgnoreCase(arrtoRun[k])){
                        String compname = compJob.getName().toUpperCase();
                        String comptorun = arrtoRun[k].toUpperCase();
                        // if(compJob.getName().contains(arrtoRun[k])){
                        if (compname.contains(comptorun)) {
                            compJob.setToRunMark(true);
                        }
                    }
                }
            } else
                compJob.setToRunMark(true);
        }
    }

    public boolean jobEnd() {
        autoSetResults();
        outputFrameLog(LOGMSG.JOB_END_DONE + getName() + " with status " +
                       getResults());
        setEndTime(Utility.getCurrentTimeInMillis());
        setJobStatus(JobStatus.FINISH);
        rerunNum++;
        isRunned = true;

        //collect run host information
        collectHostInfo();
        System.gc();
        //unload class used before

        return true;
    }

    public void collectHostInfo() {
        try {

            //JobUtil.attachDebugInfo(this);
            InetAddress ia = InetAddress.getLocalHost();
            String host = ia.getHostName();
            String IP = ia.getHostAddress();
            addProperty(JobProperties.HOSTNAME, host);

            String g_strTester =
                Utility.getSystemInfo().getProperty("userName");
            String screenpath =
                FileUtil.getAbsolutePath(getRelativeLocationPath() +
                                         File.separator + "screenshot");
            screenpath = OSCmdUtil.pathReSettle2OS(screenpath);
            String logpath = FileUtil.getAbsolutePath("framework.log");
            logpath = OSCmdUtil.pathReSettle2OS(logpath);
            String testlogpath =
                FileUtil.getAbsolutePath(getRelativeLocationPath() +
                                         File.separator + "log.txt");
            testlogpath = OSCmdUtil.pathReSettle2OS(testlogpath);

            addProperty(JobProperties.IP, IP);
            //addProperty("OSNAME",)
            addProperty(JobProperties.SCREENPATH, screenpath);
            addProperty(JobProperties.FRAMELOGPATH, logpath);
            addProperty(JobProperties.TESTLOGPATH, testlogpath);
            addProperty(JobProperties.TESTUSER, g_strTester);

            //added 2011-04-18, to make job Stauts Finished at JobEnd();


            //in case it was a script user, maybe the script output value by socket, so it is need to store the output of global data channel back to  output object, this way is MUST when it was a remoted job
            //appenderCtrl.parseAllAppenderToRealValue(subscriber);
            // moved to processQ.compactJob()
            /*
        ACJobOutput joboutput = (ACJobOutput)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_OUTPUT);
        Properties channelProp = subscriber.getServiceSubscribedDataProp();
        Enumeration keys = channelProp.keys();
        while (keys.hasMoreElements()){
            String strKey = (String)keys.nextElement();
            String textvalue = channelProp.getProperty(strKey);
            joboutput.setJobOutput(strKey,textvalue);
        }
      */
            //end of reading global data channel
        } catch (Exception e) {
            outputFrameLog("Exception occurs in jobEnd()");
            //e.printStackTrace();
        }
    }

    public void setToRunMark(boolean mark) {
        if (mark)
            elementProp.put("isToRun", "true");
        else
            elementProp.put("isToRun", "false");

    }

    public String getLogPrefix() {
        return getName() + FrameLogObserver.destinationJobPrefix;
    }

    public boolean isToRun() {
        if (getProperty("isToRun").equalsIgnoreCase("") ||
            getProperty("isToRun").equalsIgnoreCase("true"))
            return true;
        else if (getProperty("isToRun").equalsIgnoreCase("false"))
            return false;
        else
            return true;

    }
    /*
    private void setInputFromDataSubscriber(){
        Properties prop=subscriber.getServiceSubscribedDataProp();
        int paraNum = prop.size();
        //outputFrameLog("the data service has size "+paraNum);
        //TO DO: only add the values which input requires, not added all value in datasubscriber
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()){
            String strKey = (String)keys.nextElement();
            //outputFrameLog("put into input key is : "+strKey+prop.getProperty(strKey));
            inputProp.setProperty(strKey,prop.getProperty(strKey));
        }
    }

    private boolean verifyInputReady(){
        setInputFromDataSubscriber();
        Properties prop=inputProp;
        int paraNum = prop.size();
        boolean ifAllInputReady = true;
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()){
            String strKey = (String)keys.nextElement();
            if(inputProp.getProperty(strKey).equalsIgnoreCase("")){
                ifAllInputReady=false;
                outputFrameLog("Job <"+getName()+"> has input "+strKey+" with null");
                return ifAllInputReady;
            }
            //outputFrameLog("Job <"+getName()+"> has input "+strKey+" "+inputProp.getProperty(strKey));
            envInfo.addEnvProperty(strKey,inputProp.getProperty(strKey));
        }
        return ifAllInputReady;
    }
    private boolean verifyOutputReady(){
        Properties prop=outputProp;
        int paraNum = prop.size();
        boolean ifAllOutputReady = true;
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()){
            String strKey = (String)keys.nextElement();
            if(inputProp.getProperty(strKey).equalsIgnoreCase("")){
                ifAllOutputReady=false;
                outputFrameLog("Job <"+getName()+"> has output "+strKey+" with null");
                return ifAllOutputReady;
            }
        }
        return ifAllOutputReady;
    }
   */

    public void autoSetResults() {

        TestJobElement testJob = this;
        if (getParent() == null) {
            outputFrameLog("----Root job <" + testJob.getName() + ">-----");
            return;
        }
        //outputFrameLog(getElementType()+" job <"+getName()+"> has status with "+getResults());
        if (testJob.getResults().equalsIgnoreCase("") ||
            testJob.getResults().equalsIgnoreCase(TestResult.NA)) {
            int size = testJob.getChildNodesNum();
            for (int i = 0; i < size; i++) {
                TestJobElement childJob =
                    (TestJobElement)testJob.getChildNodesByIndex(i);
                childJob.autoSetResults();
                //testJob.getChildNodesByIndex(i).autoSetResults();
                //outputFrameLog(testJob.getElementType()+" job <"+testJob.getName()+"> set status with "+testJob.getChildResults());
                testJob.setResults(testJob.getChildResults());
            }
        } else {
            TestJobElement parentjob = (TestJobElement)getParent();
            parentjob.setResults(parentjob.getChildResults());
        }
    }


    /*
    public void setInputFromOutput(TestJobElement job){
        Properties prop = job.getOutputProp();
        int paraNum = prop.size();
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()){
            String strKey = (String)keys.nextElement();
            if((!prop.getProperty(strKey).equalsIgnoreCase(""))&&(inputProp.containsKey(strKey))){
                inputProp.setProperty(strKey,prop.getProperty(strKey));
            }else if(prop.getProperty(strKey).equalsIgnoreCase("")){
                outputFrameLog("Test Job <"+job.getName()+"> has null output value of "+strKey+"!");
            }

        }
    }
    */

    public String getChildResults() {
        String results = TestResult.NA;
        int iChild = getChildNodesNum();
        int iPass = 0;
        int iCNR = 0;
        for (int i = 0; i < iChild; i++) {
            TestJobElement testJob = (TestJobElement)getChildNodesByIndex(i);
            //outputFrameLog("the "+ testJob.getElementType()+"job "+testJob.getName()+" has status "+testJob.getResults());
            if (testJob.getResults().equalsIgnoreCase(TestResult.FAIL)) {
                results = TestResult.FAIL;
                return results;
            } else if (testJob.getResults().equalsIgnoreCase(TestResult.CNR)) {
                results = TestResult.CNR;
                iCNR++;
                // return results;
            } else if (testJob.getResults().equalsIgnoreCase(TestResult.PASS)) {
                iPass++;
            }
        }
        if (iPass == iChild) {
            return TestResult.PASS;
        } else if (iCNR == iChild) {
            return TestResult.CNR;
        } else {
            return TestResult.NA;
        }
    }

    public TestJobElement clone() {
        TestJobElement job = new TestJobElement();
        try {
            //TO DO: Notes the clone should be tested and verified
            job = (TestJobElement)super.clone();
            job.elementNode = (ArrayList)elementNode.clone();
            job.elementProp = (Properties)elementProp.clone();
        } catch (Exception e) {
            outputFrameLog("TestJobElement <" + getName() +
                           "> did not support clone exception!");
            e.printStackTrace();
        }
        return job;

    }

    public TestJobElement cloneOnlyProp() {
        TestJobElement job = new TestJobElement();
        try {
            //TO DO: Notes the clone should be tested and verified
            job = (TestJobElement)super.clone();
            job.elementNode = (ArrayList)elementNode.clone();
            job.elementProp = (Properties)elementProp.clone();
            job.emptyAllChildNodes();
        } catch (Exception e) {
            outputFrameLog("TestJobElement <" + getName() +
                           "> did not support clone exception!");
            e.printStackTrace();
        }
        return job;

    }

    public String getGlobalProperty(String key) {
        TestJobElement rootJob = new TestJobElement("suite");
        TestJobElement tempJob = this;
        TestJobElement currentJob = this;
        while (rootJob != null) {
            rootJob = (TestJobElement)tempJob.parentJob;
            currentJob = tempJob;
            tempJob = (TestJobElement)tempJob.parentJob;
        }
        return currentJob.getProperty(key);
    }

    public String getParentProperty(String key) {
        return parentJob.getProperty(key);
    }

    public void setDriverType(String value) {
        /*
      if((!value.equalsIgnoreCase(DriverType.DRV_JUNIT))&&(!value.equalsIgnoreCase(DriverType.DRV_QTP))&&(!value.equalsIgnoreCase(DriverType.DRV_SEL))&&(!value.equalsIgnoreCase(DriverType.DRV_JAVA))){
          outputFrameLog("Error: The driver type <"+value+"> is not supported by AC Framework!");
      }
      else
*/
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_DRVTYPE, value);
        //elementProp.put("DriverType",value);
    }

    public void setTestLang(String value) {
        //elementProp.put("lang",value);
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_LANG, value);
    }

    public String getTestLang() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_LANG);
        //return getProperty("lang");
    }

    public void setRunningRemoteHost(String value) {
        //elementProp.put("lang",value);
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_TOHOST, value);
    }

    public String getRunningRemoteHost() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_TOHOST);
        //return getProperty("lang");
    }

    public void setFromHost(String value) {
        //elementProp.put("lang",value);
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_FROMHOST, value);
    }

    public String getFromHost() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_FROMHOST);
        //return getProperty("lang");
    }

    public void setRemoteRunning(boolean bl) {
        //elementProp.put("lang",value);
        if (bl)
            elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_REMOTERUN, "true");
        else
            elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_REMOTERUN, "false");
    }

    public void setBeginTime(String value) {
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_BTIME, value);
        //elementProp.put("btime",value);
    }

    public void setEndTime(String value) {
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_ETIME, value);
        //elementProp.put("etime",value);
    }

    public String getBeginTime() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_BTIME);
        //return getProperty("btime");
    }

    public String getEndTime() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_ETIME);
        //return getProperty("etime");
    }

    public void setEndTime(long value) {
        endtimeL = value;
    }

    public void setBeginTime(long value) {
        begintimeL = value;
    }

    public long getEndTimeL() {
        return endtimeL;
    }

    public long getBeginTimeL() {
        return begintimeL;
    }
    //added 2010-10-12 to add status/job id/host for job queue controlling

    public void setJobStatus(String value) {
        //if(getJobStatus().equalsIgnoreCase("")){
        //outputFrameLog("---setting job <"+getName()+"> status to "+value+" old status is "+getJobStatus());
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_JOBSTATUS, value);
        //}
        //modified 2011-04-15 to make all child jobs will be changed
        if (value.equalsIgnoreCase(JobStatus.FINISH)) {
            if (ifHasChildNodes()) {
                int childNumbers = getChildNodesNum();
                for (int i = 0; i < childNumbers; i++) {
                    TestJobElement childJob =
                        (TestJobElement)getChildNodesByIndex(i);
                    childJob.setJobStatus(value);
                }
            }
        }
    }

    public String getJobStatus() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_JOBSTATUS);
    }

    public void setJobID(String value) {
        ACTestEnv thisEnv =
            (ACTestEnv)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        thisEnv.addEnvProperty(TestJobDOM.DEFINED_ATTRIBUTE_JOBID, value);
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_JOBID, value);
    }

    public String getJobID() {
        if (getProperty(TestJobDOM.DEFINED_ATTRIBUTE_JOBID).equalsIgnoreCase("")) {
            return getEnvProperty(TestJobDOM.DEFINED_ATTRIBUTE_JOBID);
        } else {
            return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_JOBID);
        }

    }

    public void setChildJobToRun(String value) {
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_COMP, value);
    }

    public String getChildJobToRun() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_COMP);
    }

    public void pushRoutePath(String srcHost, String destHost) {
        routeStack.pushRouteTable(srcHost, destHost);
    }

    public String popCurrentSrcHost() {
        return routeStack.popCurrentSrcHost();
    }

    public String popCurrentDestHost() {
        return routeStack.popCurrentDestHost();
    }
    //end added 2010-10-12

    public void setLocationPath(String value) {
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_RESULTSLOCATION, value);
        //elementProp.put("path",value);
    }

    public String getLocationPath() {
        String relativeLocationPath =
            getProperty(TestJobDOM.DEFINED_ATTRIBUTE_RESULTSLOCATION);
        String absoluteLocationPath = "";
        if (!relativeLocationPath.equalsIgnoreCase("")) {
            absoluteLocationPath =
                    FileUtil.getAbsolutePath(relativeLocationPath);
        }
        //shining add for 05-21 to fix the dir problem on iOS
        absoluteLocationPath = OSCmdUtil.pathReSettle2OS(absoluteLocationPath);
        //shining end
        return absoluteLocationPath;
        //return getProperty("path");
    }

    public String getRelativeLocationPath() {
        String relativeLocationPath =
            getProperty(TestJobDOM.DEFINED_ATTRIBUTE_RESULTSLOCATION);
        return relativeLocationPath;
        //return getProperty("path");
    }
    /*
   * the screenLocationPath is something like results/timestamp/jobname/screenshot/testcasename/testname/xxxx.jpg
   */

    public String generateScreenLocation() {

        //ArrayList nameArr = new ArrayList(100);
        //TestJobElement rootJob = new TestJobElement();
        TestJobElement tempJob = this;
        TestJobElement currentJob = this;
        //nameArr.add(currentJob.getName());
        String screenShotLocation = currentJob.getName();
        boolean screenshotdirGen = false;
        while (tempJob.getParentJob() != null) {
            //nameArr.add(currentJob.getName());
            //rootJob = (TestJobElement)tempJob.parentJob;
            //currentJob = tempJob;
            tempJob = (TestJobElement)tempJob.parentJob;

            //  if(!tempJob.getDriverType().equalsIgnoreCase("")&&!tempJob.getDriverType().equalsIgnoreCase(TestJobDOM.node_tag_testjob)&&!screenshotdirGen){
            /*
               * this should be componentJob like <QTP>,<Jemmy>, ensure the screenshot dir is located under componentJob
               */
            //System.out.println(tempJob.getName()+"--"+tempJob.getDriverType());
            //   screenShotLocation = TestJobDOM.screen_path_dir+File.separator+screenShotLocation;
            //   screenshotdirGen = true;
            // }

            if (tempJob.getParentJob() != null) {
                /*
                 * will ignore the root job name appears in the location path
                 */
                screenShotLocation =
                        tempJob.getName() + File.separator + screenShotLocation;
            }

            //nameArr.add(tempJob.getName());
        }

        /*
         * now temp job is the root job.
         */
        String location = tempJob.getLocationPath();
        screenShotLocation = location + File.separator + screenShotLocation;
        //System.out.println("generate screenpath here: "+screenShotLocation);
        setScreenPath(screenShotLocation);
        return screenShotLocation;
        /*
        int isize = nameArr.size();
        if(isize<=1){
            return location;
        }else{
            for(int i=isize-2;i>=0;i--){
                String name = (String)nameArr.get(i);
                if(i==isize-2){
                    location = location + File.separator +name+File.separator +TestJobDOM.screen_path_dir;
                }else{
                    location = location+File.separator +name;
                }

            }
        }
       return location;
        */
        //return getProperty("path");
    }

    public void setScreenPath(String value) {
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_SCREENSHOTLOCATION,
                        value);
        //elementProp.put("screenpath",value);
    }

    public String getScreenPath() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_SCREENSHOTLOCATION);
        //return getProperty("screenpath");
    }

    public void setTestLogPath(String value) {
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_LOGLOCATION, value);
        //elementProp.put("testlogpath",value);
    }

    public String getTestLogPath() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_LOGLOCATION);
        //return getProperty("testlogpath");
    }

    public String getElementType() {
        //return myElementType;
        return getProperty(TestJobDOM.DEFINED_ELEMENT_TYPE);
    }

    public String getDesc() {
        //return getProperty("desc");
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_DESCRIPTION);
    }

    public String getLib() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_LIB);
        //return getProperty("lib");
    }

    public String getTestData() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_DATA);
        //return getProperty("data");
    }

    public void setDesciption(String value) {
        //elementProp.put("desc",value);
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_DESCRIPTION, value);
    }

    public void setResults(String value) {
        //if(myElementType.equalsIgnoreCase(TEST)||myElementType.equalsIgnoreCase(STEP)){
        //elementProp.put("status",value);
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_RESULT, value);
        //}else
        //System.out.println("could not set results to non-test or step type elment!");

    }

    public void setLibs(String value) {
        //elementProp.put("lib",value);
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_LIB, value);
    }

    public void setTestData(String value) {
        // elementProp.put("data",value);
        elementProp.put(TestJobDOM.DEFINED_ATTRIBUTE_DATA, value);
    }

    public TestJobElement getParentJob() {
        return (TestJobElement)getParent();
    }

    public TestJobElement getRootJob() {
        TestJobElement rootjob = this;
        TestJobElement parentJob = getParentJob();
        if (parentJob == null) {
            return rootjob;
        }
        while (parentJob != null) {
            rootjob = parentJob;
            parentJob = parentJob.getParentJob();
        }
        return rootjob;
    }


    public void proccessDependency(Node na) {
        if (na.getNodeName().equalsIgnoreCase(TestJobDOM.DEFINED_ATTRIBUTE_DEPENDS)) {
            String depends = na.getNodeValue();
            String[] depend = depends.split(TestJobDOM.separator);
            if (!depends.equalsIgnoreCase("")) {
                //System.out.println("job depend is "+depends);
                int iSize = depend.length;
                for (int j = 0; j < iSize; j++) {
                    addDependency(depend[j]);
                }
            }
        }
    }

    public String getQTPFormatLib() {
        ACLibSet libset =
            (ACLibSet)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_LIB);
        return libset.getQTPFormatlib();
        //return appenderCtrl.getQTPFormatLib();
    }

    public String getQTPFormatData() {
        ACTestDataSet dataset =
            (ACTestDataSet)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
        return dataset.getQTPFormatData();
        //return appenderCtrl.getQTPFormatData();
    }

    public void addTestDataFile(String type, String filePath) {
        ACTestDataSet dataset =
            (ACTestDataSet)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
        dataset.addDataFile(type, filePath);
        //appenderCtrl.addTestDataFile(filePath);
    }

    public void addDependency(String caseB) {
        dependencyArr.add(caseB);
    }

    public void setRunSet(ACRunSet run) {
        ACRunSet runset =
            (ACRunSet)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_RUN);
        runset = run;
        //appenderCtrl.setRunSet(run);
    }

    private void setInputValue(String key, String value) {
        ACJobInput jobinput =
            (ACJobInput)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_INPUT);
        jobinput.setInput(key, value);
        //appenderCtrl.setInputValue(key,value);
    }

    public void addInputPara(String key) {
        ACJobInput jobinput =
            (ACJobInput)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_INPUT);
        jobinput.addIutputPara(key);
        //appenderCtrl.addInputPara(key);
    }

    public void setOutputValue(String key, String value) {
        //System.out.println("in testjobelement : set outputvalue: "+key+value);
        ACJobOutput joboutput =
            (ACJobOutput)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_OUTPUT);
        joboutput.setJobOutput(key, value);

        //appenderCtrl.setOutputValue(key,value);
        String currentIndicator = getCurrTestIndicator();
        if (!currentIndicator.equalsIgnoreCase("")) {
            key = currentIndicator + "_" + key;
        }
        outputFrameLog("Job <" + getName() + "> output the parameter " + key +
                       "=" + value);
        writeIntoComChannel(key, value);
    }

    public void writeIntoACChannel(String key, String value) {
        setOutputValue(key, value);
    }

    public void addOutputPara(String key) {
        ACJobOutput joboutput =
            (ACJobOutput)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_OUTPUT);
        joboutput.addOutputPara(key);
        //appenderCtrl.addOutputPara(key);
    }

    public ArrayList getDependencyArr() {
        return dependencyArr;
    }

    public void setDependencyArr(ArrayList dependArr) {
        dependencyArr = dependArr;
    }

    public String getDriverType() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_DRVTYPE);
    }

    public String getResults() {
        return getProperty(TestJobDOM.DEFINED_ATTRIBUTE_RESULT);
    }

    public void addProperty(String key, String value) {
        elementProp.setProperty(key, value);
    }

    public ACJobAppender getAppenderInstance(String type) {
        return appenderCtrl.getAppenderInstance(type);
    }

    public String getProperty(String key) {
        return elementProp.getProperty(key, "");
    }

    public void addChildJob(TestJobElement childJob) {
        /* following appender will be parsed from parent to child
         * 1. ACTestEnv
         * 2. ACTestConfig
         * 3. ACTestDataSet
         * 4. ACLibSet
         * 5. ACJobInput
         * */
        //modify to use unified process to overwrite appenderCtrl.
        childJob.appenderCtrl.overridedBy(appenderCtrl);
        /*
        ACTestEnv childEnv = (ACTestEnv)childJob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        ACTestEnv thisEnv = (ACTestEnv)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        childEnv.addMore(thisEnv);

        ACTestConfig childConf = (ACTestConfig)childJob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
        ACTestConfig thisConf = (ACTestConfig)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
        childConf.addMore(thisConf);

        //because testdata prop was stored with datafile path. the datavalue was really stored in dataProp.
        ACTestDataSet childData = (ACTestDataSet)childJob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
        ACTestDataSet thisData = (ACTestDataSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
        childData.addTestDataSet(thisData);

        ACLibSet childLib = (ACLibSet)childJob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_LIB);
        ACLibSet thisLib = (ACLibSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_LIB);
        childLib.addMore(thisLib);

         ACJobInput childInput = (ACJobInput)childJob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_INPUT);
         ACJobInput thisInput = (ACJobInput)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_INPUT);
         childInput.addMore(thisInput);
        */
        ACTestDataSet childData =
            (ACTestDataSet)childJob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
        ACTestDataSet thisData =
            (ACTestDataSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
        childData.addTestDataSet(thisData);
        addChildNode(childJob);
        //lib,data,runset,jobinput,joboutput,env,config
    }

    public ArrayList getAllLeaveJobs(TestJobElement ele) {
        ArrayList eleArr = new ArrayList(100);
        if (!ele.ifHasChildNodes()) {
            //ele.outputString();
            eleArr.add(ele);
            return eleArr;
        }
        int isize = ele.getChildNodesNum();
        for (int i = 0; i < isize; i++) {
            TestJobElement childEle =
                (TestJobElement)ele.getChildNodesByIndex(i);
            if (childEle.getJobStatus().equalsIgnoreCase(JobStatus.FINISH)) {
                ArrayList childEleArr = getAllLeaveNodes(childEle);
                eleArr = Utility.mergeTwoArrayList(eleArr, childEleArr);
            }
        }
        return eleArr;

    }
    /*
    public static void main(String[] args) {
        /*
        DataSubscriber subscriber= new DataSubscriber();
        ACTestJob jobInfo = DriverFactory.createJob("QTP","TestSuite.xml",subscriber);
        TestJobElement suiteJob = jobInfo.getJob();
        String currentDir=System.getProperty("user.dir");
        String strBaseResultsHome="results";
        String strCurrentTime = Utility.getStrCurrentTime();
        String locationPath = currentDir + "\\" + strBaseResultsHome + "\\"+ strCurrentTime+ "\\";
         String locationPath = currentDir + "\\" + strBaseResultsHome + "\\"+ "debugreport"+ "\\";
        suiteJob.setLocationPath(locationPath);
        ACTestEnv envinfo = new ACTestEnv();
        envinfo.loadEnvSetting("instanceInfo.xml");
        ACTestConfig configinfo = new ACTestConfig();
        configinfo.loadConfigSetting("config.xml");
        suiteJob.setTestLang(Utility.getSystemInfo().getProperty("userLocale"));
        suiteJob.setName("NLSAutomationSuite");
        DataSubscriber subscriber1 = new DataSubscriber();
        suiteJob.initJob(envinfo,configinfo);
        suiteJob.runJob();
        suiteJob.generateACReport();
        //suiteJob.uploadReportToFTPServer();
        */
    /* String currentDir=System.getProperty("user.dir");
        TestJobElement suiteReport = new TestJobElement(TestJobType.SUITE);
        String locationPath = currentDir + File.separator  + "results" + File.separator + "debugreport"+ File.separator ;
        TestJobElement compReport = new TestJobElement(TestJobType.COMPONENT);
        compReport.setName("OBEO");
        QTPReportParser reportParser = new QTPReportParser();
        String reportName=locationPath;
        ArrayList arrCasesRunned = new ArrayList(100);
        arrCasesRunned.add(compReport.getName()+File.separator +"scriptsFactoryReport");
        reportParser.parseResultsToACReport(reportName,arrCasesRunned,"zh_CN",true,compReport);
        suiteReport.addChildNode(compReport);
        //suiteReport.generateACReport();

    }*/
}
