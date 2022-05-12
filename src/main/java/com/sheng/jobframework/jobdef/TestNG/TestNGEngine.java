package com.sheng.jobframework.jobdef.TestNG;

import com.sheng.jobframework.utility.ClassLoaderUtil;
import com.sheng.jobframework.annotation.TestResult;
import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobEngine;
import com.sheng.jobframework.jobdef.ACTestCase;
import com.sheng.jobframework.jobdef.ACTestCaseSet;

import java.net.URLClassLoader;

import java.util.ArrayList;

import java.util.List;

import junit.framework.TestCase;

import junit.textui.TestRunner;


public class TestNGEngine extends ACJobEngine {
    //shining comments modified 2010-12-20, to protected the class searilizable
    //public TestRunner junitRunner = new TestRunner();

    public TestNGEngine() {
    }

    public void Init() {

    }

    public void runEntityJob() {
      ACTestCaseSet testNGCaseset = (ACTestCaseSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_SEL);
      URLClassLoader loader = ClassLoaderUtil.getClassLoaderInstance();
      ArrayList testCaseArr = testNGCaseset.getCaseArr();
      int iCaseSize = testCaseArr.size();
      if(iCaseSize==0){
          JOM.restruct2StatusJob(this, "NO_CASE_FOUND", "no case found in testjob "+this.getName(), TestResult.FAIL);
          return;
        }
      List<Class> classArr = new ArrayList<Class>(); 
      for (int k = 0; k < iCaseSize; k++) {
          ACTestCase testclass = (ACTestCase)testCaseArr.get(k);
          String classpath = testclass.getCasepath();
          try{
            classArr.add(Class.forName(classpath));
          }catch(Exception e){
            outputFrameLog("XXXX-can not find class +"+classpath);
            continue;
          }
      }
      Class[] classSimpleArr = new Class[classArr.size()];
      classArr.toArray(classSimpleArr);
      TestHelper testhelper= new TestHelper();
      testhelper.runTest(classSimpleArr);
      testhelper.setTestCompName(this.getName());
      this.addChildJob(testhelper.getTestJobElement());        
    }

    public void End() {

    }

}

