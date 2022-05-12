package com.sheng.jobframework;

import com.sheng.jobframework.utility.JobUtil;
import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;

import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdom.TestJobElement;

import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobEngine;
import com.sheng.jobframework.jobdef.ACTestEnv;

import com.sheng.jobframework.runner.JobRunner;
import com.sheng.jobframework.runner.RemoteRunner;

import java.io.File;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;


public class Scheduler extends ACElement {
    public TestJobElement acjob = new TestJobElement();
    public ACTestEnv envinfo = new ACTestEnv();

    public Scheduler() {
    }

    public Scheduler(TestJobElement job) {
        acjob = job;
        recuSetAppenderValues(job);
    }

    public Scheduler(ACTestEnv env) {
        envinfo = env;
    }

    public void setRunJob(TestJobElement job) {
        acjob = job;
    }

    protected TestJobElement getRunJob() {
        return acjob;
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

    public void run() {
        //Init
        //      initJob();
        recuSetAppenderValues(acjob);
        runJob(acjob);
    }

    public void Init(ACTestEnv testenv) {

    }

    public void runJob(TestJobElement job) {

        try {
            if (job.getLocationPath().equalsIgnoreCase("") &&
                (job.parentJob != null)) {

                job.setLocationPath(job.getParentJob().getRelativeLocationPath() +
                                    File.separator + job.getName());
            }
            job.setBeginTime(Utility.getCurrentTimeInMillis());
            if (!job.isToRun()) {
                outputFrameLog("---ignore the Job " + job.getName() + " " +
                               job.getElementType() +
                               " since it was not determined in $" +
                               Styler.JOB_TO_RUN + "!");
                job.setJobStatus(JobStatus.NONEEDRUN);
                job.setEndTime(Utility.getCurrentTimeInMillis());
                return;
            }
            outputFrameLog("---start to run " + job.getName() + " " +
                           job.getElementType());
            if (!job.jobPrepare()) {
                outputFrameLog("job <" + job.getName() +
                               "> could NOT run due to fail to prepare.");
                JOM.restruct2StatusJob(job, "Job_Prepare_Fail",
                                       "Test could not run due to dependency job failure, please check framework.log for detail trace",
                                       TestResult.CNR);
                recuSetResults(job, TestResult.CNR);
                job.isRunned = true;
                job.setJobStatus(JobStatus.FINISH);
                job.setEndTime(Utility.getCurrentTimeInMillis());
                return;
            }

            if (JobRunner.isRemoteJob(job)) {
                if (job.getResults().equalsIgnoreCase(TestResult.FAIL)) {
                    //the host could not be parsed.
                    return;
                } else {
                    RemoteRunner remoterunner = new RemoteRunner(job);
                    remoterunner.runRemoteJob();
                    //Risk: if there has two hosts specified, one is local host, then localhost will cause a dead cycle. so here if local host exist, should not return, execute directly
                    String ifHasLocalAssign =
                        job.getProperty(TestJobDOM.DEFINED_ATTRIBUTE_HASLOCALJOB);
                    if (Utility.strToBoolean(ifHasLocalAssign)) {
                        //nothing to do, will go on to call scheduler local run
                        job.addProperty(TestJobDOM.DEFINED_ATTRIBUTE_HOST, "");
                    } else {
                        return;
                    }
                }
            }
            if (job.isRunnable) {
                //System.out.println("in Scheduler : the java case size is "+job.getChildNodesNum());

                ACJobEngine my = (ACJobEngine)job;
                //jobInit() and jobEnd() will be called in runMyACJob.
                System.out.println("before running" + my.getClass().getName());
                my.runMyACJob();
                autoSetResults(job);
                return;
            }
            //now it is sure to be a absctract job to run
            job.jobInit();
            //here some commented code out. piece 1
            int iChildJobs = job.getChildNodesNum();
            for (int k = 0; k < iChildJobs; k++) {
                TestJobElement testJob =
                    (TestJobElement)job.getChildNodesByIndex(k);
                Thread.sleep(Styler.WAIT_BETWEEN_JOB);
                //modify 2012-04-02, all is called by scheduler
                runJob(testJob);
            }
            job.setEndTime(Utility.getCurrentTimeInMillis());
            if ((job.getResults().equalsIgnoreCase("")) &&
                job.ifHasChildNodes()) {
                job.setResults(job.getChildResults());
            }
            //modified at 2011-04-15 to move the jobend to the bottom. to reflect the real issue
            job.jobEnd();
        } catch (Exception e) {
            outputFrameLog("Error Exception was thrown when running job " +
                           job.getName());
            e.printStackTrace();
        }
    }

    public void autoSetRunned(TestJobElement job) {
        int isize = job.getChildNodesNum();
        for (int i = 0; i < isize; i++) {
            TestJobElement childJob =
                (TestJobElement)job.getChildNodesByIndex(i);
            if (childJob.isRunned)
                job.isRunned = true;
            job.setJobStatus(JobStatus.FINISH);
        }
    }

    public boolean ifAllDependencyPass(TestJobElement job) {
        boolean allPass = true;
        ArrayList dependencyArr = job.getDependencyArr();
        int size = dependencyArr.size();
        for (int i = 0; i < size; i++) {
            String name = (String)dependencyArr.get(i);
            TestJobElement jobToRun =
                (TestJobElement)job.getBrotherNodeByName(name);
            if (!jobToRun.getResults().equalsIgnoreCase(TestResult.PASS)) {
                allPass = false;
                return allPass;
            }
        }
        return true;
    }

    public void runDepedencyJob(TestJobElement testJob) {
        ArrayList dependArr = testJob.getDependencyArr();
        int size = dependArr.size();
        for (int i = 0; i < size; i++) {
            String name = (String)dependArr.get(i);
            TestJobElement jobToRun =
                (TestJobElement)testJob.getBrotherNodeByName(name);
            if ((!jobToRun.getResults().equalsIgnoreCase(TestResult.PASS)) &&
                (jobToRun.rerunNum < jobToRun.max_rerunNum) &&
                (!jobToRun.getResults().equalsIgnoreCase(TestResult.CNR))) {
                jobToRun.setToRunMark(true);
                runJob(jobToRun);
            }
            //testJob.setInputFromOutput(jobToRun);
        }
    }

    public void recuSetResults(TestJobElement job, String status) {
        job.setResults(status);
        //outputFrameLog("try to set "+getName()+" with result"+status);
        if (job.ifHasChildNodes()) {
            int childNumbers = job.getChildNodesNum();
            //    outputFrameLog("has child jobs"+childNumbers);
            for (int i = 0; i < childNumbers; i++) {
                TestJobElement childJob =
                    (TestJobElement)job.getChildNodesByIndex(i);
                //outputFrameLog("try to set "+childJob.getName()+"to result"+status);
                recuSetResults(childJob, status);
            }
        }
    }

    public void autoSetResults(TestJobElement job) {

        TestJobElement testJob = job;
        //outputFrameLog(getElementType()+" job <"+getName()+"> has status with "+getResults());
        if (testJob.getResults().equalsIgnoreCase("") ||
            testJob.getResults().equalsIgnoreCase(TestResult.NA)) {
            int size = testJob.getChildNodesNum();
            if (testJob.getElementType().equalsIgnoreCase(TestJobType.TEST)) {
                outputLog("####Test<" + testJob.getName() + "> has result:" +
                          testJob.getResults() +
                          ", will be set to default PASS!");
                testJob.setResults(TestResult.PASS);
                autoSetParentResult(testJob);
                return;
            }
            for (int i = 0; i < size; i++) {
                TestJobElement childJob =
                    (TestJobElement)testJob.getChildNodesByIndex(i);
                autoSetResults(childJob);
                //testJob.getChildNodesByIndex(i).autoSetResults();
                //outputFrameLog(testJob.getElementType()+" job <"+testJob.getName()+"> set status with "+testJob.getChildResults());
                testJob.setResults(testJob.getChildResults());
            }
        } else {
            autoSetParentResult(testJob);
        }
    }

    public void autoSetParentResult(TestJobElement testJob) {

        TestJobElement parentjob = (TestJobElement)testJob.getParent();
        if (parentjob != null) {
            if (testJob.getResults().equalsIgnoreCase(TestResult.FAIL)) {
                parentjob.setResults(parentjob.getChildResults());
            } else if (testJob.getResults().equalsIgnoreCase(TestResult.PASS)) {
                if (!parentjob.getResults().equalsIgnoreCase(TestResult.FAIL)) {
                    parentjob.setResults((TestResult.PASS));
                }
            }
        }
    }

    public void outputInfo(Properties prop) {
        int paraNum = prop.size();
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            outputFrameLog(strKey + " = " + prop.getProperty(strKey));
        }
    }

    public void initJob(ACTestEnv dteenv) {
        ACTestEnv env =
            (ACTestEnv)acjob.appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        env.addEnvSetting(dteenv);
        boolean debugmode = false;
        if (!acjob.getEnvProperty("DEBUG").equalsIgnoreCase("")) {
            debugmode = Utility.strToBoolean(acjob.getEnvProperty("DEBUG"));
        } else if (!acjob.getConfProperty("DEBUG").equalsIgnoreCase("")) {
            debugmode = Utility.strToBoolean(acjob.getConfProperty("DEBUG"));
        }
        TestJobElement.setDebugMode(debugmode);
        if (debugmode)
            JobUtil.snapJOMScreen(acjob);
    }
}
