package com.sheng.jobframework.jobdom;

import com.sheng.jobframework.Styler;
import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.annotation.DriverType;
import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;

import com.sheng.jobframework.jobdef.ACJavaClassSet;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobInput;
import com.sheng.jobframework.jobdef.ACJobOutput;
import com.sheng.jobframework.jobdef.ACLibSet;
import com.sheng.jobframework.jobdef.ACRunSet;
import com.sheng.jobframework.jobdef.ACTestCaseSet;
import com.sheng.jobframework.jobdef.ACTestConfig;
import com.sheng.jobframework.jobdef.ACTestDataSet;
import com.sheng.jobframework.jobdef.ACTestEnv;
import com.sheng.jobframework.jobdef.Ant.ACAntSet;
import com.sheng.jobframework.jobdef.Ant.AntEngine;
import com.sheng.jobframework.jobdef.Deamon.ACDeamonSet;
import com.sheng.jobframework.jobdef.Deamon.DeamonEngine;
import com.sheng.jobframework.jobdef.JOMParser;
import com.sheng.jobframework.jobdef.Java.JavaEngine;
import com.sheng.jobframework.jobdef.Junit.JunitEngine;
import com.sheng.jobframework.jobdef.ParserFactory;
import com.sheng.jobframework.jobdef.QTP.QTPEngine;
import com.sheng.jobframework.jobdef.Selenium.SeleniumEngine;

import com.sheng.jobframework.observer.ObserverSubscriber;

import java.io.File;
import java.io.StringBufferInputStream;

import java.util.ArrayList;

import javax.mail.internet.MimeUtility;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;




public class JOM extends ACElement {

    public static ObserverSubscriber observerSubscriber =
        new ObserverSubscriber();
    // public static Logger logger =  Logger.getLogger(JOM.class);

    public static void setObserver(ObserverSubscriber observerList) {
        observerSubscriber = observerList;
    }
    /*
    public static void outputLog(String strLog){
        observerSubscriber.outputLog(strLog);
    }
   */
    //following is append object to job

    public JOM() {
    }

    public static TestJobElement composeJobByLocalFile(String jobfile) {
        FileUtil.setRemoteSys(jobfile);
        TestJobElement job = loadJOMFromXMLDoc(jobfile);
        initJobRunInfo(job);
        job.setJobStatus(JobStatus.PENDING);
        //job.setJobID("kickoffByLocal");
        return job;
    }

    public static void initJobRunInfo(TestJobElement job) {
        String currentDir = FileUtil.getCurrentDir();
        String strBaseResultsHome = "results";
        String strCurrentTime = Utility.getStrCurrentTime();
        //
        //String locationPath = FileUtil.getAbsolutePath(strBaseResultsHome + "\\"+ strCurrentTime+ "\\");
        //String locationPath = currentDir+"\\"+strBaseResultsHome + "\\"+ strCurrentTime+ "\\";
        String jobID = job.getJobID();
        if (jobID.equalsIgnoreCase("")) {
            job.setJobID("local_ts_" + strCurrentTime);
            outputLog("jobID is blank, will use <timestamp>: " +
                      job.getJobID() + " as jobID");
        }
        //String locationPath = strBaseResultsHome + File.separator + strCurrentTime+ File.separator ;
        String locationPath =
            strBaseResultsHome + File.separator + job.getJobID() +
            File.separator;
        job.setLocationPath(locationPath);
        job.setTestLang(Utility.getSystemInfo().getProperty("userLocale"));

        if (job.getName().equalsIgnoreCase(""))
            job.setName("AutomationCenterDemoSuite");
    }

