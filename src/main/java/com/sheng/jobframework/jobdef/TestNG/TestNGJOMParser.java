package com.sheng.jobframework.jobdef.TestNG;

import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACTestCaseSet;
import com.sheng.jobframework.jobdef.JOMParser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TestNGJOMParser extends JOMParser {
    public TestNGJOMParser() {
        super();
    }
  public TestJobElement loadOwnJOM(Node node) {
      Element ele = (Element)node;
      TestJobElement acjob = new TestNGEngine();
      NodeList nodes = node.getChildNodes();
      int ilen = nodes.getLength();
      try {
          for (int i = 0; i < ilen; i++) {
              Node childNode = nodes.item(i);
              if (childNode.getNodeType() != Node.TEXT_NODE) {
                  //System.out.println("begin to switch the node "+childNode.getNodeName());
                  switch (TestJobDOM.nameToInt(childNode.getNodeName())) {
                  case TestJobDOM.node_tag_selcase_int:
                      {
                          ACTestCaseSet caseset =
                              (ACTestCaseSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_SEL);
                          caseset.parseNode(childNode);
                          //System.out.println("here is the testJob output branch"+job.item(i).getNodeName());
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
