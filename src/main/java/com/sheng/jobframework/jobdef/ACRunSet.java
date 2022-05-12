package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.annotation.TestJobDOM;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ACRunSet extends ACJobAppender {
    String runPath = "";
    ArrayList runPathArr = new ArrayList(100);
    int i = 0;

    public ACRunSet() {
    }

    public void setRunPath(String path) {
        runPath = path;
    }

    public void addRunPath(String path) {
        runPathArr.add(path);
    }

    public String getRunPath() {
        return runPath;
    }

    public ArrayList getRunSetArr() {
        return runPathArr;
    }

    public String getCurrentRunPath() {
        int isize = runPathArr.size();
        if (isize > 0)
            return (String)runPathArr.get(isize - 1);
        else
            System.out.println("Error in ACRunSet: there is no run path to get!");
        return "";
    }

    public void addRunArg(String argline) {
        //argArr.add(argline);
        prop.put("line" + i, argline);
        i++;

    }

    public void initialize() {
    }

    public void parseNode(Node node) {
        Element ele = (Element)node;
        String runpath = ele.getAttribute(TestJobDOM.node_attribute_path);
        //setRunPath(runpath);
        addRunPath(runpath);
        if (node.hasChildNodes()) {
            NodeList argList = node.getChildNodes();
            int iArg = argList.getLength();
            for (int i = 0; i < iArg; i++) {
                Node arg = argList.item(i);
                if (arg.getNodeName().endsWith(TestJobDOM.node_tag_arg)) {
                    Element tempele = (Element)arg;
                    String argline =
                        tempele.getAttribute(TestJobDOM.node_attribute_line);
                    addRunArg(argline);
                }
            }
        }
    }
}
