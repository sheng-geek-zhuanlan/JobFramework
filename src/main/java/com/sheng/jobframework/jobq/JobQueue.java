package com.sheng.jobframework.jobq;

import com.sheng.jobframework.Styler;
import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.TestJobElement;

import java.util.LinkedList;


public class JobQueue extends ACElement {
    public static String QUERY_STATUS_NOTFOUND = "NOT FOUND";
    private static String jobfile = Styler.jobfile;
    public static String Q_TYPE_TORUN = "Q_TORUN";
    public static String Q_TYPE_RUNNED = "Q_RUNNED";
    private String my_type = "";
    private LinkedList Q = new LinkedList();
    // private LinkedBlockingQueue<TestJobElement> Q = new LinkedBlockingQueue<TestJobElement>();
    private QueuePeeker peeker = new QueuePeeker();

    public JobQueue() {
        super();
        //outputFrameLog("custructing JobQueue");

    }

    public void setQType(String type) {
        my_type = type;
    }

    public void startPeeker() {
        setPeeker(peeker);
    }

    public String getQType() {
        return my_type;
    }

    public void addJobToQ(TestJobElement job) {
        Q.add(job);
    }

    public void setPeeker(QueuePeeker peeker) {
        peeker.setPeekQ(this);
        Thread t = new Thread(peeker);
        t.start();

    }

    public LinkedList getQ() {
        return Q;
    }

    private TestJobElement getJobByIndex(int i) {
        TestJobElement[] jobArr = (TestJobElement[])Q.toArray();
        return jobArr[i];
    }

    public TestJobElement getJob(String name) {
        int Qsize = Q.size();
        for (int i = 0; i < Qsize; i++) {
            TestJobElement job = (TestJobElement)Q.get(i);
            //TestJobElement job = getJobByIndex(i);
            String runnedJobName = job.getName();
            if (runnedJobName.equalsIgnoreCase(name)) {
                return job;
            }
        }
        return null;
    }

    public void removeJobFromQ(TestJobElement job) {
        Q.remove(job);
    }

    public String getJobStatus(String jobid) {
        int Qsize = Q.size();
        String retStatus = QUERY_STATUS_NOTFOUND;
        for (int i = 0; i < Qsize; i++) {
            TestJobElement job = (TestJobElement)Q.get(i);
            //TestJobElement job = getJobByIndex(i);
            String id = job.getJobID();
            if (jobid.equalsIgnoreCase(id)) {
                retStatus = job.getJobStatus();
            }
        }
        return retStatus;
    }

    public TestJobElement getJobByName(String jobname) {
        int Qsize = Q.size();
        String retStatus = null;
        for (int i = 0; i < Qsize; i++) {
            TestJobElement job = (TestJobElement)Q.get(i);
            //TestJobElement job = getJobByIndex(i);
            String name = job.getName();
            if (jobname.equalsIgnoreCase(name)) {
                //firstlly dumpped into local disk
                return job;
            }
        }
        return null;
    }

    public TestJobElement getJobByID(String jobid) {
        int Qsize = Q.size();
        String retStatus = null;
        for (int i = 0; i < Qsize; i++) {
            TestJobElement job = (TestJobElement)Q.get(i);
            //TestJobElement job = getJobByIndex(i);
            String id = job.getJobID();
            if (jobid.equalsIgnoreCase(id)) {
                //firstlly dumpped into local disk
                return job;
            }
        }
        return null;
    }


    public static void main(String[] args) {
        JobQueue jobQueue = new JobQueue();
    }
}
