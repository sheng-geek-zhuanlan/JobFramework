package com.sheng.jobframework.jobdef.TestNG;

import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.jobdom.TestJobElement;

import java.util.Iterator;
import java.util.List;

import org.testng.ITestContext;
import org.testng.TestListenerAdapter;

public class TestHelper {
    public TestHelper() {
        super();
    }
    
    public TestJobElement testComp =new TestJobElement(TestJobType.COMPONENT);
    public org.testng.TestNG mytest= new org.testng.TestNG();
   // public MyTestListener tla = new MyTestListener(); 
    
    
    public void setTestCompName(String str){
        testComp.setName(str);
    }
    public TestJobElement getTestJobElement(){   
        return testComp;
    }
    
    //invoke TestNG and print test result  
      public void runTest(Class[] testclass){         
         // org.testng.TestNG mytest= new org.testng.TestNG();
         //TestListenerAdapter tla = new TestListenerAdapter(); 
         MyTestListener tla = new MyTestListener(); 
         mytest.setTestClasses (testclass);   
         mytest.addListener(tla); 
         mytest.run();
         List<ITestContext> contexts = tla.getTestContexts();
         
         for(ITestContext ic:contexts){      
             Iterator arrs = ic.getAttributeNames().iterator();
             while(arrs.hasNext()){
                 String arr = (String)arrs.next();
            //   System.out.println("testCase is:"+arr);
             //TestJobElement testCase=(TestJobElement)ic.getAttribute("testcase");
                 TestJobElement testCase=(TestJobElement)ic.getAttribute(arr);
                 /*
                int n =testCase.getChildNodesNum();
                for(int i=0; i<n;i++){  
                    String name=testCase.getChildNodesByIndex(i).getName();
                    System.out.println("testMethod is:"+name);
                    TestJobElement test=(TestJobElement)testCase.getChildNodeByName(name);
                    System.out.println("result:"+test.getResults());
                }
             */
                 testComp.addChildJob(testCase);
             }
         }
        
        /*
        int passcount=tla.getPassedTests().size();
          int failcount=tla.getFailedTests().size();
          int skipcount=tla.getSkippedTests().size();
          int count=passcount+failcount+skipcount;
          DecimalFormat df =new DecimalFormat();
           if (count>0){
              df.setMaximumFractionDigits(2);
              df.setMinimumFractionDigits(2);
          
              System.out.println("Passed Method Count: "+passcount+", percent "+df.format( passcount*100.00/count)+"%");
              for (int i=0; i<passcount; i++) {
                  System.out.println(tla.getPassedTests().get(i)); 
                  System.out.println(tla.getPassedTests().get(i).getAttribute("log")); 
              }
              System.out.println("===============================================");
              System.out.println(" ");
              System.out.println("Failed Method Count: "+failcount+", percent "+df.format( failcount*100.00/count)+"%");
              for (int i=0; i<failcount; i++) {
              System.out.println(tla.getFailedTests().get(i));
              System.out.println(tla.getFailedTests().get(i).getAttribute("log")); 
              //tla.getFailedTests().get(i).getThrowable().printStackTrace();
             // tla.getFailedTests().get(i).getThrowable().printStackTrace(System.out);
              }
              System.out.println(" ");
              System.out.println("===============================================");
              System.out.println(" ");
              System.out.println("Skipped Method Count: "+skipcount+", percent "+df.format( skipcount*100.00/count)+"%");
              for (int i=0; i<skipcount; i++) {
                  System.out.println(tla.getSkippedTests().get(i));
                  System.out.println(tla.getSkippedTests().get(i).getAttribute("log")); 
              }
       }    */            
     }

}
