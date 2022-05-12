package com.sheng.jobframework;

import com.sheng.jobframework.utility.DataSubscriber;
import com.sheng.jobframework.utility.FileUtil;

import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdom.TestJobElement;

import com.sheng.jobframework.jobq.ProcessQ;
import com.sheng.jobframework.jobq.RunnedJobQueue;
import com.sheng.jobframework.jobq.TimerPeeker;
import com.sheng.jobframework.jobq.ToRunJobQueue;

import com.sheng.jobframework.observer.ObserverSubscriber;

import java.io.PrintStream;


/*
 * the agent exit problem was finally fixed by
 * 1. QPeeker add thread sleep 2000
 * 2. Agent thread did not close client after receive command
 * 3. Agent did not have while cycle to read
 * 4. add commons-httpclient-3.0.1.jar 2010-11-2 days
 * 5. Classloader maybe not refresh the class binary in the memory, if second loaded same class name with first time.
 * 6. all the file,style,timezone defination is in styler.java
 * 7. There is risk when send job with iterations to remote host, since when the job is run with interations, the job will be cloned and have the same parent, While you know,
 * only the component job is sent, so the parent is null, this will cause null pointer exception.will be fixed in future.
 * 8. add wait time between jobs, this is to make sure the remote job has been inited and tried connect to remote host, since the remote job is a thread.
 * 9. Notes: don't change the max_rerunNum in TestJobElement, it will affect the remote job, since the remote job will be only runned once, after RUNNED, the job will lost host info, since it has been trimed before sent to remote host.
 * 10. env and config was loaded when loading JOM, --changed, the envfile and confile will be loaded when job inited
 * 11. remain issue: logger to catch the Ant execute log into ac log
 * 12. the filepath of propFile and configFile was not overiddedable by the global data channel,since their prop is directlly the env/confi parameters.
 * 13. job will always call jobPrepare(), inspite of it is local/remote, abstract/enity job. so parseAllAppenderToRealValue() will be always called.
 * 14. clarify the env property change process
 *    1) load JOM from jobfile, but envfile will not be loaded(envprperty will be loaded!), at same time, such as QTP job will call addChildJob during loading, so the parent env will overwrite the child(but parent env is blank now)
 *    2) Parent job will addTestEnv(), which parsed from extern agent
 *    now it is time to start run
 *    3) scheduler.recuresetAppendeValues() will make all the childjob be overwrittend, in this time, QTP also get refreshed env values
 *    4) jobPrepare() will call parseallAppender, to make sure all the childjob was refreshed
 *    5) in jobInit(), initializeAllAppenders will overwrite the current job env with env file again
 *    3) for selenium/Java/Webservice job addChildJob() will make the child job get envproperty from parent in run time.
 * 15. The env property parsed from command or web UI will be the higher priority to overide any env property in the env file.
 *    1) because extractEnvProperty2GDC will be done when suite job started.
 *    2) except "COMP" property, because we want every job keep his own COMP to decide which child job is to run
 * 16. the <JobOutput name="CONF_ID" value="49214"/> will not work, because they are written into duing load JOM, after the job has been added to Job Q, the q will firstly clear the GDC
 * 17. deepclone not totally from root. the appenderctrl refer to same instance.
 */
public class JobStarter {
    public static String PARA_RUNMODE = "-mode";
    public static String PARA_JOBFILE = "-JobFile";
    public static String PARA_DEBUGMODE = "-debug";
    public static String PARA_HELPMODE = "-help";
    public static String VALUE_RUNNMODE_AGENT = "Listener";
    public static String VALUE_RUNNMODE_FRAME = "Local";
    private PrintStream oldps;
    private boolean agentMode = false;
    private boolean debugMode = false;
    public static String jobfile = Styler.jobfile;
    private static JobStarter testant = null;
    public DataSubscriber serviceDataSubscriber = DataSubscriber.getInstance();
    public ObserverSubscriber observerSubscriber =
        ObserverSubscriber.getInstance();

