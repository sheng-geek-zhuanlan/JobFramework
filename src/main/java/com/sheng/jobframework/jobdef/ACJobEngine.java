package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.utility.JobUtil;
import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;

import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdom.TestJobElement;

import java.io.File;

import java.util.Enumeration;
import java.util.Hashtable;


//import framework.MySecurityManager;


//runMyAcJob(Decide local or thread)->LocalMode->RunACJob(Decide iterations)
public abstract class ACJobEngine extends TestJobElement implements Runnable {
    public static int i = 1;
    public boolean isRemoteJob = false;
    public boolean isLocalJobAndThread = false;
    public String remotehost = "";

    public ACJobEngine() {
        setElementType(TestJobType.COMPONENT);
    }

    public void setRunHost(String hostname) {
        remotehost = hostname;
    }

    public String getRunHost() {
        if (remotehost.equalsIgnoreCase("")) {
            outputFrameLog("XXXXX-WARNING: the host of job<" + getName() +
                           "> is blank!");
        }
        return remotehost;
    }

    public void initACJob() {
        jobInit();
    }

    public void endACJob() {
        jobEnd();
    }

    public void run() {
        //    if(isRemoteJob){
        //     runRemoteMode();
        //    }else if(isLocalJobAndThread){
        runJobWithLocal();
        //    }
    }
    /*
    public void runRemoteMode(){
      try{
      //String runhost = getProperty(TestJobDOM.DEFINED_ATTRIBUTE_HOST);
      String runhost = getRunHost();
      InetAddress ia = InetAddress.getLocalHost();
      //String localhost = ia.getHostName();
      String  IP= ia.getHostAddress();
      //modified on 2011-01-04, push ip instead of host name, since in some network env, the host name could not be resolved
      //pushRoutePath(localhost, runhost);
      pushRoutePath(IP, runhost);
      outputFrameLog("--- -"+getName()+" is a remote job, will be sent to "+runhost);
      String timeoutvalue = getProperty(TestJobDOM.DEFINED_ATTRIBUTE_TIMEOUT);
      String defaultTimeOutValue = Styler.confProp.getProperty(Styler.DEFAULT_REMOTE_TIMEOUT,"");
      int idefaultTimeOut = Utility.strToInt(defaultTimeOutValue,3600);
      //default time out value for remote job is 3600 secons, that is one hour
      int timeout = Utility.strToInt(timeoutvalue, idefaultTimeOut);
      outputFrameLog(getLogPrefix()+"****Job<"+getName()+"> is a remote job will be sent to "+runhost);
      setJobStatus(JobStatus.RUNNING);
      isRunning=true;
      //rerunNum++;
      System.out.println("in ACJobEngine before push to remote Q: job "+getName()+" child Job size is "+getChildNodesNum());
      boolean success = ProcessQ.addJobToRemoteWatingQ(this, runhost);

      if(!success){
        outputFrameLog(getLogPrefix()+"job <"+getName()+"> failed to send host "+runhost);
        JOM.restruct2StatusJob(this, "send_remote_falied", "Exception occurs when try to send job to remote host: "+runhost, TestResult.FAIL);
        recuSetResults(this,TestResult.FAIL);
        isRunned=true;
        rerunNum++;
        setJobStatus(JobStatus.FINISH);
        isRunning=false;
        return;
      }
      outputFrameLog("job has been sent to host "+runhost+" successfully, will wait the job return!");
      isRunning=true;
      //.addJobToRemoteQ(job, runhost);
      TestJobElement returnedJob = ProcessQ.waitJobArrivedInRunnedQ(this, timeout);
      if(returnedJob!=null){
        outputFrameLog(getLogPrefix()+"*******Scheduler will replaced currentJob with returned" +returnedJob.getName());
        isRunned=true;
        isRunning=false;
        setJobStatus(JobStatus.FINISH);
        getParentJob().replaceChildNode(this,returnedJob);
        returnedJob.isRunned=true;
        return;
      }else{
        //after wait 1 hour time out, the job did not returned.
        outputFrameLog("job <"+getName()+"> wait a hours time out after sent to host "+runhost);
        JOM.restruct2StatusJob(this, "wait_remote_timeout", "Test met a time out after sent to remote host, please check framework.log on the remote host: "+runhost, TestResult.FAIL);
        recuSetResults(this,TestResult.FAIL);
        isRunned=true;
        rerunNum++;
        isRunning=false;
        setJobStatus(JobStatus.FINISH);
        return;
      }
      }catch(Exception e){
        outputFrameLog(getLogPrefix()+"XXXXX-Exception when remote engine running ");
        e.printStackTrace();
      }
    }
  */

