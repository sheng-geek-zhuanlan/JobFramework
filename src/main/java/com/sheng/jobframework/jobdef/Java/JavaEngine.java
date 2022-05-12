package com.sheng.jobframework.jobdef.Java;

import com.sheng.jobframework.utility.ClassLoaderUtil;
import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;
import com.sheng.jobframework.jobdom.ACJavaJob;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJavaClassSet;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobEngine;
import com.sheng.jobframework.jobdef.ACRunSet;

import java.net.URLClassLoader;

import java.util.ArrayList;


public class JavaEngine extends ACJobEngine {
    public JavaEngine() {
    }

    public void Init() {

    }

    public void runEntityJob() {
        System.out.println("****Java Engine is starting.....");
        try {

            ACRunSet runset =
                (ACRunSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_RUN);
            ArrayList runArr = runset.getRunSetArr();
            int runarrSize = runArr.size();
            //ClassLoaderUtil loader = ClassLoaderUtil.getClassLoaderInstance();
            emptyAllChildNodes();
            /*
        System.out.println("in JavaEngine before classloader: the java case size is "+getChildNodesNum());
           if(getChildNodesNum()>0){
             TestJobElement tempJob = (TestJobElement)getChildNodesByIndex(0);
             System.out.println("the test job is "+tempJob.getName()+" class name is "+tempJob.getClass().getName());
           }*/
            /*
         * new classloader 02-15 to fix the class crossed between different jobs
         */
            URLClassLoader loader = ClassLoaderUtil.getClassLoaderInstance();
            ACJavaClassSet classset =
                (ACJavaClassSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CLASS);
            //ACJobClassLoader loader = classset.getClassLoader();
            //ACJCL loader = classset.getClassLoader();
            //for debug purpose
            /*
          URL[] urlarray = loader.getURLs();
          int ilen = urlarray.length;
           for(int t=0;t<ilen;t++){
             URL url = urlarray[t];
            System.out.println("#####the class in java job loader is "+url.getFile()+"&&&&"+url.toString());
           }
           String classname2 = "oralce.sgt.automation.email.SendReceiveMail";
           Class cl = loader.getLoadedClass(classname2);
           if(cl==null){
             System.out.println("####Not loaded yet: "+classname2);
           }else{
             System.out.println("####loaded: "+cl.getName());
           }*/
            //end modify
            String ClassLoaded = "";
            for (int j = 0; j < runarrSize; j++) {
                //ClassLoader loader = ClassLoader.getSystemClassLoader();
                //String classToLoad = runset.getRunPath();
                //String classToLoad = runset.getCurrentRunPath();
                String classToLoad = (String)runArr.get(j);
                ClassLoaded = classToLoad;
                System.out.println("----Java Engine is to load test: " +
                                   classToLoad);
                ACJavaJob test =
                    (ACJavaJob)loader.loadClass(classToLoad).newInstance();
                //ACJavaJob test = (ACJavaJob)loader.loadClass(classToLoad);
                //ACJavaJob test = (ACJavaJob)Class.forName(classToLoad).newInstance();
                String classname = classToLoad;
                if (classToLoad.contains(".")) {
                    int g = classToLoad.lastIndexOf(".");
                    classname = classToLoad.substring(g + 1);
                }
                test.setName(classname);
                addChildJob(test);
            }
            //this is exactlly the number how many run tag in the test job
            /*
        String classname3 = "oralce.sgt.automation.email.SendReceiveMail";
        Class cl2 = loader.getLoadedClass(classname2);
        if(cl2==null){
          System.out.println("####Not loaded yet after create!: "+classname2);
        }else{
          System.out.println("####loaded after created: "+cl2.getName());
        }
           */

            int iSize = getChildNodesNum();
            System.out.println("---total java case size: " + iSize);
            for (int i = 0; i < iSize; i++) {

                //TO DO: here is potential risk, that the test job did not get job inited. but the engine has job inited

                TestJobElement testjob =
                    (TestJobElement)getChildNodesByIndex(i);
                outputFrameLog("-----is to run java case: " +
                               testjob.getName());
                //System.out.println("the test job is "+testjob.getName()+" class name is "+testjob.getClass().getName());
                ACJavaJob test;
                try {
                    test = (ACJavaJob)getChildNodesByIndex(i);
                } catch (Exception e) {
                    outputFrameLog("ignore the test job since it is not Java type, " +
                                   testjob.getName() + " class name is " +
                                   testjob.getClass().getName());
                    //e.printStackTrace();
                    continue;
                }

                //System.out.println("in java test running"+test.getName());
                //test.javaRun();
                test.setBeginTime(System.currentTimeMillis());
                try {
                    test.Init();
                } catch (Exception e) {
                    outputFrameLog(LOGMSG.JAVA_INIT_EXCEPTION +
                                   e.getMessage());
                    emptyAllChildNodes();
                    TestJobElement errTest = new TestJobElement();
                    errTest.setName("JavaJobInitFailure");
                    errTest.setResults(TestResult.CNR);
                    TestJobElement step = new TestJobElement(TestJobType.STEP);
                    step.addProperty("CNR",
                                     "Test could not run due to java test init with exception, please check framework.log for detail trace");
                    errTest.addChildJob(step);
                    test.addChildJob(errTest);
                    e.printStackTrace();
                    return;
                }
                try {
                    test.Run();
                } catch (Exception e) {
                    outputFrameLog(LOGMSG.JAVA_RUN_EXCEPTION + e.getMessage());
                    //emptyAllChildNodes();
                    TestJobElement errTest = new TestJobElement();
                    errTest.setName("JavaJobRunFailure_" +
                                    e.getClass().getName());
                    errTest.setResults(TestResult.FAIL);
                    TestJobElement step = new TestJobElement(TestJobType.STEP);
                    step.addProperty(JobStatus.FAILED,
                                     "Java Test run failure run due to exception, please check framework.log for detail trace");
                    errTest.addChildJob(step);
                    test.addChildJob(errTest);
                    e.printStackTrace();
                    return;
                }
                try {
                    test.End();
                } catch (Exception e) {
                    outputFrameLog(LOGMSG.JAVA_END_EXCEPTION + e.getMessage());
                    //emptyAllChildNodes();
                    TestJobElement errTest = new TestJobElement();
                    errTest.setName("JavaJobEndFailure");
                    errTest.setResults(TestResult.FAIL);
                    TestJobElement step = new TestJobElement(TestJobType.STEP);
                    step.addProperty(JobStatus.FAILED,
                                     "Java Test end failure with exception, please check framework.log for detail trace");
                    errTest.addChildJob(step);
                    test.addChildJob(errTest);
                    e.printStackTrace();
                    return;
                }
                test.setEndTime(System.currentTimeMillis());
                /****************/
            }
            //loader.unloadClass(ClassLoaded);
        } catch (Exception e) {
            outputFrameLog("Exception in java engine is " + e.getMessage());
            e.printStackTrace();
        }


    }

    public void End() {

    }
}
