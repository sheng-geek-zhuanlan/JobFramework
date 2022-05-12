package com.sheng.jobframework.jobdef.IOS;


import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;

import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.jobdom.TestJobElement;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;


public class iphoneAuto extends Utilities {
    // ~ only tag ~
    private boolean tag_runMain =
        false; // true: run by console or jar, false: run by ACFramework. Default is false, if call from main, set it to true.
    // ~ locale or main ~
    private String data_argsCmd[];
    private String data_logPath = "";
    private String data_reportPath = "";
    private String data_xmlPath = "";
    private String data_testLang = "";
    // ~ pre-process param ~
    private Stack stack_testcase = new Stack();
    private static Document param_logDoc; // �²�
    private static final int INFO = 1;
    private static final int ERROR = 0;
    // ~ data from param file ~
    private String data_name = "";
    private String data_picSelect = "";
    private String pathD_transFolder = "";
    private String pathD_logFolder = "";
    private String pathD_reportFolder = "";
    private String pathD_runtimeOrg = "";
    private String pathD_runtime = "";
    private String pathD_testcase = "";
    private String pathD_param = "";
    private String pathD_xlif = "";
    private String pathD_trans = "";
    private String pathD_reportACF = "";

    //shining added to make the job not hardcoded in the conferencing_param_config.xml
    private String job_name = "";
    //end of shining
    //shining2 added 05-17
    private String job_location_path = "";
    //end of shining


    public iphoneAuto() {
    }

    // ~ Ordinary Function ~ \\

    public void Init()
        //public void setUp()
    {
        this.inputProcess();
    }

    public void Init(String jobname)
        //public void setUp()
        //shining added 0515 for the parsing job name
    {
        job_name = jobname;
        this.inputProcess();
    }
    //end of shining
    //shining2 added 0517

    public void Init(TestJobElement currentJob) {
        job_name = currentJob.getName();
        job_location_path = currentJob.getLocationPath();
        this.inputProcess();
    }
    //end of Shining


