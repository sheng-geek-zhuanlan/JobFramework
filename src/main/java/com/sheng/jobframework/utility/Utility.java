package com.sheng.jobframework.utility;

import com.sheng.jobframework.jobdom.ACElement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;

import java.net.URL;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Utility extends ACElement {
    public Utility() {
    }

    public static void cleanProccess() {
        //String[] cmdArray = {"tskill.exe","notepad"};
        String[] cmdArray2 = { "tskill.exe", "qtpro" };
        String[] cmdArray3 = { "tskill.exe", "QTAutomationAgent" };
        String[] cmdArray4 = { "tskill.exe", "wscript" };
        String[] cmdArray5 = { "tskill.exe", "QTAUTO~1" };
        String[] cmdArray6 = { "tskill.exe", "ODrive.exe" };
        int result = 0;
        try {
            //Process process = Runtime.getRuntime().exec(cmdArray);
            //process.waitFor();
            Process process2 = Runtime.getRuntime().exec(cmdArray2);
            process2.waitFor();
            Process process3 = Runtime.getRuntime().exec(cmdArray3);
            process3.waitFor();
            Process process4 = Runtime.getRuntime().exec(cmdArray4);
            process4.waitFor();
            Process process5 = Runtime.getRuntime().exec(cmdArray5);
            process5.waitFor();
            Process process6 = Runtime.getRuntime().exec(cmdArray6);
            process6.waitFor();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    /*
    public static String getWorkingDir(){
        return System.getProperty("user.dir");
    }*/

    public static Properties loadProperties(String FileName) {
        //
        //System.out.println(" in Utility.loadProperties: loading files: "+FileName);
        String absolutePath = FileUtil.getAbsolutePath(FileName);
        //String fileprefix = "file:///";
        //absolutePath = fileprefix+absolutePath;
        Properties prop = new Properties();
        try {
            //File f;
            InputStream fis;
            if (!FileUtil.isLocalFile(absolutePath)) {
                URL url = new URL(absolutePath);
                fis = url.openStream();
                //URI uri = new URI(absolutePath);
                //f = new File(uri);
            } else {
                //f = new File(absolutePath);
                fis = new FileInputStream(absolutePath);
            }

            //FileInputStream fis = new FileInputStream(absolutePath);
            prop.loadFromXML(fis);
            //prop.list(System.out);
            //System.out.println("\nThe foo property: " + prop.getProperty("SCREENSHOT"));
        } catch (Exception e) {
            outputLog("XXX-Exception when loading the file " + FileName);
            e.printStackTrace();
        }
        return prop;
    }

    public static boolean isIPAddress(String str) {
        String candidate =
            "A Matcher examines the results of applying a pattern.";
        String regex =
            "([1-9]|[1-9]\\d|1\\d{2}|2[0-1]\\d|22[0-3])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        String val = null;
        //System.out.println("INPUT: " + candidate);
        //System.out.println("REGEX: " + regex +"\r\n");
        while (m.find()) {
            val = m.group();
            //System.out.println("MATCH: " + val);
        }
        if (val == null) {
            //System.out.println("NO MATCHES: ");
        }
        if (val == null) {
            return false;
        } else
            return true;

    }

    public static Properties loadParaFile(String FileName) {
        String absolutePath = FileUtil.getAbsolutePath(FileName);
        String fileprefix = "file:///";
        absolutePath = fileprefix + absolutePath;
        Properties result = new Properties();
        try {
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(absolutePath);
            Element rootElement = document.getDocumentElement();
            //TO DO: here need to tune the code for flexbility

            NodeList paraList = rootElement.getElementsByTagName("param");
            if (paraList != null) {
                for (int i = 0; i < paraList.getLength(); i++) {
                    Node node = paraList.item(i);
                    if (node.getNodeType() != Node.TEXT_NODE) {
                        Element ele = (Element)node;
                        String name = ele.getAttribute("name");
                        String value = node.getTextContent();
                        result.setProperty(name, value);
                        //System.out.println("name/value is "+name+" "+value);
                    }

                }
            }
        } catch (Exception e) {
            System.out.println("selenium config params-exception:" +
                               e.getMessage());
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public static Hashtable parseCommand(String cmdList) {
        String[] cmdArr = cmdList.split(" ");
        Hashtable hash = new Hashtable();
        hash.put("USER1", cmdArr[0]);
        hash.put("USER2", cmdArr[1]);
        hash.put("USER3", cmdArr[2]);
        hash.put("PASSWD", cmdArr[3]);
        hash.put("BUILD", cmdArr[4]);
        hash.put("SERVER", cmdArr[5]);
        hash.put("MAIL_DOMAIN", cmdArr[6]);
        hash.put("WEBDAV_PORT", cmdArr[7]);
        hash.put("PLAN_ID", cmdArr[8]);
        hash.put("TESTSUITE", cmdArr[9]);
        return hash;
    }

    public static Properties parseDTECommand(String cmdList) {
        Properties prop = new Properties();
        String[] cmdArr = cmdList.split(";");
        int arrSize = cmdArr.length;
        for (int i = 0; i < arrSize; i++) {
            String[] propArr = cmdArr[i].split("=");
            prop.put(propArr[0], propArr[1]);
        }
        return prop;
    }

    public static Properties getSystemInfo() {
        Properties prop = new Properties();
        System.getProperty("user.dir");
        prop.put("currentDir", FileUtil.getCurrentDir());
        prop.put("osName", System.getProperty("os.name"));
        prop.put("osArch", System.getProperty("os.arch"));
        prop.put("osVersion", System.getProperty("os.version"));
        prop.put("userHome", System.getProperty("user.home"));
        prop.put("userLanguage", System.getProperty("user.language"));
        prop.put("userCountry", System.getProperty("user.country"));
        prop.put("userLocale",
                 System.getProperty("user.language") + "_" + System.getProperty("user.country"));
        prop.put("userName", System.getProperty("user.name"));
        //        prop.put("userRegion",System.getProperty("user.region"));
        prop.put("userTimezone", System.getProperty("user.timezone"));
        return prop;
    }

    public static long getLocalTime(long l, String GMTtimezone) {
        //timezone should be formated like this GMT+8:00
        String[] gmttime = GMTtimezone.split("\\+");
        String offset = gmttime[1];
        String[] offsetformat = offset.split(":");
        String offset_hour = offsetformat[0];
        String offset_min = offsetformat[1];
        int offset_ihours = Integer.parseInt(offset_hour);
        int offset_imins = Integer.parseInt(offset_min);
        long retl = l + offset_ihours * 3600 * 1000 + offset_imins * 60 * 1000;
        return retl;
    }

    public static Date getCurrentTime() {
        Date currentTime = new Date();
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
            String dateString = formatter.format(currentTime);
            // System.out.println(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTime;
    }

    public static long getCurrentTimeInMillis() {
        return System.currentTimeMillis();
    }

    public static String getStrCurrentTime() {
        Date currentTime = new Date();
        String dateString = "";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
            dateString = formatter.format(currentTime);
            //System.out.println("In Utility.getStrCurrentTime: "+dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateString;
    }

    public static String getTimeDiff(Date beginTime, Date endTime) {
        String elapsedTime = "temp-test";
        if (beginTime == null)
            beginTime = new Date();
        if (endTime == null)
            endTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
        String dateStrBegin = formatter.format(beginTime);
        String dateStrEnd = formatter.format(endTime);
        System.out.println("in Utility: begin time is " + dateStrBegin +
                           "\n the end time is " + dateStrEnd);
        try {
            long l = endTime.getTime() - beginTime.getTime();
            //long day=l/(24*60*60*1000);
            //long hour=(l/(60*60*1000)-day*24);
            //long min=((l/(60*1000))-day*24*60-hour*60);
            //long s=(l/1000-day*24*60*60-hour*60*60-min*60);
            //elapsedTime= Long.toString(day)+"day "+Long.toString(hour)+"hours "+Long.toString(min)+"minutes "+Long.toString(s)+"seconds ";
            l = l / 1000;
            int iSec = (int)l;
            elapsedTime = Integer.toString(iSec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return elapsedTime;
    }


    public static long formatQTPTimeToDate(String strQtpTime) {
        //System.out.println("in Utility handle QTP time: "+strQtpTime);
        int iYear = 0;
        int iMon = 0;
        int iDay = 0;
        int iHour = 0;
        int iMin = 0;
        int iSec = 0;
        String currentLocale = getSystemInfo().getProperty("userLocale");
        //this is an fr time format
        if (currentLocale.contains("fr")) {
            //fr time format is strDateTimeArr 21/01/2011 - 14:26:17
            String[] strDateTimeArr = strQtpTime.split("-");
            String[] strArr = strDateTimeArr[0].split("/");
            String strYear = strArr[2].trim();
            iYear = Integer.parseInt(strYear);
            String strMon = strArr[1].trim();
            iMon = Integer.parseInt(strMon);
            iMon = iMon - 1;
            String strDay = strArr[0].trim();
            iDay = Integer.parseInt(strDay);
            String[] strTimeArr = strDateTimeArr[1].split(":");
            String strHour = strTimeArr[0].trim();
            iHour = Integer.parseInt(strHour);
            String strMin = strTimeArr[1].trim();
            iMin = Integer.parseInt(strMin);
            String strSec = strTimeArr[2].trim();
            iSec = Integer.parseInt(strSec);

        } else if (currentLocale.contains("en")) {
            /* this is an english time format
           * 10/17/2012 - 10:52:33
           *
           */
            String[] strDateTimeArr = strQtpTime.split("-");
            String[] strArr = strDateTimeArr[0].split("/");
            String strYear = strArr[2].trim();
            iYear = Integer.parseInt(strYear);
            String strMon = strArr[0].trim();
            iMon = Integer.parseInt(strMon);
            iMon = iMon - 1;
            String strDay = strArr[1].trim();
            iDay = Integer.parseInt(strDay);
            String[] strTimeArr = strDateTimeArr[1].split(":");
            String strHour = strTimeArr[0].trim();
            iHour = Integer.parseInt(strHour);
            String strMin = strTimeArr[1].trim();
            iMin = Integer.parseInt(strMin);
            String strSec = strTimeArr[2].trim();
            iSec = Integer.parseInt(strSec);
        } else {
            /*
             * else  should take the locale as zh_CN
             */
            //windows 7 has default short time format as 2011/07/08
            String splitChar = "-";
            if (strQtpTime.contains("/")) {
                splitChar = "/";
            }
            //String[] strArr = strQtpTime.split("-");
            String[] strArr = strQtpTime.split(splitChar);
            String strYear = strArr[0].trim();
            iYear = Integer.parseInt(strYear);
            String strMon = strArr[1].trim();
            iMon = Integer.parseInt(strMon);
            iMon = iMon - 1;
            String strDay = strArr[2].trim();
            iDay = Integer.parseInt(strDay);
            String[] strTimeArr = strArr[3].split(":");
            String strHour = strTimeArr[0].trim();
            iHour = Integer.parseInt(strHour);
            String strMin = strTimeArr[1].trim();
            iMin = Integer.parseInt(strMin);
            String strSec = strTimeArr[2].trim();
            iSec = Integer.parseInt(strSec);
        }

        //System.out.println("the time after parsed is "+strYear+strMon+strDay+iHour+strMin+strSec);
        Calendar c3 = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        c3.set(iYear, iMon, iDay, iHour, iMin, iSec);
        long l = c3.getTimeInMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String strdtmNow = formatter.format(c3.getTime());
        // System.out.println("In Utility the timestamp after QTP parsed is "+ l);
        //System.out.println("In Utility the timestamp in str is  "+ strdtmNow);
        return l;
    }

    public static boolean checkLicenseExpired(String expiredDate) {
        //the date parsed in is "yyyy-mm-dd"
        String[] strArr = expiredDate.split("-");
        String strYear = strArr[0].trim();
        int iYear = Integer.parseInt(strYear);
        String strMon = strArr[1].trim();
        int iMon = Integer.parseInt(strMon);
        iMon = iMon - 1;
        String strDay = strArr[2].trim();
        int iDay = Integer.parseInt(strDay);
        Calendar c3 = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        c3.set(iYear, iMon, iDay, 0, 0, 0);
        long l = c3.getTimeInMillis();
        long currentl = System.currentTimeMillis();
        if (currentl > l) {
            return false;
        } else {
            return true;
        }

    }

    public static String getCurrentTimeStr() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String strdtmNow = formatter.format(now.getTime());
        return strdtmNow;
    }

    public static void parseInstanceInfoToEnvFile(String instanceFile,
                                                  String envDesPath,
                                                  String envDesFileName) {
        Properties instanceProp = loadProperties(instanceFile);
        try {
            FileUtil.mkDirsIfNotExists(envDesPath);
            File f = new File(envDesPath + File.separator + envDesFileName);
            if (!f.exists()) {
                f.createNewFile();
            }
            BufferedWriter utput = new BufferedWriter(new FileWriter(f));
            /* this is a txt file parsing
            utput.write("[Environment]\n");
            int paraNum = instanceProp.size();
             Enumeration keys = instanceProp.keys();
             while (keys.hasMoreElements()){
                 String strKey = (String)keys.nextElement();
                 utput.write(strKey +"=" + instanceProp.getProperty(strKey)+"\n");
             }
             utput.close();
             */
            //this is a xml parsing
            utput.write("<Environment>\n");
            int paraNum = instanceProp.size();
            Enumeration keys = instanceProp.keys();
            while (keys.hasMoreElements()) {
                String strKey = (String)keys.nextElement();
                utput.write("  <Variable>\n");
                utput.write("      <Name>" + strKey + "</Name>\n");
                utput.write("      <Value>" +
                            instanceProp.getProperty(strKey) + "</Value>\n");
                utput.write("  </Variable>\n");
            }
            utput.write("</Environment> ");
            utput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String parseInstanceInfoToEnv(Properties instanceProp) {
        //Properties instanceProp = loadProperties(instanceFile);
        String envfile = System.getProperty("java.io.tmpdir") + "envValue.xml";
        String initVbsFile = System.getProperty("java.io.tmpdir") + "Init.vbs";
        try {
            //mkDirsIfNotExists(envDesPath);
            File f = new File(envfile);
            if (!f.exists()) {
                f.createNewFile();
            }
            File f2 = new File(initVbsFile);
            if (!f2.exists()) {
                f2.createNewFile();
            }
            BufferedWriter utput = new BufferedWriter(new FileWriter(f));
            /* this is a txt file parsing
            utput.write("[Environment]\n");
            int paraNum = instanceProp.size();
             Enumeration keys = instanceProp.keys();
             while (keys.hasMoreElements()){
                 String strKey = (String)keys.nextElement();
                 utput.write(strKey +"=" + instanceProp.getProperty(strKey)+"\n");
             }
             utput.close();
             */
            //this is a xml parsing
            utput.write("<Environment>\n");
            int paraNum = instanceProp.size();
            Enumeration keys = instanceProp.keys();
            while (keys.hasMoreElements()) {
                String strKey = (String)keys.nextElement();
                String value = instanceProp.getProperty(strKey);
                if (value.startsWith("path:")) {
                    System.out.println("***Path value " + value);
                    //String[] temp = strKey.split(":");
                    String[] temp = value.split(":");
                    String relativePath = temp[1];
                    value = FileUtil.getAbsolutePath(relativePath);
                }

                value = ReportUtil.transferCDATA(value);
                utput.write("  <Variable>\n");
                utput.write("      <Name>" + strKey + "</Name>\n");
                utput.write("      <Value>" + value + "</Value>\n");
                utput.write("  </Variable>\n");
            }
            utput.write("</Environment> ");
            utput.close();

            BufferedWriter utput2 = new BufferedWriter(new FileWriter(f2));
            // try to create init vbs file
            utput2.write("Environment.LoadFromFile " + "\"" + envfile + "\"" +
                         "\n");
            utput2.close();
            return initVbsFile;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return initVbsFile;

    }

    public static boolean strToBoolean(String bool) {

        if (bool.equalsIgnoreCase("TRUE")) {
            return true;
        } else if (bool.equalsIgnoreCase("FALSE")) {
            return false;
        } else {
            //System.out.println("the bool is not false either true");
            return false;
        }

    }

    public static int strToInt(String str, int idefault) {
        int i = idefault;
        try {
            if (str.equalsIgnoreCase("")) {
                outputLog("!!!-String is blank, will use default " + idefault);
                return idefault;
            }

            i = Integer.parseInt(str);
        } catch (Exception e) {
            outputLog("Convert String to Integer: the String value is blank or illegal: " +
                      str + " will use default: " + idefault);
            return idefault;
        }
        return i;
    }

    public static Properties mergeTwoProperties(Properties prop1,
                                                Properties prop2) {
        int isize = prop2.size();
        Enumeration keys = prop2.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String strValue = prop2.getProperty(strKey);
            //TO DO: there is potential bug if two diffrent keys has same value.
            //if(!prop1.containsKey(strKey)){
            //System.out.println("in Utility: try to add key/value is "+strKey+"/"+strValue);
            prop1.put(strKey, strValue);
            //}
        }
        return prop1;

    }

    public static Properties prop1OveridedByProp2(Properties prop1,
                                                  Properties prop2) {
        int isize = prop2.size();
        Enumeration keys = prop2.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String strValue = prop2.getProperty(strKey);
            //temp solution to screen out those COMP parameter parsed from parent job to child job
            if (!strKey.equalsIgnoreCase("COMP"))
                prop1.put(strKey, strValue);
        }
        return prop1;
    }

    public static ArrayList mergeTwoArrayList(ArrayList arr1, ArrayList arr2) {
        int isize = arr2.size();
        for (int i = 0; i < isize; i++) {
            arr1.add(arr2.get(i));
        }
        return arr1;
    }

    public static NodeList loadTestSuite(String FileName) {
        NodeList rootNode = null;
        try {
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(FileName);
            Element rootElement = document.getDocumentElement();
            //TO DO: here need to tune the code for flexbility

            NodeList suiteNode = rootElement.getElementsByTagName("suite");
            NodeList componentNode =
                rootElement.getElementsByTagName("component");
            NodeList caseNode = rootElement.getElementsByTagName("casefile");
            //Test Code for frameJob.xml

            //System.out.println("the root node name is "+rootElement.getNodeName());
            // NodeList job = rootElement.getChildNodes();
            // int ilen = job.getLength();
            //for(int i =0;i<ilen;i++){
            //    if(job.item(i).getNodeType()!=Node.TEXT_NODE)
            //    {
            //System.out.println("the node name "+job.item(i).getNodeName());
            //    }
            //}

            //end test
            rootNode = caseNode;
            if (suiteNode.getLength() > 0) {
                rootNode = suiteNode;
            } else if (componentNode.getLength() > 0)
                rootNode = componentNode;

        } catch (Exception e) {
            System.out.println("xlif-loadfile-exception:" + e.getMessage());
            e.printStackTrace();
        }
        return rootNode;
    }


    public static boolean uploadToFTPServer(String hostname, int port,
                                            String userName, String password,
                                            String reportBasePath,
                                            ArrayList arrComponentRunned,
                                            String ftpServerURL,
                                            String prefixReport) {
        FTPClient ftpClient = new FTPClient();
        port = 21;
        boolean success = false;
        try {
            int reply;
            //ftpClient.connect(hostname,port);
            ftpClient.connect(hostname);
            ftpClient.login(userName, password);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return success;
            }

            //TO DO:in factorcy mode, there should be only one factory case runned. here should add some judge code instead of for cycle
            //To do: should compose a parser class to make the structure clear
            String summaryDataPath =
                reportBasePath + File.separator + "summary.xml";
            String summaryHtmlPath =
                reportBasePath + File.separator + "SummaryReport.html";
            //            InputStream input1;
            //            input1 = new FileInputStream(summaryDataPath);
            //            InputStream input2;
            //            input2 = new FileInputStream(summaryHtmlPath);
            //mkdir
            ftpClient.makeDirectory(ftpServerURL);

            ftpClient.changeWorkingDirectory(ftpServerURL);
            // ftpClient.storeFile(prefixReport+"_Summary.xml", input1);
            // ftpClient.storeFile(prefixReport+"_SummaryReport.html", input2);
            int caseRunned = arrComponentRunned.size();
            for (int k = 0; k < caseRunned; k++) {
                String componentName = (String)arrComponentRunned.get(k);
                ftpClient.makeDirectory(componentName);
                ftpClient.changeWorkingDirectory(componentName);

                String componentXMLDataPath =
                    reportBasePath + File.separator + componentName +
                    File.separator + componentName + "summary.xml";
                String componentXHtmlPath =
                    reportBasePath + File.separator + componentName +
                    File.separator + componentName + ".html";
                //InputStream inputXml;

                //inputXml = new FileInputStream(componentXMLDataPath);
                InputStream inputHtml;
                inputHtml = new FileInputStream(componentXHtmlPath);
                //ftpClient.storeFile(prefixReport+"_Summary.xml", inputXml);
                ftpClient.storeFile(prefixReport + "_" + componentName +
                                    ".html", inputHtml);
                //inputXml.close();
                inputHtml.close();
                outputLog("the report has been uploaded to " + hostname +
                          " path " + ftpServerURL + File.separator +
                          componentName + File.separator + prefixReport + "_" +
                          componentName + ".html");
                ftpClient.changeToParentDirectory();
            }
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("faliled to connenct ftp server " + hostname);
        }
        return success;
    }


    public static void testReference(String str1, String str2) {
        String temp = str1;
        str1 = str2;
        str2 = temp;

    }

    public static double getTimeDiff(long dtmBeginL, long dtmEndL) {

        long mills = getTimeDiffMills(dtmBeginL, dtmEndL);
        double iSec = (double)mills / 1000;
        return iSec;
    }

    public static long getTimeDiffMills(long dtmBeginL, long dtmEndL) {
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dtmBeginL);
        String strdtmBegin = formatter.format(c.getTime());
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(dtmEndL);
        String strdtmEnd = formatter.format(c2.getTime());
        if (dtmBeginL == 0) {
            Calendar c3 = Calendar.getInstance();
            dtmBeginL = c3.getTimeInMillis();
        }
        if (dtmEndL == 0) {
            Calendar c3 = Calendar.getInstance();
            dtmEndL = c3.getTimeInMillis();
            String strdtmNow = formatter.format(c3.getTime());
        }
        if (dtmEndL < dtmBeginL) {
            //dtmEndL= dtmEndL+8*60*60*1000;
        }
        long diff = dtmEndL - dtmBeginL;
        return diff;

    }

    public static void outputInfo(Properties prop) {

        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            outputLog(strKey + " = " + prop.getProperty(strKey));
        }
    }

    public static void main(String[] args) {
        /*
        Properties systemProp = Utility.getSystemInfo();
        Enumeration keys = systemProp.keys();
        String customerInfo="";
        while (keys.hasMoreElements()){
            String strKey = (String)keys.nextElement();
            customerInfo=customerInfo+"\n"+strKey +"=" + systemProp.getProperty(strKey)+"\n";
        }
        Properties prop = System.getProperties();
        prop.list(System.out);
        */
        /*
        String pro = "qtpro.exe";
        System.out.println(pro.length());
        System.out.println(pro.lastIndexOf("exe"));
        System.out.println(pro.length()-pro.lastIndexOf("exe"));
        if(pro.length()-pro.lastIndexOf("exe")==3)
            System.out.println(pro.substring(0,pro.lastIndexOf("exe")-1));
        */
        //Utility.loadParaFile("sel_parameter.xml");

        /*
      System.setProperty("user.timezone","GMT+8:00");
        long l = System.currentTimeMillis();
        long retl = Utility.getLocalTime(l, "GMT+8:00");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String speicifedTriggerTime = formatter.format(c.getTime());
        System.out.println("before timezone caculate: "+speicifedTriggerTime);
        c.setTimeInMillis(retl);
      String speicifedTriggerTime2 = formatter.format(c.getTime());
      System.out.println("after timezone caculate: "+speicifedTriggerTime2);*/
        String ip = "192.168.1.100";
        System.out.println(Utility.isIPAddress(ip));
        //System.out.println(customerInfo);
        // String str1 = "test";
        //String str2 = "test2";
        //Utility.testReference(str1,str2);
        //System.out.println("str1 is "+str1);
        //System.out.println(Utility.getSystemInfo().getProperty("userLocale"));
        /*
        System.setProperty("user.timezone","GMT+8:00");
        System.out.println(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        Calendar c= Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        String strdtmBegin = formatter.format(c.getTime());
        System.out.println("current time by calendar is "+strdtmBegin);
        Date currentTime = new Date();
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        System.out.println("current time by Date is "+dateString);
        System.getProperties().list(System.out);
        */
        //Utility.loadProperties("config.xml");
        // Utility.loadTestSuite("C:\\Job_define.xml");
        // Utility.getCurrentTime();
        // System.getProperties().list(System.out);
        //ArrayList componentArr = new ArrayList(100);
        //componentArr.add("obeo");
        //componentArr.add("beetube");
        //componentArr.add("obee");
        //Utility.uploadToFTPServer("sgtqtp2.cn.oracle.com",20,"auto","","D:\\oracle",componentArr,"OCSCORE_2.0_LINUX_100227.2000_en_US.UTF-8","zh_CN_1267607370");
        //Properties prop = Utility.getSystemInfo();
        //prop.list(System.out);
        //Properties prop = Utility.loadProperties("instanceInfo.xml");
        //prop.list(System.out);
        // Enumeration keys = prop.keys();
        // while (keys.hasMoreElements()){
        //     System.out.println(keys.nextElement());
        // }
        try {
            //System.getProperties().list(System.out);
            //String os = System.getProperty("os.name");
            /*
          String os = System.getProperty("file.seperator");
           os = File.separator;
           System.out.println(os);
          */
            long begin = 1337155527950L;
            long end = 1337155537122L;
            long diff = end - begin;
            System.out.println("the long diff is " + diff);
            double isec = (double)diff / 1000;
            String strElapsedTime = String.valueOf(isec);
            //String result = String .format("%.2f");
            System.out.println("the sec is " + isec);
            System.out.println("the isec is " + strElapsedTime);

            /*
        InetAddress ia = InetAddress.getLocalHost();
        String host = ia.getHostName();
        System.out.println(host);*/

        } catch (Exception e) {

        }
    }
}
