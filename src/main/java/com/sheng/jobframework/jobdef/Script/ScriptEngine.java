package com.sheng.jobframework.jobdef.Script;

import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.utility.OSCmdUtil;
import com.sheng.jobframework.utility.Utility;
import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;
import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdom.TestJobElement;

import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobEngine;
import com.sheng.jobframework.jobdef.ACRunSet;
import com.sheng.jobframework.jobdef.ACTestConfig;
import com.sheng.jobframework.jobdef.ACTestEnv;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;


public class ScriptEngine extends ACJobEngine {
    public static String LIB_BASE =
        "common" + File.separator + "lib" + File.separator + "perl";
    public static String IMPORT_FILE_NAME = "import.txt";
    public int startsCounterOfServiceData = 0;
    public int endCounterOfServiceData = 0;

    public ScriptEngine() {
    }

    public void beginServiceCounter() {
        Properties channelProp = ACElement.subscriber.getServiceSubscribedDataProp();
        startsCounterOfServiceData = channelProp.size();
    }

    public void endServiceCounter() {
        Properties channelProp = ACElement.subscriber.getServiceSubscribedDataProp();
        endCounterOfServiceData = channelProp.size();
    }

    public void runEntityJob() {

        TestJobElement scriptjob = new TestJobElement(TestJobType.CASE);
        ACTestEnv testenv =
            (ACTestEnv)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        Properties envProp = testenv.getEnvSetting();
        ACTestConfig testconf =
            (ACTestConfig)appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
        String perlibBase = LIB_BASE;
        String importFileName =
            Utility.getStrCurrentTime() + "_" + IMPORT_FILE_NAME;
        //System.out.println("Import.txt is under "+importFileName);
        if (!testconf.getContent("PERL_LIB_BASE").equalsIgnoreCase("")) {
            perlibBase = testconf.getContent("PERL_LIB_BASE");
        }
        if (!ScriptUtil.writeImportParams(perlibBase, importFileName,
                                          envProp)) {
            outputFrameLog("XXXX-Failed to write import.txt file at " +
                           perlibBase + File.separator + importFileName);
            JOM.restruct2StatusJob(this, "IMPORT_TXT_NOTREADY",
                                   "failed to generate import.txt",
                                   TestResult.FAIL);
            return;
        }
        //For Install Purpose
        String argImportTxtPath = perlibBase + File.separator + importFileName;
        // boolean damonMode = Utility.strToBoolean(getProperty(TestJobDOM.node_attribute_damon));
        scriptjob.setName(getName());
        scriptjob.setBeginTime(System.currentTimeMillis());
        addChildJob(scriptjob);
        scriptjob.setBeginTime(System.currentTimeMillis());
        ACRunSet runset =
            (ACRunSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_RUN);
        ArrayList runArr = runset.getRunSetArr();
        //Properties argProp = runset.getProp();
        int runarrSize = runArr.size();
        int exitValue = 0;
        for (int j = 0; j < runarrSize; j++) {
            //ClassLoader loader = ClassLoader.getSystemClassLoader();
            //String classToLoad = runset.getRunPath();
            //String classToLoad = runset.getCurrentRunPath();
            TestJobElement scripTestJob = new TestJobElement(TestJobType.TEST);
            String executeFilePath = (String)runArr.get(j);
            String scriptType = FileUtil.getFileExtension(executeFilePath);
            scripTestJob.setName(FileUtil.getFileName(executeFilePath) +
                                 "_job");
            scriptjob.addChildJob(scripTestJob);
            executeFilePath = FileUtil.getAbsolutePath(executeFilePath);
            if (!FileUtil.isFileExists(executeFilePath)) {
                outputFrameLog("XXXX-The script file is missing at " +
                               executeFilePath);
                JOM.restruct2StatusJob(this, "FILE_NOT_FOUND",
                                       "script file is missing at path:" +
                                       executeFilePath, TestResult.FAIL);
                return;
            }
            String scriptParser = "";

            if (scriptType.equalsIgnoreCase("PL")) {
                outputFrameLog("****this is a perl script will call perl engine..............");
                //swith the os
                switch (OSCmdUtil.getOSType()) {
                case OSCmdUtil.WINDOWS:
                    {
                        scriptParser = "perl.exe";
                    }
                    break;
                case OSCmdUtil.LINUX:
                    {
                        scriptParser = "perl";
                    }
                    break;
                case OSCmdUtil.UNIX:
                    {
                        scriptParser = "perl";
                    }
                    break;
                default:
                    {
                        scriptParser = "perl";
                    }
                }

            } else if (scriptType.equalsIgnoreCase("VBS")) {
                switch (OSCmdUtil.getOSType()) {
                case OSCmdUtil.WINDOWS:
                    {
                        outputFrameLog("****this is a vbs script will call vbscript engine..............");
                        scriptParser = "wscript.exe";
                    }
                    break;
                default:
                    {
                        outputFrameLog("XXXX-Vb scripts only runs on windows platform!");
                        JOM.restruct2StatusJob(this, "OS_NOT_SUPPORT",
                                               "the vbscript could not run on non-windows OS",
                                               TestResult.CNR);
                        return;
                    }
                }
            }else if (scriptType.equalsIgnoreCase("BAT")) {
                switch (OSCmdUtil.getOSType()) {
                case OSCmdUtil.WINDOWS:
                    {
                        outputFrameLog("****this is a bat shell script e..............");
                        scriptParser = executeFilePath;
                        executeFilePath ="";
                    }
                    break;
                default:
                    {
                        outputFrameLog("XXXX-Vb scripts only runs on windows platform!");
                        JOM.restruct2StatusJob(this, "OS_NOT_SUPPORT",
                                               "the vbscript could not run on non-windows OS",
                                               TestResult.CNR);
                        return;
                    }
                }
            }else if (scriptType.equalsIgnoreCase("SH")) {
                switch (OSCmdUtil.getOSType()) {
                case OSCmdUtil.LINUX:
                    {
                        outputFrameLog("****this is a shell script will call shell engine..............");
                        scriptParser = "sh";
                    }
                    break;
                default:
                    {
                        outputFrameLog("XXXX-shell scripts only runs on linux platform!");
                        JOM.restruct2StatusJob(this, "OS_NOT_SUPPORT",
                                               "the vbscript could not run on non-linux OS",
                                               TestResult.CNR);
                        return;
                    }
                }
            } else {
                outputFrameLog("XXXX-Not recoginzed script type! currently only perl/vbscript/shell could be run in AC!, please contact AC for support");
                JOM.restruct2StatusJob(this, "TYPE_NOT_SUPPORT",
                                       "the script type is not supported: " +
                                       scriptType, TestResult.CNR);
                return;
            }
            beginServiceCounter();
            scripTestJob.setBeginTime(System.currentTimeMillis());
            String[] cmd = { scriptParser, executeFilePath, argImportTxtPath };
            System.out.println("----Script Engine is to invoke the script: " +
                               executeFilePath);
            StringBuffer resultStringBuffer = new StringBuffer();
            String lineToRead = "";
            // get Process to execute perl, get the output and exitValue
            try {
                outputFrameLog("---Run the command :" + scriptParser + " " +
                               executeFilePath + " " + argImportTxtPath);
                Process proc = Runtime.getRuntime().exec(cmd);
                InputStream inputStream = proc.getInputStream();
                BufferedReader bufferedRreader =
                    new BufferedReader(new InputStreamReader(inputStream));
                // save first line
                if ((lineToRead = bufferedRreader.readLine()) != null) {
                    resultStringBuffer.append(lineToRead);
                    outputFrameLog(lineToRead);
                }
                // save next lines
                while ((lineToRead = bufferedRreader.readLine()) != null) {
                    resultStringBuffer.append("\r\n");
                    resultStringBuffer.append(lineToRead);
                    outputFrameLog(lineToRead);
                }
                // Always reading STDOUT first, then STDERR, exitValue last
                proc.waitFor(); // wait for reading STDOUT and STDERR over
                exitValue = proc.exitValue();
            } catch (Exception ex) {
                resultStringBuffer = new StringBuffer("");
                ex.printStackTrace();
                exitValue = 2;
                scripTestJob.setEndTime(System.currentTimeMillis());
            }
            TestJobElement steps = new TestJobElement(TestJobType.STEP);
            steps.addProperty("done", "script task has been executed!");
            scripTestJob.addChildJob(steps);
            if (exitValue == 0) {
                scripTestJob.setJobStatus(JobStatus.FINISH);
                scripTestJob.setResults(TestResult.PASS);
            } else {
                steps.addProperty("done",
                                  "error during executing script file");
              scripTestJob.setJobStatus(JobStatus.FINISH);
                scripTestJob.setResults(TestResult.FAIL);
            }
            endServiceCounter();
            scripTestJob.setEndTime(System.currentTimeMillis());
            parseScriptJobs(scripTestJob);
        }

        scriptjob.setEndTime(System.currentTimeMillis());
        //delete the generated buid file
        //Delete delete=new Delete();
        //delete.setProject(project);
        //delete.setFile(buildFile);
        // delete.execute();
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
    public static void main(String[] args){
        try{
          
       String executefilepath="D:\\windows.bat";
      String scriptParser = executefilepath;
          executefilepath="";   
        System.out.println("scriptParser is "+scriptParser);
          System.out.println("executefilepath is "+executefilepath);
      String[] cmd = {scriptParser,executefilepath,"" };
      Process proc = Runtime.getRuntime().exec(cmd);
      InputStream inputStream = proc.getInputStream();
      BufferedReader bufferedRreader =
          new BufferedReader(new InputStreamReader(inputStream));
      // save first line
      String lineToRead = "";
      StringBuffer resultStringBuffer = new StringBuffer();
      if ((lineToRead = bufferedRreader.readLine()) != null) {
          resultStringBuffer.append(lineToRead);
          System.out.println(lineToRead);
      }
      // save next lines
      while ((lineToRead = bufferedRreader.readLine()) != null) {
          resultStringBuffer.append("\r\n");
          resultStringBuffer.append(lineToRead);
          System.out.println(lineToRead);
      }
        }catch(Exception e){
          e.printStackTrace();
        }
    }
}