    public void runJobWithLocal() {
        if (jobInit()) {
            //in fact, the isRunnable is surely be true in job engine, will remove this in future
            if (isRunnable) {
                //System.out.println("4 before force transformation this is "+job.getClass().getName());
                //outputFrameLog("----Current JOb rerunNum is "+rerunNum+" while max_rerunNum"+max_rerunNum);
                while ((rerunNum < max_rerunNum) &&
                       (!getResults().equalsIgnoreCase(TestResult.PASS))) {
                    if (rerunNum > 0)
                        outputFrameLog("Previous run failed, will rerun " +
                                       getName());
                    //System.out.println("before force transformation this is "+job.getClass().getName());
                    //outputFrameLog("Previous run failed, will rerun "+job.getName());
                    //System.out.println("after force transformation");
                    runJobWithIteration();
                    autoSetResults(this);
                    rerunNum++;
                    isRunned = true;
                    setJobStatus(JobStatus.FINISH);
                }
                if (jobEnd()) {
                    outputFrameLog("job <" + getName() + "> run completed");
                } else {
                    outputFrameLog("WARNING: job <" + getName() +
                                   "> did not output The output is ready!");
                }
                return;
            } else {
                //do nothing, if is not runnable job, will continue to figure out child jobs without return
            }
        } else {
            outputFrameLog("job <" + getName() +
                           "> could NOT run due to fail to init.");
            JOM.restruct2StatusJob(this, "Job_Init_Fail",
                                   "Test could not run due to dependency job failure, please check framework.log for detail trace",
                                   TestResult.CNR);
            recuSetResults(this, TestResult.CNR);
            isRunned = true;
            setJobStatus(JobStatus.FINISH);
            return;
        }
    }

    public void runWithThread() {
        String concurrentThread =
            getProperty(TestJobDOM.DEFINED_ATTRIBUTE_CONCURRENT);
        int concurrent = Utility.strToInt(concurrentThread, 1);
        TestJobElement parent = getParentJob();
        for (int i = 0; i < concurrent; i++) {
            outputFrameLog("-----start to new thread " + i + " to run job: " +
                           getName());
            if (i == 0) {

                //here comments following codes to remain the original job name, since if current job has dependency job, the dependecy job will wait the job end by its name
                //setName(getName()+"_"+host);
                //setLocationPath(parent.getLocationPath()+File.separator +getName());
                this.isLocalJobAndThread = true;
                Thread t = new Thread(this);
                t.start();
            } else {
                //ACJobEngine thisjob = (ACJobEngine)JOM.deepCloneJob(this);
                ACJobEngine newjob = (ACJobEngine)JobUtil.deepCloneJob(this);
                parent.addChildJob(newjob);
                newjob.setName(getName() + "_" + "thread_" + i);
                newjob.setLocationPath(parent.getLocationPath() +
                                       File.separator + newjob.getName());
                newjob.isLocalJobAndThread = true;
                Thread t = new Thread(newjob);
                t.start();
            }
        }
    }

