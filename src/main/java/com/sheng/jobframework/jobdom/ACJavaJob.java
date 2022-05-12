package com.sheng.jobframework.jobdom;

import com.sheng.jobframework.utility.OSCmdUtil;

import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;


public abstract class ACJavaJob extends TestJobElement {
    public boolean hasCaseChild = false;
    public boolean hasTestChild = false;
    public static String ASSERT_PREFIX = "ASSERT Point ";
    public static String VERIFY_PREFIX = "VERIFY Point ";
    public static String TRANSACTIONCASE = "PerformanceTrans";
    public static String TAG_PREFIX = "TS_";
    public static String PREFIX_T = "T_";
    public static String PREFIX_DIS_T = "T_DIS_";
    public static String PROP_START_DESC = "PROP_START_DESC";
    public static String PROP_END_DESC = "PROP_END_DESC";
    public static String PROP_TS_DESC = "PROP_TS_DESC";
    private String T_Start_Desc = "";
    private String T_End_Desc = "";
    private String TS_Desc = "";

    public ACJavaJob() {
    }

    public void reportPass() {
        setResults(TestResult.PASS);
    }

    public void pass(String msg) {
        reportPass(msg);

    }

    public void fail(String msg) {
        reportFail(msg);
        //throw new ExitTestException();
    }

    private TestJobElement getTransactionCase() {
        TestJobElement compJob = null;
        if (getParentJob() == null) {
            outputFrameLog("XXXX-comp null, problem with transaction test case");
            compJob = this;
        } else {
            compJob = getParentJob();
        }
        TestJobElement testcase =
            (TestJobElement)compJob.getChildNodeByName(TRANSACTIONCASE);
        if (testcase != null)
            return testcase;
        testcase = new TestJobElement(TestJobType.CASE);
        testcase.setName(TRANSACTIONCASE);
        testcase.setResults(TestResult.PASS);
        compJob.insertFirstChild(testcase);
        return testcase;

    }

    public void tag_event(String name, String desc) {
        TS_Desc = desc;
        tag_event(name);
        TS_Desc = "";
    }

    public void tag_event(String name, long wastedTime, String desc) {
        TS_Desc = desc;
        long currenttime = System.currentTimeMillis();
        long timestamp = currenttime - wastedTime;
        tag_event(name, timestamp);
        TS_Desc = "";
    }

    private void tag_event(String name, long timestamp) {
        TestJobElement transactionCase = getTransactionCase();
        if (transactionCase.getChildNodeByName(name) != null) {
            outputFrameLog("XXXX-start transaction name <" + name +
                           ">  has existed! please change another name");
            return;
        }
        TestJobElement event = new TestJobElement(TestJobType.TEST);
        event.setResults(TestResult.PASS);
        event.addProperty(PROP_TS_DESC, TS_Desc);
        event.setName(TAG_PREFIX + name);
        long l = timestamp;
        event.setBeginTime(l);
        event.setEndTime(l);
        transactionCase.addChildJob(event);
        TestJobElement step = new TestJobElement(TestJobType.STEP);
        step.addProperty(JobStatus.PASSED,
                         "Event <" + event.getName() + "> tagged with timestamp at time " +
                         l + " @host:" + OSCmdUtil.getHostName());
        event.addChildJob(step);
        outputFrameLog("******Event <" + name +
                       "> has been marked with timestamp " + l);
    }

    public void tag_event(String name) {
        long timestamp = System.currentTimeMillis();
        tag_event(name, timestamp);
    }

    public void startTransaction(String name, String desc) {
        T_Start_Desc = desc;
        startTransaction(name);
        T_Start_Desc = "";
    }

    public void startTransaction(String name) {
        TestJobElement transactionCase = getTransactionCase();
        if (transactionCase.getChildNodeByName(name) != null) {
            outputFrameLog("XXXX-start transaction name <" + name +
                           ">  has existed! please change another name");
            return;
        }
        TestJobElement transaction = new TestJobElement(TestJobType.TEST);
        transaction.setName(PREFIX_T + name);
        transaction.addProperty(PROP_START_DESC, T_Start_Desc);
        long begintimeL = System.currentTimeMillis();
        transaction.setBeginTime(begintimeL);
        TestJobElement step = new TestJobElement(TestJobType.STEP);
        step.addProperty(JobStatus.PASSED,
                         "Start Transaction<" + transaction.getName() +
                         "> at time " + begintimeL + " @host:" +
                         OSCmdUtil.getHostName());
        transaction.addChildJob(step);
        outputFrameLog("******Transaction <" + name + "> starting at time " +
                       begintimeL);
        transactionCase.addChildJob(transaction);

    }