    public JobStarter() {
        super();
        //System.out.println("-----try to get config.xml from path: "+FileUtil.getAbsolutePath(Styler.conffile));
        System.out.println("The current OS is " +
                           System.getProperty("os.name"));

        //System.out.println("The temp dir is "+System.getProperty("java.io.tmpdir"));
        boolean b =
            FileUtil.isFileExists(FileUtil.getAbsolutePath(Styler.conffile));
        if (!b) {
            System.out.println("XXXXX-please make sure config.xml exists in TestAnt working dir!");
            System.exit(11);
        }
        oldps = System.out;
        Styler.initialize();
    }
    public static JobStarter getTestAntInstance(){
        if(testant == null){
          testant = new JobStarter();
          String[] args = {"-mode","Listener"};
          testant.startTestAnt(args);
        }
        return testant;
    }


    public void startTestAnt(String[] args) {
        try {
            processArgs(args);
            ToRunJobQueue.getInstance();
            RunnedJobQueue.getInstance();
            //timer peeker will start to run do daily regression testing
            TimerPeeker.getInstance();
            if (!agentMode) {
                outputFrameLog("****Test Job Starting with local mode....try to add default job to waiting Q");
                TestJobElement job = JOM.composeJobByLocalFile(jobfile);
                ProcessQ.addJobToLocalWatingQ(job);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void outputFrameLog(String msg) {
        observerSubscriber.outputFrameLog(msg);
    }

    private void printUsage() {
        String lSep = System.getProperty("line.separator");
        StringBuffer msg = new StringBuffer();
        msg.append("TestAnt [options]" + lSep);
        msg.append("Options: " + lSep);
        msg.append("  -JobFile <TestJobFile.xml>       determin the job file to run" +
                   lSep);
        msg.append("  -mode, [Listener] [local]        run listener mode or local mode " +
                   lSep);
        msg.append("  -debug           run testAnt with debug Mode" + lSep);
        msg.append("  -h           list the command help" + lSep);
        String s = new String(msg);
        outputFrameLog(s);
    }

    private void processArgs(String[] args) {
        int len = args.length;
        for (int i = 0; i < len; i++) {
            if (args[i].equalsIgnoreCase(PARA_JOBFILE)) {
                if (i + 1 < len) {
                    jobfile = args[i + 1];
                } else {
                    System.out.println("Error: JobFile value is missing, will exit!");
                    printUsage();
                    System.exit(11);
                }
            } else if (args[i].equalsIgnoreCase(PARA_RUNMODE)) {
                if (i + 1 < len) {
                    String runmode = args[i + 1];
                    if (runmode.equalsIgnoreCase(VALUE_RUNNMODE_AGENT)) {
                        agentMode = true;
                    } else if (runmode.equalsIgnoreCase(VALUE_RUNNMODE_FRAME)) {
                        agentMode = false;
                    } else {
                        outputFrameLog("illegal value of mode " + runmode);
                        printUsage();
                        System.exit(11);
                    }
                } else {
                    System.out.println("Error: mode value is missing, will exit!");
                    printUsage();
                    System.exit(11);
                }
            } else if (args[i].equalsIgnoreCase(PARA_DEBUGMODE)) {
                debugMode = true;
            } else if (args[i].equalsIgnoreCase(PARA_HELPMODE) ||
                       args[i].equalsIgnoreCase("-h")) {
                printUsage();
                System.exit(11);
            }

        }
    }

    public void endTestAnt() {
        System.setOut(oldps);
        //frameServiceStop();
    }

    public static void main(String[] args) {
        try {
            /*
            TestAnt testant = new TestAnt();
            testant.startTestAnt(args);
            */
            JobStarter.getTestAntInstance();
            //testant.endTestAnt();
        } catch (Exception e) {
            //observerSubscriber.outputFrameLog("Exception thrown when run ACFrame run job "+e.getMessage());
            e.printStackTrace();
        }
    }
}
