package com.sheng.jobframework.utility;

import java.util.Properties;

public class DataSubscriber {
    Properties dataProp = new Properties();
    public static DataSubscriber datasubscriber;

    public DataSubscriber() {
    }

    public static DataSubscriber getInstance() {
        if (datasubscriber == null)
            datasubscriber = new DataSubscriber();
        return datasubscriber;
    }

    public void addSubscribe(String strToBeSubsribed) {

    }

    public void addData(String key, String value) {
        String oldvalue = (String)dataProp.setProperty(key, value);
    }

    public String getData(String key, String defaultValue) {
        return dataProp.getProperty(key, defaultValue);
    }

    public Properties getServiceSubscribedDataProp() {
        return dataProp;
    }

    public void cleanData() {
        dataProp.clear();
    }
}
