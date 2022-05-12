package com.sheng.jobframework.jobdef.TestNG;

import java.util.Hashtable;

public class DataClass{

    public DataClass() {
        super();
    }
    
    public String indicator="";
    private Hashtable paramHash = new Hashtable();
    
    public void setTestDataHash(Hashtable hs) {
        paramHash=hs;
    }
    public Hashtable getTestDataHash() {
        return paramHash;
    }
    
}