    public static TestJobElement composeJobByAgentCmd(String cmdList,
                                                      String jobID) {
        ACTestEnv envInfo = new ACTestEnv();
        envInfo.parseDTECommand(cmdList, jobID, "AC");
        String defaultJobfile = Styler.jobfile;
        String job_type = envInfo.getEnvProperty(Styler.job_type);
        if (job_type.equalsIgnoreCase("mini")) {
            defaultJobfile = Styler.MINI_JOBFILE;
        }
        String jobfileToRun =
            envInfo.getEnvProperty(TestJobDOM.DEFINED_ENV_KEY_JOBFILE);
        if (!jobfileToRun.equalsIgnoreCase("")) {
            outputLog("***Parse the command List......Has got the customized job file ,will run " +
                      jobfileToRun);
            defaultJobfile = jobfileToRun;
        } else {
            //this branck is built for oracle
            String buildVersion = envInfo.getEnvProperty("BUILD");
            if (buildVersion.contains("MAIN")) {
                outputLog("***Will run TestJobFile.xml since the build is " +
                          buildVersion);
                defaultJobfile = Styler.jobfile;
            } else if (buildVersion.contains("2.0")) {
                outputLog("****Will run 2.0_TestJobFile.xml since the build is " +
                          buildVersion);
                defaultJobfile = "2.0_TestJobFile.xml";
            } else if (buildVersion.contains("2.1")) {
                outputLog("****Will run TestJobFile.xml since the build is " +
                          buildVersion);
                defaultJobfile = Styler.jobfile;
            } else {
                outputLog("Will run default TestJobFile.xml since the build is blank");
                defaultJobfile = Styler.jobfile;
            }
        }
        String localSys =
            envInfo.getEnvProperty(TestJobDOM.DEFINED_ENV_KEY_LOCALFILESYS);
        boolean bLocal = Utility.strToBoolean(localSys);
        /*comment for temp on 2012-10-17
      if(!bLocal){
        DownloadJobFile download = new DownloadJobFile();
        outputLog("**will start download job artifacts from central web");
        download.Download(defaultJobfile, FileUtil.getCurrentDir());
        outputLog("**download completed");
      }
      */
        TestJobElement job = JOM.loadJOMFromXMLDoc(defaultJobfile);
        //envInfo.addEnvProperty("JOBID", jobID);
        job.addTestEnv(envInfo);
        String historyStoreDir =
            FileUtil.getCurrentDir() + File.separator + Styler.jobhistoryDir +
            File.separator;
        String currentTimeStamp = Utility.getStrCurrentTime();
        FileUtil.mkDirsIfNotExists(historyStoreDir);
        String filePath = historyStoreDir + currentTimeStamp + "_env.xml";
        envInfo.outputToPropFile(filePath);
        job.setJobID(jobID);
        job.setJobStatus(JobStatus.PENDING);
        initJobRunInfo(job);
        return job;
    }
    // @Profiled

    public static TestJobElement loadJOMFromXMLDoc(String jobfile) {

        //  PropertyConfigurator.configureAndWatch("log4j.properties");

        //StopWatch stopWatch = new Slf4JStopWatch();
        //logger.debug("my test JOM");
        String fileprefix = "file:///";
        if (FileUtil.isLocalFile(jobfile)) {
            jobfile =
                    fileprefix + FileUtil.getCurrentDir() + File.separator + jobfile;
        } else {
            outputLog("***is to load remote file at " + jobfile);
        }
        String strSchemafile =
            FileUtil.getCurrentDir() + File.separator + "JobSchemas" +
            File.separator + "TestJobSchema.xsd";
        outputLog("******Is to validate the job xml with schema " +
                  strSchemafile);
        NodeList rootNode = null;
        Element rootElement;
        //JOM jom = new JOM();
        //valid with schema files
        boolean schema_valid =
            Utility.strToBoolean(Styler.confProp.getProperty(Styler.SCHEMA_VALID,
                                                             "false"));
        if (schema_valid) {
            //will valid the job XML file with schema files
            try {
                SchemaFactory schemaFactory =
                    SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                File schemaFile = new File(strSchemafile);
                Schema schema = schemaFactory.newSchema(schemaFile);
                Validator validator = schema.newValidator();
                Source source = new StreamSource(jobfile);
                validator.validate(source);
            } catch (Exception ex) {
                outputLog("XXXXXXX-XML validate failed, will exit with exception: " +
                          ex.getMessage());
                ex.printStackTrace();
                System.exit(11);
            }

            outputLog("******Schema Valid successfull*********** ");
        } else {
            outputLog("******Schema validation is disbaled, Please notes that potenatial risk when use illegal TestJobFile ");
        }

        //Begin to load job xml files
        outputLog(LOGMSG.LOADING_JOM + jobfile);
        try {
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(jobfile);
            rootElement = document.getDocumentElement();
            //System.out.println("here"+rootElement.getTextContent());
        } catch (Exception e) {
            outputLog(LOGMSG.FAILED_LOAD_JOM);
            outputLog(e.getMessage());
            e.printStackTrace();
            System.exit(11);
            return null;
        }
        TestJobElement job = new TestJobElement();
        //job = loadJOMFromXMLDOM(rootElement);
        job = loadACJOMFromXMLDOM(rootElement);
        job.setElementType(TestJobType.SUITE);
        //stopWatch.stop("example1","custommessagetext");

        //formatTheLevelOfJob(job);
        outputLog(LOGMSG.SUCCEED_LOAD_JOM);
        return job;

    }

