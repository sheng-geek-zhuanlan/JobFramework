package com.sheng.jobframework.jobdef.Selenium;

import com.sheng.jobframework.utility.ClassLoaderUtil;
import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.utility.ReflectionUtil;
import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.jobdom.ACJavaJob;

import com.sheng.jobframework.jobdef.ACJavaClassSet;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobEngine;
import com.sheng.jobframework.jobdef.ACTestCase;
import com.sheng.jobframework.jobdef.ACTestCaseSet;

import java.io.File;

import java.lang.reflect.Method;

import java.net.URLClassLoader;

import java.util.ArrayList;


//current Seleinum Engine is for running all those UI test cases
public class SeleniumEngine extends ACJobEngine {
    public static String SEL_INIT_METHOD = "setUp";
    public static String SEL_END_METHOD = "tearDown";
    public static String SEL_STARTTEST_METHOD = "beginTest";
    public static String SEL_ENDTEST_METHOD = "endTest";
    public static String SEL_REPORT_FAIL = "reportFail";
    public static String SEL_REPORT_PASS = "reportPass";
    public static String SEL_SPECIFIC_PASS = "pass";

    public SeleniumEngine() {
    }

    public void runEntityJob() {
        try {
            ACTestCaseSet selcaseset =
                (ACTestCaseSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_SEL);
            //for debug
            //outputFrameLog("begin run selenium or web service job!");
            //ACTestEnv thisEnv = (ACTestEnv)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
            //thisEnv.getEnvSetting().list(System.out);
            //for debug
            //ClassLoaderUtil loader = ClassLoaderUtil.getClassLoaderInstance();
            /*
             * new classloader 02-15 to fix the class crossed between different jobs
             */
            URLClassLoader loader = ClassLoaderUtil.getClassLoaderInstance();
            ACJavaClassSet classset =
                (ACJavaClassSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CLASS);
            // ACJCL loader = classset.getClassLoader();
            //end modify
            // URLClassLoader loader = ClassLoaderUtil.getClassLoaderInstance();
            //Properties prop = selcaseset.getProp();
            //Enumeration keys = prop.keys();
            //System.out.println("In selenium Engine, the class path is :   "+System.getProperty("java.class.path"));
            //Case->Test->Method
            ArrayList selCaseArr = selcaseset.getCaseArr();
            int iCaseSize = selCaseArr.size();
            for (int k = 0; k < iCaseSize; k++) {
                //while (keys.hasMoreElements()){
                //String strKey = (String)keys.nextElement();
                //SelTestCase selcase = (SelTestCase)prop.get(strKey);
                ACTestCase selcase = (ACTestCase)selCaseArr.get(k);
                String classToLoad = selcase.getCasepath();
                outputFrameLog(LOGMSG.SEL_RUN_CASE + classToLoad);
                //modified at 2010-11-24, to make selenium engine compatible with webservice dev engine
                //ACSeleniumJob seljob = (ACSeleniumJob)loader.loadClass(classToLoad).newInstance();
                ACJavaJob seljob =
                    (ACJavaJob)loader.loadClass(classToLoad).newInstance();
                //ACJavaJob seljob = (ACJavaJob)loader.loadClass(classToLoad);
                seljob.setName(selcase.getCaseName());
                addChildJob(seljob);
                Class cl = Class.forName(classToLoad);
                Method initmthd;
                Method endmthd;
                try {
                    //shining modified for radvision solution, getMethod will return all the method belong to current class or parent class. while getDeclaredMethod only contain the current class method
                    //initmthd=cl.getDeclaredMethod(SEL_INIT_METHOD);
                    //endmthd=cl.getDeclaredMethod(SEL_END_METHOD);
                    initmthd = cl.getMethod(SEL_INIT_METHOD);
                    endmthd = cl.getMethod(SEL_END_METHOD);
                    initmthd.setAccessible(true);
                    endmthd.setAccessible(true);
                } catch (Exception e) {
                    outputFrameLog(LOGMSG.SEL_METHOD_DEFINE);
                    outputFrameLog(LOGMSG.AC_QUIT_CURRENTJOB);
                    e.printStackTrace();
                    return;
                }
                try {
                    outputFrameLog("***is to run method: " + SEL_INIT_METHOD);
                    initmthd.invoke(seljob);
                } catch (Exception e) {
                    outputFrameLog(LOGMSG.SEL_SETUP_FAIL);
                    outputFrameLog(LOGMSG.AC_QUIT_CURRENTJOB);
                    Class[] types = new Class[] { String.class };
                    Method begintestmethd =
                        cl.getMethod(SEL_STARTTEST_METHOD, types);
                    begintestmethd.invoke(seljob, SEL_INIT_METHOD);
                    Method reportFailMethod =
                        cl.getMethod(SEL_REPORT_FAIL, types);
                    reportFailMethod.invoke(seljob,
                                            "Test could NOT run due to exception in setUp(): " +
                                            e.getMessage());
                    Method endtestmethd = cl.getMethod(SEL_ENDTEST_METHOD);
                    endtestmethd.invoke(seljob);
                    e.printStackTrace();
                    return;
                }
                int iTest = selcase.getTestNum();
                for (int i = 0; i < iTest; i++) {
                    String testname = selcase.getTestNameByIndex(i);
                    outputFrameLog(LOGMSG.SEL_RUN_TEST + testname);
                    ArrayList testMethodToRun =
                        ReflectionUtil.getDeclaredMethod(classToLoad,
                                                         testname);
                    int iMethodToRun = testMethodToRun.size();
                    //the method is for vague match, if exactly match, there should be only one method in one test.
                    for (int j = 0; j < iMethodToRun; j++) {
                        /*
                        TestJobElement seltest = new TestJobElement(TestJobType.TEST);
                        seljob.addChildJob(seltest);

                        seltest.setName(methodName);
                        seltest.setBeginTime(System.currentTimeMillis());
                        */

                        String methodName = (String)testMethodToRun.get(j);
                        //outputFrameLog(LOGMSG.SEL_RUN_TEST+"debug in method cycle "+methodName);
                        //outputFrameLog(LOGMSG.SEL_RUN_TEST+methodName);
                        Class[] types = new Class[] { String.class };
                        Method begintestmethd =
                            cl.getMethod(SEL_STARTTEST_METHOD, types);
                        begintestmethd.invoke(seljob, methodName);

                        try {
                            Method testmthd = cl.getMethod(methodName);
                            Method reportPassMethod =
                                cl.getMethod(SEL_SPECIFIC_PASS, types);
                            reportPassMethod.invoke(seljob,
                                                    "Test <" + methodName +
                                                    "> started ");
                            testmthd.invoke(seljob);
                        } catch (Exception e) {
                            String classname = e.getClass().getName();
                            if (classname.contains("ExitTestException")) {
                                outputFrameLog("Exit Current Test...");
                                break;
                            } else {
                                outputFrameLog("Exception thrown when running test " +
                                               methodName);
                                Method reportFailMethod =
                                    cl.getMethod(SEL_REPORT_FAIL, types);
                                reportFailMethod.invoke(seljob,
                                                        "Test <" + methodName +
                                                        "> failed due to exception: " +
                                                        e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        Method endtestmethd = cl.getMethod(SEL_ENDTEST_METHOD);
                        endtestmethd.invoke(seljob);

                        //move the png file to the test dir
                        String classfolder = seljob.generateScreenLocation();
                        String testfolder =
                            classfolder + File.separator + methodName;
                        copyScreenFilesToTestDir(classfolder, testfolder);
                        //System.out.println("here is to load test");
                        /*
                        TestJobElement selStep = new TestJobElement(TestJobType.STEP);
                        seltest.addChildJob(selStep);
                        selStep.addProperty("done","has been run "+"completely");
                        seltest.setResults(TestResult.PASS);
                        seltest.setEndTime(System.currentTimeMillis());
                       */
                    }

                }
                try {
                    endmthd.invoke(seljob);
                } catch (Exception e) {
                    outputFrameLog(LOGMSG.SEL_TEARDOWN_FAIL);
                    outputFrameLog(LOGMSG.AC_QUIT_CURRENTJOB);
                    //e.printStackTrace();
                    return;
                }

            }

        } catch (Exception e) {
            outputFrameLog("Exception in Test engine is " + e.getMessage());
            e.printStackTrace();
            //            System.out.println(e.getCause().getMessage());
        }
    }

    private void copyScreenFilesToTestDir(String classfolder,
                                          String testfolder) {
        try {
            File f = new File(classfolder);
            File[] fileArr = f.listFiles();
            if (fileArr != null) {
                int fileNum = fileArr.length;
                for (int i = 0; i < fileNum; i++) {
                    File pngfile = fileArr[i];
                    if (FileUtil.isPngFile(pngfile)) {
                        FileUtil.copyFile(classfolder + File.separator +
                                          pngfile.getName(),
                                          testfolder + File.separator +
                                          pngfile.getName(), true);
                    }
                }
            } else {
                outputFrameLog("no screenshot generated under folder: " +
                               classfolder);
            }

        } catch (Exception e) {
            outputFrameLog("Exception during selenium engine copying snapshot files " +
                           e.getMessage());
            e.printStackTrace();
        }
    }
}
