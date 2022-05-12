package com.sheng.jobframework.jobdef.Deamon;

//import framework.Append.ACDeamonSet;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.JOMParser;

import org.w3c.dom.Node;


//import framework.JobEngine.DeamonEngine;

public class DeamonJOMParser extends JOMParser {
    public DeamonJOMParser() {
    }

    public TestJobElement loadOwnJOM(Node node) {
        TestJobElement acjob = new DeamonEngine();
        ACDeamonSet deamonSet =
            (ACDeamonSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DEAMON);
        deamonSet.parseNode(node);
        return acjob;
    }
}
