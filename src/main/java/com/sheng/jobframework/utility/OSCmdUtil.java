package com.sheng.jobframework.utility;

import com.sheng.jobframework.jobdom.ACElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import java.net.InetAddress;

import java.util.StringTokenizer;


public class OSCmdUtil extends ACElement {
    public final static int WINDOWS = 1;
    public final static int LINUX = 2;
    public final static int UNIX = 3;
    public final static int MAC = 4;
    public final static int OTHERS = 5;
    private static final int FAULTLENGTH = 10;
    private static final int CPUTIME = 30;
    private static final int PERCENT = 100;


    public OSCmdUtil() {
    }

    public static String getOSParameter(String parameter) {
        return "";
    }

    public static int getOSType() {
        String strOSVersion = System.getProperty("os.name").toLowerCase();
        int ret = OTHERS;
        if (strOSVersion.contains("windows")) {
            ret = WINDOWS;
        } else if (strOSVersion.contains("linux")) {
            ret = LINUX;
        } else if (strOSVersion.contains("unix")) {
            ret = UNIX;
        } else if (strOSVersion.contains("mac")) {
            ret = MAC;
        } else {
            outputLog("XXXX-Not support OS Type: " + strOSVersion);
            ret = OTHERS;
        }
        return ret;
    }

    public static double getCpuRatio() {
        double ratio = 0;
        switch (getOSType()) {
        case WINDOWS:
            {
                ratio = getCpuRatioForWinodws();
            }
            break;
        case LINUX:
            {
                ratio = getCpuRateForLinux();
            }
            break;
        case MAC:
            {
                outputLog("XXXX-MAC not supported yet for getCpuRatio!");
            }
            break;
        default:
            {
                outputLog("XXXX-Unkown OSType():" + getOSType() +
                          " will use temp dir as debug dir! ");

            }
        }
        return 0;
    }