    public void endTransaction(String name, String desc) {
        T_End_Desc = desc;
        endTransaction(name);
        T_End_Desc = "";

    }

    public void endTransaction(String name, long wastedTime, String desc) {
        T_End_Desc = desc;
        endTransaction(name, wastedTime);
        T_End_Desc = "";

    }

    public void endTransaction() {
        long endtimel = System.currentTimeMillis();
        endTransaction("", endtimel, false);

    }

    public void endTransaction(String name) {
        long endTimeL = System.currentTimeMillis();
        endTransaction(name, endTimeL, true);
    }

    public void endTransaction(String name, long wastedTime) {
        long endtimeL = System.currentTimeMillis();
        long endTime = endtimeL - wastedTime;
        endTransaction(name, endTime, true);
    }

    private void endTransaction(String name, long endtime, boolean byname) {
        TestJobElement transactionCase = getTransactionCase();
        TestJobElement transaction = null;
        if (!byname) {
            transaction = (TestJobElement)transactionCase.getCurrentChildJob();
        } else {
            transaction =
                    (TestJobElement)transactionCase.getChildNodeByName(PREFIX_T +
                                                                       name);
        }
        if (transaction == null) {
            outputFrameLog("XXXX-end transaction name <" + name +
                           ">  not matched with any openning transaction");
            return;
        }
        transaction.addProperty(PROP_END_DESC, T_End_Desc);
        TestJobElement step = new TestJobElement(TestJobType.STEP);
        step.addProperty(JobStatus.PASSED,
                         "Transaction<" + transaction.getName() +
                         "> end at time: " + endtime + " @host:" +
                         OSCmdUtil.getHostName());
        long costtime = endtime - transaction.getBeginTimeL();
        TestJobElement step2 = new TestJobElement(TestJobType.STEP);
        step2.addProperty(JobStatus.PASSED,
                          "Transaction<" + transaction.getName() +
                          "> cost time: " + costtime);
        transaction.addChildJob(step);
        transaction.addChildJob(step2);
        transaction.setResults(TestResult.PASS);
        outputFrameLog("******transaction <" + transaction.getName() +
                       ">  end at time " + endtime);
        transaction.setEndTime(endtime);
    }

    public void reportPass(String msg) {
        TestJobElement currentTestJob = getCurrentTestJob();
        if (!currentTestJob.getResults().equalsIgnoreCase(TestResult.FAIL))
            currentTestJob.setResults(TestResult.PASS);
        TestJobElement stepJob = new TestJobElement(TestJobType.STEP);
        stepJob.addProperty(JobStatus.PASSED, msg);
        currentTestJob.addChildJob(stepJob);
        String name = currentTestJob.getName();
        outputFrameLog("<" + name + "> pass point: " + msg);
        //System.out.println("in acjava job, name is "+getParentJob().getName()+" "+getName());
    }

    public void beginTestCase(String casename) {
        TestJobElement testcase = new TestJobElement(TestJobType.CASE);
        testcase.setName(casename);
        addChildJob(testcase);
        testcase.setBeginTime(System.currentTimeMillis());
        hasCaseChild = true;
        outputFrameLog("*******Starting test case " + casename);
    }

    public void endTestCase() {
        if (!hasCaseChild) {
            outputFrameLog("Error when try end testcase, must beginTestCase firstly!");
        } else {
            TestJobElement testcase = (TestJobElement)getCurrentChildJob();
            if (testcase == null) {
                outputFrameLog("Error when try end testcase, must beginTestCase firstly!");
            } else {
                testcase.setEndTime(System.currentTimeMillis());
                outputFrameLog("********Case ended");
            }

        }

    }

