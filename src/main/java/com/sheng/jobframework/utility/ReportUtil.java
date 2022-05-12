package com.sheng.jobframework.utility;


import com.sheng.jobframework.ACReport.TestCaseReport;
import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACTestConfig;
import com.sheng.jobframework.jobdef.ACTestEnv;
import com.sheng.jobframework.Styler;

import com.sheng.jobframework.annotation.DefinedPara;
import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.annotation.TestResult;

import com.sheng.jobframework.commands.RestructureJob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.net.InetAddress;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.taskdefs.Delete;


public class ReportUtil extends ACElement {
    public static boolean buildForOracle = true;
    public static String reportLang = "en";
    public static String XSLStyle_component = "component";
    public static String XSLStyle_suite = "suite";
    public static String XSLStyle_case = "case";
    public static String XSLStyle_test = "test";
    public static String g_strClientReport = "Client Automation";
    public static String HeaderStyle_ReportDebug = "ReportDebug";
    public static String HeaderStyle_ReportEnv = "TestEnv";
    public static String HeaderStyle_RTC = "RTC";
    public static String HeaderStyle_ReportConfig = "TestConfig";
    public static String HeaderStyle_ReportHeader = "ReportHeader";
    public static String HeaderStyle_JMeterSummary = "JMeterSummary";
    public static String testplan = "RTCTestScenario";
    public static int indicatorSuiteUtput = 1;
    public static int inidcatorTemp = 1;


    public ReportUtil() {
    }

    public static void setLicensed(boolean b) {
        buildForOracle = b;
    }

    public static void setReportLang(String lang) {
        reportLang = lang;
    }

    public static String getRelativeScreenPath(TestJobElement componentJob,
                                               String abspath) {
        //the relateive path should starts from the component.
        //String absolutePath = testjob.generateScreenLocation();
        //String absolutePath = testjob.getScreenPath();
        String absolutePath = abspath;
        //System.out.println("in reportutil, the full path is "+absolutePath);
        //int pos = absolutePath.indexOf(TestJobDOM.screen_path_dir);
        int pos = absolutePath.indexOf(componentJob.getName());
        String relativePath = "";
        if (pos != -1) {
            //relativePath = absolutePath.substring(pos);
            //this is a screenshot snapped by javaJob
            relativePath =
                    absolutePath.substring(pos + componentJob.getName().length() +
                                           1);
        } else {
            //this is a screenshot snapped by non-java job, script or iOS
            int pos2 = absolutePath.indexOf(TestJobDOM.screen_path_dir);
            if (pos2 != -1) {
                relativePath = absolutePath.substring(pos2);
            } else {
                outputLog("--Warning, the screenpath could not be handled properly:" +
                          abspath);
            }

        }
        return relativePath;
    }

    public static String transferCDATA(String str) {
        return "<![CDATA[" + str + "]]>";

    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.io.tmpdir"));
        //ReportUtil reportUtil = new ReportUtil();
        //Styler.initialize();
        //ReportUtil.buildHtml("C:\\ReportBase\\", "C:\\matieralBase\\summary.xml", XSLStyle_suite, "AutomationCenterSuite");
    }

