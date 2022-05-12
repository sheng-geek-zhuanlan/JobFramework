package com.sheng.jobframework.jobdom;

import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.annotation.TestJobType;

import com.sheng.jobframework.exception.ExitTestException;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;

import java.io.File;


public class ACUITestJob extends ACJavaJob {
    public ACUITestJob() {
        super();
    }
    //implement init,run, end here, because most selenium Job has junit style with setup,teardown. then ignore init,run,end

    public void Init() {
    }

    public void Run() {
    }

    public void End() {
    }

    public void fail(String msg) {
        reportFail(msg);
        snapScreen();
        throw new ExitTestException();
    }
    //steup and teardown is for oracle selenium jobs, change to protected for oracle, public should be for all customer

    public void setUp() throws Exception {
    }
    //shining modified for radvision soultion, from protected to public

    public void tearDown() throws Exception {

    }

    public void wait(int isecs) {
        long ms = isecs * 1000;
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            outputFrameLog("Exception when calling thread.sleep....");
            e.printStackTrace();
        }

    }

    public void pass(String msg) {
        reportPass(msg);
        snapScreen();
    }

    public void log(String msg) {
        outputFrameLog(msg);
        snapScreen();
    }

    public String getScreenPath() {
        TestJobElement currentRunningJob = this;
        if (ifHasChildNodes()) {
            TestJobElement testcase = (TestJobElement)getCurrentChildJob();
            if (testcase.getElementType().equalsIgnoreCase(TestJobType.TEST)) {
                currentRunningJob = testcase;
            } else if (testcase.ifHasChildNodes()) {
                TestJobElement test =
                    (TestJobElement)testcase.getCurrentChildJob();
                currentRunningJob = test;
            }
        }
        /*end of adding */
        String screenpath = currentRunningJob.generateScreenLocation();
        //String screenpath = currentRunningJob.getLocationPath();
        String fileName =
            screenpath + File.separator + getName() + "_" + Utility.getCurrentTimeInMillis();
        currentRunningJob.setScreenPath(screenpath);
        return fileName;
    }

    public void snapScreen() {
        //String fileName = getLocationPath()+"\\"+getName()+"_"+Utility.getCurrentTimeInMillis();
        /*adding for snap screen directly into the path*/
        String fileName = getScreenPath();
        addScreenShotToResult(fileName);
    }

    private String getScreenShot(String fileName) {
        if (!fileName.toLowerCase().endsWith(".png")) {
            fileName = fileName + ".png";
        }

        // Determine current screen size
        java.awt.Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Rectangle screenRect = new Rectangle(screenSize);

        try {
            File f = new File(fileName);
            FileUtil.createParentDir(f);
            // Create screen shot
            Robot robot = new Robot();
            java.awt.image.BufferedImage image =
                robot.createScreenCapture(screenRect);

            // save captured image to PNG file
            javax.imageio.ImageIO.write(image, "png", f);
            // give feedback
            /*System.out.println("Saved screen shot (" + image.getWidth() +
                          " x " + image.getHeight() + " pixels) to file \"" +
                          new File(fileName).getAbsolutePath() + "\".");*/
        } catch (Exception e) {
            outputFrameLog("Exception found in getScreenShot() : " + e);
            fileName = "";
            e.printStackTrace();
        }

        return fileName;
    }

    private void addScreenShotToResult(String strFileName) {
        String strOsName = (System.getProperty("os.name")).toLowerCase();
        char chrDirSeperator = '/';

        if (strOsName.indexOf("windows") > -1)
            chrDirSeperator = '\\';
        else if (strOsName.indexOf("unix") > -1 ||
                 strOsName.indexOf("linux") > -1)
            chrDirSeperator = '/';

        if (new File(getScreenShot(strFileName)).exists())
            //outputFrameLog("The screenshot :" + strFileName  + ".png" + strFileName +"");
            outputFrameLog("ScreenShot was taken");
        else
            outputFrameLog("The screenshot " + strFileName + " of the " +
                           "error could not be taken");
    }
}
