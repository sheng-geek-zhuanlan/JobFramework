package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.annotation.TestJobDOM;

import com.sheng.jobframework.jobdom.JOM;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ACTestCaseSet extends ACJobAppender {
    int i = 0;
    public ArrayList selCaseArr = new ArrayList(100);

    public ACTestCaseSet() {
    }

    public void initialize() {

    }

    public void addCase(ACTestCase selcase) {
        selCaseArr.add(selcase);
    }

    public ArrayList getCaseArr() {
        return selCaseArr;
    }

    public void addProp(String text) {
        prop.put("selcase" + i, text);
    }

    public void parseNode(Node node) {
        Element ele = (Element)node;
        String casepath = ele.getAttribute(TestJobDOM.node_attribute_path);
        String nodetext = JOM.getOuterXML(node);
        addProp(nodetext);
        //setRunPath(runpath);
        ACTestCase selcase = new ACTestCase();
        selcase.setCasepath(casepath);
        String casename = "";
        if (casepath.contains(".")) {
            int g = casepath.lastIndexOf(".");
            casename = casepath.substring(g + 1);
        }
        selcase.setCaseName(casename);
        if (node.hasChildNodes()) {
            NodeList testList = node.getChildNodes();
            int iTests = testList.getLength();
            for (int i = 0; i < iTests; i++) {
                Node test = testList.item(i);
                if ((test.getNodeName().endsWith(TestJobDOM.node_tag_seltest)) ||
                    (test.getNodeName().endsWith(TestJobDOM.node_tag_test))) {
                    Element tempele = (Element)test;
                    String testname =
                        tempele.getAttribute(TestJobDOM.node_attribute_name);
                    selcase.addTest(testname);
                    ;
                }
            }
        }
        addCase(selcase);
    }
}
