package com.sheng.jobframework.jobdef.Ant;

import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobEngine;
import com.sheng.jobframework.observer.Log4jPrintStream;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;


public class AntEngine extends ACJobEngine {
    public AntEngine() {
    }

    public void runEntityJob() {
        TestJobElement antjob = new TestJobElement(TestJobType.TEST);
        // boolean damonMode = Utility.strToBoolean(getProperty(TestJobDOM.node_attribute_damon));
        antjob.setBeginTime(System.currentTimeMillis());
        antjob.setName(getName());
        addChildJob(antjob);
        Project project = new Project();

        DefaultLogger consoleLogger = new DefaultLogger();
        /*
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
      */

        consoleLogger.setErrorPrintStream(Log4jPrintStream.getInstance());
        consoleLogger.setOutputPrintStream(Log4jPrintStream.getInstance());

        //Styler.initLogStyle();
        //consoleLogger.setMessageOutputLevel(Project.MSG_VERBOSE);


        project.init();
        try{
          
     
        ACAntSet antset =
            (ACAntSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ANT);
        String antContent = antset.getAntTarget();
        String buildfile = composeBuildfile(antContent);
        File buildFile = new File(buildfile);
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        helper.parse(project, buildFile);
        project.addBuildListener(consoleLogger);


        // consoleLogger.
        // helper.parse(project, buildFile2);
        project.executeTarget(project.getDefaultTarget());
        //System.out.println("console log to string is "+consoleLogger.toString());
        project.fireBuildFinished(null);
        TestJobElement steps = new TestJobElement(TestJobType.STEP);
        steps.addProperty("done", "ant task has been executed completely!");
        antjob.addChildJob(steps);
        antjob.setResults(TestResult.PASS);
        antjob.setJobStatus(JobStatus.FINISH);
        antjob.setEndTime(System.currentTimeMillis());
        }catch(Exception e){
          outputFrameLog("xxx-exception is thrown is "+e.getMessage());
          TestJobElement steps = new TestJobElement(TestJobType.STEP);
          steps.addProperty("done", "ant task has been executed with error!");
          antjob.addChildJob(steps);
          antjob.setResults(TestResult.FAIL);
          antjob.setJobStatus(JobStatus.FINISH);
          antjob.setEndTime(System.currentTimeMillis());
        }
        //this need to be verified
       
        //delete the generated buid file
        //Delete delete=new Delete();
        //delete.setProject(project);
        //delete.setFile(buildFile);
        // delete.execute();
    }

    public String composeBuildfile(String content) {
        //System.out.println("in ant engine : the ant content is "+content);
        Random r = new Random(System.currentTimeMillis());
        int randomint = r.nextInt();

        String buildfile =
            System.getProperty("java.io.tmpdir") + File.separator +
            "AntTask_" + randomint + ".xml";
        try {
            OutputStreamWriter utput =
                new OutputStreamWriter(new FileOutputStream(buildfile),
                                       "UTF-8");
            //FileWriter fw = new FileWriter(buildfile);
            String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns:oracle=\"antlib:oracle\" basedir=\".\" default=\"" +
                getName() + "\" name=\"convert\">\n" +
                "<target name=\"" + getName() + "\">\n" +
                content + "</target>\n" +
                "</project>";
            utput.write(s, 0, s.length());
            utput.flush();
            //fw.write(s,0,s.length());
            //fw.flush();
        } catch (Exception e) {
            outputFrameLog("Exception occurs in AntEngine compose build file! " +
                           e.getMessage());
            e.printStackTrace();
        }
        return buildfile;
    }
}
