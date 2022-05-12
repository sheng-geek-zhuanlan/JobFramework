package com.sheng.jobframework.jobdef.IOS;

import com.sheng.jobframework.jobdom.ACJavaJob;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


public class Utilities extends ACJavaJob //ACUITestJob//ACJavaJob
{
    // ~ tag ~
    private static boolean tag_runMain =
        false; // true: run by console or jar, false: run by ACFramework. Default is false, if call from main, set it to true.
    private static boolean tag_transCreated =
        false; // ..... getACfgParam(), contains trans_files or not
    // ~ pre-process ~
    private static final int INFO = 1;
    private static final int ERROR = 0;
    private static Document[] param_docList;
    // ~ important param ~
    private static String data_position = "";
    private static String data_lang = "";

    public Utilities() {
    }

    public Utilities(String path_xml) {
        data_position = path_xml;
        System.out.println("Utilities construction: data_position: " +
                           data_position);
    }

    public Utilities(String path_xml, boolean run_mode) {
        data_position = path_xml;
        tag_runMain = run_mode;
        System.out.println("Utilities construction: data_position: " +
                           data_position);
    }

    public void Init()
        //public void setUp()
    {
    }

    public void Run() {
    }

    public void End()
        //public void tearDown()
    {
    }

    ///////////////////////////////////////////////
    //          get param from param xml         //
    ///////////////////////////////////////////////

    // ~ 0. pre-process ~

    public void setUtlParam(boolean trans_created) {
        tag_transCreated = trans_created;
    }

    public void setUtlParam(String path_xml, String lang_runtime) {
        data_position = path_xml;
        data_lang = lang_runtime;
    }

    public void setUtlParam(boolean run_main, String lang_runtime) {
        tag_runMain = run_main;
        data_lang = lang_runtime;
    }

    public void setUtlParam(String path_xml, boolean run_main,
                            String lang_runtime) {
        data_position = path_xml;
        tag_runMain = run_main;
        data_lang = lang_runtime;
    }

    // ~ 1. parse xml, return doc[] ~
    // ~ this is ordinary steps ~

