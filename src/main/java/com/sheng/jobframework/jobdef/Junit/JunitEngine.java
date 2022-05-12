package com.sheng.jobframework.jobdef.Junit;

import com.sheng.jobframework.utility.ReflectionUtil;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobEngine;
import com.sheng.jobframework.jobdef.ACRunSet;

import java.util.ArrayList;

import junit.framework.TestCase;

import junit.textui.TestRunner;


public class JunitEngine extends ACJobEngine {
    //shining comments modified 2010-12-20, to protected the class searilizable
    //public TestRunner junitRunner = new TestRunner();

    public JunitEngine() {
    }

    public void Init() {

    }

    public void runEntityJob() {
        try {
            TestRunner junitRunner = new TestRunner();
            ACRunSet runset =
                (ACRunSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_RUN);
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            int itestToRun = runset.getRunSetArr().size();
            for (int i = 0; i < itestToRun; i++) {
                TestJobElement junitcase =
                    new TestJobElement(TestJobType.CASE);
                String runPath = (String)runset.getRunSetArr().get(i);
                String classToLoad = runPath;
                String classname = classToLoad;
                junitcase.setName(classname);
                addChildJob(junitcase);
                //System.out.println("in junit engine,the class to beloaded is "+classToLoad);

                //TO DO: here could use reflection to traverse all the test method, and run it.
                ArrayList testMethodToRun =
                    ReflectionUtil.getDeclaredMethod(classToLoad, "test");
                int iMethodToRun = testMethodToRun.size();
                for (int j = 0; j < iMethodToRun; j++) {
                    TestJobElement junittest =
                        new TestJobElement(TestJobType.TEST);
                    junitcase.addChildJob(junittest);
                    String methodName = (String)testMethodToRun.get(j);
                    junittest.setName(methodName);
                    junittest.setBeginTime(System.currentTimeMillis());
                    //System.out.println("here is to load test");
                    TestCase test =
                        (TestCase)loader.loadClass(classToLoad).newInstance();
                    //System.out.println("after load test");
                    test.setName(methodName);
                    junit.framework.TestResult result = junitRunner.run(test);
                    TestJobElement junitStep =
                        new TestJobElement(TestJobType.STEP);
                    junittest.addChildJob(junitStep);
                    int icount = result.runCount();
                    junitStep.addProperty("done",
                                          "has been run " + "completely");
                    junittest.setResults(TestResult.PASS);
                    junittest.setEndTime(System.currentTimeMillis());

                }

                //System.out.println("in junit engine,have set name "+junittest.getName());
            }
        } catch (Exception e) {
            outputFrameLog("Exception in JunitEngine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void End() {

    }

}