    public void Run() {
        // int result_clean = 0, result_start = 0, result_report = 0;
        this.setUtlParam(data_xmlPath, tag_runMain, data_testLang);

        // ~~~~~~~~~~~~~~~ //
        // ~             ~ //
        // ~ cleanUp Env ~ //
        // ~             ~ //
        // ~ ~~~~~~~~~~~~~ //
        if (this.folderDelete(pathD_transFolder) ==
            1) // ~ wunaia, is only a suffix... to fit the logClean function ~
            print("\n\t **Step 1-1: CleanUp translation: success!\n\n");
        else
            print("\n\t **Step 1-1: CleanUp translation: fail!\n\n");

        if (this.folderClean(pathD_logFolder) ==
            1) // + "Run 1" + File.separator) == 1)
            print("\n\t **Step 1-2: CleanUp log: success!\n\n");
        else
            print("\n\t **Step 1-2: CleanUp log: fail!\n\n");

        if (this.folderDelete(pathD_reportFolder) == 1)
            print("\n\t **Step 1-3: CleanUp report: success!\n\n");
        else
            print("\n\t **Step 1-3: CleanUp report: fail!\n\n");

        if (this.fileCopy(pathD_runtimeOrg, pathD_runtime) == 1)
            print("\n\t **Step 1-4: Copy runtime File: success!\n\n");
        else
            print("\n\t **Step 1-4: Copy runtime File: fail!\n\n");


        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
        // ~                           ~ //
        // ~ create runtime param file ~ //
        // ~                           ~ //
        // ~ ~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
        fileGene go = new fileGene();

        if (go.runtimeGenerate(this, pathD_runtime) == 1)
            print("\n\t **Step 2-1: Create runtime param: success!\n\n");
        else
            print("\n\t **Step 2-1: Create runtime param: fail!\n\n");

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
        // ~                          ~ //
        // ~ replace javascript param ~ //
        // ~                          ~ //
        // ~ ~~~~~~~~~~~~~~~~~~~~~~~~~~ //
        //fileGene go = new fileGene();

        if (go.fileInterpret(pathD_testcase, pathD_runtime) == 1)
            print("\n\t **Step 2-2: Interpret runtime testcase: success!\n\n");
        else {
            print("\n\t **Step 2-2: Interpret runtime testcase: fail!\n\n");
            quit(2, "\n\t**iOS Logging: Testcase Interpret fail, throws!");
        }


        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
        // ~                         ~ //
        // ~ create translation file ~ //
        // ~                         ~ //
        // ~ ~~~~~~~~~~~~~~~~~~~~~~~~~ //
        //fileGene go = new fileGene();

        if (go.transGenerate(pathD_param, pathD_xlif, pathD_trans) == 1)
            print("\n\t **Step 2-3: Create translation: success!\n\n");
        else
            print("\n\t **Step 2-3: Create translation: fail!\n\n");
        this.setUtlParam(true);


        // ~~~~~~~~~~~~~~~~~~~~ //
        // ~                  ~ //
        // ~ start instrument ~ //
        // ~                  ~ //
        // ~ ~~~~~~~~~~~~~~~~~~ //
        if (this.startInstrument() == 1)
            print("\n\t **Step 3: Run Automation: success!\n\n");
        else {
            print("\n\t **Step 3: Run Automation: fail!\n\n");
            quit(3);
        }


        // ~~~~~~~~~~~~~~~ //
        // ~             ~ //
        // ~  parse log  ~ //
        // ~             ~ //
        // ~ ~~~~~~~~~~~~~ //
        //this.setUtlParam(true);
        if (this.logParse(data_logPath, data_reportPath) == 1)
            print("\n\t **Step 4: Generate Report: success!\n\n");
        else {
            print("\n\t **Step 4: Generate Report: fail!\n\n");
            quit(3);
        }
        // ~ move pic from instrument log to amelia report log ~
        this.imgCollect(data_logPath, pathD_reportFolder, data_picSelect);


        // ~~~~~~~~~~~~~~~~~~~~~ //
        // ~                   ~ //
        // ~ report screenshot ~ //
        // ~                   ~ //
        // ~ ~~~~~~~~~~~~~~~~~~~ //
        // ~ move pic from instrument log to ACFrame report log ~
        String testcase = "";
        String picFolder = "";
        while (stack_testcase.size() > 0) {
            testcase = stack_testcase.pop().toString();
            picFolder =
                    pathD_reportACF + File.separator + this.getACfgParam("tag_screenshot_instrument") +
                    testcase;
            File acfr = new File(picFolder);
            if (!acfr.isDirectory())
                acfr.mkdirs();
            this.imgCollect(data_logPath, picFolder,
                            data_picSelect + testcase);
            print("\n\t **Step 5: Collect ACF Pic: success! (to " + picFolder +
                  ")\n");
        }

    }

    public void End()
        //public void tearDown()
    {
    }

    // ~ prepare input string ~ \\