    public static void buildHtml(String strHtmlReportPath, String xmldatafile,
                                 String XSLStyle, String compname) {
        // outputLog("In reportUtil.. is building report"+strHtmlReportPath);
        //System.out.println("In Utility, the html Report Path is "+strHtmlReportPath);
        //String buildfile = System.getProperty("java.io.tmpdir")+File.separator+"tempBuild.xml";

        inidcatorTemp++;
        String buildfile =
            System.getProperty("java.io.tmpdir") + File.separator +
            "tempBuild" + inidcatorTemp + ".xml";
        //String logfile = System.getProperty("java.io.tmpdir")+"tempdictionary.xml";
        String logfile = xmldatafile;
        String xslfile =
            System.getProperty("java.io.tmpdir") + File.separator + "temp" +
            inidcatorTemp + ".xsl";
        strHtmlReportPath = FileUtil.getAbsolutePath(strHtmlReportPath);

        try {
            String reportpath = "";
            File fileXSL = new File(xslfile);
            File filename = new File(buildfile);
            if (fileXSL.exists()) {
                fileXSL.delete();
            }
            if (!fileXSL.exists()) {
                fileXSL.createNewFile();
            }
            if (XSLStyle.equalsIgnoreCase("component")) {
                reportpath =
                        strHtmlReportPath + File.separator + compname + ".html";
                outputLog("***building component report at path " +
                          reportpath);
                Writer.writeComponentXSL(xslfile);
            } else if (XSLStyle.equalsIgnoreCase("suite")) {
                //reportpath =strHtmlReportPath+File.separator +"SummaryReport.html";
                reportpath =
                        strHtmlReportPath + File.separator + compname + ".html";
                outputLog("***building suite report at path " + reportpath);
                Writer.writeSuiteXSL(xslfile);
            }
            if (!filename.exists()) {
                filename.createNewFile();
            }
            OutputStreamWriter utput =
                new OutputStreamWriter(new FileOutputStream(buildfile),
                                       "UTF-8");
            //  FileWriter fw = new FileWriter(buildfile);
            String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns:oracle=\"antlib:oracle\" basedir=\".\" default=\"run-test\" name=\"convert\">\n" +
                "<target name=\"run-test\">\n" +
                "<style in=\"" + logfile + "\" out=\"" + reportpath +
                "\" style=\"" + xslfile + "\"/>\n" +
                "</target>\n" +
                "</project>";
            utput.write(s);
            //fw.write(s,0,s.length());
            //fw.flush();
            //fw.close();
            utput.flush();
            utput.close();
            Project project = new Project();
            project.init();
            File buildFile = new File(buildfile);
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            helper.parse(project, buildFile);

            // helper.parse(project, buildFile2);
            project.executeTarget(project.getDefaultTarget());
            FileUtil.copyFile(xmldatafile,
                              strHtmlReportPath + File.separator + compname +
                              "summary.xml", true);
            project.fireBuildFinished(null);

            Delete delete = new Delete();
            delete.setProject(project);
            delete.setFile(new File(logfile));
            delete.execute();
            //delete.setFile(new File(xslfile));
            //delete.execute();
            //fileXSL.delete();
            delete.setFile(new File(buildfile));
            delete.execute();

        } catch (Exception e) {
            outputLog(LOGMSG.REPORT_BUILD_EXCEPTION + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Properties dumpReportDebugInfo(TestJobElement job) {
        Properties debugProp = new Properties();
        Utility.mergeTwoProperties(debugProp, job.getTestPerfProp());
        debugProp.setProperty("AC Hostname", job.getProperty("HOSTNAME"));
        //debugProp.setProperty("OS Name", job.getProperty("OS"));

        //debugProp.setProperty("OS Version", job.getProperty("OSVERSION"));
        debugProp.setProperty("AC Host IP", job.getProperty("IP"));
        //debugProp.setProperty("Java version",job.getProperty("JAVAVERSION"));
        //debugProp.setProperty("Java vendor", job.getProperty("JAVAVENDOR"));
        /*
      if(!job.getProperty("OS").toLowerCase().contains("mac")){
        debugProp.setProperty("Total Memory",job.getProperty("TOTALMEMORY"));
        debugProp.setProperty("Used Memory",job.getProperty("USEDMEMORY"));
        debugProp.setProperty("%Usage Memory", job.getProperty("USAGEMEMORY"));
        debugProp.setProperty("%CPU", job.getProperty("USAGECPU"));
      }
      */

        debugProp.setProperty("Windows User", job.getProperty("TESTUSER"));
        debugProp.setProperty("Windows passwd", "cdctest");
        debugProp.setProperty("ScreenShot Path",
                              job.getProperty("SCREENPATH"));
        debugProp.setProperty("Framework Log Path",
                              job.getProperty("FRAMELOGPATH"));
        debugProp.setProperty("Test Log Path", job.getProperty("TESTLOGPATH"));

        return debugProp;
    }

    public static void generateReportHeader(String style,
                                            Properties headerProp,
                                            OutputStreamWriter utput) {
        try {
            if (style.equalsIgnoreCase(HeaderStyle_ReportDebug) ||
                style.equalsIgnoreCase(HeaderStyle_ReportEnv) ||
                style.equalsIgnoreCase(HeaderStyle_ReportConfig) ||
                style.equalsIgnoreCase(HeaderStyle_JMeterSummary) ||
                style.equalsIgnoreCase(HeaderStyle_RTC)) {
                utput.write("<" + style + ">\n");
                Enumeration debugkeys = headerProp.keys();
                while (debugkeys.hasMoreElements()) {
                    String strKey = (String)debugkeys.nextElement();
                    utput.write("<param name=\"" + strKey + "\">" +
                                ReportUtil.transferCDATA(headerProp.getProperty(strKey)) +
                                "</param>\n");
                }
                utput.write("</" + style + ">\n");
            } else {
                outputLog("XXXXX-unknown style of generateHeader():" + style);
            }
        } catch (Exception e) {
            outputLog("XXXX-exception during generateHeader()");
            e.printStackTrace();
        }
    }

    public static void generateTestScenario(TestJobElement suitejob) {
        /*
      JobFileParse parse = new JobFileParse(Styler.jobfile);
      String scenarioPath = suitejob.getLocationPath();
      FileUtil.copyFile("JobSchemas"+File.separator+"jobinfo.xsl", scenarioPath+File.separator+"jobinfo.xsl",false);
        //param    the path of svg file which would been generated
      parse.drawing(scenarioPath+File.separator+"jobsvg.svg");

        //param    the path of info file recorded some host name
      parse.generateInfo(scenarioPath+File.separator+"jobinfo.xml");
        //xslt transform method.
        //the first param is jobinfo.xml,
        //the second param is xsl file path,
        //the third param is target html document
      parse.transform(scenarioPath+File.separator+"jobinfo.xml", scenarioPath+File.separator+"jobinfo.xsl", scenarioPath+File.separator+testplan+".html");
      */
    }

    public static void generateReportTab(TestJobElement job,
                                         OutputStreamWriter utput) {
        try {
            String tabs = job.getProperty(RestructureJob.REPORT_TAB_PROPERTY);
            String[] tabArr = tabs.split(",");
            if (tabArr.length > 1) {
                utput.write("<report-tab>");

                //utput.write("<tabs name=\""+testplan+"\" type=\"0\"></tabs>");
                for (int i = 0; i < tabArr.length; i++) {
                    String linktab = tabArr[i];
                    if (linktab.startsWith("##")) {
                        //this is a main tab
                        linktab = linktab.substring(2);
                        utput.write("<tabs name=\"" + linktab +
                                    "\" type=\"0\"></tabs>");
                    } else {
                        utput.write("<tabs name=\"" + linktab +
                                    "\" type=\"1\"></tabs>");
                    }
                }
                utput.write("</report-tab>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void generateSummary(TestJobElement job, String style,
                                       OutputStreamWriter utput) {
        try {
            if (style.equalsIgnoreCase(XSLStyle_suite)) {
                utput.write("<suite name=\"" + job.getName() + "\">\n");
                int isize = job.getChildNodesNum();
                for (int k = 0; k < isize; k++) {
                    TestJobElement compjob =
                        (TestJobElement)job.getChildNodesByIndex(k);
                    generateSummary(compjob, XSLStyle_component, utput);
                    generateComponentACReport(compjob);
                }
                utput.write("</suite>\n");
            } else if (style.equalsIgnoreCase(XSLStyle_component)) {
                if (job.isToRun()) {
                    String compname =
                        StringEscapeUtils.escapeHtml(job.getName());
                    utput.write("<component name=\"" + compname + "\">\n");
                    int isize = job.getChildNodesNum();
                    for (int k = 0; k < isize; k++) {
                        TestJobElement casejob =
                            (TestJobElement)job.getChildNodesByIndex(k);
                        generateSummary(casejob, XSLStyle_case, utput);
                    }
                    utput.write("</component>\n");
                }
            } else if (style.equalsIgnoreCase(XSLStyle_case)) {
                job.autoSetResults();
                //String casename = ReportUtil.transferCDATA(casejob.getName());
                String casename = StringEscapeUtils.escapeHtml(job.getName());
                utput.write("<testcase name=\"" + casename + "\" result=\"" +
                            TestResult.transferACResultToXSLResult(job.getResults()) +
                            "\">\n");
                int tSize = job.getChildNodesNum();
                for (int t = 0; t < tSize; t++) {
                    TestJobElement testjob =
                        (TestJobElement)job.getChildNodesByIndex(t);
                    generateSummary(testjob, XSLStyle_test, utput);
                }
                utput.write("</testcase>\n");
            } else if (style.equalsIgnoreCase(XSLStyle_test)) {
                long dtmbegin = job.getBeginTimeL();
                long dtmend = job.getEndTimeL();
                String testname = StringEscapeUtils.escapeHtml(job.getName());
                double iSec = Utility.getTimeDiff(dtmbegin, dtmend);
                utput.write("<test name=\"" + testname + "\" result=\"" +
                            TestResult.transferACResultToXSLResult(job.getResults()) +
                            "\" elapsed=\"" + iSec + "\"/>");
            } else {
                outputLog("XXXX-illegal style of ReportUtil::generateSummary: " +
                          style);
            }
        } catch (Exception e) {
            outputLog("XXXX-exception during generateSummary()");
            e.printStackTrace();
        }
    }

    public static void generateSummary2Version(TestJobElement job,
                                               String style,
                                               OutputStreamWriter utput) {
        try {
            if (style.equalsIgnoreCase(XSLStyle_suite)) {
                utput.write("<testjob name=\"" + job.getName() + "\">\n");

                Properties debugInfo = ReportUtil.dumpReportDebugInfo(job);
                ACTestConfig Conf =
                    (ACTestConfig)job.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
                ACTestEnv Env =
                    (ACTestEnv)job.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
                Properties envProp = Env.getEnvSetting();
                Properties confProp = Conf.getConfigSetting();
                String jobtype = job.getProperty(RestructureJob.JOB_TYPE);
                if (jobtype.equalsIgnoreCase("")) {
                    ReportUtil.generateReportHeader(HeaderStyle_ReportDebug,
                                                    debugInfo, utput);
                }
                ReportUtil.generateReportHeader(HeaderStyle_ReportEnv, envProp,
                                                utput);
                ReportUtil.generateReportHeader(HeaderStyle_ReportConfig,
                                                confProp, utput);

                int isize = job.getChildNodesNum();
                for (int k = 0; k < isize; k++) {
                    TestJobElement childjob =
                        (TestJobElement)job.getChildNodesByIndex(k);
                    if (JOM.isComponentJob(childjob)) {
                        generateSummary2Version(childjob, XSLStyle_component,
                                                utput);
                        generateComponentACReport(childjob);
                    } else {
                        generateSummary2Version(childjob, XSLStyle_suite,
                                                utput);
                        generateSuiteACReport(childjob);
                    }
                }
                utput.write("</testjob>\n");
            } else if (style.equalsIgnoreCase(XSLStyle_component)) {
                if (job.isToRun()) {
                    String compname =
                        StringEscapeUtils.escapeHtml(job.getName());
                    utput.write("<testjob name=\"" + compname + "\">\n");

                    Properties debugInfo = ReportUtil.dumpReportDebugInfo(job);
                    ACTestConfig Conf =
                        (ACTestConfig)job.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
                    ACTestEnv Env =
                        (ACTestEnv)job.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
                    Properties envProp = Env.getEnvSetting();
                    Properties confProp = Conf.getConfigSetting();
                    String jobtype = job.getProperty(RestructureJob.JOB_TYPE);
                    if (jobtype.equalsIgnoreCase("")) {
                        ReportUtil.generateReportHeader(HeaderStyle_ReportDebug,
                                                        debugInfo, utput);
                    }
                    ReportUtil.generateReportHeader(HeaderStyle_ReportEnv,
                                                    envProp, utput);
                    ReportUtil.generateReportHeader(HeaderStyle_ReportConfig,
                                                    confProp, utput);


                    int isize = job.getChildNodesNum();
                    for (int k = 0; k < isize; k++) {
                        TestJobElement casejob =
                            (TestJobElement)job.getChildNodesByIndex(k);
                        generateSummary2Version(casejob, XSLStyle_case, utput);
                    }
                    utput.write("</testjob>\n");
                }
            } else if (style.equalsIgnoreCase(XSLStyle_case)) {
                job.autoSetResults();
                //String casename = ReportUtil.transferCDATA(casejob.getName());
                String casename = StringEscapeUtils.escapeHtml(job.getName());
                utput.write("<testjob name=\"" + casename + "\" result=\"" +
                            TestResult.transferACResultToXSLResult(job.getResults()) +
                            "\">\n");
                int tSize = job.getChildNodesNum();
                for (int t = 0; t < tSize; t++) {
                    TestJobElement testjob =
                        (TestJobElement)job.getChildNodesByIndex(t);
                    generateSummary2Version(testjob, XSLStyle_test, utput);
                }
                utput.write("</testjob>\n");
            } else if (style.equalsIgnoreCase(XSLStyle_test)) {
                long dtmbegin = job.getBeginTimeL();
                long dtmend = job.getEndTimeL();
                String testname = StringEscapeUtils.escapeHtml(job.getName());
                double iSec = Utility.getTimeDiff(dtmbegin, dtmend);
                utput.write("<test name=\"" + testname + "\" result=\"" +
                            TestResult.transferACResultToXSLResult(job.getResults()) +
                            "\" elapsed=\"" + iSec + "\">");
                Properties debugInfo = ReportUtil.dumpReportDebugInfo(job);
                ReportUtil.generateReportHeader(HeaderStyle_ReportDebug,
                                                debugInfo, utput);
                utput.write("</test>");
            } else {
                outputLog("XXXX-illegal style of ReportUtil::generateSummary: " +
                          style);
            }
        } catch (Exception e) {
            outputLog("XXXX-exception during generateSummary()");
            e.printStackTrace();
        }
    }

    public static void generateComponentACReport(TestJobElement componentJob) {
        String strSuiteName = "ACDemoSuite";
        String strDestFileName, strOSName, strOSVersion;
        String g_strClientReport = componentJob.getName();
        long begintimel = componentJob.getBeginTimeL();
        Date beginTime = new Date(begintimel);
        //SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String strCurrentTime = formatter.format(beginTime);
        TestJobElement parentjob = componentJob.getParentJob();
        String strLanguage = parentjob.getTestLang();
        strDestFileName =
                System.getProperty("java.io.tmpdir") + File.separator +
                "tempdictionary" + inidcatorTemp + ".xml";
        //System.out.println("the QTP report path to be parsed is:" +strReportName);
        try {

            File f = new File(strDestFileName);
            if (!f.exists()) {
                f.createNewFile();
            }
            OutputStreamWriter utput =
                new OutputStreamWriter(new FileOutputStream(strDestFileName),
                                       "UTF-8");
            strOSName = componentJob.getProperty("OS");
            strOSVersion = componentJob.getProperty("OSVERSION");
            String host = componentJob.getProperty("HOSTNAME");
            String g_strTester =
                Utility.getSystemInfo().getProperty("userName");
            //build up the report debug information:
            InetAddress ia = InetAddress.getLocalHost();
            //String host = ia.getHostName();
            String IP = ia.getHostAddress();

            //for the property name define, please refer TestJobElement.endJob()
            Properties debugInfo =
                ReportUtil.dumpReportDebugInfo(componentJob);


            ACTestConfig Conf =
                (ACTestConfig)componentJob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
            Properties confProp = Conf.getConfigSetting();
            ACTestEnv Env =
                (ACTestEnv)componentJob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
            Properties envProp = Env.getEnvSetting();
            Properties ownJobProp = null;
            //write Jmeter own JOM into xml file
            if (componentJob.getOwnJOM() != null) {
                ownJobProp = componentJob.getOwnJOM().getJobProp();
            }
            String reportdebug = "AC Host: " + host + "\n";
            reportdebug = reportdebug + "Windows User: " + g_strTester + "\n";
            reportdebug =
                    reportdebug + "Screenshot dir: " + componentJob.getLocationPath() +
                    "screenshot" + "\n";
            reportdebug =
                    reportdebug + "log dir: " + componentJob.getLocationPath() +
                    "log.txt" + "\n";

            utput.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            utput.write("<?xml-stylesheet type=\"text/xsl\" href=\"../test-reporting.xsl\"?>\n");
            utput.write("<report xmlns=\"http://www.oracle.com/2005/ocs/qa/framework/junit/schema\">\n");
            utput.write("<client>" + g_strClientReport + "</client>\n");
            utput.write("<time>" + strCurrentTime + "</time>\n");
            utput.write("<JobType>" + componentJob.getDriverType() +
                        "</JobType>\n");

            String jobtype = componentJob.getProperty(RestructureJob.JOB_TYPE);
            //System.out.println("job type is "+jobtype);
            if (jobtype.equalsIgnoreCase("")) {
                ReportUtil.generateReportHeader(HeaderStyle_ReportDebug,
                                                debugInfo, utput);
            }
            ReportUtil.generateReportHeader(HeaderStyle_ReportEnv, envProp,
                                            utput);
            ReportUtil.generateReportHeader(HeaderStyle_ReportConfig, confProp,
                                            utput);
            //write report Debug into xml file
            if (ownJobProp != null) {
                ReportUtil.generateReportHeader(HeaderStyle_JMeterSummary,
                                                ownJobProp, utput);
            }
            utput.write("<system>\n");
            utput.write("<OS>" + strOSName + "</OS>\n");
            utput.write("<OS-version>" + strOSVersion + "</OS-version>\n");
            utput.write("<OS-arch></OS-arch>\n");
            utput.write("<java-version></java-version>\n");
            utput.write("<java-vendor></java-vendor>\n");
            utput.write("</system>\n");
            utput.write("<tester>" + g_strTester + "</tester>\n");
            utput.write("<language>" + strLanguage + "</language>\n");
            utput.write("<script>VBScript</script>\n");

            utput.write("<build>" + componentJob.getEnvProperty("BUILD") +
                        "</build>\n");
            utput.write("<client_version>" +
                        componentJob.getEnvProperty("CLIENT_VERSION") +
                        "</client_version>\n");
            utput.write("<suite>\n");
            utput.write("<name>" + strSuiteName + "</name>\n");
            int caseRunned = componentJob.getChildNodesNum();
            for (int k = 0; k < caseRunned; k++) {
                TestJobElement caseJob =
                    (TestJobElement)componentJob.getChildNodesByIndex(k);
                // System.out.println("the case name is "+caseJob.getName());
                utput.write("<testcase>\n");
                utput.write("<id>" + caseJob.getName() + "</id>\n");
                utput.write("<name>" + caseJob.getName() + "</name>\n");
                utput.write("<exec-file>Client</exec-file>\n");
                utput.write("<component>" + caseJob.getParentJob().getName() +
                            "</component>\n");
                utput.write("<subcomponent>" + caseJob.getDesc() +
                            "</subcomponent>\n");
                utput.write("<area>" + caseJob.getDesc() + "</area>\n");
                utput.write("<description>" + caseJob.getDesc() +
                            "</description>\n");
                String qtpFullName = caseJob.getProperty("productName");
                String qtpVer = caseJob.getProperty("productVer");
                String os = caseJob.getProperty("os");
                //String host = caseJob.getProperty("host");
                String strCaseName = caseJob.getName();
                String startTime = caseJob.getProperty("sTime");
                String endTime = caseJob.getProperty("eTime");


                TestCaseReport objTestCase = new TestCaseReport();
                objTestCase.SetTestCaseName(caseJob.getName());
                //arrTestCases.add(intTestCaseCount,objTestCase);
                //intTestCaseCount = intTestCaseCount +1;
                objTestCase.SetCaseDesc(caseJob.getDesc());
                int iTest = caseJob.getChildNodesNum();
                for (int j = 0; j < iTest; j++) {
                    TestJobElement testJob =
                        (TestJobElement)caseJob.getChildNodesByIndex(j);
                    //System.out.println("the test name is "+testJob.getName());
                    long dtmBeginL = testJob.getBeginTimeL();
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(dtmBeginL);
                    String strdtmBegin = formatter.format(c.getTime());
                    long dtmEndL = testJob.getEndTimeL();
                    // System.out.println("test begin time is  "+dtmBeginL);
                    //System.out.println("test end time is  "+dtmEndL);
                    double iSec = 0;
                    iSec = Utility.getTimeDiff(dtmBeginL, dtmEndL);
                    //  System.out.println("the diff after transfered to second is  "+iSec);
                    String strElapsedTime = String.valueOf(iSec);
                    //System.out.println("the string transfered to second is  "+iSec);
                    //String strElapsedTime=Integer.toString(iSec);
                    String strLog, strErr, strConvertedLog, strLogURL;
                    String testname =
                        ReportUtil.transferCDATA(testJob.getName());
                    String testdesc =
                        ReportUtil.transferCDATA(testJob.getDesc());
                    String result =
                        TestResult.transferACResultToXSLResult(testJob.getResults());
                    utput.write("<test timestamp=\"" + strdtmBegin +
                                "\" elapsed=\"" + strElapsedTime +
                                "\" result=\"" + result + "\">\n");
                    utput.write("<name>" + testname + "</name>\n");
                    utput.write("<desc>" + testdesc + "</desc>\n");
                    utput.write("<level></level>\n");
                    utput.write("<type></type>\n");

                    strLog = "";
                    strErr = "";
                    int iStep = testJob.getChildNodesNum();
                    if (testJob.getResults().equalsIgnoreCase(TestResult.CNR) &&
                        (iStep < 1)) {
                        TestJobElement stepNode = new TestJobElement();
                        stepNode.addProperty(JobStatus.FAILED,
                                             "Test can not run due to depended Job failure");
                        testJob.addChildNode(stepNode);
                    }
                    for (int q = 0; q < iStep; q++) {
                        TestJobElement stepJob =
                            (TestJobElement)testJob.getChildNodesByIndex(q);
                        Properties prop = stepJob.getElementProperty();

                        int paraNum = prop.size();
                        Enumeration keys = prop.keys();
                        while (keys.hasMoreElements()) {
                            String strKey = (String)keys.nextElement();
                            String value = prop.getProperty(strKey);
                            if (strKey.equalsIgnoreCase(JobStatus.FAILED)) {
                                strErr = strErr + strKey + ":" + value + "\n";
                            }

                            else {
                                if ((!strKey.equalsIgnoreCase(TestJobDOM.DEFINED_ELEMENT_TYPE)) &&
                                    (!strKey.equalsIgnoreCase(TestJobDOM.DEFINED_ATTRIBUTE_JOBSTATUS)) &&
                                    (!strKey.equalsIgnoreCase("protype"))) {
                                    strLog =
                                            strLog + strKey + ":" + value + "\n";
                                }

                            }
                        }

                    }
                    strLogURL = "test-RuntimeLog";
                    // strLog = StringEscapeUtils.escapeHtml(strLog);
                    strLog = ReportUtil.transferCDATA(strLog);
                    // strErr = StringEscapeUtils.escapeHtml(strErr);
                    strErr = ReportUtil.transferCDATA(strErr);
                    int intPos = strLogURL.lastIndexOf("\\");
                    if (intPos > 0) {
                        strLogURL = strLogURL.substring(intPos + 1);
                    }
                    utput.write("<output>\n");
                    utput.write(strLog + "\n");
                    utput.write("</output>\n");
                    //caseJob.autoSetResults();
                    if (testJob.getResults().equalsIgnoreCase(TestResult.FAIL)) {
                        utput.write("<error failure=\"true\" type=\"failure\">\n");
                        utput.write(strErr + "\n");
                        utput.write("</error>\n");
                    }
                    //TO DO-shining: here need to be implemented

                    strLogURL = "log.txt";
                    //String screenPath="screenshot";
                    String absolutePath = testJob.getScreenPath();
                    if (absolutePath.equalsIgnoreCase("")) {
                        absolutePath = testJob.generateScreenLocation();
                    }
                    //String screenPath = ReportUtil.getRelativeScreenPath(componentJob,testJob);
                    String screenPath =
                        ReportUtil.getRelativeScreenPath(componentJob,
                                                         absolutePath);
                    //new branch for Jmeter report format, if it is a jmeter report, will have the screeshot path as jmeter jtl results path
                    if (componentJob.getDriverType().equalsIgnoreCase(TestJobDOM.job_engine_jmeter)) {
                        outputLog("----Jmeter report, processing it");
                        screenPath = "ac_jmeter.jtl";
                    }
                    screenPath.replaceAll("//", "////");
                    //shining added 03 09
                    //screenPath = OSCmdUtil.pathReSettle2OS(screenPath);
                    //screenPath.replaceAll("\\","////");
                    //generateScreenShotPage(testJob.generateScreenLocation(),testJob.getName());
                    //generateScreenShotPage(testJob.getScreenPath(),testJob.getName());
                    generateScreenShotPage(absolutePath, testJob.getName());
                    screenPath = screenPath + "/" + "index.html";


                    utput.write("<system-out href=\"" + strLogURL +
                                "\">log</system-out>\n");
                    //utput.write("<qtp-out href=\"./Report/Results.qtp\">qtp report</qtp-out>\n");
                    // utput.write("<screenshot href=\"" + "screenshot" + "/" + strTRSID + "/" + strTestName + "/index.html\">screenshot</screenshot>\n");
                    //utput.write("<screenshot href=\"" + screenPath + "/" + caseJob.getName() + "/" + testJob.getName() + "\">screenshot</screenshot>\n");
                    utput.write("<screenshot href=\"" + screenPath +
                                "\">screenshot</screenshot>\n");
                    utput.write("</test>\n");
                }
                utput.write("</testcase>\n");

            }
            utput.write("</suite>\n");
            utput.write("</report>\n");
            utput.close();
            //System.out.println("is to generate report at path: "+componentJob.getLocationPath());
            String componentLocationPath =
                OSCmdUtil.pathReSettle2OS(componentJob.getLocationPath());
            //ReportUtil.buildHtml(componentJob.getLocationPath(),strDestFileName,XSLStyle_component,componentJob.getName());
            ReportUtil.buildHtml(componentLocationPath, strDestFileName,
                                 XSLStyle_component, componentJob.getName());
        } catch (Exception e) {
            System.out.println("XXXX-exception when try to write indo to file " +
                               strDestFileName);
            e.printStackTrace();
        }
    }

    public static void generateSuiteACReport(TestJobElement suiteJob) {
        TestJobElement suitejob = suiteJob;
        String strTester = Utility.getSystemInfo().getProperty("userName");
        String strSuiteName = suiteJob.getName();
        indicatorSuiteUtput++;
        String strSuiteDesc = suiteJob.getDesc();
        String strReportPath = suiteJob.getLocationPath();
        String strLanguage = suiteJob.getTestLang();
        String xmlDataFilePath =
            System.getProperty("java.io.tmpdir") + File.separator +
            "tempSummary" + indicatorSuiteUtput + ".xml";
        Date dtmBegin = Utility.getCurrentTime();
        OutputStreamWriter utput;
        try {
            //File f = new File(g_strReportPath + "\\Summary.xml");
            File f = new File(xmlDataFilePath);
            if (!f.exists())
                f.createNewFile();
            utput = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            ;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        long begintimel = suitejob.getBeginTimeL();
        Date beginTime = new Date(begintimel);
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        //String strCurrentTime = formatter.format(Utility.getCurrentTime());
        String strCurrentTime = formatter.format(beginTime);
        String strDestFileName;

        //strDestFileName = strReportPath + "\\Summary.xml";
        //TEMP: commented the suite summary xml to temporary directory
        strDestFileName =
                System.getProperty("java.io.tmpdir") + File.separator +
                "tempSummary.xml";
        try {
            Properties debugInfo = ReportUtil.dumpReportDebugInfo(suiteJob);
            ACTestConfig Conf =
                (ACTestConfig)suitejob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_CONFIG);
            ACTestEnv Env =
                (ACTestEnv)suitejob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
            Properties envProp = Env.getEnvSetting();
            Properties confProp = Conf.getConfigSetting();

            //strOSName = System.getProperty("os.name");
            //strOSVersion = System.getProperty("os.version");
            utput.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            utput.write("<?xml-stylesheet type=\"text/xsl\" href=\"test-reporting-summary.xsl\"?>\n");
            utput.write("<report xmlns=\"http://www.oracle.com/2005/ocs/qa/framework/junit/schema\">\n");
            utput.write("<client>" + g_strClientReport + "</client>\n");
            utput.write("<time>" + strCurrentTime + "</time>\n");
            String jobtype = suiteJob.getProperty(RestructureJob.JOB_TYPE);
            //System.out.println("job type is "+jobtype);
            if (jobtype.equalsIgnoreCase("")) {
                ReportUtil.generateReportHeader(HeaderStyle_ReportDebug,
                                                debugInfo, utput);
            }
            Properties rtcProp = generateRTCProp(suiteJob);
            if (Styler.rtcStyle) {
                ReportUtil.generateReportHeader(HeaderStyle_RTC, rtcProp,
                                                utput);
            }
            ReportUtil.generateReportHeader(HeaderStyle_ReportEnv, envProp,
                                            utput);
            ReportUtil.generateReportHeader(HeaderStyle_ReportConfig, confProp,
                                            utput);

            utput.write("<system>\n");
            utput.write("<OS>" + suiteJob.getProperty("OS") + "</OS>\n");
            utput.write("<OS-version>" + suiteJob.getProperty("OSVERSION") +
                        "</OS-version>\n");
            utput.write("<OS-arch></OS-arch>\n");
            utput.write("<java-version></java-version>\n");
            utput.write("<java-vendor></java-vendor>\n");
            utput.write("</system>\n");
            utput.write("<tester>" + strTester + "</tester>\n");
            utput.write("<language>" + strLanguage + "</language>\n");
            //utput.write("<proccess_job>" + suitejob.getProperty("PROCCESS_JOB") + "</proccess_job>\n");
            utput.write("<script>VBScript</script>\n");
            utput.write("<build>" + suitejob.getEnvProperty("BUILD") +
                        "</build>\n");
            utput.write("<client_version>" +
                        suitejob.getEnvProperty("CLIENT_VERSION") +
                        "</client_version>\n");
            String portal_link =
                Styler.confProp.getProperty(DefinedPara.CONFIG_PARA_PERFORMANCE_PORTAL);
            if (!portal_link.equalsIgnoreCase("")) {
                if (suitejob.getProperty(RestructureJob.JOB_TYPE).equalsIgnoreCase(RestructureJob.PERFORMANCE_TYPE)) {
                    utput.write("<link name=\"Performance Portal Link\">" +
                                Styler.confProp.getProperty(DefinedPara.CONFIG_PARA_PERFORMANCE_PORTAL) +
                                "</link>\n");
                }
            }
            generateReportTab(suitejob, utput);
            /*
           * commented out for debug purpose
           */
            //ReportUtil.generateSummary(suitejob, XSLStyle_suite, utput);
            ReportUtil.generateSummary2Version(suitejob, XSLStyle_suite,
                                               utput);
            utput.write("</report>\n");
            utput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ReportUtil.buildHtml(strReportPath, xmlDataFilePath, XSLStyle_suite,
                             suiteJob.getName());
        //shining add for scenario job
        //generateTestScenario(suiteJob);
    }

    private static Properties generateRTCProp(TestJobElement job) {

        Properties rtcProp = new Properties();
        String server = job.getEnvProperty("SERVER");
        server = server.toLowerCase();
        String topology = "";
        if (server.contains("dfrtc-stage")) {
            topology = "DFRTC-STAGE";
        } else if (server.contains("dfrtc")) {
            topology = "DFRTC";
        } else if (server.contains("us")) {
            topology = "US";
        } else if (server.contains("cn")) {
            topology = "CN";
        } else {
            topology = "CN";
        }
        String localhost = OSCmdUtil.getHostName();
        localhost = localhost.toLowerCase();
        String client = "";
        rtcProp.setProperty("server-topo", topology);
        if (localhost.contains("bej") || localhost.contains("cn") ||
            localhost.contains("CN")) {
            client = "CN";
        } else {
            client = "US";
        }
        rtcProp.setProperty("client-topo", client);
        rtcProp.setProperty("confMaster", localhost);
        return rtcProp;
    }

    public static void generateScreenShotPage(String srcDir, String testname) {
        try {
            FileUtil.mkDirsIfNotExists(FileUtil.getAbsolutePath(srcDir));
            if (!FileUtil.isFileExists(srcDir)) {
                System.out.println("XXXX-the screenshot dir for [" + testname +
                                   "]does not exist " + srcDir);

                return;
            }
            String screenHtml = srcDir + File.separator + "index.html";
            OutputStreamWriter utput =
                new OutputStreamWriter(new FileOutputStream(screenHtml),
                                       "UTF-8");
            utput.write("<html>");
            utput.write("<title>");
            utput.write("Screenshot Preview Page for Test Case - " + testname);
            utput.write("</title>");
            utput.write("<body>");
            utput.write("<b><Strong>");
            utput.write("Screen shots for test case: " + testname);
            utput.write("</Strong></b>");
            utput.write("<table BORDER=\"0\" WIDTH=\"100%\" cellspacing=\"1\" cellpadding=\"0\">");
            utput.write("<TR bgcolor=\"White\">");
            File f = new File(srcDir);
            File[] imgfiles = f.listFiles();
            int itotal = imgfiles.length;
            if (itotal == 0)
                System.out.println("XXXXX-no img files under the screenshot dir " +
                                   f.getAbsolutePath());
            int iflag = 0;
            for (int l = 0; l < itotal; l++) {
                File imgfile = imgfiles[l];
                if (FileUtil.getFileExtension(imgfile.getName()).equalsIgnoreCase("png") ||
                    FileUtil.getFileExtension(imgfile.getName()).equalsIgnoreCase("bmp")) {
                    iflag++;
                    utput.write("<TD align=\"center\">");
                    utput.write("<a href=\"./" + imgfile.getName() + "\"");
                    utput.write(">");
                    utput.write("<img src= \"./" + imgfile.getName() + "\"");
                    utput.write(" width=\"200\" height=\"150\"</a><BR>");
                    utput.write("</TD>");
                    if (iflag % 4 == 0) {
                        utput.write("</TR>");
                        utput.write("<TR bgcolor=\"White\">");
                    }
                }
            }
            utput.write("</TR>");
            utput.write("</table>");
            utput.write("</body>");
            utput.write("</html>");
            //fw.write(s,0,s.length());
            //fw.flush();
            //fw.close();
            utput.flush();
            utput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
