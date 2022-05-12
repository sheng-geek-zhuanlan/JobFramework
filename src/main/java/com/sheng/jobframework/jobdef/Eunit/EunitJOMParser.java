package com.sheng.jobframework.jobdef.Eunit;

import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.JOMParser;

import com.sheng.jobframework.jobdef.Junit.JunitEngine;

import org.w3c.dom.Node;


//import framework.JobEngine.JunitEngine;
//import framework.JobEngine.QTPEngine;


public class EunitJOMParser extends JOMParser {
    public EunitJOMParser() {
    }

    public TestJobElement loadOwnJOM(Node node) {
        TestJobElement acjob = new JunitEngine();
        return acjob;
    }
}