    public void inputProcess() {
        // ~ debug from main ~
        if (tag_runMain) { //1.2.1
            //data_testLang = getACfgParam("config_lang_runtimeDefault");
            // getACfgParam should be init with setUtlParam,
            data_testLang = "zh_TW";
            this.setUtlParam(true, data_testLang);

            // ~ switch 1: params from input ~
            if (data_argsCmd != null) { //1.2.1.1
                // ~ look for param help ~
                int p = 0;
                String temp = "";

                // ~ check for help first ~
                for (p = 0; p < data_argsCmd.length; p++) {
                    temp = data_argsCmd[p];

                    if (temp.equals("--help") || temp.equals("-help") ||
                        temp.equals("--Help") || temp.equals("-Help") ||
                        temp.equals("--h") || temp.equals("-h") ||
                        temp.equals("--H") || temp.equals("-H")) { //3_1
                        announce("all");
                        quit(1);
                    } //3_1
                }

                // ~ check for manditory param "-xml" ~
                for (p = 0; p < data_argsCmd.length; p++) {
                    if (data_argsCmd[p].equals("-xml"))
                        break;
                }
                if (p == data_argsCmd.length) {
                    System.out.println("\n **iOS Logging Warning: The xml path value is manditory, please specify it!");
                    announce("xml");
                    quit(1);
                }

                // ~ look for param path ~
                int i = 0;
                while (i < data_argsCmd.length) { //1.2.1.1.1
                    // ~ if "-xml" ~
                    if (data_argsCmd[i].equals("-xml")) {
                        i++;
                        if ((i == data_argsCmd.length) ||
                            data_argsCmd[i].startsWith("-")) {
                            System.out.println("\n **iOS Logging Warning: The xml path value is null or invalid, please check!");
                            announce("xml");
                            quit(1);
                        } else
                            data_xmlPath = data_argsCmd[i];
                    }
                    // ~ if "-log" ~
                    else if (data_argsCmd[i].equals("-log")) {
                        i++;
                        if ((i == data_argsCmd.length) ||
                            data_argsCmd[i].startsWith("-")) {
                            System.out.println("\n **iOS Logging Warning: The log path value is null or invalid, please check!");
                            announce("log");
                            quit(1);
                        } else
                            data_logPath = data_argsCmd[i];
                    }
                    // ~ if "-report" ~
                    else if (data_argsCmd[i].equals("-report")) { //4
                        i++;
                        if ((i == data_argsCmd.length) ||
                            data_argsCmd[i].startsWith("-")) { //5
                            System.out.println("\n **iOS Logging Warning: The report path value is null or invalid, please check!");
                            announce("report");
                            quit(1);
                        } //5
                        else
                            data_reportPath = data_argsCmd[i]; // need more
                    } //4
                    i++;
                } //1.2.1.1.1

                // ~ if data_logPath, data_reportPath is not defined from input, so find them in config.xml ~
                this.setUtlParam(data_xmlPath, true, data_testLang);
                data_logPath =
                        getACfgParam("path_root") + File.separator + getACfgParam("path_log");
                data_reportPath =
                        getACfgParam("path_root") + File.separator + getACfgParam("path_report");
            } //1.2.1.1
            else { // ~ debug in locale  ~
                data_xmlPath =
                        "C:\\JDeveloper\\Auto_iphone\\data\\param\\iphone_param_position_xp.xml";
                data_logPath =
                        "C:\\JDeveloper\\Auto_iphone\\reportW\\data\\log";
                data_reportPath = data_logPath;
            }
        } //1.2.1
        else { // ~ debug from ACFramework ~
            //data_xmlPath = getEnvProperty("$PATH_XML", "/Users/wanmikey/Desktop/amelia/UIAutomation/iphoneAutomation_amelia/iphone_params/iphone_param_position.xml");
            data_xmlPath = getEnvProperty("$PATH_XML", "");
            if (data_xmlPath.equalsIgnoreCase("")) {
                outputFrameLog("XXXX-Blank position xml path! will quit!");
                return;
            }
            data_xmlPath = FileUtil.getAbsolutePath(data_xmlPath);
            //data_logPath = getEnvProperty("$PATH_LOG", "/Users/wanmikey/Desktop/amelia/UIAutomation/iphoneAutomation_amelia/iphone_logs/");
            //data_reportPath = getEnvProperty("$PATH_REPORT", "/Users/wanmikey/Desktop/amelia/UIAutomation/iphoneAutomation_amelia/iphone_results/");
            this.setUtlParam(data_xmlPath, tag_runMain, data_testLang);
            data_testLang = getEnvProperty("$TEST_LANG", "zh_TW");
            data_logPath =
                    this.getACfgParam("path_root") + File.separator + this.getACfgParam("path_log");
            data_reportPath =
                    this.getACfgParam("path_root") + File.separator + this.getACfgParam("path_result");
        }

        this.setUtlParam(data_xmlPath, tag_runMain, data_testLang);
        // ~~~~~~~~~~~~~~~ //
        // ~             ~ //
        // ~  Env Param  ~ //
        // ~             ~ //
        // ~ ~~~~~~~~~~~~~ //
        // ~ path_transFolder = "xx/iphone_params/conferencing/translation" ~
        pathD_transFolder =
                this.getACfgParam("path_root") + File.separator + this.getACfgParam("path_translation") +
                File.separator + this.getACfgParam("comp").toLowerCase() +
                File.separator + "translation";
        pathD_logFolder = data_logPath;
        pathD_reportFolder = data_reportPath + File.separator + "report";
        // ~ name = "conferencing" ~
        data_name = this.getACfgParam("name").toLowerCase();
        // ~ path_param = "xx/iphone_params/conferencing/conferencing_param_check.xml" ~
        pathD_param =
                this.getACfgParam("path_root") + File.separator + this.getACfgParam("path_translation") +
                File.separator + data_name + File.separator + data_name +
                this.getACfgParam("param_testcase_nlsCheck");
        // ~ path_xlif = "xx/iphone_xlif/conferencing/oracle.ocs.mobilexx.communicator_de.xlf" ~
        pathD_xlif =
                this.getACfgParam("path_root") + File.separator + this.getACfgParam("path_xlif") +
                File.separator + data_name + File.separator +
                this.xlifTitle(data_testLang);
        // ~ path_trans = "xx/iphone_params/conferencing/translation/conferencing_trans_zh_CN.xml" ~
        pathD_trans =
                pathD_transFolder + File.separator + data_name + "_" + this.getACfgParam("param_translation_xlif") +
                "_" + data_testLang + ".xml";
        pathD_runtimeOrg =
                this.getACfgParam("path_root") + File.separator + this.getACfgParam("path_translation") +
                File.separator + data_name + File.separator + "template_" +
                data_name + this.getACfgParam("param_testcase_runtime");
        pathD_runtime =
                this.getACfgParam("path_root") + File.separator + this.getACfgParam("path_translation") +
                File.separator + data_name + File.separator + data_name +
                this.getACfgParam("param_testcase_runtime");
        pathD_testcase =
                this.getACfgParam("path_root") + File.separator + this.getACfgParam("path_testcase") +
                File.separator + data_name + File.separator +
                this.getACfgParam("testcase_js_1");
        // ~ move pics to ACFramework ~
        // ~ path_reportACF = "xx/ACFrame/results/local_ts_120115134747/IPhone_Conferencing/screenshot/TestCase_checkCommunicator_1/test_Starting\ Test" ~
        //shining added 0515 for screen shot path
        //shining2 commented out the old path_reportSS, generate path based on the job
        /*
    String path_reportSS = this.getACfgParam("path_acframe") + File.separator + "results" + File.separator
                        //  + this.getEnvProperty("JOBID") + File.separator + this.getACfgParam("report_acframe_job") + File.separator
                        + this.getEnvProperty("JOBID") + File.separator + job_name;
    */
        String path_reportSS = job_location_path;
        //end of shining2
        //pathD_reportACF = path_reportSS + File.separator + this.getACfgParam("report_acframe_testcase");// + File.separator
        pathD_reportACF =
                path_reportSS + File.separator + this.getACfgParam("report_acframe_testcase") +
                File.separator + "screenshot";
        //shining end
        // + this.getACfgParam("report_acframe_test");
        // ~ only move the pic begins with "pic_select" ~
        data_picSelect = this.getACfgParam("tag_screenshot_instrument");

    }

