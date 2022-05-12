package com.sheng.jobframework.jobdef.IOS;

import com.sheng.jobframework.annotation.TestResult;
import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdef.ACJobEngine;


public class IOSEngine extends ACJobEngine {
    public IOSEngine() {
    }

    public void runEntityJob() {
        iphoneAuto gogo = new iphoneAuto();
        addChildJob(gogo);
        try {
            //gogo.Init();
            //gogo.Init(getName());
            gogo.Init(this);
            //gogo.setUp();
            gogo.Run();
            gogo.End();
            //gogo.endTest();
        } catch (Exception e) {
            outputFrameLog("XXXX-Exception thrown when run IOS engine");
            JOM.restruct2StatusJob(this, "IOS_FAIL",
                                   "Exceptio during IOS running",
                                   TestResult.FAIL);
            e.printStackTrace();
        }
    }
}