    public Document getParamXMLDoc(String param_path) {
        try {
            // ~ prepare dom parse params ~
            DocumentBuilderFactory domfac =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder dombuilder = domfac.newDocumentBuilder();
            InputStream is = new FileInputStream(param_path);
            Document doc_param = dombuilder.parse(is);

            return doc_param;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ~ 2. parse param position xml, and get the params xml position ~

    /**
     * Name: getParamListDoc
     * Usages: Get all the docs[] in all the xmls which registered in iphone_param_position.xml.
     * Prerequisite: The formated iphone_param_position.xml existed.
     *
     * @param    param_path   The iphone_param_position.xml (special format)
     * @return   boolean  true/false
     * @author  amelia.han@oracle.com
     */
    public boolean getParamListDoc(String param_path) {
        int a;
        Document doc = this.getParamXMLDoc(param_path);
        // ~ parse position xml, to get the position ~
        if (doc.getClass() == null) {
            System.out.println("** Do not find param_position file! please check!!");
            return false;
        }

        // ~ files to be included:
        // 1.position itself, 2.config file, 3.strings to be check nls, 4.nls translation(if have, control by tag_transCreated)
        // 5. .. others files to be checked and added next time ~
        // so each file line is a file to be included, (there have "3" extra line, comp, path_config, path_translation, which is path not file, should excluded)
        // so, a is the number should be excluded
        if (tag_transCreated)
            a =
  11 - 1; // 11: comp, path param should be excluded, 1: position file itself should be included
        else
            a =
  11 + 1 - 1; // 11: should be excluded by default, 1: translation is not created so translation file should be excluded, 1: position file included
        // ~ no_param means param files needs to be included ~

        int no_param =
            doc.getElementsByTagName("param").getLength() - a; // means, absent 11 path file, remains an param files and itself
        //System.out.println("IOS logging: a="+a+" no_param: "+no_param);
        int count_item = 0, count_file;
        String[] paramList = new String[no_param];
        String comp = "", path_root = "", path_config = "", path_translation =
            "", param, file;
        NodeList f =
            doc.getElementsByTagName("param"); //.item(0).getAttributes().getNamedItem("name").getNodeValue().toString();

        // ~ first Round ~
        // ~ get path_root first ~
        while (count_item < no_param + a) {
            param =
                    f.item(count_item).getAttributes().getNamedItem("name").getNodeValue();
            if (param.equals("comp"))
                comp = f.item(count_item).getTextContent();
            if (param.equals("path_root"))
                path_root = f.item(count_item).getTextContent();
            count_item++;
        }
        if (comp.equals(""))
            quit(1, "** Param: \"comp\" is not define, quit!");
        if (path_root.equals(""))
            quit(1, "** Param: \"path_root\" is not define, quit!");

        // ~ second Round ~
        // ~ get path first ~ so the "path_config" do not have to be placed at the fixed position
        count_item = 0;
        while (count_item < no_param + a) {
            param =
                    f.item(count_item).getAttributes().getNamedItem("name").getNodeValue();
            if (param.equals("path_config"))
                path_config =
                        path_root + File.separator + f.item(count_item).getTextContent();
            if (param.equals("path_translation"))
                path_translation =
                        path_root + File.separator + f.item(count_item).getTextContent();
            count_item++;
        }
        if (path_config.equals(""))
            quit(1, "** Param: \"path_config\" is not define, quit!");
        if (path_translation.equals(""))
            quit(1, "** Param: \"path_testcase\" is not define, quit!");

        // ~ then, get the files ~
        count_item = 0;
        count_file = 1;
        paramList[0] = param_path; // position file is in paramList[0]
        while (count_item < no_param + a) {
            param =
                    f.item(count_item).getAttributes().getNamedItem("name").getNodeValue();
            if (param.equals("param_config")) {
                file =
comp.toLowerCase() + f.item(count_item).getTextContent();
                paramList[1] =
                        path_config + File.separator + comp.toLowerCase() +
                        File.separator +
                        file; // ~ paramList[0] is preserved for config file ~
            }
            if (param.contains("param_testcase")) {
                file =
comp.toLowerCase() + f.item(count_item).getTextContent();
                paramList[++count_file] =
                        path_translation + File.separator + comp.toLowerCase() +
                        File.separator + file;
                // ~ add no exception for count_file overflow... ~
            }
            if (tag_transCreated)
                if (param.contains("param_translation")) { // ~ add suffix to this param!!! ~
                    // ~ param is "communicator_trans" ~
                    // ~ need "communicator_trans_ko.xml" ~
                    file =
comp.toLowerCase() + "_" + f.item(count_item).getTextContent() + "_" +
 data_lang + ".xml";
                    paramList[++count_file] =
                            path_translation + File.separator +
                            comp.toLowerCase() + File.separator +
                            "translation" + File.separator + file;
                }

            count_item++;
        }

        if (paramList[1] == null)
            quit(1, "** Param: \"param_config\" is not define, quit!");
        if (tag_transCreated)
            if (paramList[2] == null)
                quit(1,
                     "** Param: \"param_translation\" or \"param_testcase\" is not define, quit!");

        // ~ parse each xml ~
        Document[] paramsDoc = new Document[no_param];
        count_item = 0;
        while (count_item < no_param) {
            if (paramList[count_item] != null)
                paramsDoc[count_item] =
                        this.getParamXMLDoc(paramList[count_item]);

            count_item++;
        }
        param_docList = paramsDoc;

        return true;
    }
    /*
   public boolean getParamListDoc(String param_path)
  {
      int a;
      Document doc = this.getParamXMLDoc(param_path);
      // ~ parse position xml, to get the position ~
      if(doc.getClass() == null)
      {
          System.out.println("** Do not find param_position file! please check!!");
          return false;
      }

      // ~ files to be included:
      // 1.position itself, 2.config file, 3.strings to be check nls, 4.nls translation(if have, control by tag_transCreated)
      // 5. .. others files to be checked and added next time ~
      // so each file line is a file to be included, (there have "3" extra line, comp, path_config, path_translation, which is path not file, should excluded)
      // so, a is the number should be excluded
      if(tag_transCreated)
        a = 3 - 1; // 3: comp, path param should be excluded, 1: position file itself should be included
      else
        a = 3 + 1 - 1; // 3: should be excluded by default, 1: translation is not created so translation file should be excluded, 1: position file included
      // ~ no_param means param files needs to be included ~
      int no_param = doc.getElementsByTagName("param").getLength() - a; // means, absent 3 path file, remains an param files and itself
      int count_item = 0, count_file;
      String[] paramList = new String[no_param];
      String comp = "", path_config = "", path_translation = "", param, file;
      NodeList f = doc.getElementsByTagName("param");//.item(0).getAttributes().getNamedItem("name").getNodeValue().toString();

      // ~ first Round ~
      // ~ get path first ~ so the "path_config" do not have to be placed at the fixed position
      while(count_item < no_param + a)
      {
          param = f.item(count_item).getAttributes().getNamedItem("name").getNodeValue();
          if(param.equals("comp"))
            comp = f.item(count_item).getTextContent();
          if(param.equals("path_config"))
            path_config = f.item(count_item).getTextContent();
          if(param.equals("path_translation"))
            path_translation = f.item(count_item).getTextContent();
          count_item ++;
      }
      if(comp.equals(""))
        quit(1, "** Param: \"comp\" is not define, quit!");
      if(path_config.equals(""))
        quit(1, "** Param: \"path_config\" is not define, quit!");
      if(path_translation.equals(""))
        quit(1, "** Param: \"path_testcase\" is not define, quit!");

      // ~ then, get the files ~
      count_item = 0;
      count_file = 1;
      paramList[0] = param_path; // position file is in paramList[0]
      while(count_item < no_param + a)
      {
          param = f.item(count_item).getAttributes().getNamedItem("name").getNodeValue();
          if(param.equals("param_config"))
          {
              file = comp.toLowerCase() + f.item(count_item).getTextContent();
              paramList[1] = path_config + File.separator + comp.toLowerCase() + File.separator + file;// ~ paramList[0] is preserved for config file ~
          }
          if(param.contains("param_testcase"))
          {
              file = comp.toLowerCase() + f.item(count_item).getTextContent();
              paramList[++count_file] = path_translation + File.separator + comp.toLowerCase() + File.separator + file;
              // ~ add no exception for count_file overflow... ~
          }
          if(tag_transCreated)
            if(param.contains("param_translation"))
            {   // ~ add suffix to this param!!! ~
                // ~ param is "communicator_trans" ~
                // ~ need "communicator_trans_ko.xml" ~
                file = comp.toLowerCase() + f.item(count_item).getTextContent() + "_" + data_lang + ".xml";
                paramList[++count_file] = path_translation + File.separator + comp.toLowerCase() + File.separator + "translation" + File.separator + file;
            }

          count_item ++;
      }

      if(paramList[1] == null)
        quit(1, "** Param: \"param_config\" is not define, quit!");
      if(tag_transCreated)
        if(paramList[2] == null)
            quit(1, "** Param: \"param_translation\" or \"param_testcase\" is not define, quit!");

      // ~ parse each xml ~
      Document[] paramsDoc = new Document[no_param];
      count_item = 0;
      while(count_item < no_param)
      {
          if(paramList[count_item] != null)
            paramsDoc[count_item] = this.getParamXMLDoc(paramList[count_item]);

          count_item ++;
      }
      param_docList = paramsDoc;

      return true;
  }

 */


    // ~ 3. get string type param ~

    public String getACfgParam(String param) {
        if (!this.getParamListDoc(data_position))
            quit(2, "** getParamListDoc fail!");

        int a = -1;
        NodeList nodeList;

        while ((++a < param_docList.length) && (param_docList[a] != null)) {
            nodeList = param_docList[a].getElementsByTagName("param");
            int b = -1;
            while (++b < nodeList.getLength())
                if (nodeList.item(b).getAttributes().getNamedItem("name").getNodeValue().equals(param))
                    return nodeList.item(b).getTextContent();

        }

        quit(1, "Do not find param: " + param);

        return "do not find!"; //......
    }

    // ~ 4. get int type param ~

    public int getACfgIntParam(String param) {
        return Integer.valueOf(getACfgParam(param));
    }

    ///////////////////////////////////////////////
    //         commen operation on Mac           //
    ///////////////////////////////////////////////
    // ~ clean up env ~ \\

    public int fileCopy(String path_original, String path_copy) {
        int tp_last = 0;

        path_original = this.str_trans(path_original);
        path_copy = this.str_trans(path_copy);

        // ~ create translation folder ~
        if (path_copy.lastIndexOf(File.separator) > 0)
            tp_last = path_copy.lastIndexOf(File.separator);
        File file_copy_folder = new File(path_copy.substring(0, tp_last));
        if (!file_copy_folder.exists())
            if (!file_copy_folder.mkdirs())
                quit(2);

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process;

            String cmd = "cp " + path_original + " " + path_copy;
            String appleScriptCmd = "do shell script \"" + cmd + "\"";
            String[] args = { "osascript", "-e", appleScriptCmd };

            System.out.println(" **iOS Logging: " + appleScriptCmd);

            //if(wuyu)
            process = runtime.exec(args);

            // ~ thread to redirect "outpurstream" ~
            InputStream stdin = process.getInputStream(); //
            InputStream stderr = process.getErrorStream();
            Thread tIn = new Thread(new consoleS(stdin, INFO));
            Thread tErr = new Thread(new consoleS(stderr, ERROR));

            tIn.start(); // thread start
            tErr.start();
            int result = process.waitFor();
            tIn.join(); // son thread run first, then father thread continue to run
            tErr.join();

            if (result == 0) {
                System.out.println(" **iOS Logging: File copy SUCCESS!");
                return 1;
            } else {
                System.out.println(" **iOS Logging: File copy FAILED!");
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int folderDelete(String log_path) {
        log_path = this.str_trans(log_path);

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process;

            // ~ remove xx/iphone_logs ~
            // ~ use command, save time to del each file under the folder ~
            String cmd = "rm -r " + log_path; //delete_path;
            String appleScriptCmd = "do shell script \"" + cmd + "\"";
            String[] args = { "osascript", "-e", appleScriptCmd };

            System.out.println(" **iOS Logging: " + appleScriptCmd);

            //if(wuyu)
            process = runtime.exec(args);

            // ~ thread to redirect "outpurstream" ~
            InputStream stdin = process.getInputStream(); //
            InputStream stderr = process.getErrorStream();
            Thread tIn = new Thread(new consoleS(stdin, INFO));
            Thread tErr = new Thread(new consoleS(stderr, ERROR));

            tIn.start(); // thread start
            tErr.start();
            int result = process.waitFor();
            tIn.join(); // son thread run first, then father thread continue to run
            tErr.join();

            if (result == 0) {
                System.out.println(" **iOS Logging: CleanUp Env SUCCESS!");
                return 1;
            } else {
                System.out.println(" **iOS Logging: CleanUp Env FAILED!");
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int folderClean(String log_path) {
        log_path = this.str_trans(log_path);

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process;

            // ~ remove xx/iphone_logs ~
            // ~ use command, save time to del each file under the folder ~
            String cmd = "rm -r " + log_path; //delete_path;
            String appleScriptCmd = "do shell script \"" + cmd + "\"";
            String[] args = { "osascript", "-e", appleScriptCmd };

            System.out.println(" **iOS Logging: " + appleScriptCmd);

            process = runtime.exec(args);

            // ~ thread to redirect "outpurstream" ~
            InputStream stdin = process.getInputStream(); //
            InputStream stderr = process.getErrorStream();
            Thread tIn = new Thread(new consoleS(stdin, INFO));
            Thread tErr = new Thread(new consoleS(stderr, ERROR));

            tIn.start(); // thread start
            tErr.start();
            int result = process.waitFor();
            tIn.join(); // son thread run first, then father thread continue to run
            tErr.join();

            cmd = "mkdir -p " + log_path;
            appleScriptCmd = "do shell script \"" + cmd + "\"";
            args[2] = appleScriptCmd;

            process = runtime.exec(args);

            if (result == 0) {
                System.out.println(" **iOS Logging: CleanUp Env SUCCESS!");
                return 1;
            } else {
                System.out.println(" **iOS Logging: CleanUp Env FAILED!");
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void imgCollect(String log_path, String report_path) {
        log_path =
                this.str_trans(log_path + File.separator + "Run 1" + File.separator);
        //report_path = this.str_trans(report_path + File.separator + "report" + File.separator);
        // ~ report_path must translation the " " in config param xml file !! ~
        report_path = this.str_trans(report_path + File.separator);

        try {
            Runtime runtime = Runtime.getRuntime();

            String cmd =
                "cp " + log_path + File.separator + "*.png " + report_path;
            String appleScriptCmd = "do shell script \"" + cmd + "\"";
            //appleScriptCmd = "tell application \"Terminal\"\n" + "activate\n" + "do script \"" + cmd + "\"\n" + "end tell";
            String[] args = { "osascript", "-e", appleScriptCmd };

            System.out.println(" **iOS Logging: " + appleScriptCmd);

            Process process;

            // ~ run cmd when it is mac operation system ~
            // if(!tag_runMain)
            process = runtime.exec(args);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void imgCollect(String log_path, String report_path,
                           String start_tag) {
        log_path =
                this.str_trans(log_path + File.separator + "Run 1" + File.separator);
        //report_path = this.str_trans(report_path + File.separator + "report" + File.separator);
        // ~ report_path must translation the " " in config param xml file !! ~
        report_path = this.str_trans(report_path + File.separator);
        start_tag = this.str_trans(start_tag);

        try {
            Runtime runtime = Runtime.getRuntime();

            String cmd =
                "cp " + log_path + File.separator + start_tag + "*.png " +
                report_path;
            String appleScriptCmd = "do shell script \"" + cmd + "\"";
            //appleScriptCmd = "tell application \"Terminal\"\n" + "activate\n" + "do script \"" + cmd + "\"\n" + "end tell";
            String[] args = { "osascript", "-e", appleScriptCmd };

            System.out.println(" **iOS Logging: " + appleScriptCmd);

            Process process;

            // ~ run cmd when it is mac operation system ~
            // if(!tag_runMain)
            process = runtime.exec(args);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ~ only do shell script could use this ~ \\

    public String str_trans(String before) {
        // ~ applescript :  do shell script "cp /Users/Run\\ 1/ /u/ " ~
        String[] split = before.split(" ");
        String temp = "", after = "";
        int tata = 0;

        // add: "/x/x x/x " => "/x/x\\\\ x/x\\\\ " ~
        // not add: " \x\x" => " \x\x"
        for (int kaka = 0; kaka < split.length; kaka++) {
            temp = temp + split[kaka];
            // ~ when split[0]=="", means string begins with " ", then no need to add "\\" ~
            if (!((kaka == 0) && before.startsWith(" "))) {
                // when split[last], if string is not ends with " ", no need to add "\\" ~
                if (!((kaka == split.length - 1) &&
                      (!String.valueOf(before.charAt(before.length() -
                                                     1)).equals(" ")))) {
                    for (tata = split[kaka].length() - 1; tata >= 0; tata--) {
                        if (!String.valueOf(split[kaka].charAt(tata)).equals("\\"))
                            break;
                    }
                    for (int sasa = 0;
                         sasa < 2 - (split[kaka].length() - 1 - tata); sasa++)
                        temp = temp + "\\";
                }
            }
            // ~ when split[last], 1. if string is not end with " ", then no need to add "\\", 2. else, add " " ~
            if (!((kaka == split.length - 1) &&
                  (!String.valueOf(before.charAt(before.length() -
                                                 1)).equals(" "))))
                temp = temp + " ";
        }
        after = temp;
        //String after = before.replaceAll(" ", "\\\\\\\\ ");
        return after;
    }

    ///////////////////////////////////////////////
    //            ordinary function              //
    ///////////////////////////////////////////////

    public String xlifTitle(String lang) {
        String xlif;

        if (lang.equals("en"))
            xlif = ".xlf";
        else
            xlif = "_" + lang + ".xlf";

        return this.getACfgParam("file_trans_xlif") + xlif;
    }

    /**
     * Name: langInte
     * Usages: In ios, the zh_CN = zh-Hans, zh_TW = zh-Hanz, so coordinate ios and xp and linux, all use zh_CN and zh_TW
     *         1. set ios language. 2. ...... we will see
     * Prerequisite:  null
     *
     * @param    lang   the language need intepret
     * @return   String  language valid on ios
     * @author  amelia.han@oracle.com
     */
    public String langInte(String lang) {
        if (lang.equals("zh_TW"))
            return "zh-Hanz";
        else if (lang.equals("zh_CN"))
            return "zh-Hans";
        else
            return lang;
    }

    public String getLogType(String number) {
        String logType;
        int logLV = Integer.valueOf(number);
        // ~ type:
        // ~ logStart  : 4
        // ~ logMessage: 1
        // ~ logDebug  : 0
        // ~ logPass   : 5
        // ~ logFail   : 7
        // ~ logError  : 3
        // ~ logIssue  : 6
        // ~ logWarning: 2 ~
        switch (logLV) {
        case 0:
            logType = "Debug";
            break;
        case 1:
            logType = "Message";
            break;
        case 2:
            logType = "Warning";
            break;
        case 3:
            logType = "Error";
            break;
        case 4:
            logType = "Start";
            break;
        case 5:
            logType = "Pass";
            break;
        case 6:
            logType = "Issue";
            break;
        case 7:
            logType = "Fail";
            break;
        default:
            logType = "Message"; // ...
        }
        return logType;
    }

    // ~~~~~~~~~~~~~~ ordinary ~~~~~~~~~~~~~~`

    public static void announce(String type) { //1
        System.out.println("\n\t***** IPhone/IPad Communicator Report Help *****");
        System.out.println("\n-help:\t list all params requird");

        if (type.equals("xml") || type.equals("all")) { //2
            System.out.println("\n-xml:\t automation param positipn xml and path, this is mandatory." +
                               "\n\t e.g. java -jar iphoneAuto.jar -xml \"c:\\xliff files\\a.xml\"" +
                               "\n\t e.g. java -jar iphoneAuto.jar -xml \"/Users/wanmikey/Desktop/amelia/UIAutomation/iphoneAutomation_amelia/iphone_params/iphone_param_position.xml\"");
        } //2

        if (type.equals("log") || type.equals("all")) { //2
            System.out.println("\n-log:\t log path, this is not mandatory, it can be get from config xml also, \"path_log_output\"." +
                               "\n\t e.g. java -jar iphoneAuto.jar -xml \"c:\\a.xml\" -log \"c:\\xliff files\\\"" +
                               "\n\t e.g. java -jar iphoneAuto.jar -xml \"/Users/xx/logs/a.xml\" -log \"/Users/wanmikey/Desktop/amelia/UIAutomation/iphoneAutomation_amelia/iphone_logs/");
        } //2

        if (type.equals("report") || type.equals("all")) { //2
            System.out.println("\n-report:\t report path, this is not mandatory, default value is log path. It can be get from config xml also, \"path_report_output\"" +
                               "\n\t e.g. java -jar iphoneAuto.jar -xml \"c:\\log files\\\" -report \"c:\\log report\\report\\\"" +
                               //check
                    "\n\t e.g. java -jar iphoneAuto.jar -xml \"/Users/xx/logs/a.xml\" -report \"/Users/wanmikey/Desktop/amelia/UIAutomation/iphoneAutomation_amelia/iphone_results/");
        } //2

        if (type.equals("all")) { //2
            System.out.println("\n\n Example: 1. input xml, log and report path" +
                               "\n\t e.g. java -jar iphoneAuto.jar -xml \"c:\\a.xml\" -log \"c:\\xliff files\\\" -report \"c:\\xliff report\\\"" +
                               "\n\n Example: 2. input xml and log path" +
                               "\n\t e.g. java -jar iphoneAuto.jar -xml \"c:\\a.xml\" -log \"c:\\xliff files\\\"" +
                               "\n\n Example: 3. input xml and report path" +
                               "\n\t e.g. java -jar iphoneAuto.jar -xml \"c:\\a.xml\" -report \"c:\\xliff report\\\"" +
                               "\n\n Example: 4. input xml path only" +
                               "\n\t e.g. java -jar iphoneAuto.jar -xml \"c:\\a.xml\"");
        }

        System.out.println("\n\t***** End *****");
    } //1

    public void quit(int type) { //1 replace the System.exit(0/1)
        String message;

        switch (type) {
        case 1:
            message = "quitCode 101: The input value is invalid, throws! ";
            break;
        case 2:
            message = "quitCode 102: The file is not find, throws! ";
            break;
        case 3:
            message = "quitCode 103: The unexpected results return, throws";
            break;
        default:
            message = "quitCode 100: Unfined error, throws! ";
        }

        if (tag_runMain)
            System.exit(1);
        else // ACFramework will catch this exception
            throw new RuntimeException(message);

    } //1

    public void quit(int type, String message) {
        System.out.println(message);
        quit(type);
    }

    public void print(Object msg) {
        System.out.println(msg.toString());
    }
}
