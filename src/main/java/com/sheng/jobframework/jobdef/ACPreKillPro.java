package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.utility.OSCmdUtil;

import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;

import java.util.Enumeration;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class ACPreKillPro extends ACJobAppender {
    int i = 0;

    public ACPreKillPro() {
    }

    public void addPrekillPro(String name) {
        prop.put("processname_" + i, name);
        i++;
    }

    public void initialize() {
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String proToBeKilled = prop.getProperty(strKey);
            OSCmdUtil.killProcess(proToBeKilled);
            outputFrameLog(LOGMSG.PRE_KILL_PROCESS + proToBeKilled);
        }
    }

    public void parseNode(Node node) {
        Element ele = (Element)node;
        String proname = ele.getAttribute(TestJobDOM.node_attribute_name);
        addPrekillPro(proname);
        //setRunPath(runpath);
    }
}
