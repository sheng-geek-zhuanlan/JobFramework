package com.sheng.jobframework.jobdef.TestNG;

import java.lang.reflect.InvocationTargetException;

import java.lang.reflect.Method;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class MyTestListener extends TestListenerAdapter {
    public MyTestListener() {
        super();
    }
    
    @Override
    public void onTestSkipped(ITestResult result){
        super.onTestSkipped(result);
        try {
            // System.out.println("++++++++++++++++++"+result.getTestClass().getRealClass());
            // result.setAttribute("log", "it depends on "+result.getMethod().getMethodsDependedUpon()[0]+" failed");
             Method m= result.getTestClass().getRealClass().getMethod("outResult", ITestResult.class);
            // System.out.println("++++++++++++++++++"+m.getName());
             m.invoke(result.getTestClass().getInstances(true)[0], result);
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }
    
}
    
