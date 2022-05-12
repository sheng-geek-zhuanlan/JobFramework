package com.sheng.jobframework.jobq;

import com.sheng.jobframework.Styler;
import frameservice.JTCPClient;

import com.sheng.jobframework.utility.JobUtil;
import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.TestJobDOM;

import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.TestJobElement;

import com.sheng.jobframework.jobdef.ACJobAppendController;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobOutput;

import java.util.Enumeration;
import java.util.Properties;


public class ProcessQ extends ACElement {
    public static ToRunJobQueue waitingQ = ToRunJobQueue.getInstance();
    public static RunnedJobQueue runnedQ = RunnedJobQueue.getInstance();

    public ProcessQ() {
    }

    public static void addJobToLocalWatingQ(TestJobElement job) {
        job.setJobStatus(JobStatus.PENDING);
        waitingQ.addJobToQ(job);
        outputLog("****has added job <" + job.getName() +
                  "> into local waiting Q successfully");
    }

    public static void addJobToLocalRunnedQ(TestJobElement job) {
        job.setJobStatus(JobStatus.FINISH);
        runnedQ.addJobToQ(job);
    }

    public static TestJobElement getCurrentRunningJob() {
        return (TestJobElement)waitingQ.getQ().getFirst();
    }

    public static TestJobElement waitJobArrivedInRunnedQ(TestJobElement job,
                                                         int timeoutSec) {
        try {

            outputLog("******waiting the job returned from remote host until time out " +
                      timeoutSec + " seconds");
            long startTimeInMillis = Utility.getCurrentTimeInMillis();
            long currentTimeInMillis = Utility.getCurrentTimeInMillis();
            String jobname = job.getName();
            while (currentTimeInMillis <
                   startTimeInMillis + timeoutSec * 1000) {
                TestJobElement runnedJob = runnedQ.getJob(jobname);
                if (runnedJob == null) {
                    Thread.sleep(Styler.FREQUENCY_CHECK_RUNNED_Q);
                    currentTimeInMillis = Utility.getCurrentTimeInMillis();
                } else {
                    extractRemoteRunnedJob(runnedJob);
                    outputLog("*****Job returned from remote host, will be caught JOM screen in C:\\ACDebug!");
                    if (TestJobElement.debugMode)
                        JobUtil.snapJOMScreen(runnedJob);
                    //here will control the runnedJob rerun number
                    //runnedJob.rerunNum++;
                    return runnedJob;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void extractRemoteRunnedJob(TestJobElement job) {
        //this will retract the job output to current host AC channel
        ACJobOutput joboutput =
            (ACJobOutput)job.appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_OUTPUT);
        Properties outputProp = joboutput.getProp();
        Enumeration keys = outputProp.keys();
        outputLog("***Start Extracting the remote job output:");
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            job.setOutputValue(strKey, outputProp.getProperty(strKey));
        }
        outputLog("***End of Extracting");
    }

    public static void parentConnectOperation(TestJobElement job) {
        job.parentJob = null;
    }

    public static void compactToRemoteRunJob(TestJobElement job) {
        //compact job before sent to remote host.
        outputLog("***Start compacting the job before sending");
        job.addProperty(TestJobDOM.DEFINED_ATTRIBUTE_HOST, "");
        job.getDependencyArr().clear();
        //write global data channel back to output
        ACJobOutput joboutput =
            (ACJobOutput)job.appenderCtrl.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_OUTPUT);
        //remove the class loder from memory before sent to remote host
        ACJobAppendController aCJobAppendController = job.getAppenderCtrl();
        //for jemmy remote job, the jar must be loaded and initialized at remoted job, so the class appender could not be removed at this point.
        //aCJobAppendController.removeJobAppender(ACJobAppender.APPENDER_CLASS_NAME_CLASS);
        Properties channelProp = subscriber.getServiceSubscribedDataProp();
        Enumeration keys = channelProp.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String textvalue = channelProp.getProperty(strKey);
            outputLog("******reverting GDC key/value=" + strKey + "//" +
                      textvalue + " to output Prop");
            joboutput.setJobOutput(strKey, textvalue);
        }
        outputLog("***End of Compacting");

    }

    public static void addJobToRemoteQ(TestJobElement job, String host) {

        compactToRemoteRunJob(job);
        JTCPClient qClient = new JTCPClient();
        if (TestJobElement.debugMode)
            JobUtil.snapJOMScreen(job);
        //job=JOM.composeJobByLocalFile("TestJobFile.xml");
        try {
            qClient.submitToRemoteQ(job, host);
        } catch (Exception e) {
            outputLog("XXXXX-Exception when add Job " + job.getName() +
                      " to remote host " + host);
        }

        //waitingQ.addJobToQ(job);
    }

    public static boolean addJobToRemoteWatingQ(TestJobElement job,
                                                String host) {
        boolean ret = true;
        TestJobElement clonedjob = JobUtil.deepCloneJob(job);
        /*modified on 03-07 that will send the cloned job to remote host, not current, because job must be cut off the parent-child dependency before sending to remote host, if do this operation to the current job, that will bring much risk.
    compactToRemoteRunJob(job);
    parentConnectOperation(job);
    */
        compactToRemoteRunJob(clonedjob);
        parentConnectOperation(clonedjob);
        JTCPClient qClient = new JTCPClient();
        //job=JOM.composeJobByLocalFile("TestJobFile.xml");
        //JOM.snapJOMScreen(job);
        if (TestJobElement.debugMode)
            JobUtil.snapJOMScreen(clonedjob);
        try {
            //qClient.submitToRemoteQ(job,host);
            ret = qClient.submitToRemoteQ(clonedjob, host);
        } catch (Exception e) {
            outputLog("XXXXX-Exception when add Job " + job.getName() +
                      " to remote host " + host);
            ret = false;
            return ret;
        }
        return ret;
    }

    public static TestJobElement trimRemoteRunnedJob(TestJobElement job) {
        //trim the job before sent to remote host
        outputLog("deep trimed Test JOb before sent to remote Q!");
        //JOM.snapJOMScreen(job);
        TestJobElement cloneJob = new TestJobElement();
        cloneJob = JobUtil.deepTrimedJob(job);
        ACJobAppendController aCJobAppendController =
            cloneJob.getAppenderCtrl();
        aCJobAppendController.removeJobAppender(ACJobAppender.APPENDER_CLASS_NAME_CLASS);
        return cloneJob;

    }

    public static boolean addJobToRemoteRunnedQ(TestJobElement job,
                                                String host) {
        boolean ret = true;
        TestJobElement clonedJob = new TestJobElement();
        //shining modified 2011-0317
        //compactToRemoteRunJob(clonedJob);
        compactToRemoteRunJob(job);
        //end added
        clonedJob = trimRemoteRunnedJob(job);
        outputLog("job has been runned ,will be caught snapshot before sending back to host " +
                  host);
        //comment because of the exception run on bej301035. disabled.

        //JobUtil.snapJOMScreen(clonedJob);
        JTCPClient qClient = new JTCPClient();
        //job=JOM.composeJobByLocalFile("TestJobFile.xml");
        try {
            qClient.submitToRemoteQ(clonedJob, host);
        } catch (Exception e) {
            outputLog("XXXXX-Exception when add Job " + job.getName() +
                      " to remote host " + host);
            e.printStackTrace();
            return false;
        }
        return ret;
    }

    public static void main(String[] args) {
        ProcessQ.addJobToRemoteQ(null, "");
    }
}
