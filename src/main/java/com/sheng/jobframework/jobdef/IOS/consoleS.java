package com.sheng.jobframework.jobdef.IOS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class consoleS implements Runnable {
    private volatile boolean isStop = false;
    private static final int INFO = 1;
    private static final int ERROR = 0;
    private InputStream is;
    private int type;

    public consoleS(InputStream is, int type) {
        this.is = is;
        this.type = type;
    }

    public void run() {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        try {
            String s = reader.readLine();
            while ((!isStop) && s != null) {
                if (s.length() != 0) {
                    if (type == INFO) {
                        System.out.println(" INFO> " + s);
                    } else if (type == ERROR) {
                        System.err.println(" ERROR> " + s);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                s = reader.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        isStop = true;
    }
}
