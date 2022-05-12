package com.sheng.jobframework.jobdef.TestNG;

import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;
import com.sheng.jobframework.jobdom.TestJobElement;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

// a common class as parent of all testNG cases, providing data, results outputting
public class TestNGBase extends TestJobElement {
    public TestNGBase() {
        super(TestJobType.CASE);
        System.out.println("hi*******************TestNGBase");
    }
    private String logmess="log:\n";
   // public TestJobElement testCase =new TestJobElement(TestJobType.CASE);
    
    public void setLog(String log){
        logmess+=log+"\n";
    }
    
    public String getLog(){
        return logmess;
    }
    
    /*
     * a common data provider
     */  
    @DataProvider
    // public Object[][] Value(Method m){
    public Iterator <Object[]> Value(Method m){ 
        //System.out.println(m.getName());
        System.out.println(m.getAnnotation(Test.class).description());
        
        ReadXML readxml = new ReadXML();
        String datasource=m.getName()+".xml";
        readxml.loadData(datasource);
        Hashtable datahash = readxml.testDataHashArr;
        Enumeration datakeys = datahash.keys();
        ArrayList <Object[]> datalist =new ArrayList <Object[]>();  
        while (datakeys.hasMoreElements()) {
            String indicator = (String)datakeys.nextElement();
            DataClass data =new DataClass();
            data.indicator=indicator;
           // data.paramHash= (Hashtable)datahash.get(indicator);
            data.setTestDataHash((Hashtable)datahash.get(indicator));
            datalist.add(new Object[]{data});
        }
        return datalist.iterator(); 
        
    }
    
        /*
    @DataProvider(name = "IntValue")
    public Object[][] IntValue(){
        return new Object[][]{
           { new Integer(0),"hello1"},
           { new Integer(1),"hello2"},
           { new Integer(10),"hello3"},
          // { Integer.MAX_VALUE},
        };
    }
    
    @DataProvider(name = "StrValue")
    public Object[][] StrValue(){
        return new Object[][]{
           {"hello"},
           {""}, 
           {"12345678901234567890"},       //????
        };
    }
    */

    @AfterMethod (alwaysRun=true)
    public void outResult(ITestResult result){
       // System.out.println(  result.getStatus() );
      // Reporter.setCurrentTestResult(result); 
      
        TestJobElement test=new TestJobElement(TestJobType.TEST);
        test.setName(result.getName());
       //// System.out.println("getMethodName :"+result.getMethod().getMethodName());
       //// System.out.println("getTestName :"+result.getTestName()); //null
      // System.out.println("getName :"+result.getName());
        
        //test.setResults(TestResult.PASS); 
        switch (result.getStatus()){ 
            case ITestResult.SKIP:
                test.setResults(TestResult.CNR);
                String [] dependmethods=result.getMethod().getMethodsDependedUpon();
                logmess+="it depends on: \n";
                for (String dm:dependmethods){
                    logmess+="          "+dm+"\n";
                }
                logmess+="          "+"at least one of them is failed"; 
                break;
            case ITestResult.SUCCESS: 
                test.setResults(TestResult.PASS);  break;
            case ITestResult.FAILURE:
                test.setResults(TestResult.FAIL);  
                logmess+=result.getThrowable().toString()+"\n"; ;
                StackTraceElement[] logs= result.getThrowable().getStackTrace();
                for (StackTraceElement ele:logs){
                    logmess+="          at "+ele.toString()+"\n";             
                }
                break;    
            default: 
       // case ITestResult.SUCCESS_PERCENTAGE_FAILURE:test.setResults(TestResult.WARN); break;
        }
        result.setAttribute("log", logmess);
        //System.out.println("testResult is :"+result.getStatus());
                                     
        TestJobElement step=new TestJobElement(TestJobType.STEP);
        step.setName("step");
        step.addProperty("log", logmess);
        test.addChildJob(step);
        //testCase.addChildJob(test);   
        
        this.addChildJob(test); 
        logmess="log:\n";  
        
    }
    
    @BeforeClass
    public void setup(){
        //testCase.setName(this.getClass().getName());
        this.setName(this.getClass().getName());
       // System.out.println("test class is :"+this.getClass().getName());
    }
    @AfterClass
    public void summaryResult(ITestContext ic){
       // System.out.println("summaryResult:"+ this.getClass().getName());
        //ic.setAttribute("testcase", testCase);
        
        //ic.setAttribute(this.getClass().getName(), testCase);
        ic.setAttribute(this.getClass().getName(), this);
              
    }
}
