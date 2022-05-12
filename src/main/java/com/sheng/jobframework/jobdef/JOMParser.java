package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;

import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.TestJobElement;

import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


//import framework.JobEngine.QTPEngine;


public abstract class JOMParser extends ACElement {
    public JOMParser() {
    }

    public abstract TestJobElement loadOwnJOM(Node node);

    private void loadCommonJOM(TestJobElement acjob, Node node) {
        //parset the attribute into job file
        Element ele = (Element)node;
        NamedNodeMap map = ele.getAttributes();
        int z = map.getLength();
        for (int j = 0; j < z; j++) {
            Node attributes = map.item(j);
            //TO DO:here need to add some code to check if the attributes is a defined in JOM
            acjob.proccessDependency(attributes);
            acjob.addProperty(attributes.getNodeName(),
                              attributes.getNodeValue());
        }
        //parse the child nodes into appenders
        NodeList nodes = node.getChildNodes();
        int ilen = nodes.getLength();
        try {
            for (int i = 0; i < ilen; i++) {
                Node childNode = nodes.item(i);
                if (childNode.getNodeType() != Node.TEXT_NODE) {
                    //System.out.println("begin to switch the node "+childNode.getNodeName());
                    switch (TestJobDOM.nameToInt(childNode.getNodeName())) {
                    case TestJobDOM.node_tag_propfile_int:
                        {
                            ACTestEnv testenv =
                                (ACTestEnv)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
                            testenv.parseEnvFileNode(childNode);
                        }
                        break;
                    case TestJobDOM.node_tag_prop_int:
                        {
                            ACTestEnv testenv =
                                (ACTestEnv)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
                            testenv.parseEnvPropertyNode(childNode);
                        }
                    case TestJobDOM.node_tag_config_int:
                        {
                            ACTestConfig testconfig =
                                (ACTestConfig)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
                            testconfig.parseConfPropertyNode(childNode);
                        }
                        break;
                    case TestJobDOM.node_tag_lib_int:
                        {
                            ACLibSet libset =
                                (ACLibSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_LIB);
                            libset.parseNode(childNode);
                        }
                        break;
                    case TestJobDOM.node_tag_classpath_int:
                        {
                            ACJavaClassSet classset =
                                (ACJavaClassSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CLASS);
                            classset.parseNode(childNode);
                        }
                        break;
                    case TestJobDOM.node_tag_jobinput_int:
                        {
                            ACJobInput jobinput =
                                (ACJobInput)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_INPUT);
                            jobinput.parseNode(childNode);
                        }
                        break;
                    case TestJobDOM.node_tag_joboutput_int:
                        {
                            ACJobOutput joboutput =
                                (ACJobOutput)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_OUTPUT);
                            joboutput.parseNode(childNode);
                            //System.out.println("here is the testJob output branch"+job.item(i).getNodeName());
                        }
                        break;

                    case TestJobDOM.node_tag_run_int:
                        {
                            //isRunnable=true;
                            ACRunSet runset =
                                (ACRunSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_RUN);
                            runset.parseNode(childNode);
                        }
                        break;
                    case TestJobDOM.node_tag_data_int:
                        {
                            ACTestDataSet dataset =
                                (ACTestDataSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);
                            dataset.parseNode(childNode);
                        }
                        break;
                    case TestJobDOM.node_tag_configfile_int:
                        {
                            ACTestConfig configset =
                                (ACTestConfig)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
                            configset.parseConfFileNode(childNode);
                        }
                        break;
                    case TestJobDOM.node_tag_selParaFile_int:
                        {
                            ACTestConfig configset =
                                (ACTestConfig)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
                            configset.parseSelConfFileNode(childNode);
                        }
                        break;
                    case TestJobDOM.node_tag_prekillpro_int:
                        {
                            ACPreKillPro prekillset =
                                (ACPreKillPro)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_PREKILL);
                            prekillset.parseNode(childNode);
                        }
                        break;
                    case TestJobDOM.node_tag_prestate_int:
                        {
                            ACPreState jobprestate =
                                (ACPreState)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_PRESTATE);
                            jobprestate.parseNode(childNode);
                        }
                        break;
                    case TestJobDOM.node_tag_poststate_int:
                        {
                            ACPostState jobpoststate =
                                (ACPostState)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_POSTSTATE);
                            jobpoststate.parseNode(childNode);
                        }
                        break;
                    default:
                        {
                        }
                    }
                }
            }
        } catch (Exception e) {
            outputFrameLog(LOGMSG.EXCEPTION_JOM_LOAD + e.getMessage());
            e.printStackTrace();
        }

    }

    public TestJobElement loadJOM(Node node) {
        TestJobElement job = loadOwnJOM(node);
        loadCommonJOM(job, node);
        return job;
    }

    public void validateXMLUseSchema(String schemaPath, Node node) {
        try {
            String schemaLang = "http://www.w3.org/2001/XMLSchema";
            SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
            InputStream is = this.getClass().getResourceAsStream(schemaPath);
            StreamSource sc = new StreamSource(is);
            Schema schema = factory.newSchema(sc);
            Validator validator = schema.newValidator();
            // at last perform validation:
            Source source = new DOMSource(node);
            validator.validate(source);
        } catch (Exception e) {
            //outputFrameLog("XML valid error: "+e.getMessage());
            //e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //JOMParser jOMParser = new JOMParser();
        //test code
        //oracle.ocs.maui.junit.rest_apis.bhmail.MauiBhmailSendMsgTest test = new oracle.ocs.maui.junit.rest_apis.bhmail.MauiBhmailSendMsgTest();
    }
}
