package com.sheng.jobframework.jobdef.Jmeter;

import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.jobdef.ACJobAppender;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class ACJmeterSet extends ACJobAppender {
    public int threads = 0;
    public int loops = 0;
    public int rampup = 0;
    public int rampdown = 0;
    public int ramp_time = 0;
    public String JMXPath = "";

    public ACJmeterSet() {
    }

    public void parseNode(Node node) {
        String parsename = node.getNodeName();
        Element ele = (Element)node;
        String strNum = ele.getAttribute(TestJobDOM.node_attribute_value);
        //System.out.println("the parse name is "+parsename+" has number is "+strNum);
        if (parsename.equalsIgnoreCase(TestJobDOM.node_tag_threads)) {
            threads = Integer.parseInt(strNum);
        }
        if (parsename.equalsIgnoreCase(TestJobDOM.node_tag_loops)) {
            loops = Integer.parseInt(strNum);
        }
        if (parsename.equalsIgnoreCase(TestJobDOM.node_tag_rampup)) {
            rampup = Integer.parseInt(strNum);
        }
        if (parsename.equalsIgnoreCase(TestJobDOM.node_tag_rampdown)) {
            rampdown = Integer.parseInt(strNum);
        }
        if (parsename.equalsIgnoreCase(TestJobDOM.node_tag_ramptime)) {
            ramp_time = Integer.parseInt(strNum);
        }
    }

    public int getThreads() {
        return threads;
    }

    public int getLoops() {
        return loops;
    }

    public int getRampup() {
        return rampup;
    }

    public int getRampdown() {
        return rampdown;
    }

    public int getRamptime() {
        return ramp_time;
    }

    public String getJMXPath() {
        return JMXPath;
    }

    public void parseJMXNode(Node node) {
        Element ele = (Element)node;
        String jmxpath = ele.getAttribute(TestJobDOM.node_attribute_location);
        JMXPath = FileUtil.getAbsolutePath(jmxpath);
        //ACLibSet libset = new ACLibSet();
        //libset.addLib(libpath);
        //return libpath;
    }

    public void initialize() {
        if (JMXPath.equalsIgnoreCase("")) {
            outputFrameLog("XXXXX-JMXPath is null!");
        }
    }
}