    public void beginTest(String testname) {
        TestJobElement test = new TestJobElement(TestJobType.TEST);
        test.setName(testname);
        test.setBeginTime(System.currentTimeMillis());
        outputFrameLog("******test " + testname + " starting");
        if (hasCaseChild) {
            TestJobElement testcase = (TestJobElement)getCurrentChildJob();
            //System.out.println("add test to the case "+testcase.getName());
            testcase.addChildJob(test);
        } else {
            addChildJob(test);
            hasTestChild = true;
        }


    }

    public void endTest() {
        outputFrameLog("******test  ended");
        if (hasCaseChild) {
            TestJobElement testcase = (TestJobElement)getCurrentChildJob();
            TestJobElement test =
                (TestJobElement)testcase.getCurrentChildJob();
            test.setEndTime(System.currentTimeMillis());
        } else {
            TestJobElement test = (TestJobElement)getCurrentChildJob();
            test.setEndTime(System.currentTimeMillis());
        }
    }

    public void reportFail() {

        setResults(TestResult.FAIL);
    }

    public void reportFail(String msg) {
        TestJobElement currentTestJob = getCurrentTestJob();
        TestJobElement stepJob = new TestJobElement(TestJobType.STEP);
        stepJob.addProperty(JobStatus.FAILED, msg);
        currentTestJob.setResults(TestResult.FAIL);
        currentTestJob.addChildJob(stepJob);
        String name = currentTestJob.getName();
        outputFrameLog("<" + name + "> fail point: " + msg);
    }

    public void reportWarning() {
        setResults(TestResult.NA);
    }

    public abstract void Init();

    public abstract void Run();

    public abstract void End();

    public void javaRun() {

        setBeginTime(System.currentTimeMillis());
        try {
            Init();
        } catch (Exception e) {
            outputFrameLog(LOGMSG.QTP_INIT_EXCEPTION + e.getMessage());
            emptyAllChildNodes();
            TestJobElement errTest = new TestJobElement();
            errTest.setName("JavaJobInitFailure");
            errTest.setResults(TestResult.CNR);
            TestJobElement step = new TestJobElement(TestJobType.STEP);
            step.addProperty("CNR",
                             "Test could not run due to java test init with exception, please check framework.log for detail trace");
            errTest.addChildJob(step);
            addChildJob(errTest);
            e.printStackTrace();
            return;
        }
        try {
            Run();
        } catch (Exception e) {
            outputFrameLog(LOGMSG.QTP_INIT_EXCEPTION + e.getMessage());
            //emptyAllChildNodes();
            TestJobElement errTest = new TestJobElement();
            errTest.setName("JavaJobRunFailure");
            errTest.setResults(TestResult.FAIL);
            TestJobElement step = new TestJobElement(TestJobType.STEP);
            step.addProperty(JobStatus.FAILED,
                             "Java Test run failure run due to exception, please check framework.log for detail trace");
            errTest.addChildJob(step);
            addChildJob(errTest);
            e.printStackTrace();
            return;
        }
        try {
            End();
        } catch (Exception e) {
            outputFrameLog(LOGMSG.QTP_INIT_EXCEPTION + e.getMessage());
            //emptyAllChildNodes();
            TestJobElement errTest = new TestJobElement();
            errTest.setName("JavaJobEndFailure");
            errTest.setResults(TestResult.FAIL);
            TestJobElement step = new TestJobElement(TestJobType.STEP);
            step.addProperty(JobStatus.FAILED,
                             "Java Test end failure with exception, please check framework.log for detail trace");
            errTest.addChildJob(step);
            addChildJob(errTest);
            e.printStackTrace();
            return;
        }

        setEndTime(System.currentTimeMillis());

    }

    public TestJobElement getCurrentTestJob() {
        if (hasCaseChild) {
            TestJobElement testcase = (TestJobElement)getCurrentChildJob();
            if (testcase.getChildNodesNum() == 0) {
                outputFrameLog("Error: must beginTest before reportPass or reportFail!");
                System.out.println("Error: test case " + testcase.getName() +
                                   "has no child!");
                return null;
            } else {
                TestJobElement test =
                    (TestJobElement)testcase.getCurrentChildJob();
                return test;
            }
        } else if (hasTestChild) {
            TestJobElement test = (TestJobElement)getCurrentChildJob();
            return test;
        } else
            return this;
    }


}
