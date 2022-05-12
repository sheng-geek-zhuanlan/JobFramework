package com.sheng.jobframework.jobdef.Jmeter;

import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.JOMParser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


//import framework.JobEngine.AntEngine;
//import framework.JobEngine.QTPEngine;


public class JmeterJOMParser extends JOMParser {
    public JmeterJOMParser() {
    }

    public TestJobElement loadOwnJOM(Node node) {
        Element ele = (Element)node;
        TestJobElement acjob = new JmeterEngine();
        NodeList nodes = node.getChildNodes();
        int ilen = nodes.getLength();
        try {
            for (int i = 0; i < ilen; i++) {
                Node childNode = nodes.item(i);
                if (childNode.getNodeType() != Node.TEXT_NODE) {
                    //System.out.println("begin to switch the node "+childNode.getNodeName());

                    switch (TestJobDOM.nameToInt(childNode.getNodeName())) {
                        //<JMXPath
                    case TestJobDOM.node_tag_JMX_int:
                        {
                            ACJmeterSet jmeterset =
                                (ACJmeterSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_JMETER);
                            jmeterset.parseJMXNode(childNode);
                            //System.out.println("here is the testJob output branch"+job.item(i).getNodeName());
                        }
                        break;
                    case TestJobDOM.node_tag_threads_int:
                        {
                            ACJmeterSet jmeterset =
                                (ACJmeterSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_JMETER);
                            jmeterset.parseNode(childNode);
                            //System.out.println("here is the testJob output branch"+job.item(i).getNodeName());
                        }
                        break;
                    case TestJobDOM.node_tag_loops_int:
                        {
                            ACJmeterSet jmeterset =
                                (ACJmeterSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_JMETER);
                            jmeterset.parseNode(childNode);
                            //System.out.println("here is the testJob output branch"+job.item(i).getNodeName());
                        }
                        break;
                    case TestJobDOM.node_tag_rampup_int:
                        {
                            ACJmeterSet jmeterset =
                                (ACJmeterSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_JMETER);
                            jmeterset.parseNode(childNode);
                            //System.out.println("here is the testJob output branch"+job.item(i).getNodeName());
                        }
                        break;
                    case TestJobDOM.node_tag_rampdown_int:
                        {
                            ACJmeterSet jmeterset =
                                (ACJmeterSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_JMETER);
                            jmeterset.parseNode(childNode);
                            //System.out.println("here is the testJob output branch"+job.item(i).getNodeName());
                        }
                        break;
                    default:
                        {
                            outputFrameLog("XXXXX-unkown tag name : " +
                                           childNode.getNodeName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            outputFrameLog(LOGMSG.EXCEPTION_JOM_LOAD + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return acjob;
    }
}