    public void runMyACJob() {
        try {

            String runhost = getProperty(TestJobDOM.DEFINED_ATTRIBUTE_HOST);
            String threadMode =
                getProperty(TestJobDOM.DEFINED_ATTRIBUTE_THREAD);
            boolean threadmode = Utility.strToBoolean(threadMode);
            /*
          InetAddress ialocal = InetAddress.getLocalHost();
          String localhost = ialocal.getHostName();
          String  IP= ialocal.getHostAddress();
          String remoteIP = "";
          if(!runhost.equalsIgnoreCase("")){
            try{
              InetAddress iaremote = InetAddress.getByName(runhost);
              remoteIP = iaremote.getHostAddress();
            }catch(Exception e){
              outputFrameLog("XXXX-could not resolve the host name: "+runhost);
              JOM.restruct2StatusJob(this, "host_not_resolved", "specified remote host: "+runhost+" could not be resolved!", TestResult.FAIL);
              return;
            }
          }
          if(!runhost.equalsIgnoreCase("")&&(!IP.equalsIgnoreCase(remoteIP))){
              //start thread to remote run
              String[] hostArr = runhost.split(",");
              if(hostArr.length==1){
                outputFrameLog("-----One remote Host: start to run remote job: "+getName()+" against host: "+runhost+"---------");
                this.isRemoteJob=true;
                this.setRunHost(runhost);
                Thread t = new Thread(this);
                t.start();
              }else if(hostArr.length>1){
                int hosts= hostArr.length;
                outputFrameLog("----Total "+hosts+" hosts to run job "+getName());
                TestJobElement parent = getParentJob();
                for(int i=0;i<hosts;i++){
                    String host = hostArr[i];
                    if(i==0){
                      outputFrameLog("-----start to run remote job: "+getName()+" against host: "+host+"---------");
                      //here comments following codes to remain the original job name, since if current job has dependency job, the dependecy job will wait the job end by its name
                      //setName(getName()+"_"+host);
                      //setLocationPath(parent.getLocationPath()+File.separator +getName());
                      this.isRemoteJob=true;
                      this.setRunHost(host);
                      Thread t = new Thread(this);
                      t.start();
                    }else{
                      //ACJobEngine thisjob = (ACJobEngine)JOM.deepCloneJob(this);
                      ACJobEngine newjob = (ACJobEngine)JOM.deepCloneJob(this);
                      parent.addChildJob(newjob);
                      newjob.setName(getName()+"_"+host);
                      outputFrameLog("-----start to run remote job: "+newjob.getName()+" against host: "+host+"---------");
                      newjob.setLocationPath(parent.getLocationPath()+File.separator +newjob.getName());
                      newjob.isRemoteJob=true;
                      newjob.setRunHost(host);
                      Thread t = new Thread(newjob);
                      t.start();
                    }
                }
              }
            */
            //}else if(threadmode){
            if (threadmode) {
                runWithThread();
            } else {
                runJobWithLocal();
            }

        } catch (Exception e) {
            outputFrameLog("XXXXXX-Exception when run Job Engine");
            e.printStackTrace();
        }
    }

