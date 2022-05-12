package com.sheng.jobframework.jobdef.Java;

import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.JOMParser;

import org.w3c.dom.Node;


//import framework.JobEngine.JavaEngine;
//import framework.JobEngine.JunitEngine;


public class JavaJOMParser extends JOMParser {
    public JavaJOMParser() {
    }

    public TestJobElement loadOwnJOM(Node node) {
        TestJobElement acjob = new JavaEngine();
        validateXMLUseSchema("/com/sheng/jobframework/jobdef/Java/JavaJOMSchema.xsd", node);
        return acjob;
    }
}