    // ~ instrument start ~ \\

    public int startInstrument() {
        String path_automation =
            this.str_trans(this.getACfgParam("config_path_instrumentsTemplate"));
        String path_application =
            this.str_trans(this.getACfgParam("config_path_application"));
        String path_output =
            this.str_trans(this.getACfgParam("path_root") + File.separator +
                           this.getACfgParam("path_log"));
        String path_testcase =
            this.str_trans(this.getACfgParam("path_root") + File.separator +
                           this.getACfgParam("path_testcase"));
        // ~ run the interpreted testcase ~
        String testcase_1 = "runtime_" + this.getACfgParam("testcase_js_1");
        String name = this.getACfgParam("name").toLowerCase();

        String path_lib =
            this.str_trans(this.getACfgParam("path_root") + File.separator +
                           this.getACfgParam("path_lib"));
        String ver_sdk = this.getACfgParam("config_lang_sdkVersion");

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process;

            // ~ change iphone/ipad language ~
            String cmd =
                path_lib + "/ios-sim-locale -sdk " + ver_sdk + " -language " +
                this.langInte(data_testLang) + " -locale " +
                this.langInte(data_testLang);
            String appleScriptCmd = "do shell script \"" + cmd + "\"";
            String[] args = { "osascript", "-e", appleScriptCmd };
            System.out.println("**iOS Logging** change iphone language: " +
                               appleScriptCmd);

            process = runtime.exec(args);

            // ~ start to run~
            cmd =
"instruments -t " + path_automation + " " + path_application +
  " -e UIASCRIPT " + path_testcase + File.separator + name + File.separator +
  testcase_1 + " -e UIARESULTSPATH " + path_output;

            appleScriptCmd = "do shell script \"" + cmd + "\"";
            //appleScriptCmd = "tell application \"Terminal\"\n" + "activate\n" + "do script \"" + cmd + "\"\n" + "end tell";
            args[2] = appleScriptCmd;
            System.out.println("**iOS Logging** run instrument script: " +
                               appleScriptCmd);

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

            // if (result == 1 )
            if (result == 0) {
                System.out.println("iOS Logging: Instruments run SUCCESS!");
                return 1;
            } else {
                System.out.println("iOS Logging: Instruments run FAILED!");
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            quit(3, "Exception in instrumentStart.java");
            return 0;
        }
    }

