package com.sheng.jobframework.jobdef.WebService;

import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobEngine;
import com.sheng.jobframework.jobdef.ACTestDataSet;
import com.sheng.jobframework.utility.HttpClient.HttpClientUtils;
import com.sheng.jobframework.utility.HttpClient.HttpServerResponse;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.regex.Matcher;


public class WSUserEngine extends ACJobEngine {
    private String lastResponseStr = "";
    private Properties localSavedPara = new Properties();

    public WSUserEngine() {
        super();
    }

    public void runEntityJob() {
        outputFrameLog("----start to run web service engine---------");
        SimpleSOAPCmdSet SimpleCmdset =
            (SimpleSOAPCmdSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_WSSIMPLECMD);
        ACTestDataSet testdataset =
            (ACTestDataSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_DATA);

        //get auth info
        outputFrameLog("----Auth info -----");
        String wshost = SimpleCmdset.getWSHost();
        outputFrameLog("--------Host: " + wshost + " -----");
        String wsport = SimpleCmdset.getWSPort();
        outputFrameLog("--------Port: " + wsport + " -----");
        String wsuser = SimpleCmdset.getWSUser();
        outputFrameLog("--------User: " + wsuser + " -----");
        String wspasswd = SimpleCmdset.getWSPwd();
        outputFrameLog("--------Password: " + wspasswd + " -----");
        ArrayList cmdSet = SimpleCmdset.getCmdSet();
        try {
            HttpClientUtils hcu = new HttpClientUtils(wsuser, wspasswd, "");


            int cmdSize = cmdSet.size();
            outputFrameLog("--- " + cmdSize + " command to run----");
            for (int k = 0; k < cmdSize; k++) {
                //while (keys.hasMoreElements()){
                //String strKey = (String)keys.nextElement();
                //SelTestCase selcase = (SelTestCase)prop.get(strKey);

                SOAPCmd cmd = (SOAPCmd)cmdSet.get(k);
                outputFrameLog("---- start to run " + cmd.getName() +
                               " command----");
                String cmdType = cmd.getType();
                //begin compose report
                TestJobElement myTestCase = new TestJobElement();
                myTestCase.setName("WebService-" + cmd.getName());
                TestJobElement myTest = new TestJobElement();
                myTest.setName(cmd.getType() + "-" + cmd.getName());
                try {
                    if (cmdType.equalsIgnoreCase(SOAPCmd.CMD_TYPE_DEL)) {

                        //to do
                    } else if (cmdType.equalsIgnoreCase(SOAPCmd.CMD_TYPE_PUT)) {
                        String url = cmd.getURI();
                        Properties headers = cmd.getHeaders();
                        Properties queryParas = cmd.getParas();
                        replacePropWithParam(headers);
                        replacePropWithParam(queryParas);
                        String body = cmd.getBody();
                        //to do
                    } else if (cmdType.equalsIgnoreCase(SOAPCmd.CMD_TYPE_PARA)) {
                        String paraname = cmd.getParaname();
                        String LB = cmd.getLB();
                        String RB = cmd.getRB();
                        int occurence = cmd.getOcurrence();
                        TestJobElement step =
                            new TestJobElement(TestJobType.STEP);
                        String caughtValue =
                            runCaptureAndSaveCmd(paraname, LB, RB, occurence);
                        if (caughtValue.equalsIgnoreCase("")) {
                            outputFrameLog("Did not find the value match LB is " +
                                           LB + " and RB is " + RB +
                                           " in last response body: ");
                            outputFrameLog(lastResponseStr);
                            step.addProperty(JobStatus.FAILED,
                                             "Did not find the value match LB is " +
                                             LB + " and RB is " + RB);
                            myTest.setResults(TestResult.FAIL);
                        } else {
                            outputFrameLog("Has caught the value " +
                                           caughtValue +
                                           " and saved to param <" + paraname +
                                           ">");
                            step.addProperty(JobStatus.PASSED,
                                             "Has caught the value " +
                                             caughtValue +
                                             " and saved to param <" +
                                             paraname + ">");
                            myTest.setResults(TestResult.PASS);
                        }
                        myTest.addChildJob(step);
                        myTestCase.addChildJob(myTest);
                        addChildJob(myTestCase);
                        //to do
                    } else if (cmdType.equalsIgnoreCase(SOAPCmd.CMD_TYPE_GET)) {
                        String url = cmd.getURI();
                        Properties propHeaders = cmd.getHeaders();
                        Properties propQueryParas = cmd.getParas();
                        replacePropWithParam(propHeaders);
                        replacePropWithParam(propQueryParas);
                        Hashtable headers = (Hashtable)propHeaders;
                        Hashtable queryParas = (Hashtable)propQueryParas;
                        String name = cmd.getName();
                        HttpServerResponse response =
                            hcu.get(url, headers, queryParas);
                        TestJobElement step =
                            new TestJobElement(TestJobType.STEP);
                        int retCode = response.getReturnCode();
                        if (isSuccessHttpCode(retCode)) {
                            step.addProperty(JobStatus.PASSED,
                                             "the get cmd has been sent to web service server, return response code is " +
                                             retCode);
                            myTest.setResults(TestResult.PASS);
                        } else {
                            step.addProperty(JobStatus.FAILED,
                                             "the get cmd has been sent to web service server,return response code is " +
                                             retCode);
                            myTest.setResults(TestResult.FAIL);
                        }
                        myTest.addChildJob(step);
                        myTestCase.addChildJob(myTest);
                        addChildJob(myTestCase);
                        //set last
                        lastResponseStr =
                                response.getResponsePayloadAsString();
                        outputFrameLog("command <" + cmd.getName() +
                                       "> executed with code :" + retCode +
                                       " response body: ");
                        outputFrameLog(lastResponseStr);
                        //to do
                    } else if (cmdType.equalsIgnoreCase(SOAPCmd.CMD_TYPE_POST)) {

                        hcu.setContentType("text/xml; charset=utf-8");
                        String soapXML = cmd.getBody();
                        String soapBody = replaceDataWithParam(soapXML);
                        //debug purpose
                        //String soapEnvelope = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\"><soap:Header><wsa:Action></wsa:Action><wsa:MessageID>uuid:ca025af7-eaac-468d-ae2b-c988b38fac3b</wsa:MessageID><wsa:ReplyTo><wsa:Address>http://schemas.xmlsoap.org/ws/2004/03/addressing/role/anonymous</wsa:Address></wsa:ReplyTo><wsa:To>http://sgtcnpc48.cn.oracle.com:7777/ws/soap/MessagingService</wsa:To></soap:Header><soap:Body><send xmlns=\"http://www.oracle.com/beehive/2010/01/ws\" /></soap:Body></soap:Envelope>";
                        //soapBody=soapEnvelope;
                        Properties propHeaders = cmd.getHeaders();
                        Properties propQueryParas = cmd.getParas();
                        replacePropWithParam(propHeaders);
                        replacePropWithParam(propQueryParas);
                        propQueryParas.list(System.out);
                        Hashtable headers = (Hashtable)propHeaders;
                        Hashtable queryParas = (Hashtable)propQueryParas;
                        String url = cmd.getURI();
                        HttpServerResponse response =
                            hcu.post(url, soapBody, headers, queryParas);
                        TestJobElement step =
                            new TestJobElement(TestJobType.STEP);
                        int retCode = response.getReturnCode();
                        if (isSuccessHttpCode(retCode)) {
                            step.addProperty(JobStatus.PASSED,
                                             "the get cmd has been sent to web service server, return response code is " +
                                             retCode);
                            myTest.setResults(TestResult.PASS);
                        } else {
                            step.addProperty(JobStatus.FAILED,
                                             "the get cmd has been sent to web service server,return response code is " +
                                             retCode);
                            myTest.setResults(TestResult.FAIL);
                        }
                        myTest.addChildJob(step);
                        myTestCase.addChildJob(myTest);
                        addChildJob(myTestCase);
                        //set last
                        lastResponseStr =
                                response.getResponsePayloadAsString();
                        outputFrameLog("command <" + cmd.getName() +
                                       "> executed with code :" + retCode +
                                       " response body: ");
                        outputFrameLog(lastResponseStr);
                        //to do
                    } else if (cmdType.equalsIgnoreCase(SOAPCmd.CMD_TYPE_SOAP)) {

                        //to do

                    }
                } catch (Exception e) {
                    TestJobElement step = new TestJobElement(TestJobType.STEP);
                    step.addProperty(JobStatus.FAILED, "failed to run test!");
                    myTest.addChildJob(step);
                    myTest.setResults(TestResult.FAIL);
                    myTestCase.addChildJob(myTest);
                    addChildJob(myTestCase);
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            outputFrameLog("XXXX-error when lauch httpClientUtils---");
            e.printStackTrace();
        }

    }

    private String replaceDataWithParam(String soapValue) {
        soapValue = Matcher.quoteReplacement(soapValue);
        Enumeration keys = localSavedPara.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String strValue = localSavedPara.getProperty(strKey);
            strKey = Matcher.quoteReplacement(strKey);
            strKey = Matcher.quoteReplacement(strKey);
            //System.out.println("before replaced all, soapValue is "+soapValue+ "key/value is "+ strKey + " " +strValue);
            soapValue = soapValue.replaceAll(strKey, strValue);
            if (soapValue.startsWith("\\")) {
                soapValue = soapValue.substring(1);
            }
            //System.out.println("after replaced all, soap value is "+soapValue);

        }
        if (soapValue.startsWith("$")) {
            //this should be a parameter
            //System.out.println("the soap value is "+soapValue);
            String[] parasFormat = soapValue.split("\\.");
            //System.out.println("the length of value is "+parasFormat.length);
            if (parasFormat.length <= 1) {
                outputFrameLog("XXXXX-error when parse the data parameter : " +
                               soapValue);
                return null;
            } else {
                String paraname = parasFormat[1];
                System.out.println("The paras name after parsed is " +
                                   paraname);
                String soapBody = getDataProperty(paraname);
                System.out.println("The paras value after getDataProperty is " +
                                   soapBody);
                return soapBody;
            }
        } else {
            return soapValue;
        }

    }

    private boolean isSuccessHttpCode(int statuscode) {
        if ((statuscode >= 200) && (statuscode < 300)) {
            return true;
        } else
            return false;

    }

    private String runCaptureAndSaveCmd(String paraname, String LB, String RB,
                                        int occurence) {
        lastResponseStr.replaceAll("/r/n", "");
        lastResponseStr.replaceAll("/r", "");
        lastResponseStr.replaceAll("/n", "");
        int lbindex = lastResponseStr.indexOf(LB);
        int lblength = LB.length();
        int rbindex = lastResponseStr.indexOf(RB);
        if (rbindex > lbindex) {
            String paravalue =
                lastResponseStr.substring(lbindex + lblength, rbindex);
            addData(paraname, paravalue);
            localSavedPara.setProperty(paraname, paravalue);
            return paravalue;
        } else {
            return "";
        }
    }

    //only replace the property value, not key

    private void replacePropWithParam(Properties prop) {
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String strValue = prop.getProperty(strKey);
            strValue = replaceDataWithParam(strValue);
            prop.setProperty(strKey, strValue);
        }
    }
}
