package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.jobdom.ACElement;

import java.util.ArrayList;


public class ACTestCase extends ACElement {
    String casename = "";
    String casepath = "";
    ArrayList testArr = new ArrayList(100);

    public ACTestCase() {
    }

    public ACTestCase(String name) {
        casename = name;
    }

    public void setCasepath(String path) {
        casepath = path;
    }

    public void setCaseName(String name) {
        casename = name;
    }

    public String getCasepath() {
        return casepath;
    }

    public String getCaseName() {
        return casename;
    }

    public void addTest(String testname) {
        testArr.add(testname);
    }

    public int getTestNum() {
        return testArr.size();
    }

    public String getTestNameByIndex(int i) {
        return (String)testArr.get(i);
    }
}
