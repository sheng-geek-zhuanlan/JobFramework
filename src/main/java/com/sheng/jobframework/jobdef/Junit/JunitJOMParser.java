package com.sheng.jobframework.jobdef.Junit;

import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.JOMParser;

import org.w3c.dom.Node;


//import framework.JobEngine.JunitEngine;
//import framework.JobEngine.QTPEngine;


public class JunitJOMParser extends JOMParser {
    public JunitJOMParser() {
    }

    public TestJobElement loadOwnJOM(Node node) {
        TestJobElement acjob = new JunitEngine();
        return acjob;
    }
}
