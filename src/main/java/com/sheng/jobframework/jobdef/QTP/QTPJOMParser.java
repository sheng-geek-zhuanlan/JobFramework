package com.sheng.jobframework.jobdef.QTP;

import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.JOMParser;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


//import framework.JobEngine.QTPEngine;


public class QTPJOMParser extends JOMParser {
    //public static String

    public QTPJOMParser() {
    }

    public TestJobElement loadOwnJOM(Node node) {
        Element ele = (Element)node;
        String factoryMode =
            ele.getAttribute(TestJobDOM.node_attribute_acmode);
        TestJobElement acjob = new TestJobElement();
        switch (TestJobDOM.nameToInt(node.getNodeName())) {
        case TestJobDOM.job_qtp_case_int:
            {
                acjob = new TestJobElement(TestJobType.CASE);
            }
            break;
        case TestJobDOM.job_qtp_test_int:
            {
                acjob = new TestJobElement(TestJobType.TEST);
            }
            break;
        default:
            {
                acjob = new QTPEngine();
            }
        }
        NodeList nodes = node.getChildNodes();
        // validateXMLUseSchema("/framework/JobDef/QTP/QTPJOMSchema.xsd",node);
        //System.out.println("QTP JOM loading node");

        //the code must be here, since the test case name/test name should be set
        NamedNodeMap map = ele.getAttributes();
        int z = map.getLength();
        for (int j = 0; j < z; j++) {
            Node attributes = map.item(j);
            //TO DO:here need to add some code to check if the attributes is a defined in JOM
            //acjob.proccessDependency(attributes);
            acjob.addProperty(attributes.getNodeName(),
                              attributes.getNodeValue());
        }

        int ilen = nodes.getLength();
        try {
            for (int i = 0; i < ilen; i++) {
                Node childNode = nodes.item(i);
                if (childNode.getNodeType() != Node.TEXT_NODE) {
                    //System.out.println("begin to switch the node "+childNode.getNodeName());
                    switch (TestJobDOM.nameToInt(childNode.getNodeName())) {
                    case TestJobDOM.job_qtp_case_int:
                        {
                            //System.out.println("QTP JOM loading case");
                            TestJobElement childJob = new TestJobElement();
                            childJob = loadOwnJOM(childNode);
                            acjob.addChildJob(childJob);
                        }
                        break;
                    case TestJobDOM.job_qtp_test_int:
                        {
                            TestJobElement childJob = new TestJobElement();
                            childJob = loadOwnJOM(childNode);
                            acjob.addChildJob(childJob);
                        }
                        break;
                    case TestJobDOM.node_tag_testjob_int:
                        {
                            TestJobElement childJob = new TestJobElement();
                            childJob = loadOwnJOM(childNode);
                            acjob.addChildJob(childJob);
                        }
                        break;
                    default:
                        {
                            //  if(childNode.getNodeType()!=Node.COMMENT_NODE){
                            //     outputFrameLog(LOGMSG.NOT_RECOGINZED_TAG+childNode.getNodeName());
                            //  }
                        }
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

