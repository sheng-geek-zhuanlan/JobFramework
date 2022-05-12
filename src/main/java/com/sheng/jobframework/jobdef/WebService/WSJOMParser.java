package com.sheng.jobframework.jobdef.WebService;

import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACTestCaseSet;
import com.sheng.jobframework.jobdef.JOMParser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class WSJOMParser extends JOMParser {
    private static String simpleUserMode = "simple";
    private static String complexDevMode = "dev";

    public WSJOMParser() {
        super();
    }

    public TestJobElement loadOwnJOM(Node node) {
        TestJobElement acjob;
        Element ele = (Element)node;
        String wsMode = ele.getAttribute(TestJobDOM.node_attribute_wsmode);
        if (wsMode.equalsIgnoreCase(simpleUserMode)) {
            //acjob = new WSUserEngine();
            acjob = parseSimpleModeJOM(node);
        } else if (wsMode.equalsIgnoreCase(complexDevMode)) {
            //acjob = new WSDevEngine();
            acjob = parseDevModeJOM(node);
        } else if (wsMode.equalsIgnoreCase("")) {
            outputFrameLog("Warnig: wsmode is blank,will use WSUserEngine as default!");
            acjob = new WSUserEngine();
        } else {
            outputFrameLog("XXXX-Error in parse WS JOM, not recoginzed wsmode: " +
                           wsMode);
            return null;
        }
        return acjob;
    }

    private TestJobElement parseSimpleModeJOM(Node node) {
        TestJobElement acjob = new WSUserEngine();
        NodeList nodes = node.getChildNodes();
        int ilen = nodes.getLength();
        try {
            for (int i = 0; i < ilen; i++) {
                Node childNode = nodes.item(i);
                if (childNode.getNodeType() != Node.TEXT_NODE) {
                    //System.out.println("begin to switch the node "+childNode.getNodeName());
                    SimpleSOAPCmdSet soapCmdSet =
                        (SimpleSOAPCmdSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_WSSIMPLECMD);
                    soapCmdSet.parseNode(childNode);
                }
            }
        } catch (Exception e) {
            outputFrameLog(LOGMSG.EXCEPTION_JOM_LOAD + e.getMessage());
            e.printStackTrace();
        }
        return acjob;
    }

    private TestJobElement parseDevModeJOM(Node node) {
        TestJobElement acjob = new WSDevEngine();
        NodeList nodes = node.getChildNodes();
        int ilen = nodes.getLength();
        try {
            for (int i = 0; i < ilen; i++) {
                Node childNode = nodes.item(i);
                if (childNode.getNodeType() != Node.TEXT_NODE) {
                    String nodename = childNode.getNodeName();
                    if (nodename.equalsIgnoreCase(TestJobDOM.node_tag_testcase)) {
                        ACTestCaseSet caseset =
                            (ACTestCaseSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_SEL);
                        caseset.parseNode(childNode);
                        //System.out.println("here is the testJob output branch"+job.item(i).getNodeName());
                    }
                }
            }
        } catch (Exception e) {
            outputFrameLog(LOGMSG.EXCEPTION_JOM_LOAD + e.getMessage());
            e.printStackTrace();
        }
        return acjob;
    }
}