    private static double getCpuRatioForWinodws() {
        try {
            String procCmd =
                System.getenv("windir") + "\\system32\\wbem\\wmic.exe process get Caption,CommandLine," +
                "KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
            // ȡ������Ϣ
            long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
            Thread.sleep(CPUTIME);
            long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
            if (c0 != null && c1 != null) {
                long idletime = c1[0] - c0[0];
                long busytime = c1[1] - c0[1];
                return Double.valueOf(PERCENT * (busytime) /
                                      (busytime + idletime)).doubleValue();
            } else {
                return 0.0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0;
        }
    }

    private static long[] readCpu(final Process proc) {
        long[] retn = new long[2];
        try {
            proc.getOutputStream().close();
            InputStreamReader ir =
                new InputStreamReader(proc.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line = input.readLine();
            if (line == null || line.length() < FAULTLENGTH) {
                return null;
            }
            int capidx = line.indexOf("Caption");
            int cmdidx = line.indexOf("CommandLine");
            int rocidx = line.indexOf("ReadOperationCount");
            int umtidx = line.indexOf("UserModeTime");
            int kmtidx = line.indexOf("KernelModeTime");
            int wocidx = line.indexOf("WriteOperationCount");
            long idletime = 0;
            long kneltime = 0;
            long usertime = 0;
            while ((line = input.readLine()) != null) {
                if (line.length() < wocidx) {
                    continue;
                }
                // �ֶγ���˳��Caption,CommandLine,KernelModeTime,ReadOperationCount,
                // ThreadCount,UserModeTime,WriteOperation

                String caption =
                    BytesUtil.substring(line, capidx, cmdidx - 1).trim();
                String cmd =
                    BytesUtil.substring(line, cmdidx, kmtidx - 1).trim();
                if (cmd.indexOf("wmic.exe") >= 0) {
                    continue;
                }
                // log.info("line="+line);
                if (caption.equals("System Idle Process") ||
                    caption.equals("System")) {
                    idletime +=
                            Long.valueOf(BytesUtil.substring(line, kmtidx, rocidx -
                                                             1).trim()).longValue();
                    idletime +=
                            Long.valueOf(BytesUtil.substring(line, umtidx, wocidx -
                                                             1).trim()).longValue();
                    continue;
                }

                kneltime +=
                        Long.valueOf(BytesUtil.substring(line, kmtidx, rocidx -
                                                         1).trim()).longValue();
                usertime +=
                        Long.valueOf(BytesUtil.substring(line, umtidx, wocidx -
                                                         1).trim()).longValue();
            }
            retn[0] = idletime;
            retn[1] = kneltime + usertime;
            return retn;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                proc.getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static double getCpuRateForLinux() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader brStat = null;
        StringTokenizer tokenStat = null;
        String linuxVersion = System.getProperty("os.version");
        try {
            System.out.println("Get usage rate of CUP , linux version: " +
                               linuxVersion);

            Process process = Runtime.getRuntime().exec("top -b -n 1");
            is = process.getInputStream();
            isr = new InputStreamReader(is);
            brStat = new BufferedReader(isr);

            if (linuxVersion.contains("2.4")) {
                brStat.readLine();
                brStat.readLine();
                brStat.readLine();
                brStat.readLine();

                tokenStat = new StringTokenizer(brStat.readLine());
                tokenStat.nextToken();
                tokenStat.nextToken();
                String user = tokenStat.nextToken();
                tokenStat.nextToken();
                String system = tokenStat.nextToken();
                tokenStat.nextToken();
                String nice = tokenStat.nextToken();

                System.out.println(user + " , " + system + " , " + nice);

                user = user.substring(0, user.indexOf("%"));
                system = system.substring(0, system.indexOf("%"));
                nice = nice.substring(0, nice.indexOf("%"));

                float userUsage = new Float(user).floatValue();
                float systemUsage = new Float(system).floatValue();
                float niceUsage = new Float(nice).floatValue();

                return (userUsage + systemUsage + niceUsage) / 100;
            } else {
                brStat.readLine();
                brStat.readLine();

                tokenStat = new StringTokenizer(brStat.readLine());
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                String cpuUsage = tokenStat.nextToken();


                System.out.println("CPU idle : " + cpuUsage);
                Float usage =
                    new Float(cpuUsage.substring(0, cpuUsage.indexOf("%")));

                return (1 - usage.floatValue() / 100);
            }


        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            freeResource(is, isr, brStat);
            return 1;
        } finally {
            freeResource(is, isr, brStat);
        }

    }

    private static void freeResource(InputStream is, InputStreamReader isr,
                                     BufferedReader br) {
        try {
            if (is != null)
                is.close();
            if (isr != null)
                isr.close();
            if (br != null)
                br.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public static String getTestAntExecutable() {
        String strOSVersion = System.getProperty("os.name").toLowerCase();
        String ret = "";
        if (strOSVersion.contains("windows")) {
            ret = "TestAnt.bat";
        } else if (strOSVersion.contains("linux")) {
            ret = "TestAnt.sh";
        } else if (strOSVersion.contains("unix")) {
            ret = "TestAnt.sh";
        } else if (strOSVersion.contains("mac")) {
            ret = "TestAnt.sh";
        } else {
            outputLog("XXXX-Not support OS Type: " + strOSVersion);
            ret = "";
        }
        return ret;
    }

    public static String getDebugTraceDir() {
        String debugDir = "";
        switch (getOSType()) {
        case WINDOWS:
            {
                debugDir = "C:\\ACDebug";
            }
            break;
        case LINUX:
            {
                debugDir = System.getProperty("java.io.tmpdir").toLowerCase();
            }
            break;
        case MAC:
            {
                debugDir = System.getProperty("java.io.tmpdir").toLowerCase();
            }
            break;
        default:
            {
                outputLog("XXXX-Unkown OSType():" + getOSType() +
                          " will use temp dir as debug dir! ");
                debugDir = System.getProperty("java.io.tmpdir").toLowerCase();
            }
        }
        return debugDir;
    }

    public static void killProcess(String processname) {
        //String[] cmdArray = {"tskill.exe","notepad"};
        //processname.lastIndexOf("exe")
         String KILL = "taskkill /IM ";
        String strOSVersion = System.getProperty("os.name");
        if (strOSVersion.contains("Windows")) {
            //convert the proccess name "qtpro.exe" to "qtp"
            if (processname.length() - (processname.lastIndexOf("exe")) == 3) {
                //processname = processname.substring(0, processname.lastIndexOf("exe") -1);
            }else{
              processname = processname+".exe";
            }
           // String[] cmdArray = { "tskill.exe", processname };
           //String[] cmdArray = { "taskkill /IM", processname };
           
            int result = 0;
            try {
                //Process process = Runtime.getRuntime().exec(cmdArray);
                //System.out.println("is to kill the process "+processname.toUpperCase());
                Process process = Runtime.getRuntime().exec(KILL+processname.toUpperCase());
                process.waitFor();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                outputLog("Exception thrown when kill process: " +
                          processname);
                e.printStackTrace();
            }
        } else {
            outputLog("WARNING: TODO:Current OS is not Windows, need to implemented killProcess in OSCmdUtil.java");
        }

    }

    public static String pathReSettle2OS(String dirPath) {
        String ret = dirPath;
        if (!FileUtil.isLocalFile(dirPath))
            return dirPath;
        int iType = getOSType();
        if (iType == WINDOWS) {
            ret = dirPath.replace("/", File.separator);
            //ret = dirPath.replace("\\",File.separator);
        } else if (iType == LINUX || iType == UNIX) {
            ret = dirPath.replace("\\", File.separator);
        } else if (iType == MAC) {
            ret = dirPath.replace("\\", File.separator);
            //outputLog("XXXX-Warning: MAC not supported, will not do convert  ");
        } else {
            outputLog("XXXX-Warning: Not known OS, will not do convert");
        }
        return ret;
    }

    public static void cleanQTPEnv() {
        killProcess("qtpro");
        killProcess("QTAutomationAgent");
        killProcess("wscript");
        killProcess("QTAUTO~1");
        killProcess("ODrive.exe");
    }

    public static String getHostName() {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            String localhost = ia.getHostName();
            return localhost;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
  public static boolean isProcessRunging(String serviceName){
    String TASKLIST = "tasklist";
    try{
      
    
   Process p = Runtime.getRuntime().exec(TASKLIST);
   BufferedReader reader = new BufferedReader(new InputStreamReader(
     p.getInputStream()));
   String line;
   while ((line = reader.readLine()) != null) {
   
   // System.out.println(line);
    if (line.contains(serviceName)) {
     return true;
    }
   }
    }catch(Exception e){
      e.printStackTrace();
    }
   return false;
   
  }
    public static String getHostIP() {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            String IP = ia.getHostAddress();
            return IP;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        /*
        System.out.println("the file sepeate char is " + File.separator);
        String proccessname = "NOTEPAD";
        System.out.println("the process name is "+OSCmdUtil.isProcessRunging(proccessname));
        OSCmdUtil.killProcess(proccessname);*/
        System.getProperties().list(System.out);
        //System.out.println(OSCmdUtil.getCpuRatioForWinodws());
    }
}