    public static TestJobElement mergeAllBlocksOfCompJobs(TestJobElement job,
                                                          String compname) {
        int isize = job.getChildNodesNum();
        ArrayList compBlocksList = new ArrayList(100);
        for (int i = 0; i < isize; i++) {
            TestJobElement childJob =
                (TestJobElement)job.getChildNodesByIndex(i);
            //TO DO: here is potential risk, if access job2,then all the childs will point to job1.
            //TestJobElement cloneJob = childJob.clone();
            if (childJob.getName().contains(compname)) {
                compBlocksList.add(childJob);
            }
        }
        TestJobElement mergedComp = new TestJobElement();
        int arrMergeSize = compBlocksList.size();
        if (arrMergeSize > 0) {
            mergedComp = (TestJobElement)compBlocksList.get(0);
            for (int j = 1; j < arrMergeSize; j++) {
                TestJobElement toBeMergedJob =
                    (TestJobElement)compBlocksList.get(j);
                TestJobElement cloneJob = toBeMergedJob.clone();
                mergedComp = mergeJobs(mergedComp, cloneJob);
                job.removeChildNode(toBeMergedJob);
            }
            mergedComp.setName(compname);
            mergedComp.setLocationPath(mergedComp.getParentJob().getLocationPath() +
                                       File.separator + mergedComp.getName());
        }
        return mergedComp;
    }

    public static TestJobElement mergeJobs(TestJobElement job1,
                                           TestJobElement job2) {
        int isize2 = job2.getChildNodesNum();
        for (int i = 0; i < isize2; i++) {
            TestJobElement childJob2 =
                (TestJobElement)job2.getChildNodesByIndex(i);
            //TO DO: here is potential risk, if access job2,then all the childs will point to job1.
            job1.addChildJob(childJob2);
        }
        return job1;
    }

