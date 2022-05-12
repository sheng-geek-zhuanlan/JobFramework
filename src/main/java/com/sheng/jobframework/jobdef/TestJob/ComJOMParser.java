package com.sheng.jobframework.jobdef.TestJob;

import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.JOMParser;

import org.w3c.dom.Node;


public class ComJOMParser extends JOMParser {
    public ComJOMParser() {
    }

    public TestJobElement loadOwnJOM(Node node) {
        TestJobElement acjob = new TestJobElement();
        return acjob;
    }
}
