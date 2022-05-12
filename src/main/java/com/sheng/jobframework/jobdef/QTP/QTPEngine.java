package com.sheng.jobframework.jobdef.QTP;

import com.sheng.jobframework.drivers.ACDriver;
import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobEngine;
import com.sheng.jobframework.drivers.qtp.QTPDriver;


//import framework.ACUtility.ACTestJob;
//import framework.ACUtility.QTPTestJob;


public class QTPEngine extends ACJobEngine {
    ACDriver drv;

    public QTPEngine() {
    }

    public void Init() {
    }

    public void runEntityJob() {
        //outputString();
        //Utility.cleanProcess();
        //ACDriver drv  = DriverFactory.createDriver(getDriverType());
        QTPDriver drv;
        try {
            drv = new QTPDriver();
        } catch (Exception e) {
            outputFrameLog(LOGMSG.QTP_INIT_EXCEPTION + e.getMessage());
            emptyAllChildNodes();
            TestJobElement errCase = new TestJobElement();
            errCase.setName("QTPInstanceInit");
            TestJobElement errTest = new TestJobElement();
            errTest.setName("QTPInstanceInitException");
            errTest.setResults(TestResult.CNR);
            TestJobElement step = new TestJobElement(TestJobType.STEP);
            step.addProperty("CNR",
                             "Test could not run due to QTP instance failed to initialize, please check framework.log for detail trace");
            errTest.addChildJob(step);
            errCase.addChildJob(errTest);
            addChildJob(errCase);
            e.printStackTrace();
            return;
        }
        // drv.setObserver(observerSubscriber);
        //ACTestJob testjob = new QTPTestJob();
        //ACTestEnv env=getTestEnv();
        //ACTestConfig config=getTestConfig();
        //drv.driverInit(frameLog,testLog,testObserver,testjob,env,config);
        try {
            drv.testInit(this);
        } catch (Exception e) {
            outputFrameLog(LOGMSG.QTP_INIT_EXCEPTION + e.getMessage());
            emptyAllChildNodes();
            TestJobElement errCase = new TestJobElement();
            errCase.setName("QTPTestInit");
            TestJobElement errTest = new TestJobElement();
            errTest.setName("QTPTestInitException");
            errTest.setResults(TestResult.CNR);
            TestJobElement step = new TestJobElement(TestJobType.STEP);
            step.addProperty("CNR",
                             "Test could not run due to QTP test failed to initialize, please check framework.log for detail trace");
            errTest.addChildJob(step);
            errCase.addChildJob(errTest);
            addChildJob(errCase);
            e.printStackTrace();
            return;
        }
        try {
            drv.runTestJob(this);
        } catch (Exception e) {
            outputFrameLog(LOGMSG.QTP_INIT_EXCEPTION + e.getMessage());
            emptyAllChildNodes();
            TestJobElement errCase = new TestJobElement();
            errCase.setName("QTPTestRun");
            TestJobElement errTest = new TestJobElement();
            errTest.setName("QTPTestRunException");
            errTest.setResults(TestResult.CNR);
            TestJobElement step = new TestJobElement(TestJobType.STEP);
            step.addProperty("CNR",
                             "Test could not run due to QTP test run with exception, please check framework.log for detail trace");
            errTest.addChildJob(step);
            errCase.addChildJob(errTest);
            addChildJob(errCase);
            e.printStackTrace();
            return;
        }
        try {
            drv.testEnd(this);
        } catch (Exception e) {
            outputFrameLog(LOGMSG.QTP_INIT_EXCEPTION + e.getMessage());
            emptyAllChildNodes();
            TestJobElement errCase = new TestJobElement();
            errCase.setName("QTPTestEnd");
            TestJobElement errTest = new TestJobElement();
            errTest.setName("QTPTestEnd");
            errTest.setResults(TestResult.CNR);
            TestJobElement step = new TestJobElement(TestJobType.STEP);
            step.addProperty("CNR",
                             "Test could not run due to QTP test end with exception, please check framework.log for detail trace");
            errTest.addChildJob(step);
            errCase.addChildJob(errTest);
            addChildJob(errCase);
            e.printStackTrace();
            return;
        }
    }

    public void End() {

    }
}
