package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.annotation.TestJobDOM;

import com.sheng.jobframework.jobdom.ACElement;

import com.sheng.jobframework.jobdef.Ant.AntJOMParser;
import com.sheng.jobframework.jobdef.Deamon.DeamonJOMParser;
import com.sheng.jobframework.jobdef.IOS.IOSJOMParser;
import com.sheng.jobframework.jobdef.JDBC.DBJOMParser;
import com.sheng.jobframework.jobdef.Java.JavaJOMParser;
import com.sheng.jobframework.jobdef.Jmeter.JmeterJOMParser;
import com.sheng.jobframework.jobdef.Junit.JunitJOMParser;
import com.sheng.jobframework.jobdef.QTP.QTPJOMParser;
import com.sheng.jobframework.jobdef.Script.ScriptJOMParser;
import com.sheng.jobframework.jobdef.Selenium.SelJOMParser;
import com.sheng.jobframework.jobdef.TestJob.ComJOMParser;
import com.sheng.jobframework.jobdef.TestNG.TestNGJOMParser;
import com.sheng.jobframework.jobdef.WebService.WSJOMParser;


public class ParserFactory extends ACElement {
    public ParserFactory() {
    }

    public static JOMParser createParser(String engineType) {

        JOMParser parser;
        //System.out.println("in factory: the type is "+engineType);
        if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_ac)) {
            parser = new DeamonJOMParser();
        } else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_java)) {
            parser = new JavaJOMParser();
        } else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_junit)) {
            parser = new JunitJOMParser();
        } else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_qtp)) {
            parser = new QTPJOMParser();
        } else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_ant)) {
            parser = new AntJOMParser();
        } else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_selenium)) {
            parser = new SelJOMParser();
        } else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_jemmy)) {
            parser = new SelJOMParser();
        } else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_jdbc)) {
            parser = new DBJOMParser();
        } else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_ws)) {
            parser = new WSJOMParser();
        } else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_script)) {
            parser = new ScriptJOMParser();
        } else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_jmeter)) {
            parser = new JmeterJOMParser();
        } else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_ios)) {
            parser = new IOSJOMParser();
        }else if (engineType.equalsIgnoreCase(TestJobDOM.job_engine_testng)) {
            parser = new TestNGJOMParser();
        }else {
            parser = new ComJOMParser();
            //outputLog(LOGMSG.ENGINE_TYPE_NOTRECOGINZED+engineType);
        }
        return parser;

    }

    public static void main(String[] args) {
        ParserFactory parserFactory = new ParserFactory();
    }
}
