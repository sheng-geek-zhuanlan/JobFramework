package com.sheng.jobframework.jobdef.Deamon;

import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.utility.JobUtil;
import com.sheng.jobframework.commands.ACDeamonJob;
import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobEngine;

import com.sheng.jobframework.report.main.ReportGenerate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;


//import framework.Append.ACDeamonSet;
//import framework.Append.ACJobAppender;


public class DeamonEngine extends ACJobEngine {
    private ArrayList processList = new ArrayList(10);

    public DeamonEngine() {
    }

    public void runEntityJob() {
        ACDeamonSet deamonSet =
            (ACDeamonSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DEAMON);
        //System.out.println("here is in DeamonEngine");
        //outputFrameLog("here is in DeamonEngine");
        ArrayList deamonJobArr = deamonSet.getDeamonJobArr();
        int isize = deamonJobArr.size();
        TestJobElement rootJob = getRootJob();
        //to end the root job here, so that the host infomation could be collected
        rootJob.collectHostInfo();
        rootJob.addEnvProperty("BUILD",
                               getDataFromACChannel("BUILD", getEnvProperty("BUILD")));
        rootJob.addEnvProperty("CLIENT_VERSION",
                               getDataFromACChannel("CLIENT_VERSION",
                                                    getEnvProperty("CLIENT_VERSION")));
        JobUtil.parseEnvProperty2Child(rootJob, "ALL");
        //remainRunnedJob will call JOM.checkAndWaitRemoteJobFinished() to wait until the job returned.
        if (TestJobElement.debugMode)
            JobUtil.snapJOMScreen(rootJob);
        JOM.remainRunnedJob(rootJob);
        if (TestJobElement.debugMode)
            JobUtil.snapJOMScreen(rootJob);
        processList.add(rootJob);
        /**
         * debug for JOM to xml
         */
        try {
            String locationPath =
                rootJob.getLocationPath() + File.separator + "rootJob.obj";
            locationPath = FileUtil.getAbsolutePath(locationPath);
            FileOutputStream outStream = new FileOutputStream(locationPath);
            ObjectOutputStream objectOutputStream =
                new ObjectOutputStream(outStream);
            objectOutputStream.writeObject(rootJob);
            outStream.close();
        } catch (Exception e) {
            outputFrameLog("WARNING: serialized with "+e.getMessage());
        }
        /*
      try{
        FileInputStream freader;
        String locationPath = rootJob.getLocationPath()+File.separator+"rootJob.obj";
        locationPath = FileUtil.getAbsolutePath(locationPath);
        freader = new FileInputStream(locationPath);
        ObjectInputStream objectInputStream = new ObjectInputStream(freader);
        TestJobElement job = (TestJobElement)objectInputStream.readObject();
        JobUtil.snapJOMScreen(job);
      }catch(Exception e){
        e.printStackTrace();
      }*/
        /**
         * end of debug
         */
        //JOM.removeDeamonJob(rootJob);
        for (int i = 0; i < isize; i++) {
            ACDeamonJob deamonjob = (ACDeamonJob)deamonJobArr.get(i);
            outputFrameLog("---AC Deamon Job running " + deamonjob.getName() +
                           " --------");
            addChildJob(deamonjob);
            //deamonjob.setWorkingJobDOM(rootJob);
            //deamonjob.runDeamonJob();
            deamonjob.setProcessJobList(processList);
            deamonjob.processDeamonJobList();
            //process list maybe changed during deamon job running
            processList = deamonjob.getJobProcessList();
        }
        /* here should not set results, otherwise autosetResults will throw error. since entityJob has been removed from the suitejob,
         * getParent() will throw null error.*/
        //setResults(TestResult.PASS);
    }

    public static void main(String[] args) {

        try {
            FileInputStream freader;
            String locationPath =
                FileUtil.getCurrentDir() + File.separator + "rootJob.obj";
            locationPath = FileUtil.getAbsolutePath(locationPath);
            freader = new FileInputStream(locationPath);
            ObjectInputStream objectInputStream =
                new ObjectInputStream(freader);
            TestJobElement job =
                (TestJobElement)objectInputStream.readObject();
            String testjobFile =
                FileUtil.getCurrentDir() + File.separator + "TestJobFile.xml";
            ReportGenerate rg =
                new ReportGenerate("C:\\DebugACReport\\", job, testjobFile);
            rg.execute(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
