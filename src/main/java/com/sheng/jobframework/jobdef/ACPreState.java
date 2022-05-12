package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.annotation.TestJobDOM;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class ACPreState extends ACJobAppender {
    public ACPreState() {
        super();
    }

    public void initialize() {

    }

    public void addPreState(String key) {
        prop.put(key, "");
    }

    public void parseNode(Node node) {
        Element ele = (Element)node;
        String paraName = ele.getAttribute(TestJobDOM.node_attribute_name);
        addPreState(paraName);
    }
}
