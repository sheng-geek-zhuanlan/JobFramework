package com.sheng.jobframework.jobdef.IOS;

import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.JOMParser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


//import framework.JobEngine.AntEngine;
//import framework.JobEngine.QTPEngine;


public class IOSJOMParser extends JOMParser {
    public IOSJOMParser() {
    }

    public TestJobElement loadOwnJOM(Node node) {
        Element ele = (Element)node;
        TestJobElement acjob = new IOSEngine();
        return acjob;
    }
}
