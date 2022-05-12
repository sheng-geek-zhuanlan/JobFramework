package com.sheng.jobframework.jobdef.Eunit;

import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobEngine;


public abstract class EunitEngine extends ACJobEngine {
    //shining comments modified 2010-12-20, to protected the class searilizable
    //public TestRunner junitRunner = new TestRunner();

    public EunitEngine() {
    }

    public void Init() {
    }

    public TestJobElement addCase(String casename) {
        TestJobElement ecase = new TestJobElement(TestJobType.CASE);
        ecase.setName(casename);
        addChildJob(ecase);
        return ecase;
    }

    public TestJobElement addTest(TestJobElement casejob, String testname) {
        TestJobElement etest = new TestJobElement(TestJobType.TEST);
        etest.setName(testname);
        casejob.addChildJob(etest);
        return etest;
    }

    public void addStep(TestJobElement testjob, String stepname) {
        TestJobElement estep = new TestJobElement(TestJobType.STEP);
        estep.addProperty("done", stepname);
        testjob.addChildJob(estep);
    }

    public void addPass(TestJobElement testjob, String passname) {
        TestJobElement estep = new TestJobElement(TestJobType.STEP);
        estep.addProperty(JobStatus.PASSED, passname);
        testjob.addChildJob(estep);
    }

    public void addFail(TestJobElement testjob, String passname) {
        TestJobElement estep = new TestJobElement(TestJobType.STEP);
        estep.addProperty(JobStatus.FAILED, passname);
        testjob.addChildJob(estep);
    }

    public abstract void runEntityJob();

    public void End() {

    }

}
