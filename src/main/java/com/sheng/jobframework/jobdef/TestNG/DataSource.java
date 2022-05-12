package com.sheng.jobframework.jobdef.TestNG;

import java.util.Hashtable;

public abstract class DataSource {
    public Hashtable testDataHashArr = new Hashtable();

    public DataSource() {
    }

    public abstract boolean loadData(String filepath);

    public Hashtable getTestDataHash() {
        return testDataHashArr;
    }
}