    public void runJobWithIteration() {
        //setting security manager
        /*
        MySecurityManager xMySecurityManager = new MySecurityManager();
        System.setSecurityManager(xMySecurityManager);
        System.out.println("setting my security manager");
*/
        //end of security
        String iterations =
            getProperty(TestJobDOM.DEFINED_ATTRIBUTE_ITERATION);
        String name = getName();
        if (iterations.equalsIgnoreCase("")) {
            //if no iteration determined, then will run the first data array
            /*
            ACTestDataSet dataset = (ACTestDataSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
            Hashtable datahash = dataset.getDataHashArr();
            Enumeration datakeys = datahash.keys();

            String indicator="";
            if(datakeys.hasMoreElements()){
                indicator = (String)datakeys.nextElement();
            }
            //outputFrameLog("----------Test Job  "+getName()+" has no iteration determined, will run fir "+iall+ " data iterations-------");
            dataset.setDataIteration(indicator);
            */
            runEntityJob();
        } else if (iterations.equalsIgnoreCase(TestJobDOM.DEFINED_ATTRIBUTE_ITERVALUE_ALL)) {
            ACTestDataSet dataset =
                (ACTestDataSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
            Hashtable datahash = dataset.getDataHashArr();
            //outputFrameLog("the datahash size has size: "+datahash.size());
            int iall = datahash.size();
            outputFrameLog("----------Test Job  " + getName() + " has total " +
                           iall + " data iterations-------");
            //Must clone the job before it first run, otherwise, the second iteration will cloned from the results of first running.
            ACJobEngine thisjob = (ACJobEngine)JobUtil.deepCloneJob(this);
            Enumeration datakeys = datahash.keys();
            int repeatnumber = 0;
            while (datakeys.hasMoreElements()) {
                repeatnumber++;
                String indicator = (String)datakeys.nextElement();
                dataset.setDataIteration(indicator);
                ACTestEnv testenv =
                    (ACTestEnv)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
                testenv.addEnvProperty(TestJobDOM.DEFINED_VALUE_INDICATOR,
                                       indicator);
                TestJobElement parent = getParentJob();
                if (repeatnumber > 1) {
                    ACJobEngine newjob =
                        (ACJobEngine)JobUtil.deepCloneJob(thisjob);
                    //ACJobEngine newjob = (ACJobEngine)JOM.deepCloneJob(this);
                    parent.addChildJob(newjob);
                    newjob.setName(name + "_" + indicator);
                    outputFrameLog("-----start to run iteration job: " +
                                   newjob.getName() + "---------");
                    newjob.setLocationPath(parent.getLocationPath() +
                                           File.separator + newjob.getName());
                    newjob.runEntityJob();
                    newjob.isRunned = true;
                    newjob.setJobStatus(JobStatus.FINISH);
                    newjob.jobEnd();
                } else {
                    setName(name + "_" + indicator);
                    outputFrameLog("-----start to run iteration job: " +
                                   getName() + "---------");
                    setLocationPath(parent.getLocationPath() + File.separator +
                                    getName());
                    runEntityJob();
                    jobEnd();
                }
            }
        } else {
            String[] iterArr = iterations.split(",");
            int iterNums = iterArr.length;
            int repeatnumber = 0;
            outputFrameLog("----------Test Job  " + getName() + " has total " +
                           iterNums + " data iterations!----------");
            ACJobEngine thisjob = (ACJobEngine)JobUtil.deepCloneJob(this);
            //
            for (int i = 0; i < iterNums; i++) {
                repeatnumber++;
                String indicator = iterArr[i];
                ACTestDataSet dataset =
                    (ACTestDataSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);

                dataset.setDataIteration(indicator);
                Hashtable datahash = dataset.getDataHashArr();
                // outputFrameLog("iteration sepcified: the datahash size has size: "+datahash.size());
                ACTestEnv testenv =
                    (ACTestEnv)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
                testenv.addEnvProperty(TestJobDOM.DEFINED_VALUE_INDICATOR,
                                       indicator);
                TestJobElement parent = getParentJob();
                if (repeatnumber > 1) {
                    ACJobEngine newjob =
                        (ACJobEngine)JobUtil.deepCloneJob(thisjob);
                    //ACJobEngine newjob = (ACJobEngine)JOM.deepCloneJob(this);
                    ACTestDataSet dataset2 =
                        (ACTestDataSet)newjob.getAppenderCtrl().getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
                    Hashtable datahash2 = dataset2.getDataHashArr();
                    //outputFrameLog("the datahash size in the cloned job has size: "+datahash2.size());
                    newjob.setName(name + "_" + indicator);
                    outputFrameLog("-----start to run iteration job: " +
                                   newjob.getName() + "---------");
                    newjob.setLocationPath(parent.getLocationPath() +
                                           File.separator + newjob.getName());
                    parent.addChildJob(newjob);
                    newjob.runEntityJob();
                    newjob.isRunned = true;
                    newjob.setJobStatus(JobStatus.FINISH);
                    newjob.jobEnd();
                } else {
                    setName(name + "_" + indicator);
                    outputFrameLog("-----start to run iteration job: " +
                                   getName() + "---------");
                    setLocationPath(parent.getLocationPath() + File.separator +
                                    getName());
                    runEntityJob();
                    jobEnd();
                }

            }
        }
    }

    public abstract void runEntityJob();

    public void recuSetResults(TestJobElement job, String status) {
        job.setResults(status);
        //outputFrameLog("try to set "+getName()+" with result"+status);
        if (job.ifHasChildNodes()) {
            int childNumbers = job.getChildNodesNum();
            //    outputFrameLog("has child jobs"+childNumbers);
            for (int i = 0; i < childNumbers; i++) {
                TestJobElement childJob =
                    (TestJobElement)job.getChildNodesByIndex(i);
                //outputFrameLog("try to set "+childJob.getName()+"to result"+status);
                recuSetResults(childJob, status);
            }
        }
    }

    public void autoSetResults(TestJobElement job) {

        TestJobElement testJob = job;
        //outputFrameLog(getElementType()+" job <"+getName()+"> has status with "+getResults());
        if (testJob.getResults().equalsIgnoreCase("") ||
            testJob.getResults().equalsIgnoreCase(TestResult.NA)) {
            int size = testJob.getChildNodesNum();
            for (int i = 0; i < size; i++) {
                TestJobElement childJob =
                    (TestJobElement)testJob.getChildNodesByIndex(i);
                autoSetResults(childJob);
                //testJob.getChildNodesByIndex(i).autoSetResults();
                //outputFrameLog(testJob.getElementType()+" job <"+testJob.getName()+"> set status with "+testJob.getChildResults());
                testJob.setResults(testJob.getChildResults());
            }
        } else {
            TestJobElement parentjob = (TestJobElement)job.getParent();
            parentjob.setResults(parentjob.getChildResults());
        }
    }
}
