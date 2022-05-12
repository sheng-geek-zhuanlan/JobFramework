package com.sheng.jobframework.jobdom;

import com.sheng.jobframework.utility.DataSubscriber;

import com.sheng.jobframework.observer.ObserverSubscriber;

import java.io.Serializable;

import java.util.ArrayList;


public class ACElement implements Serializable {
    public static DataSubscriber subscriber = DataSubscriber.getInstance();
    public static ObserverSubscriber observerSubscriber =
        ObserverSubscriber.getInstance();
    public ArrayList elementNode = new ArrayList(800);

    public ACElement() {
    }
    //this API will be called from styler, so the subsriber and observer will be the static instance of global

    public static void setObserver(ObserverSubscriber observerList) {
        observerSubscriber = observerList;
    }

    public void outputFrameLog(String strLog) {
        observerSubscriber.outputFrameLog(strLog);
    }

    public static void outputLog(String strLog) {
        observerSubscriber.outputFrameLog(strLog);
    }

    public static void outputJobLog(String strLog) {
        observerSubscriber.outputJobLog(strLog);
    }

    public static void setCommChannel(DataSubscriber serviceDataSubscriber) {
        subscriber = serviceDataSubscriber;
    }

    public void writeIntoComChannel(String key, String value) {
        subscriber.addData(key, value);
    }

    public String getDataFromACChannel(String key, String defaultValue) {
        return subscriber.getData(key, defaultValue);
    }

}
