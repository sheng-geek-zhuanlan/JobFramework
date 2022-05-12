package com.sheng.jobframework.jobdef.Deamon;

import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdef.ACJobAppender;

import com.sheng.jobframework.commands.ACDeamonJob;
import com.sheng.jobframework.commands.BuildReport;
import com.sheng.jobframework.commands.ImpReport;
import com.sheng.jobframework.commands.MailReport;
import com.sheng.jobframework.commands.RestructureJob;
import com.sheng.jobframework.commands.UploadReport;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


//import framework.JobEngine.JavaEngine;


public class ACDeamonSet extends ACJobAppender {
    ArrayList deamonJobArr = new ArrayList(100);

    public ACDeamonSet() {
    }

    public ArrayList getDeamonJobArr() {
        return deamonJobArr;
    }

    public void addDeamonJob(ACDeamonJob job) {
        deamonJobArr.add(job);
    }

    public void initialize() {
        //System.out.append("In ACDemonSet initialize: the text of AC Job is "+prop.getProperty("acjobouterxml"));
        //temp solution since found after several run, the ac root node is missing.
        String outerxml = prop.getProperty("acjobouterxml");
        String trimedStr = outerxml.trim();
        if (!trimedStr.startsWith("<AC")) {
            outputFrameLog("*****Deamon job apppended!");
            outerxml = "<AC>" + outerxml + "</AC>";
        }
        Node node = JOM.getNodeFromOuterXML(outerxml);
        if (node.hasChildNodes()) {
            NodeList jobList = node.getChildNodes();
            int iJobs = jobList.getLength();

            for (int i = 0; i < iJobs; i++) {
                Node job = jobList.item(i);
                if (job.getNodeType() != Node.TEXT_NODE) {
                    switch (TestJobDOM.nameToInt(job.getNodeName())) {
                    case TestJobDOM.node_tag_buildreport_int:
                        {
                            BuildReport buildreport = new BuildReport();
                            buildreport.parseNode(job);
                            addDeamonJob(buildreport);
                        }
                        break;
                    case TestJobDOM.node_tag_mailreport_int:
                        {
                            MailReport mailreport = new MailReport();
                            mailreport.parseNode(job);
                            addDeamonJob(mailreport);
                        }
                        break;
                    case TestJobDOM.node_tag_uploadreport_int:
                        {
                            UploadReport uploadreport = new UploadReport();
                            uploadreport.parseNode(job);
                            addDeamonJob(uploadreport);
                        }
                        break;
                    case TestJobDOM.node_tag_report2db_int:
                        {
                            ImpReport impreport = new ImpReport();
                            impreport.parseNode(job);
                            addDeamonJob(impreport);
                        }
                        break;
                    case TestJobDOM.node_tag_restructjob_int:
                        {
                            RestructureJob restructreport =
                                new RestructureJob();
                            restructreport.parseNode(job);
                            addDeamonJob(restructreport);
                        }
                        break;
                    default:
                        {
                            System.out.println("in ACDeamonSet The job is not defined <" +
                                               job.getNodeName() + ">");
                        }
                    }
                }
            }
        }
    }

    public void parseNode(Node node) {
        prop.put("acjobouterxml", JOM.getOuterXML(node));
        //System.out.append("In ACDemonSet parsing node: the text of AC Job is "+JOM.getOuterXML(node));
        //System.out.println(JOM.getOuterXML(node));

    }
}