    public static TestJobElement loadJOMFromXMLDOM(Node node) {
        //To DO, here need to be enhanced the job did not know his type until he enter this function
        //System.out.println("here output the env info of "+getName());

        //System.out.println(node.getTextContent());
        Element ele = (Element)node;
        //String s = getOuterXML(node);
        /*
        Text txt = (Text)node;
        String s = node.getTextContent();
        Document d = node.getOwnerDocument();
        //Document d1 = d.getOwnerDocument();
        String s1 = d.toString();
        //s1 = tempe.getTextContent();
         */
        //System.out.println("the string is "+s);

        TestJobElement acjob;
        String curDrv = ele.getAttribute(TestJobDOM.node_attribute_drvtype);
        String runACAPI = ele.getAttribute(TestJobDOM.node_attribute_acmode);
        switch (DriverType.drvTypeToInt(curDrv)) {
        case DriverType.DRV_JAVA_INT:
            {
                acjob = new JavaEngine();
                acjob.isRunnable = true;
            }
            break;
        case DriverType.DRV_QTP_INT:
            {
                acjob = new QTPEngine();
                acjob.isFactoryMode = Utility.strToBoolean(runACAPI);
                acjob.isRunnable = true;
            }
            break;
        case DriverType.DRV_JUNIT_INT:
            {
                acjob = new JunitEngine();
                acjob.isRunnable = true;
            }
            break;
        case DriverType.DRV_ANT_INT:
            {
                acjob = new AntEngine();
                acjob.isRunnable = true;
            }
            break;
        case DriverType.DRV_SEL_INT:
            {
                acjob = new SeleniumEngine();
                acjob.isRunnable = true;
            }
            break;
        case DriverType.DRV_AC_INT:
            {
                //System.out.println("try to parse node, which is AC");
                acjob = new DeamonEngine();
                acjob.isRunnable = true;
                ACDeamonSet deamonSet =
                    (ACDeamonSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DEAMON);
                deamonSet.parseNode(node);
                acjob.isDeamonJob = true;
                acjob.setToRunMark(true);
                //return acjob;
            }
            break;
        default:
            {
                acjob = new TestJobElement();
            }
        }
        NamedNodeMap map = ele.getAttributes();
        int z = map.getLength();
        for (int j = 0; j < z; j++) {
            Node attributes = map.item(j);
            //TO DO:here need to add some code to check if the attributes is a defined in JOM
            acjob.proccessDependency(attributes);
            acjob.addProperty(attributes.getNodeName(),
                              attributes.getNodeValue());
        }
        NodeList job = node.getChildNodes();
        int ilen = job.getLength();
        try {
            for (int i = 0; i < ilen; i++) {
                Node childNode = job.item(i);
                if (childNode.getNodeType() != Node.TEXT_NODE) {
                    //System.out.println("begin to switch the node "+childNode.getNodeName());
                    switch (TestJobDOM.nameToInt(childNode.getNodeName())) {
                    case TestJobDOM.node_tag_testjob_int:
                        {
                            Element childEle = (Element)childNode;
                            TestJobElement childJob = new TestJobElement();
                            childJob = loadJOMFromXMLDOM(childNode);
                            acjob.addChildJob(childJob);
                        }
                        break;
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
                    case TestJobDOM.node_tag_selcase_int:
                        {
                            ACTestCaseSet selset =
                                (ACTestCaseSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_SEL);
                            selset.parseNode(childNode);
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
                    default:
                        {
                            if (acjob.getDriverType().equalsIgnoreCase(DriverType.DRV_ANT)) {
                                ACAntSet antset =
                                    (ACAntSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ANT);
                                antset.parseNode(childNode);
                            } else if (!acjob.getDriverType().equalsIgnoreCase(DriverType.DRV_AC)) {
                                if (childNode.getNodeType() !=
                                    Node.COMMENT_NODE) {
                                    outputLog(LOGMSG.NOT_RECOGINZED_TAG +
                                              childNode.getNodeName());
                                }
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            outputLog(LOGMSG.EXCEPTION_JOM_LOAD + e.getMessage());
            e.printStackTrace();
        }
        return acjob;
    }

    public static TestJobElement loadACJOMFromXMLDOM(Node node) {
        //System.out.println("load JOM "+node.getNodeName());
        Element ele = (Element)node;
        TestJobElement acjob = null;
        String runACAPI = ele.getAttribute(TestJobDOM.node_attribute_acmode);
        String jobtype = node.getNodeName();
        //System.out.println("in JOM "+jobtype+" "+TestJobDOM.nameToInt(jobtype));
        switch (TestJobDOM.nameToInt(jobtype)) {
        case TestJobDOM.job_engine_java_int:
            {
                //System.out.println("this is a jave engine");
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.setDriverType(jobtype);
                acjob.isRunnable = true;
                processDependencyFromElement(acjob, ele);
            }
            break;
        case TestJobDOM.job_engine_qtp_int:
            {
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.setDriverType(jobtype);
                acjob.isFactoryMode = Utility.strToBoolean(runACAPI);
                acjob.isRunnable = true;
                processDependencyFromElement(acjob, ele);
            }
            break;
        case TestJobDOM.job_engine_junit_int:
            {
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.setDriverType(jobtype);
                acjob.isRunnable = true;
                processDependencyFromElement(acjob, ele);
            }
            break;
        case TestJobDOM.job_engine_ant_int:
            {
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.isRunnable = true;
                acjob.setDriverType(jobtype);
                processDependencyFromElement(acjob, ele);
            }
            break;
        case TestJobDOM.job_engine_jmeter_int:
            {
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.isRunnable = true;
                acjob.setDriverType(jobtype);
                processDependencyFromElement(acjob, ele);
            }
            break;
        case TestJobDOM.job_engine_selenium_int:
            {
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.isRunnable = true;
                acjob.setDriverType(jobtype);
                processDependencyFromElement(acjob, ele);
            }
            break;
        case TestJobDOM.job_engine_ios_int:
            {
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.isRunnable = true;
                acjob.setDriverType(jobtype);
                processDependencyFromElement(acjob, ele);
            }
            break;
        case TestJobDOM.job_engine_jdbc_int:
            {
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.isRunnable = true;
                acjob.setDriverType(jobtype);
                processDependencyFromElement(acjob, ele);
            }
            break;
        case TestJobDOM.job_engine_ac_int:
            {
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.setDriverType(jobtype);
                acjob.isDeamonJob = true;
                acjob.setToRunMark(true);
                acjob.isRunnable = true;
                processDependencyFromElement(acjob, ele);
                //return acjob;
            }
            break;
        case TestJobDOM.job_engine_ws_int:
            {
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.setDriverType(jobtype);
                acjob.isRunnable = true;
                processDependencyFromElement(acjob, ele);
            }
            break;
        case TestJobDOM.job_engine_script_int:
            {
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.setDriverType(jobtype);
                acjob.isRunnable = true;
                processDependencyFromElement(acjob, ele);
            }
            break;
          case TestJobDOM.job_engine_testng_int:
              {
                  JOMParser parser = ParserFactory.createParser(jobtype);
                  acjob = parser.loadJOM(node);
                  acjob.setDriverType(jobtype);
                  acjob.isRunnable = true;
                  processDependencyFromElement(acjob, ele);
              }
              break;
        case TestJobDOM.node_tag_testjob_int:
            {
                //System.out.println("in JOM try to parse node, which is testjob");
                JOMParser parser = ParserFactory.createParser(jobtype);
                acjob = parser.loadJOM(node);
                acjob.setDriverType(jobtype);
                processDependencyFromElement(acjob, ele);
                NodeList job = node.getChildNodes();
                int ilen = job.getLength();
                for (int i = 0; i < ilen; i++) {
                    Node childNode = job.item(i);
                    if ((childNode.getNodeType() != Node.TEXT_NODE) &&
                        (childNode.getNodeType() != Node.COMMENT_NODE)) {
                        //if(childNode.getNodeName().equalsIgnoreCase(TestJobDOM.node_tag_testjob)){
                        TestJobElement childJob = new TestJobElement();
                        childJob = loadACJOMFromXMLDOM(childNode);
                        if (childJob != null) {
                            acjob.addChildJob(childJob);
                        }
                        //}

                    }
                }

                //return acjob;
            }
            break;
        default:
            {
                //acjob = new TestJobElement();
                //outputLog(LOGMSG.NOT_RECOGINZED_JOB+jobtype);
            }
        }

        return acjob;
    }

    public static void processDependencyFromElement(TestJobElement acjob,
                                                    Element ele) {
        NamedNodeMap map = ele.getAttributes();
        int z = map.getLength();
        for (int j = 0; j < z; j++) {
            Node attributes = map.item(j);
            //TO DO:here need to add some code to check if the attributes is a defined in JOM
            acjob.proccessDependency(attributes);
            acjob.addProperty(attributes.getNodeName(),
                              attributes.getNodeValue());
        }
    }

    public static boolean isComponentJob(TestJobElement job) {
        boolean ret = false;
        if (job.getElementType().equalsIgnoreCase(TestJobType.COMPONENT)) {
            ret = true;
        } else {
            ret = false;
        }
        return ret;
    }

    public static void reStructureJob(TestJobElement job) {
        //NOTES: this job must be a root job, so the level4 level5
        //System.out.println("Before traverse get all leave nodes");
        ArrayList leaveList = job.getAllLeaveNodes(job);
        //System.out.println("After traverse get all leave nodes");
        int iLeaves = leaveList.size();
        for (int i = 0; i < iLeaves; i++) {
            TestJobElement leave = (TestJobElement)leaveList.get(i);
            //System.out.println("try to parse "+leave.getName());
            //  if(!leave.getElementType().equalsIgnoreCase(TestJobType.STEP)){
            if (leave.getElementType().equalsIgnoreCase(TestJobType.TEST)) {
                //this leave nodes should be a test node, need to add step node
                TestJobElement stepNode = new TestJobElement(TestJobType.STEP);
                if (leave.getResults().equalsIgnoreCase(TestResult.CNR)) {
                    stepNode.addProperty("CNR",
                                         LOGMSG.TEST_CNR_TEXT + leave.getName());
                    leave.addChildJob(stepNode);
                    outputLog(LOGMSG.REPORT_RESTRUCT_ADDSTEP +
                              leave.getName() + " CNR");
                } else if (leave.getResults().equalsIgnoreCase(TestResult.FAIL)) {
                    stepNode.addProperty(JobStatus.FAILED,
                                         LOGMSG.TEST_FAIL_TEXT +
                                         leave.getName());
                    leave.addChildJob(stepNode);
                    outputLog(LOGMSG.REPORT_RESTRUCT_ADDSTEP +
                              leave.getName() + " failed");
                } else if (leave.getResults().equalsIgnoreCase(TestResult.PASS)) {
                    stepNode.addProperty(JobStatus.PASSED,
                                         LOGMSG.TEST_UNKNOWN_TEXT +
                                         leave.getName());
                    leave.addChildJob(stepNode);
                    outputLog(LOGMSG.REPORT_RESTRUCT_ADDSTEP +
                              leave.getName() + " passed");
                } else {
                    //no indicated results, this should be passed if no failer point
                    //stepNode.addProperty("passed",LOGMSG.TES;
                    //stepNode.addProperty("passed",LOGMSG.TEST_PASS_TEXT+leave.getName());
                    //stepNode.addProperty("unknown",LOGMSG.TEST_UNKNOWN_TEXT+leave.getName());
                    stepNode.addProperty(JobStatus.PASSED,
                                         LOGMSG.TEST_UNKNOWN_TEXT +
                                         leave.getName());
                    leave.addChildJob(stepNode);
                    outputLog(LOGMSG.REPORT_RESTRUCT_ADDSTEP +
                              leave.getName() + " unknown");
                }
                //System.out.println("the level of nodes is "+getLevelOfNode(leave));
                while ((getLevelOfNode(leave) != 4) &&
                       (getLevelOfNode(leave) < 5)) {
                    TestJobElement caseJob = leave.getParentJob();
                    TestJobElement tempJob = new TestJobElement();
                    tempJob = caseJob.cloneOnlyProp();
                    if (!caseJob.insertParent(tempJob)) {
                        outputLog("EXIT RESTUCTURE.....");
                        break;
                    }
                    outputLog(LOGMSG.REPORT_RESTRUCT_INSERT + leave.getName());
                    /*
                    if(leave.getName().equalsIgnoreCase("")){
                        TestJobElement caseJob = leave.getParentJob();
                        leave.setName(caseJob.getName());
                    }

                     TestJobElement tempJob = new TestJobElement();
                     tempJob = leave.cloneOnlyProp();
                     leave.insertParent(tempJob);
                     */
                }
            } else if (leave.getElementType().equalsIgnoreCase(TestJobType.COMPONENT)) {
                if (leave.getResults().equalsIgnoreCase(TestResult.PASS)) {
                    JOM.restruct2StatusJob(leave, "pass_unknown",
                                           "set to pass but no detailed case and test report",
                                           TestResult.PASS);
                } else {
                    JOM.restruct2StatusJob(leave, "cnr_unknown",
                                           "set to CNR because of no detailed case and test report",
                                           TestResult.CNR);
                }
            } else if (leave.getElementType().equalsIgnoreCase(TestJobType.SUITE)) {
                TestJobElement comp =
                    new TestJobElement(TestJobType.COMPONENT);
                JOM.restruct2StatusJob(comp, "not_run",
                                       "suite is null, did not run any job",
                                       TestResult.PASS);
                leave.addChildJob(comp);
            } else if (leave.getElementType().equalsIgnoreCase(TestJobType.CASE)) {
                TestJobElement test = new TestJobElement(TestJobType.TEST);
                TestJobElement step = new TestJobElement(TestJobType.STEP);
                if (leave.getResults().equalsIgnoreCase(TestResult.PASS)) {
                    test.setName("framework_generate_test");
                    test.setResults(TestResult.PASS);
                    step.addProperty(JobStatus.PASSED, "auto set pass");
                    test.addChildJob(step);
                } else {
                    test.setName("framework_generate_test");
                    test.setResults(TestResult.CNR);
                    step.addProperty(JobStatus.FAILED,
                                     "auto set with not known status");
                    test.addChildJob(step);
                }
                leave.addChildJob(test);
            } else if (leave.getElementType().equalsIgnoreCase(TestJobType.STEP)) {
                if (farToComponentNode(leave) > 3) {
                    TestJobElement goForNoTagJob = leave;
                    while (!goForNoTagJob.getParentJob().getElementType().equalsIgnoreCase(TestJobType.COMPONENT)) {
                        //System.out.println("here go for notag job "+goForNoTagJob.getName());
                        goForNoTagJob = goForNoTagJob.getParentJob();
                    }
                    goForNoTagJob.getParentJob().deleteChild(goForNoTagJob);
                }
                //while(getLevelOfNode(leave)<5){
                //while(!JOM.far4ToComponentNode(leave)){
                while (JOM.farToComponentNode(leave) < 3) {
                    //System.out.println("in while:the level of nodes in step mode is "+getLevelOfNode(leave)+leave.getName());

                    TestJobElement testJob = leave.getParentJob();
                    //System.out.println("in while:the level of nodes in step mode test name is "+testJob.getName());
                    /*
                   * if there only 3 level nodes, the case job has been in fact the component job, so if cloned to insert, the component job will be alwasys, in the far4ToComponentNode, always 3.
                   */
                    TestJobElement caseJob = testJob.getParentJob();
                    //System.out.println("in while:the level of nodes in step mode case name is "+caseJob.getName());
                    //System.out.println("in while:the level of nodes in step mode comp name is "+caseJob.getParentJob().getName());
                    TestJobElement tempJob = new TestJobElement();
                    tempJob = caseJob.cloneOnlyProp();
                    tempJob.setElementType("");
                    //if(!caseJob.insertParent(tempJob)){
                    if (!testJob.insertParent(tempJob)) {
                        outputLog("EXIT RESTUCTURE.....");
                        break;
                    }
                    //System.out.println("after parse:the level of nodes in step mode is "+getLevelOfNode(leave));
                    outputLog(LOGMSG.REPORT_RESTRUCT_INSERT + leave.getName());
                }
            } else {
            }
        }
    }

    public static void removeDeamonJob(TestJobElement job) {
        //System.out.println("in remainRunnedJob the job name is "+job.getName());
        int iChilds = job.getChildNodesNum();
        ArrayList unrunnedArr = new ArrayList(100);
        //System.out.println("in remainRunnedJob the childjobs number is "+iChilds);
        for (int i = 0; i < iChilds; i++) {
            //System.out.println(i);
            TestJobElement childJob =
                (TestJobElement)job.getChildNodesByIndex(i);
            //System.out.println("Restructuring JOM....traverse job "+childJob.getName());
            //System.out.println("in remainRunnedJob the job name is "+childJob.getName());
            if ((childJob.isDeamonJob) || (!childJob.isToRun())) {
                // if(!childJob.isDeamonJob){
                outputLog(LOGMSG.JOM_DEL_JOB + childJob.getName());
                unrunnedArr.add(childJob);
            }
        }
        int isize = unrunnedArr.size();
        for (int j = 0; j < isize; j++) {
            TestJobElement unrunnedJob = (TestJobElement)unrunnedArr.get(j);
            job.removeChildNode(unrunnedJob);
        }
    }
    //this function is called before build report

    public static void remainRunnedJob(TestJobElement job) {
        //System.out.println("in remainRunnedJob the job name is "+job.getName());
        int iChilds = job.getChildNodesNum();
        ArrayList unrunnedArr = new ArrayList(100);
        //System.out.println("in remainRunnedJob the childjobs number is "+iChilds);
        for (int i = 0; i < iChilds; i++) {
            //System.out.println(i);
            TestJobElement childJob =
                (TestJobElement)job.getChildNodesByIndex(i);
            if (!childJob.isDeamonJob) {
                outputLog("****Checking the status of job " +
                          childJob.getName());
                checkAndWaitRemoteJobFinished(childJob);
            }

            //System.out.println("in remainRunnedJob the job name is "+childJob.getName());
            /*
            if(childJob.getJobStatus().equalsIgnoreCase(JobStatus.RUNNING)){
              outputLog("-----job "+childJob.getName()+" is still in running status");
              String timeoutvalue = childJob.getProperty(TestJobDOM.DEFINED_ATTRIBUTE_TIMEOUT);
              String defaultTimeOutValue = Styler.confProp.getProperty(Styler.DEFAULT_REMOTE_TIMEOUT,"");
              long currentTimeInMillis = Utility.getCurrentTimeInMillis();
              long jobstarttime = childJob.getBeginTimeL();
              long elapsedtime = currentTimeInMillis-jobstarttime;
              int iseconds = (int)elapsedtime/1000;
              if((timeoutvalue.equalsIgnoreCase(""))&&(defaultTimeOutValue.equalsIgnoreCase(""))){
                outputLog("-----job "+childJob.getName()+" will be abandoned since it is still running status for "+iseconds+" sencods!");
                outputLog("-----to avoid job be abandoned, please make sure time out value set ");
              }else{
                //use 3600 default for temporarily.
                int itimeout = 3600;
                int toWaitTime = (itimeout-iseconds)*1000;
                outputLog("Use 3600s time out as default, Will wait for "+toWaitTime/1000+" seconds");
                long startTimeInMillis = Utility.getCurrentTimeInMillis();
                try{
                    while(currentTimeInMillis<startTimeInMillis+toWaitTime){
                        if(childJob.getJobStatus().equalsIgnoreCase(JobStatus.RUNNING)){
                          Thread.sleep(2000);
                          currentTimeInMillis=Utility.getCurrentTimeInMillis();
                        }else{
                          //refresh current job, since it has been replaced by the remote job
                          Thread.sleep(2000);
                          childJob= (TestJobElement)job.getChildNodeByName(childJob.getName());
                          childJob.setJobStatus(JobStatus.FINISH);
                          childJob.isRunned=true;
                          currentTimeInMillis=startTimeInMillis+toWaitTime+10000;
                          //break;
                        }
                    }
                  }catch(Exception e){
                     outputLog("Exception when in remainRunnedJob wait running job finised!");
                     e.printStackTrace();
                  }
              }
            }
            */
            //System.out.println("in un remained childJOb "+childJob.getName()+" status is "+childJob.getJobStatus());
            //if(!childJob.getJobStatus().equalsIgnoreCase(JobStatus.FINISH)){
            if (!childJob.isRunned) {
                // if(!childJob.isDeamonJob){
                //outputLog("is to remove the job "+childJob.getName()+childJob.isRunned+childJob.isRunning);
                unrunnedArr.add(childJob);
            }
        }
        int isize = unrunnedArr.size();
        for (int j = 0; j < isize; j++) {
            TestJobElement unrunnedJob = (TestJobElement)unrunnedArr.get(j);
            job.removeChildNode(unrunnedJob);
        }
    }

    public static void formatTheLevelOfJob(TestJobElement job) {
        int iChilds = job.getChildNodesNum();
        for (int i = 0; i < iChilds; i++) {
            TestJobElement childJob =
                (TestJobElement)job.getChildNodesByIndex(i);
            //System.out.println("the name is "+childJob.getName()+childJob.isRunnable+childJob.isFactoryMode);
            if ((childJob.isRunnable) && (!childJob.isFactoryMode)) {

                while (!is3rdLevelTestNode(childJob)) {
                    TestJobElement tempJob = new TestJobElement();
                    tempJob = childJob.cloneOnlyProp();
                    childJob.insertParent(tempJob);
                }
                return;
            } else {
                formatTheLevelOfJob(childJob);
            }
        }

    }

    public static boolean is3rdLevelTestNode(TestJobElement job) {
        int i = 0;

        while (job != null) {
            job = job.getParentJob();
            i++;
        }
        //System.out.println("the recycle number is " +i);
        if (i == 3) {
            return true;
        } else
            return false;

    }

    public static int getLevelOfNode(TestJobElement job) {
        int i = 0;
        while (job != null) {
            job = job.getParentJob();
            i++;
        }
        //System.out.println("the recycle number is " +i);
        return i;

    }

    public static int farToComponentNode(TestJobElement job) {
        int i = 0;
        while ((job != null) &&
               (!job.getElementType().equalsIgnoreCase(TestJobType.COMPONENT))) {

            job = job.getParentJob();
            //System.out.println(job.getName()+": "+job.getElementType());
            i++;
        }
        //System.out.println("the recycle number is " +i);
        return i;
    }

    public static boolean far4ToComponentNode(TestJobElement job) {
        int i = 0;
        while ((job != null) &&
               (!job.getElementType().equalsIgnoreCase(TestJobType.COMPONENT))) {

            job = job.getParentJob();
            //System.out.println(job.getName()+": "+job.getElementType());
            i++;
        }
        //System.out.println("the recycle number is " +i);
        if (i >= 3) {
            return true;
        } else {
            return false;
        }
    }

    public static String getOuterXML(Node node) {
        DOMImplementationLS domImplementation =
            (DOMImplementationLS)node.getOwnerDocument().getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        if (!(node instanceof Document)) {
            lsSerializer.getDomConfig().setParameter("xml-declaration", false);
        }
        return lsSerializer.writeToString(node);
    }

    public static Node getNodeFromOuterXML(String outerxml) {
        Node node = null;
        Element rootElement = null;
        try {
            StringBufferInputStream stringBuffer =
                new StringBufferInputStream(outerxml);
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stringBuffer);

            rootElement = document.getDocumentElement();
        } catch (Exception e) {
            outputLog("Exception in JOM.getNodeOuterXML " + e.getMessage() +
                      "::::" + outerxml);
            e.printStackTrace();
        }
        return rootElement;
    }


    //must be a component level job

    public static void restruct2StatusJob(TestJobElement job,
                                          String postfixofname, String msg,
                                          String result) {
        job.emptyAllChildNodes();
        //shining added to make suite job also can display the report well
        job.setElementType(TestJobType.COMPONENT);
        TestJobElement errCase = new TestJobElement(TestJobType.CASE);
        errCase.setName(job.getName() + "_" + postfixofname);
        TestJobElement errTest = new TestJobElement(TestJobType.TEST);
        errTest.setName(job.getName() + "_" + postfixofname);
        errTest.setResults(result);
        TestJobElement step = new TestJobElement(TestJobType.STEP);
        step.addProperty(result, msg);
        //errTest.addProperty("failed","Test could not run due to fail to Init job, please check framework.log for detail trace");
        errTest.addChildJob(step);
        errCase.addChildJob(errTest);
        job.addChildJob(errCase);
        //recuSetResults(job,TestResult.FAIL);
    }
    //

    public static void checkAndWaitMultiTHreadsJobFinished(TestJobElement job) {
        TestJobElement parent = (TestJobElement)job.getParent();
        if (parent != null) {
            int ichilds = parent.getChildNodesNum();
            for (int i = 0; i < ichilds; i++) {
                TestJobElement childJob =
                    (TestJobElement)parent.getChildNodesByIndex(i);
                if (childJob.getName().contains(job.getName()))
                    checkAndWaitRemoteJobFinished(childJob);
            }
        } else {
            outputLog("!!!job <" + job.getName() + ">'s parent is null");
        }
    }

    public static void checkAndWaitMultiChildTHreadsJobFinished(TestJobElement job) {

        // if(job.getResults().equalsIgnoreCase(TestResult.PASS)||job.getResults().equalsIgnoreCase(TestResult.FAIL)||)
        int ichilds = job.getChildNodesNum();
        for (int i = 0; i < ichilds; i++) {
            TestJobElement childJob =
                (TestJobElement)job.getChildNodesByIndex(i);
            //  if(childJob.getName().contains(job.getName()))
            if (childJob.getJobStatus().equalsIgnoreCase(JobStatus.NONEEDRUN)) {
                continue;
            }
            checkAndWaitRemoteJobFinished(childJob);
        }

    }

    public static void checkAndWaitRemoteJobFinished(TestJobElement jobToRun) {
        //there is potential risk
        TestJobElement parent = (TestJobElement)jobToRun.getParent();
        outputLog("checking status of job <" + jobToRun.getName() + "> is " +
                  jobToRun.getJobStatus() + " AND results is " +
                  jobToRun.getResults());
        if (jobToRun.getJobStatus().equalsIgnoreCase(JobStatus.RUNNING) ||
            jobToRun.isRunning == true) {
            outputLog("job <" + jobToRun.getName() +
                      "> is a remote job,is running status, will wait job returned until timeout! ");
            try {
                boolean runflag = true;
                while (runflag) {
                    if (jobToRun.getJobStatus().equalsIgnoreCase(JobStatus.RUNNING) ||
                        jobToRun.isRunning == true) {
                        Thread.sleep(Styler.FREQUENCY_JOM_CHECKJOB);
                    } else {
                        //refresh current job, since it has been replaced by the remote job when returned, while current job will be set to finished status when remote job returned(in ACJobEngine), once retunred, current
                        //job will be replaced by remoted job, current job will be a lonely node, so it is to be refreshed.
                        Thread.sleep(Styler.FREQUENCY_JOM_CHECKJOB);
                        jobToRun =
                                (TestJobElement)parent.getChildNodeByName(jobToRun.getName());
                        jobToRun.isRunned = true;
                        runflag = false;
                    }
                }
            } catch (Exception e) {
                outputLog("XXXXX-Exception when query job running status cycle");
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {

    }


}