    // ~ reportWriter.java ~ \\

    public int logParse(String logPath, String reportPath) { // 1.1
        String slash = File.separator;
        String log_path = ""; //logPath + slash + "Run 1" + slash;
        String log_name = "Automation Results.plist";
        String report_path = reportPath + slash + "report" + slash;
        String report_name = "report.txt"; //"report.xml";

        // ~ get log path ~
        File folder = new File(logPath);
        String[] chld = folder.list();

        if (chld[0].startsWith("Run"))
            log_path = logPath + slash + chld[0] + slash;
        else
            quit(2, "  **iOS Logging: Log Folder is not find! Return Fail!");

        try { // 2.1
            File file_log = new File(log_path + log_name);
            File file_report_path = new File(report_path);
            FileOutputStream file_report;
            BufferedWriter w_file_report;
            //////////////////////////////////////////////////////////////////
            // ~ check log file exists or not ~
            if (!file_log.isFile())
                quit(2,
                     " **iOS Logging: File path: " + file_log.getAbsolutePath() +
                     " not exist! Return Fail!");
            // ~ if report folder existed, delete ~
            if (file_report_path.isDirectory()) {
                file_report_path.delete();
                System.out.println(" **iOS Logging: File path: " +
                                   file_report_path.getAbsolutePath() +
                                   " existed! So delete and create new!");
            }
            // ~ create new folder path ~
            file_report_path.mkdirs();
            // ~ check ~
            if (file_report_path.isDirectory())
                System.out.println(" **iOS Logging: File path: " +
                                   file_report_path.getAbsolutePath() +
                                   " create success!");
            else
                quit(2,
                     " **iOS Logging: File path: " + file_report_path.getName() +
                     " create failed! return fail!");
            ///////////////////////////////////////////////////////////////////////
            // ~ define log file params ~
            //file_report = new File(report_path + report_name);
            file_report = new FileOutputStream(report_path + report_name);
            w_file_report =
                    new BufferedWriter((new OutputStreamWriter(file_report,
                                                               "UTF-16")));
            //PrintWriter w_file_report = new PrintWriter((new OutputStreamWriter(file_report, "UTF-16"))); // ...............amelia why !!!!!!!!!!16!!!!!!!!!!!!!


            // ~ get the strings from plist file ~
            NSDictionary rootDict =
                (NSDictionary)PropertyListParser.parse(file_log);
            NSObject[] parameters =
                ((NSArray)rootDict.objectForKey("All Samples")).getArray();

            ///////////////////////////////////////////////////////////////////
            NSObject param;
            Document doc;
            //NodeList list_log_string;
            String log_type_no, log_type, log_msg;
            String nls_name, nls_string, nls_value;
            String nls_tag = this.getACfgParam("param_translation_xlif") + "_";
            String[] temp;
            int count_testcase = 0, count_step = 0, count_pass =
                0, count_fail = 0;
            int count_nls = 0, count_correct = 0, count_incorrect = 0;
            int i = -1;
            // ~ example of a log report ~
            // ~ one param ~
            //<plist version="1.0">
            // <dict>
            //  <key>LogType</key>
            //  <string>Debug</string> // Default(Message) // Pass // Fail
            //  <key>Message</key>
            //  <string>nls string</string>
            //  <key>Timestamp</key>
            //  <date>2011-12-19T08:57:00Z</date>
            //  <key>Type</key>
            //  <integer>0</integer>
            // </dict>
            // </plist>

            ////////////////////////
            //// TestCase //////////
            ////////////////////////
            //beginTestCase("TestCase_checkCommunicator_1");
            beginTestCase(this.getACfgParam("report_acframe_testcase"));
            this.add_to_log(w_file_report,
                            "###########> TestCase Report <###########");

            // ~ parse each log into each report line ~
            while (++i < parameters.length) { // 3.1
                // ~ first: parse each param, get message ~
                param = parameters[i];
                if (this.getPlistArrayDoc(param))
                    doc = param_logDoc;
                else { //4.2
                    this.add_to_log(w_file_report,
                                    "=\\=> Exit: Parse log Error! Maybe miss some LogStart... Next");
                    this.reportFail("=\\=> Exit: Parse log Error! Maybe miss some LogStart... Next");
                    continue;
                } //4.2

                log_type_no =
                        doc.getElementsByTagName("integer").item(0).getTextContent();
                log_type = this.getLogType(log_type_no);
                log_msg =
                        doc.getElementsByTagName("string").item(1).getTextContent();

                // ~ second: switch ~
                if (log_type.equals("Start")) { // 4.1
                    ////////////////////////
                    //// TestCase //////////
                    ////////////////////////
                    beginTest(this.getACfgParam("tag_screenshot_instrument") +
                              log_msg); //this.getACfgParam("report_acframe_test")
                    stack_testcase.push(log_msg);

                    this.add_to_log(w_file_report,
                                    "\r\n** TestCase No." + (++count_testcase) +
                                    " **\n todo: " + log_msg);
                    //this.pass(" TestCase No." + (count_testcase) + " **\n todo: " + log_msg); // amelia: add into output
                    this.reportPass(" TestCase No." + (count_testcase) +
                                    " **\n todo: " +
                                    log_msg); // amelia: add into output

                    count_step = 0;
                    // ~ continue to parse until reach: 1. Error exit 2. Case end ~
                    while (++i < parameters.length) { // 5.1
                        // ~ parse next node ~
                        param = parameters[i];
                        if (this.getPlistArrayDoc(param))
                            doc = param_logDoc;
                        else { //6.6
                            this.add_to_log(w_file_report,
                                            "=\\=> Exit: Parse log Error! Maybe miss some LogStart... Next");
                            this.reportFail("=\\=> Exit: Parse log Error! Maybe miss some LogStart... Next");
                            continue;
                        } //6.6

                        log_type_no =
                                doc.getElementsByTagName("integer").item(0).getTextContent();
                        log_type = getLogType(log_type_no);
                        log_msg =
                                doc.getElementsByTagName("string").item(1).getTextContent();

                        // ~ continue to parse next, until reach: 1. Pass/Fail 2. Error/Issue ~
                        // ~ Debug: nls check! ~
                        //if(log_type.equals("Warning")) // warning takes pictures, it is a waste of time and space, so change to "Debug"
                        if (log_type.equals("Debug")) //  format:  "amelia:trans_settings_about:xxxx"
                        { //6.0
                            temp = log_msg.split(":");

                            if (temp[0].startsWith(nls_tag))
                            //if(temp[0].startsWith("trans_"))
                            {
                                count_nls++;
                                nls_name = temp[0];
                                nls_string = temp[1];
                                nls_value =
                                        this.getACfgParam(this.getACfgParam(nls_name));

                                if (nls_string.equals(nls_value)) {
                                    count_correct++;
                                    this.add_to_log(w_file_report,
                                                    "++ NLS " + count_nls +
                                                    ": " + data_testLang +
                                                    ": Correct! -- " +
                                                    nls_name + ": " +
                                                    nls_string);
                                    this.pass("++ NLS " + count_nls + ": " +
                                              data_testLang +
                                              ": Correct! -- " + nls_name +
                                              ": " + nls_string);
                                } else {
                                    count_incorrect++;
                                    this.add_to_log(w_file_report,
                                                    "!!! NLS " + count_nls +
                                                    ": " + data_testLang +
                                                    ": Incorrect! -- " +
                                                    nls_name + ": " +
                                                    nls_string +
                                                    " -- expected: " +
                                                    nls_value);
                                    this.pass("!!! NLS " + count_nls + ": " +
                                              data_testLang +
                                              ": Incorrect! -- " + nls_name +
                                              ": " + nls_string +
                                              " -- expected: " + nls_value);
                                }
                            } else
                                System.out.println("**iOS Logging**: it is debug string, pass it this time");

                        } else //6.0
                        if (log_type.equals("Pass")) { // 6.1
                            this.add_to_log(w_file_report,
                                            "--> Function test Results: Pass: " +
                                            log_msg);
                            this.pass("--> Function test Results: Pass: " +
                                      log_msg);
                            //this.reportPass("--> Results: Pass: " + log_msg); // amelia: add into output
                            count_pass++;
                            break;
                        } else //6.1
                        if (log_type.equals("Fail")) { //6.2
                            this.add_to_log(w_file_report,
                                            "=/=> Function test Results: Fail: " +
                                            log_msg);
                            this.fail("=/=> Function test Results: Fail: " +
                                      log_msg);
                            count_fail++;
                            break;
                        } else //6.2
                        if (log_type.equals("Issue") ||
                            log_type.equals("Error")) { //6.3
                            this.add_to_log(w_file_report,
                                            "=\\=> Exit: Meet " + log_type +
                                            " : " + log_msg);
                            this.fail("=\\=> Exit: Meet " + log_type + " : " +
                                      log_msg);
                            break;
                        } else //6.3
                        if (log_type.equals("Start")) { //6.4
                            this.add_to_log(w_file_report,
                                            "-\\-> Warning: Start new testcase without an end(pass/fail/error) tag.. parse again..");
                            //this.outputFrameLog("-\\-> Warning: Start new testcase without an end(pass/fail/error) tag.. parse again..");
                            this.reportPass("-\\-> Warning: Start new testcase without an end(pass/fail/error) tag.. parse again..");
                            i--;
                            break;
                        } //6.4
                        else // Default(message)
                        if (log_type.equals("Message")) { //6.5
                            this.add_to_log(w_file_report,
                                            "-step " + ++count_step + ": " +
                                            log_type + " : " + log_msg);
                            //this.outputFrameLog("-step " + count_step +": " + log_type + " : " + log_msg); // amelia: add into output
                            this.pass("-step " + count_step + ": " + log_type +
                                      " : " + log_msg);
                        } //6.5
                        else // without an end tag, every case is started when meet logStart
                        {
                            System.out.println("**iOS Logging**: Let's check what she is: " +
                                               log_msg);
                            System.out.println("**iOS Logging**: it should be ...., errr, pass it this time\n");
                        }
                    } // 5.1
                    endTest();
                    ///////////////////////////////////////////////////////
                } // 4.1
            } // 3.1
            if (i == parameters.length) { //3.2
                if (count_nls == count_correct) {
                    this.add_to_log(w_file_report,
                                    "-->    NLS check Results: Pass: Totel: " +
                                    count_nls + " , Correct: " +
                                    count_correct + " , Incorrect: " +
                                    count_incorrect);
                    this.pass("-->    NLS check Results: Pass: Totel: " +
                              count_nls + " , Correct: " + count_correct +
                              " , Incorrect: " + count_incorrect);
                } else {
                    this.add_to_log(w_file_report,
                                    "=/=>    NLS check Results: Fail: Totel: " +
                                    count_nls + " , Correct: " +
                                    count_correct + " , Incorrect: " +
                                    count_incorrect);
                    this.fail("=/=>    NLS check Results: Fail: Totel: " +
                              count_nls + " , Correct: " + count_correct +
                              " , Incorrect: " + count_incorrect);
                }
                this.add_to_log(w_file_report,
                                "\r\n###########> This is the end <###########");
            } //3.2

            this.add_to_log(w_file_report,
                            " Function \nTotel: " + count_testcase +
                            " , Pass: " + count_pass + " , Fail: " +
                            count_fail);
            this.pass(" Function \nTotel: " + count_testcase + " , Pass: " +
                      count_pass + " , Fail: " +
                      count_fail); // amelia: add into output
            this.add_to_log(w_file_report,
                            " NLS check \nTotel: " + count_nls + " , Correct: " +
                            count_correct + " , Incorrect: " +
                            count_incorrect);
            this.pass(" NLS check \nTotel: " + count_nls + " , Correct: " +
                      count_correct + " , Incorrect: " +
                      count_incorrect); // amelia: add into output

            endTestCase();
            /////////////////////////////////////////////////////////////////
            w_file_report.close();

            return 1;
        } // 2.1
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean getPlistArrayDoc(NSObject param) {
        try {
            // ~ prepare dom parse params ~
            DocumentBuilderFactory domfac =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder dombuilder = domfac.newDocumentBuilder();
            // ~ prepare dom parse params ~
            String para = param.toXMLPropertyList();
            // ~ erase the url ~
            if (para.contains("<!DOCTYPE plist"))
                para =
para.replace("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">",
             "");
            // ~ parse ~
            // ~ Cautions!!! this is important, the "UTF-8" could get the correct NLS strings ~
            InputStream is = new ByteArrayInputStream(para.getBytes("UTF-8"));
            // ~ get doc ~
            param_logDoc = dombuilder.parse(is);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getEnvParam(String title) {
        String result = getEnvProperty("$" + title.toUpperCase());
        if (result.equals(""))
            result = getEnvProperty(title.toUpperCase());

        return result;
    }

    public String getEnvParam(String title, String def) {
        String result = getEnvProperty("$" + title.toUpperCase());
        if (result.equals(""))
            result = getEnvProperty(title.toUpperCase());
        if (result.equals(""))
            result = def;

        return result;
    }

    public void add_to_log(Writer log_file, String message) {
        try {
            System.out.println(message);

            message = message + "\r" + "\n";
            log_file.write(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void input_cmd(String[] args_input) {
        tag_runMain = true;

        if (args_input.length > 0)
            data_argsCmd = args_input;
    }

    public static void main(String[] args) {
        iphoneAuto gogo = new iphoneAuto();
        gogo.input_cmd(args);
        gogo.Init();
        //gogo.setUp();
        gogo.Run();
    }
}
