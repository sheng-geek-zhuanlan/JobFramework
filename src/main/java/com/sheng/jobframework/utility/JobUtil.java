package com.sheng.jobframework.utility;

import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppendController;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobParaParser;
import com.sheng.jobframework.jobq.JobQueue;
import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;

import com.sheng.jobframework.commands.JobCommander;
import com.sheng.jobframework.commands.RemoteJobCommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.net.URLClassLoader;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class JobUtil extends ACElement {
    public JobUtil() {
        super();
    }

    public static void setJobFinished(TestJobElement job) {
        job.isRunned = true;
        job.rerunNum++;
        job.setJobStatus(JobStatus.FINISH);
        job.isRunning = false;
    }
    //the name could be either jobname or jobid

    public static TestJobElement getFullStatusJOM(JobQueue jobQ, String name) {
        TestJobElement job = jobQ.getJobByName(name);
        if (job == null)
            job = jobQ.getJobByID(name);
        if (job == null)
            return null;
        TestJobElement statusJob = statusCloneJob(job);
        return statusJob;
    }

    public static String getFullStatusJOM2Str(JobQueue jobQ, String name) {
        //firstly get a cloned status job
        TestJobElement statusjob = getFullStatusJOM(jobQ, name);
        String retStatus = null;
        if (statusjob == null)
            return retStatus;
        //firstlly dumpped into local disk, with a xml node
        String dumpFilePath = snapJOMStatusScreen(statusjob);
        //read from local disk to string
        if (dumpFilePath != null) {
            retStatus = FileUtil.fullDumppedFile2Str(dumpFilePath);
            return retStatus;
        }
        return retStatus;
    }

    private static TestJobElement statusCloneJob(TestJobElement job) {
        try {
            ClassLoader sysloader = ClassLoader.getSystemClassLoader();
            URLClassLoader loader = ClassLoaderUtil.getClassLoaderInstance();
            String classname = job.getClass().getName();
            TestJobElement clonedjob =
                (TestJobElement)Class.forName(classname).newInstance();
            Properties elementProp = job.getElementProperty();
            Enumeration elmentInfoKeys = elementProp.keys();
            while (elmentInfoKeys.hasMoreElements()) {
                String strKey = (String)elmentInfoKeys.nextElement();
                String value = elementProp.getProperty(strKey);
                clonedjob.addProperty(strKey, value);
                // ele.setAttribute(strKey,value);
            }
            clonedjob.setElementType(job.getElementType());
            clonedjob.isRunnable = job.isRunnable;
            clonedjob.setBeginTime(job.getBeginTimeL());
            clonedjob.setEndTime(job.getEndTimeL());
            clonedjob.isFactoryMode = job.isFactoryMode;
            clonedjob.isRunned = job.isRunned;
            clonedjob.setJobStatus(job.getJobStatus());
            clonedjob.isDeamonJob = job.isDeamonJob;
            clonedjob.routeStack = job.routeStack;


            int ichilds = job.getChildNodesNum();
            for (int j = 0; j < ichilds; j++) {
                TestJobElement childJob =
                    (TestJobElement)job.getChildNodesByIndex(j);
                if (childJob.getProperty(TestJobDOM.DEFINED_ATTRIBUTE_REMOTERUN).equalsIgnoreCase("true")) {
                    RemoteJobCommand cmd = new RemoteJobCommand();
                    cmd.setCommand(RemoteJobCommand.STATUS_QUERY);
                    cmd.setJobName(childJob.getName());
                    String host = childJob.getRunningRemoteHost();
                    TestJobElement returnedjob =
                        JobCommander.sendCommand(cmd, host);
                    job.replaceChildNode(childJob, returnedjob);
                }
                clonedjob.addChildJob(statusCloneJob(childJob));
                //ele.appendChild(getJOMScreen(doc,childJob));
            }
            return clonedjob;
        } catch (Exception e) {
            outputLog("Error: clone failed of " + job.getName());
            outputLog(e.getMessage());
            e.printStackTrace();
            return null;
        }

    }
    //deep clone was used for iterations clone/remote job clone

    public static TestJobElement deepCloneJob(TestJobElement job) {
        try {
            //ClassLoaderUtil loader = ClassLoaderUtil.getClassLoaderInstance();
            //ClassLoader sysloader = ClassLoader.getSystemClassLoader();
            ClassLoader sysloader = ClassLoader.getSystemClassLoader();
            URLClassLoader loader = ClassLoaderUtil.getClassLoaderInstance();
            String classname = job.getClass().getName();

            //TestJobElement clonedjob= (TestJobElement)loader.loadClass(classname).newInstance();
            //TestJobElement clonedjob= (TestJobElement)sysloader.loadClass(classname).newInstance();
            TestJobElement clonedjob =
                (TestJobElement)Class.forName(classname).newInstance();

            //doc.appendChild(ele);
            Properties elementProp = job.getElementProperty();
            Enumeration elmentInfoKeys = elementProp.keys();
            while (elmentInfoKeys.hasMoreElements()) {
                String strKey = (String)elmentInfoKeys.nextElement();
                String value = elementProp.getProperty(strKey);
                clonedjob.addProperty(strKey, value);
                // ele.setAttribute(strKey,value);
            }
            clonedjob.setElementType(job.getElementType());
            clonedjob.isRunnable = job.isRunnable;
            clonedjob.setBeginTime(job.getBeginTimeL());
            clonedjob.setEndTime(job.getEndTimeL());
            clonedjob.isFactoryMode = job.isFactoryMode;
            clonedjob.isRunned = job.isRunned;
            clonedjob.setJobStatus(job.getJobStatus());
            clonedjob.isDeamonJob = job.isDeamonJob;
            clonedjob.routeStack = job.routeStack;
            clonedjob.setDependencyArr(job.getDependencyArr());
            ACJobAppendController appenderCtrl = job.getAppenderCtrl();
            //ACJobAppendController clonedAppenderCtrl = appenderCtrl;
            clonedjob.appenderCtrl = appenderCtrl;

            int ichilds = job.getChildNodesNum();
            for (int j = 0; j < ichilds; j++) {
                TestJobElement childJob =
                    (TestJobElement)job.getChildNodesByIndex(j);
                clonedjob.addChildJob(deepCloneJob(childJob));
                //ele.appendChild(getJOMScreen(doc,childJob));
            }
            return clonedjob;
        } catch (Exception e) {
            outputLog("Error: clone failed of " + job.getName());
            outputLog(e.getMessage());
            e.printStackTrace();
            return null;
        }

    }
    //this was used to trim the job to only testjobelement.
    //deepTrimedJob was used for job trimed before sending back to the host.

    public static TestJobElement deepTrimedJob(TestJobElement job) {
        try {

            String classname = job.getClass().getName();
            TestJobElement clonedjob = new TestJobElement();
            //doc.appendChild(ele);
            Properties elementProp = job.getElementProperty();
            Enumeration elmentInfoKeys = elementProp.keys();
            while (elmentInfoKeys.hasMoreElements()) {
                String strKey = (String)elmentInfoKeys.nextElement();
                String value = elementProp.getProperty(strKey);
                clonedjob.addProperty(strKey, value);
                // ele.setAttribute(strKey,value);
            }
            clonedjob.addProperty("protype", classname);
            clonedjob.setElementType(job.getElementType());
            clonedjob.isRunnable = job.isRunnable;
            clonedjob.rerunNum = job.rerunNum;
            clonedjob.isFactoryMode = job.isFactoryMode;
            clonedjob.isRunned = job.isRunned;
            clonedjob.setBeginTime(job.getBeginTimeL());
            clonedjob.setEndTime(job.getEndTimeL());
            clonedjob.setJobStatus(job.getJobStatus());
            clonedjob.isDeamonJob = job.isDeamonJob;
            //clonedjob
            ACJobAppendController appenderCtrl = job.getAppenderCtrl();
            //ACJobAppendController clonedAppenderCtrl = appenderCtrl;
            clonedjob.appenderCtrl = appenderCtrl;

            int ichilds = job.getChildNodesNum();
            for (int j = 0; j < ichilds; j++) {
                TestJobElement childJob =
                    (TestJobElement)job.getChildNodesByIndex(j);
                clonedjob.addChildJob(deepTrimedJob(childJob));
                //ele.appendChild(getJOMScreen(doc,childJob));
            }
            return clonedjob;
        } catch (Exception e) {
            outputLog("Error: deep trimed failed of " + job.getName());
            outputLog(e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    private static String snapJOMStatusScreen(TestJobElement job) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        String ret = null;
        try {
            builder = dbf.newDocumentBuilder();
        } catch (Exception e) {
            outputLog(LOGMSG.EXCEPTION_SNAPJOM_NEWDOC + e.getMessage());
            e.printStackTrace();
            return ret;
        }
        try {
            Document doc = builder.newDocument();
            Element ele = getJOMStatusScreen(doc, job);
            doc.appendChild(ele);
            String outfile =
                OSCmdUtil.getDebugTraceDir() + File.separator + Utility.getStrCurrentTime() +
                "_" + job.getName() + "_" + job.getJobID() + "_status.xml";
            File f = new File(outfile);
            if (!f.exists()) {
                String parentDir = f.getParent();
                File dir = new File(parentDir);
                if (!dir.exists())
                    dir.mkdirs();
                f.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(outfile);
            OutputStreamWriter outwriter = new OutputStreamWriter(fos);
            callWriteXmlFile(doc, outwriter, "utf-8");
            return outfile;
        } catch (Exception e) {
            outputLog(LOGMSG.EXCEPTION_SNAPJOM_WRITEDOC + e.getMessage());
            e.printStackTrace();
            return ret;
        }

    }

    private static void callWriteXmlFile(Document doc, java.io.Writer w,
                                         String encoding) {
        try {
            Source source = new DOMSource(doc);
            Result result = new StreamResult(w);
            Transformer xformer =
                TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private static Element getJOMStatusScreen(Document doc,
                                              TestJobElement job) {
        Element ele = doc.createElement("TestJob");
        //doc.appendChild(ele);
        //doc.createElement()
        SimpleDateFormat formatter =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(job.getBeginTimeL());
        String strdtmBegin = formatter.format(c.getTime());
        if ((job.getEndTimeL() == 0) &&
            (job.getJobStatus().equalsIgnoreCase(JobStatus.RUNNING))) {
            c.setTimeInMillis(System.currentTimeMillis());
        }
        c.setTimeInMillis(job.getEndTimeL());
        String strdtmEnd = formatter.format(c.getTime());
        ele.setAttribute("name", job.getName());
        ele.setAttribute("beginTime", strdtmBegin);

        ele.setAttribute("endTime", strdtmEnd);

        ele.setAttribute("jobStatus", job.getJobStatus());
        ele.setAttribute("result", job.getResults());
        ele.setAttribute("isDeamonJob", Boolean.toString(job.isDeamonJob));
        ele.setAttribute("type", job.getElementType());
        if (job.getElementType().equalsIgnoreCase(TestJobType.STEP)) {
            if (!job.getProperty(JobStatus.PASSED).equalsIgnoreCase("") ||
                !job.getProperty(JobStatus.PASSED.toUpperCase()).equalsIgnoreCase("")) {
                ele.setAttribute("result", TestResult.PASS);
                ele.setAttribute("name",
                                 job.getProperty(JobStatus.PASSED) + job.getProperty(JobStatus.PASSED.toUpperCase()));
                ele.setAttribute("jobStatus", JobStatus.FINISH);
            }
            if (!job.getProperty(JobStatus.FAILED).equalsIgnoreCase("") ||
                !job.getProperty(JobStatus.FAILED.toUpperCase()).equalsIgnoreCase("")) {
                ele.setAttribute("result", TestResult.FAIL);
                ele.setAttribute("name",
                                 job.getProperty(JobStatus.FAILED) + job.getProperty(JobStatus.FAILED.toUpperCase()));
                ele.setAttribute("jobStatus", JobStatus.FINISH);
            }
            if (!job.getProperty("done").equalsIgnoreCase("") ||
                !job.getProperty("DONE").equalsIgnoreCase("")) {
                ele.setAttribute("result", TestResult.PASS);
                ele.setAttribute("name",
                                 job.getProperty("done") + job.getProperty("DONE"));
                ele.setAttribute("jobStatus", JobStatus.FINISH);
            }
        }
        int ichilds = job.getChildNodesNum();
        for (int j = 0; j < ichilds; j++) {
            TestJobElement childJob =
                (TestJobElement)job.getChildNodesByIndex(j);
            ele.appendChild(getJOMStatusScreen(doc, childJob));
        }
        return ele;
    }


    //get the JOM screen(more than status) to the the local disk temp directory

    public static void snapJOMScreen(TestJobElement job) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = dbf.newDocumentBuilder();
        } catch (Exception e) {
            outputLog(LOGMSG.EXCEPTION_SNAPJOM_NEWDOC + e.getMessage());
            e.printStackTrace();
        }
        try {
            Document doc = builder.newDocument();
            Element ele = getJOMScreen(doc, job);
            doc.appendChild(ele);
            String outfile =
                OSCmdUtil.getDebugTraceDir() + File.separator + Utility.getStrCurrentTime() +
                "_" + job.getName() + "_full.xml";
            File f = new File(outfile);
            if (!f.exists()) {
                String parentDir = f.getParent();
                File dir = new File(parentDir);
                if (!dir.exists())
                    dir.mkdirs();
                f.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(outfile);
            OutputStreamWriter outwriter = new OutputStreamWriter(fos);
            callWriteXmlFile(doc, outwriter, "utf-8");
        } catch (Exception e) {
            outputLog(LOGMSG.EXCEPTION_SNAPJOM_WRITEDOC + e.getMessage());
            e.printStackTrace();

        }

    }

    private static Element getJOMScreen(Document doc, TestJobElement job) {


        Element ele = doc.createElement("TestJob");
        //doc.appendChild(ele);
        Properties elementProp = job.getElementProperty();
        Enumeration elmentInfoKeys = elementProp.keys();
        while (elmentInfoKeys.hasMoreElements()) {
            String strKey = (String)elmentInfoKeys.nextElement();
            String value = elementProp.getProperty(strKey);
            ele.setAttribute(strKey, value);
        }
        //doc.createElement()
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(job.getBeginTimeL());
        String strdtmBegin = formatter.format(c.getTime());
        c.setTimeInMillis(job.getEndTimeL());
        String strdtmEnd = formatter.format(c.getTime());
        ele.setAttribute("beginTime", strdtmBegin);
        ele.setAttribute("endTime", strdtmEnd);
        ele.setAttribute("elementType", job.getElementType());
        ele.setAttribute("isRunnable", Boolean.toString(job.isRunnable));
        ele.setAttribute("isFactoryMode", Boolean.toString(job.isFactoryMode));
        ele.setAttribute("jobStatus", job.getJobStatus());
        ele.setAttribute("isDeamonJob", Boolean.toString(job.isDeamonJob));
        ele.setAttribute("driver_type", job.getDriverType());
        ACJobAppendController appenderCtrl = job.getAppenderCtrl();
        ArrayList appenderList = appenderCtrl.getAppenderList();
        int isize = appenderList.size();
        for (int i = 0; i < isize; i++) {
            ACJobAppender appender = (ACJobAppender)appenderList.get(i);
            String classname = appender.getClass().getName();
            Element appenderele = doc.createElement(classname);
            Properties prop = appender.getProp();
            Enumeration propKeys = prop.keys();
            while (propKeys.hasMoreElements()) {
                String key = (String)propKeys.nextElement();
                String value = prop.getProperty(key);
                //System.out.println("key/value: "+key+"/"+value);
                if (key.startsWith(ACJobParaParser.paraIndicator)) {
                    key = key.substring(1);
                }
                if ((value instanceof String) && value.equalsIgnoreCase("")) {
                    value = "NA";
                }
                //key=Utility.transferCDATA(key);
                //value=Utility.transferCDATA(value);
                //System.out.println("In JOM snapscreen, key value is "+key+":"+value);
                //value=ReportUtil.transferCDATA(value);
                //key=ReportUtil.transferCDATA(key);
                //System.out.println("key is "+key+"  value is "+value);
                appenderele.setAttribute(key, value);
            }
            ele.appendChild(appenderele);
        }
        int ichilds = job.getChildNodesNum();
        for (int j = 0; j < ichilds; j++) {
            TestJobElement childJob =
                (TestJobElement)job.getChildNodesByIndex(j);
            ele.appendChild(getJOMScreen(doc, childJob));
        }
        return ele;
    }

    public static void parseEnvProperty2Child(TestJobElement job,
                                              String propertyname) {

        if (job.ifHasChildNodes()) {
            int childNumbers = job.getChildNodesNum();
            for (int i = 0; i < childNumbers; i++) {
                TestJobElement childJob =
                    (TestJobElement)job.getChildNodesByIndex(i);
                if (propertyname.equalsIgnoreCase("ALL")) {
                    childJob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV).overridedBy(job.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV));
                    //outputFrameLog("try to set "+childJob.getName()+"to result"+status);
                    parseEnvProperty2Child(childJob, propertyname);
                }
            }
        }
    }

    public static void attachDebugInfo(TestJobElement job) {
        String strOSName = System.getProperty("os.name");
        String strOSVersion = System.getProperty("os.version");


        String jreVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        /*
    * the folloowing code is not portable until we find a solution
    if(!(OSCmdUtil.getOSType()==OSCmdUtil.MAC)){
      long usedMem = Runtime.getRuntime().totalMemory();
      String mb_usedSize = Long.toString(usedMem/1000);
      OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
      long totalMemorySize = osmxb.getTotalPhysicalMemorySize();
      String mb_memorySize = Long.toString(totalMemorySize/1000);
      String usage = Long.toString(usedMem*100/totalMemorySize);
      double cpuratio = OSCmdUtil.getCpuRatio();
      String ratio = Double.toString(cpuratio);
      addProperty("TOTALMEMORY",mb_memorySize);
      addProperty("USEDMEMORY",mb_usedSize);
      addProperty("USAGEMEMORY",usage);
      addProperty("USAGECPU",ratio);
    }
    */
        job.addProperty("OS", strOSName);
        job.addProperty("OSVERSION", strOSVersion);
        job.addProperty("JAVAVERSION", jreVersion);
        job.addProperty("JAVAVENDOR", javaVendor);
        int ichild = job.getChildNodesNum();
        for (int i = 0; i < ichild; i++) {
            TestJobElement child = (TestJobElement)job.getChildNodesByIndex(i);
            if (!child.getElementType().equalsIgnoreCase(TestJobType.STEP)) {
                attachDebugInfo(child);
            }
        }

    }


}
