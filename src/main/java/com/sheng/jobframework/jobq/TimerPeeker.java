package com.sheng.jobframework.jobq;


import com.sheng.jobframework.Styler;
import com.sheng.jobframework.utility.OSCmdUtil;
import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdom.TestJobElement;

import com.sheng.jobframework.types.TimeClock;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;


public class TimerPeeker extends ACElement implements Runnable {
    private static TimerPeeker timer;
    private static String configFile = Styler.conffile;
    private static String jobFile = Styler.jobfile;
    private ArrayList regTimeArr = new ArrayList(10);
    //private static String jobFile="TestJobFile.xml";
    //private static String REGRESSION_CYCLE_MODE="REGRESSION_CYCLE_MODE";
    //private static String REGRESSION_TIME="REGRESSION_TIME";
    private String reg_cycle_mode = "";
    private String reg_test_time = "";
    //private Date trigger_time = new Date();
    private int trigger_hour = 0;
    private int trigger_secs = 0;

    public TimerPeeker() {
        super();

        //outputFrameLog("----Timer Peeker is starting");
        //System.out.println("----Timer Peeker is generating");

    }

    private void refreshTestTime() {
        //ACTestConfig configInfo = new ACTestConfig();
        //configInfo.loadConfigSetting(configFile);
        Styler.initConfStyle();
        reg_test_time =
                Styler.confProp.getProperty(Styler.REGRESSION_TIME, "");
        //System.out.println("get time is "+reg_test_time);
        if (reg_test_time.equalsIgnoreCase("")) {
            outputFrameLog("-----No timer specified-----");
            // trigger_time =  new Date();
            //trigger_time.setYear(1000);
        } else if (true) {
            String[] runTimeArr = reg_test_time.split(",");
            //clear the regtime arr
            regTimeArr.clear();
            int iTimePoints = runTimeArr.length;
            for (int i = 0; i < iTimePoints; i++) {
                String timepoint = runTimeArr[i];
                String[] time = timepoint.split(":");
                if (time.length > 1) {
                    String hour = time[0];
                    String mins = time[1];
                    //Date trigger_time =  new Date();
                    TimeClock starttime = new TimeClock();
                    starttime.setHour(Integer.parseInt(hour));
                    starttime.setMin(Integer.parseInt(mins));
                    regTimeArr.add(starttime);
                } else {
                    outputFrameLog("XXXXXX--error time format for regression testing: " +
                                   time);
                }
            }


        }
    }

    public static TimerPeeker getInstance() {
        //Styler.initTZStyle();
        if (timer == null) {
            timer = new TimerPeeker();
            Thread t = new Thread(timer);
            t.start();
        }
        return timer;
    }

    public void run() {

        /// System.out.println("in timepeeker, currenttime zone is "+System.getProperty("user.timezone"));
        refreshTestTime();
        if (!isRegressionHost()) {
            outputFrameLog("----Local host not specified in  " +
                           Styler.confProp.getProperty(Styler.REGRESSION_RUN_HOST,
                                                       "") +
                           " time peeker will not be started");
            return;
        }
        if (!reg_test_time.equalsIgnoreCase("")) {
            SimpleDateFormat formatter =
                new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            outputFrameLog("----Daily Regression triggered time is " +
                           reg_test_time);
            try {
                outputFrameLog("----[PEEKER] Timer Peeker is starting to run");
                while (true) {
                    Thread.sleep(10000);
                    refreshTestTime();
                    if (reg_test_time.equalsIgnoreCase("")) {
                        outputFrameLog("the regression time specified is null, will quit timer peeker!");
                    }
                    //TimeZone tz = new TimeZone(System.getProperty("user.timezone"))
                    //Date currenttime = c.getTime();
                    if (ifTimeMatched()) {
                        //SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                        // String strCurrentTime = formatter.format(currenttime);
                        System.out.println("Time triggered Job Start! Triggered time is " +
                                           reg_test_time);
                        outputFrameLog("***Timer starting....try to add default job to waiting Q");
                        TestJobElement job =
                            JOM.composeJobByLocalFile(jobFile);
                        //JOM.snapJOMScreen(job);
                        ProcessQ.addJobToLocalWatingQ(job);
                        //thread sleep 5 minutes to coninute query the time format, to avoid the job was loaded in 1 minutes
                        Thread.sleep(300000);
                    }
                }
            } catch (Exception e) {
                outputFrameLog("XXXX--Exception occurs in Timer Peeker!");
                e.printStackTrace();
            }
        } else {
            outputFrameLog("XXXX [PEEKER] Timer peeker will not run since no regression time specified by " +
                           Styler.REGRESSION_TIME);
        }
    }

    private boolean ifTimeMatched() {
        Date currenttime = new Date();
        int itimepoints = regTimeArr.size();
        for (int i = 0; i < itimepoints; i++) {
            
            
            
            TimeClock t2 = (TimeClock)regTimeArr.get(i);
            
            int hour1 = currenttime.getHours();
            int min1 = currenttime.getMinutes();
            int hour2 = t2.getHour();
            int min2 = t2.getMin();
           // System.out.println("daily run is "+hour2+":"+min2);
           // System.out.println("now is "+hour1+":"+min1);
            if ((hour1 == hour2) && (min1 == min2)) {
                return true;
            } else {
            }
        }
        return false;
    }

    private boolean isRegressionHost() {
        String dailyRunHosts =
            Styler.confProp.getProperty(Styler.REGRESSION_RUN_HOST, "");
        if (dailyRunHosts.equalsIgnoreCase("")) {
            //if not specify the regression host, then will run any host
            System.out.println("----No value specified in conflig.xml for " +
                               Styler.REGRESSION_RUN_HOST +
                               " will take localhost as a daily run host");
            return true;
        }
        String[] hostArr = dailyRunHosts.split(",");
        int isize = hostArr.length;
        for (int i = 0; i < isize; i++) {
            String host = hostArr[i];
            if (Utility.isIPAddress(host)) {
                String IP = OSCmdUtil.getHostIP();
                if (IP.equalsIgnoreCase(host))
                    return true;
            }
            String localhost = OSCmdUtil.getHostName();
            if (host.contains(localhost))
                return true;
        }
        return false;
    }

    public static void main(String[] args) {
        //System.getProperties().list(System.out);
        System.setProperty("user.timezone", "GMT+8:00");
        TimerPeeker.getInstance();

    }
}
