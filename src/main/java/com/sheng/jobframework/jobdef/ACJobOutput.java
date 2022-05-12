package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.annotation.TestJobDOM;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class ACJobOutput extends ACJobAppender {
    public ACJobOutput() {
    }

    public void initialize() {

    }

    public void addOutputPara(String key) {
        prop.put(key, "");
    }

    public void addOutputPara(String key, String sql) {
        prop.put(key, sql);
    }

    public void setJobOutput(String key, String value) {
        prop.put(key, value);
        //writeIntoComChannel(key,value);
        //outputFrameLog("Developping Mode: output parameter from Job File "+key+":"+value);
    }

    public String getOutputValueByName(String key) {
        return getContent(key);
    }

    public void parseNode(Node node) {
        Element ele = (Element)node;
        String paraName = ele.getAttribute(TestJobDOM.node_attribute_name);

        String paraValue = ele.getAttribute((TestJobDOM.node_attribute_value));
        //System.out.println("Job output name:value is "+paraName+":"+paraValue);
        String fetchSQL = ele.getAttribute((TestJobDOM.node_attribute_sql));
        if (!paraValue.equalsIgnoreCase("")) {
            outputFrameLog("---setting outputValue of JobOutput: " + paraName +
                           ":" + paraValue);
            writeIntoComChannel(paraName, paraValue);
        } else if (!fetchSQL.equalsIgnoreCase("")) {
            addOutputPara(paraName, fetchSQL);
        } else {
            addOutputPara(paraName);
        }

    }
}
