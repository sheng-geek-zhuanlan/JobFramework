package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.annotation.TestJobDOM;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class ACJobInput extends ACJobAppender {

    public ACJobInput() {
    }

    public void initialize() {

    }

    public void addIutputPara(String key) {
        prop.put(key, "");
    }

    public void setInput(String key, String value) {
        prop.put(key, value);
    }

    public String getInputValueByName(String key) {
        return getContent(key);
    }

    public void parseNode(Node node) {
        Element ele = (Element)node;
        String paraName = ele.getAttribute(TestJobDOM.node_attribute_name);
        addIutputPara(paraName);
    }
}
