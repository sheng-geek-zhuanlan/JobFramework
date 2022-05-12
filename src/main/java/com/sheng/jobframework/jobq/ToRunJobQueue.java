package com.sheng.jobframework.jobq;

public class ToRunJobQueue extends JobQueue {
    private static ToRunJobQueue jobQ = null;

    public ToRunJobQueue() {
        super();
        outputFrameLog("----[QUEUE] starting toRunJobQ, setting the QType to " +
                Q_TYPE_TORUN);
        setQType(Q_TYPE_TORUN);
        startPeeker();
    }

    public static ToRunJobQueue getInstance() {
        if (jobQ == null) {
            jobQ = new ToRunJobQueue();
        }
        return jobQ;
    }

    public static void main(String[] args) {
        ToRunJobQueue toRunJobQueue = new ToRunJobQueue();
    }
}
