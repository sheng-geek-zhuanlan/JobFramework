package com.sheng.jobframework.jobq;

import com.sheng.jobframework.Scheduler;
import com.sheng.jobframework.Styler;
import com.sheng.jobframework.utility.DataSubscriber;
import com.sheng.jobframework.utility.JobUtil;

import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.TestJobDOM;

import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.TestJobElement;

import com.sheng.jobframework.jobdef.ACTestEnv;

import com.sheng.jobframework.runner.StateRunner.ConfScenarioManager;

import java.util.LinkedList;


public class QueuePeeker extends ACElement implements Runnable {
    private JobQueue peekQ;

    public QueuePeeker() {
        super();
    }

    public void run() {
        String Qtype = peekQ.getQType();
        if (Qtype.equalsIgnoreCase(JobQueue.Q_TYPE_TORUN)) {
            outputFrameLog("----[PEEKER] TORUN Job Q peeker is starting work-----");
            peekToRunQ();
        } else if (Qtype.equalsIgnoreCase(JobQueue.Q_TYPE_RUNNED)) {
            outputFrameLog("----[PEEKER] RUNNED Job Q peeker is starting work-----");
            peekRunnedQ();
        } else {
            outputFrameLog("XXXX--the Q type was not recognized with value " +
                           Qtype);
        }
    }
    /*
     * Potential risk:
     * if the to be run job have interator, that means in the run time, the job will be cloned multiple times, but only one job will be written into runnedQ.
     * this only happens a remote sent job has the interator.
     */

    private void peekToRunQ() {
        try {
            while (true) {

                Thread.sleep(Styler.FREQUENCY_CHECK_TORUN_Q);
                //Thread.sleep(2000);
                //outputFrameLog("----ToRun Q is being peeked-----------------");
                LinkedList Q = peekQ.getQ();
                int Qsize = Q.size();
                while (Qsize > 0) {
                    outputFrameLog("----ToRun Job Qsize is  " + Qsize +
                                   " will get first Job to run");
                    //TestJobElement suiteJob = (TestJobElement)Q.getFirst();
                    TestJobElement suiteJob = (TestJobElement)Q.peek();
                    suiteJob.setJobStatus(JobStatus.RUNNING);
                    outputFrameLog("----Got the job from waiting Q, name is  " +
                                   suiteJob.getName());
                    //outputFrameLog("----the child job size is   "+suiteJob.getChildNodesNum());
                    Scheduler testscheduler;
                    if (suiteJob.getProperty(TestJobDOM.node_attribute_type).equalsIgnoreCase(TestJobDOM.node_attribute_type_conf)) {
                        testscheduler = new ConfScenarioManager();
                    } else {
                        testscheduler = new Scheduler();
                    }

                    DataSubscriber.getInstance().getServiceSubscribedDataProp().clear();
                    suiteJob.extractEnvProperty2GDC();

                    suiteJob.setScheduler(testscheduler);
                    ACTestEnv envInfo = new ACTestEnv();
                    testscheduler.initJob(envInfo);
                    testscheduler.run();
                    Q.poll();
                    Qsize = Q.size();
                    //after job finished, write it into runned job Q
                    suiteJob.setJobStatus(JobStatus.FINISH);
                    outputFrameLog("******job <" + suiteJob.getName() +
                                   "> has run completed ,will be pushed into local Runned Q!");
                    ProcessQ.addJobToLocalRunnedQ(suiteJob);
                }
            }
        } catch (Exception e) {
            outputFrameLog("XXXX--Exception occurs in ToRunQ Peeker!");
            e.printStackTrace();
        }
    }

    private void peekRunnedQ() {
        try {
            while (true) {
                //Thread.sleep(2000);
                Thread.sleep(Styler.FREQUENCY_CHECK_RUNNED_Q);
                //outputFrameLog("----ToRun Q is being peeked-----------------");
                LinkedList Q = peekQ.getQ();
                int Qsize = Q.size();
                while (Qsize > 0) {
                    outputFrameLog("----Runned Job Qsize is  " + Qsize +
                                   " will parse the fisrt Job");
                    TestJobElement suiteJob = (TestJobElement)Q.peek();

                    outputFrameLog("----Got the job from Runned Q, name is  " +
                                   suiteJob.getName() + " with status <" +
                                   suiteJob.getJobStatus() + "> results <" +
                                   suiteJob.getResults());
                    String srcHost = suiteJob.popCurrentSrcHost();
                    if (!srcHost.equalsIgnoreCase("")) {
                        outputFrameLog("***This is a remoted job sent from " +
                                       srcHost);
                        outputFrameLog("---send back the job to the src host" +
                                       srcHost);

                        ProcessQ.addJobToRemoteRunnedQ(suiteJob, srcHost);

                    } else {
                        outputFrameLog("---will remove the job from local Runned Q after " +
                                       Styler.TIME_STAY_RUNNED_Q +
                                       " minute, since it is no need to send back!");
                    }
                    //wait 300 s, then pop, so the job could be caught if it is job back from remote, will be used by local
                    if (TestJobElement.debugMode)
                        JobUtil.snapJOMScreen(suiteJob);
                    Thread.sleep(Styler.TIME_STAY_RUNNED_Q);
                    //outputFrameLog("---Remove the job"+suiteJob.getName()+" from local Runned Q!");
                    Q.poll();
                    Qsize = Q.size();
                }
            }
        } catch (Exception e) {
            outputFrameLog("XXXX--Exception occurs in Runned Q Peeker!");
            e.printStackTrace();
        }
    }

    public void setPeekQ(JobQueue q) {
        peekQ = q;
    }
}
