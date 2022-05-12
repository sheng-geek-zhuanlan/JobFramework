package com.sheng.jobframework.jobdef;

import org.w3c.dom.Node;


public class ACPerfCollection extends ACJobAppender {
    public ACPerfCollection() {
        super();
    }


    public void addPerfData(String name, String value) {
        prop.put(name, value);

    }

    public void initialize() {
        String strOSName = System.getProperty("os.name");
        String strOSVersion = System.getProperty("os.version");


        String jreVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        /*
    * the folloowing code is not portable until we find a solution
    if(!(OSCmdUtil.getOSType()==OSCmdUtil.MAC)){
      long usedMem = Runtime.getRuntime().totalMemory();
      String mb_usedSize = Long.toString(usedMem/1000);
      OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
      long totalMemorySize = osmxb.getTotalPhysicalMemorySize();
      String mb_memorySize = Long.toString(totalMemorySize/1000);
      String usage = Long.toString(usedMem*100/totalMemorySize);
      double cpuratio = OSCmdUtil.getCpuRatio();
      String ratio = Double.toString(cpuratio);
      addProperty("TOTALMEMORY",mb_memorySize);
      addProperty("USEDMEMORY",mb_usedSize);
      addProperty("USAGEMEMORY",usage);
      addProperty("USAGECPU",ratio);
    }
    */
        addPerfData("OS", strOSName);
        addPerfData("OSVERSION", strOSVersion);
        addPerfData("JAVAVERSION", jreVersion);
        addPerfData("JAVAVENDOR", javaVendor);
    }

    public void parseNode(Node node) {
    }
}
