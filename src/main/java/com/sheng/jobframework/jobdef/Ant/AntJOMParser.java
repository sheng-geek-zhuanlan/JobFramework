package com.sheng.jobframework.jobdef.Ant;

import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.JOMParser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


//import framework.JobEngine.AntEngine;
//import framework.JobEngine.QTPEngine;


public class AntJOMParser extends JOMParser {
    public AntJOMParser() {
    }

    public TestJobElement loadOwnJOM(Node node) {
        Element ele = (Element)node;
        TestJobElement acjob = new AntEngine();
        NodeList nodes = node.getChildNodes();
        int ilen = nodes.getLength();
        try {
            for (int i = 0; i < ilen; i++) {
                Node childNode = nodes.item(i);
                if ((childNode.getNodeType() != Node.TEXT_NODE) &&
                    (childNode.getNodeType() != Node.COMMENT_NODE)) {
                    ACAntSet antset =
                        (ACAntSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ANT);
                    antset.parseNode(childNode);
                }
            }
        } catch (Exception e) {
            outputFrameLog(LOGMSG.ANT_PARSE_NODE + e.getMessage());
            e.printStackTrace();
        }

        return acjob;
    }
}
