package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.annotation.TestJobDOM;

import java.util.ArrayList;
import java.util.Enumeration;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class ACLibSet extends ACJobAppender {
    private ArrayList libArr = new ArrayList(100);
    public int i = 0;

    public ACLibSet() {
    }

    public void addLib(String libpath) {
        //libArr.add(libpath);
        prop.put("lib" + i, libpath);
        i++;
    }

    public void parseNode(Node node) {
        Element ele = (Element)node;
        String libpath = ele.getAttribute(TestJobDOM.node_attribute_location);
        addLib(libpath);
        //ACLibSet libset = new ACLibSet();
        //libset.addLib(libpath);
        //return libpath;
    }

    public void addLib(ACLibSet libset) {
        addMore(libset);
    }

    public void initialize() {
        // System.out.println("output lib set");
        //prop.list(System.out);
    }

    public String getQTPFormatlib() {
        String ret = "";
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            if (!ret.equalsIgnoreCase(""))
                ret = ret + ";";
            String strKey = (String)keys.nextElement();
            ret = ret + prop.getProperty(strKey);
        }
        return ret;
    }
}
