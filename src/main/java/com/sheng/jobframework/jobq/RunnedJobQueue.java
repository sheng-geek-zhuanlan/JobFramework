package com.sheng.jobframework.jobq;

public class RunnedJobQueue extends JobQueue {
    private static RunnedJobQueue jobQ;

    public RunnedJobQueue() {
        super();
        outputFrameLog("----[QUEUE] starting runned Job Q, setting the QType to " +
                Q_TYPE_RUNNED);
        setQType(Q_TYPE_RUNNED);
        startPeeker();
    }

    public static RunnedJobQueue getInstance() {
        if (jobQ == null) {
            jobQ = new RunnedJobQueue();
        }
        return jobQ;
    }

    public static void main(String[] args) {
        RunnedJobQueue runnedJobQueue = new RunnedJobQueue();
    }
}
