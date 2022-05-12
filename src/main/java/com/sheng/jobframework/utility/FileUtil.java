package com.sheng.jobframework.utility;

import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.annotation.LOGMSG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;


public class FileUtil extends ACElement {
    public static boolean isRemoteFileSys = false;
    public static String parentURL = "";
    public static String WORKING_BASE = "";

    public FileUtil() {
    }

    public static void setWorkBase(String baseDir) {
        WORKING_BASE = baseDir;
    }

    public static void setRemoteSys(String jobfile) {
        boolean b = FileUtil.isLocalFile(jobfile);
        if (b) {
            isRemoteFileSys = false;
            parentURL = "";
        } else {
            isRemoteFileSys = true;
            getParentURL(jobfile);
        }
    }

    public static String fullDumppedFile2Str(String dumpFilePath) {
        try {
            File f = new File(dumpFilePath);
            if (f.exists()) {
                FileInputStream inputf = new FileInputStream(dumpFilePath);
                byte[] buf = new byte[inputf.available()];
                inputf.read(buf, 0, inputf.available());
                String str = new String(buf);
                return str;
            } else {
                outputLog("XXXX-the job status file is not generated at path: " +
                          dumpFilePath);
                return null;
            }

        } catch (Exception e) {
            outputLog("XXX-Exception in JobQueue dumpped job status from file " +
                      dumpFilePath);
            e.printStackTrace();
        }
        return null;
    }

    public static boolean clearFile(String filepath) {
        File f = new File(getAbsolutePath(filepath));
        try {
            if (f.exists()) {
                if (f.isDirectory()) {
                    outputLog("is to Clear the files under : " + filepath);
                    File[] jarfiles = f.listFiles();
                    int itotal = jarfiles.length;
                    if (itotal == 0)
                        System.out.println("XXXXX-no files under the class dir " +
                                           f.getAbsolutePath());
                    for (int l = 0; l < itotal; l++) {
                        File jarfile = jarfiles[l];
                        jarfile.delete();
                    }
                } else {
                    f.delete();
                }
            } else {
                outputLog("not found the file at path " + filepath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean isLocalFile(String jobfilepath) {
        if (jobfilepath.startsWith("http") || jobfilepath.startsWith("HTTP")) {
            return false;
        } else {
            return true;
        }
    }

    public static String getLatestFileUnderDir(File filesPath, String token) {
        String ret = null;
        File fileRet = null;
        try {
            if (filesPath.exists()) {
                if (filesPath.isDirectory()) {
                    File sourceFile[] = filesPath.listFiles();
                    for (int i = 0; i < sourceFile.length; i++) {
                        if (sourceFile[i].exists()) {
                            if (sourceFile[i].isDirectory()) {
                                outputLog("***ignore the dir artifacts " +
                                          sourceFile[i].getName());
                            } else {
                                if (sourceFile[i].getName().contains(token)) {
                                    if (fileRet == null) {
                                        fileRet = sourceFile[i];
                                    } else if (sourceFile[i].lastModified() >
                                               fileRet.lastModified()) {
                                        fileRet = sourceFile[i];
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                outputLog("XXXX-the dir not exist " +
                          filesPath.getAbsolutePath());
                return null;
            }
        } catch (Exception e) {
            outputLog("XXXX- in calling FileUtil.getLatestFileUnderDir()");
            return null;
        }
        if (fileRet != null) {
            return fileRet.getAbsolutePath();
        } else
            return null;
    }

    public static boolean isPngFile(File f) {
        try {
            String filename = f.getName();
            int ipos = filename.lastIndexOf(".");
            String extname = filename.substring(ipos + 1);
            if (extname.equalsIgnoreCase("PNG")) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            outputLog(LOGMSG.FILE_UTIL_EXCEPTION + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static String getFileExtension(String filename) {
        String fileext = "";
        int ipos = filename.lastIndexOf(".");
        if (ipos == -1) {
        } else {
            fileext = filename.substring(ipos + 1);
        }
        return fileext;
    }

    public static String getFileNameNoExt(String filename) {
        String name = "";
        int ipos = filename.lastIndexOf(".");
        if (ipos == -1) {
        } else {
            name = filename.substring(0, ipos);
        }
        return name;
    }

    public static int deepthOfFolder(File f) {
        int i = 0;
        while (f.getParent() != null) {
            i++;
            String path = f.getParent();
            //System.out.println("iteration "+i+path);
            f = new File(path);
        }
        return i;
    }

    public static String getQTPWorkingCurr(String runtimePathStr) {
        //runtimePathStr = "D:\\learn\\AutomationCenter\\Project\\driver\\results\\110321232758\\OBEO_Install_QTP\\OBEO_Install_QTP_runtime\\dsadsadsa\\dasdasd";
        String workingPath = runtimePathStr;
        File f = new File(runtimePathStr);
        int qtpPath = deepthOfFolder(f);
        //System.out.println(qtpPath);
        // String base = Utility.getWorkingDir();
        String base = FileUtil.getCurrentDir();
        File f2 = new File(base);
        int workipath = deepthOfFolder(f2);
        //System.out.println(workipath);
        int pathIntermediate = qtpPath - workipath;
        if (pathIntermediate == 4) {
            //System.out.println("that is right");
        } else if (pathIntermediate < 4) {
            System.out.println("XXXX-wrong deepth in the QTP path");
        } else if (pathIntermediate > 4) {
            System.out.println("****QTP path needs to do reversed " +
                               (pathIntermediate - 4));
            int reverseDeepth = qtpPath - workipath - 4;
            File newFile = new File(runtimePathStr);
            workingPath = reverseFolder(newFile, reverseDeepth);
            //System.out.println("after reversed: "+path);
        }
        return workingPath;
    }

    public static String reverseFolder(File f, int deepth) {
        String mypath = "";
        for (int i = 0; i < deepth; i++) {
            //i++;
            String path = f.getParent();
            //System.out.println("iteration "+i+path);
            f = new File(path);
            mypath = path;
        }
        return mypath;
    }

    public static String getFileName(String filepath) {
        File f = new File(getAbsolutePath(filepath));
        return f.getName();
    }

    public static void getParentURL(String filepath) {
        String currentLocation = filepath;
        String parentLocation = "";
        int i = currentLocation.lastIndexOf("/");
        if (i == -1) {
            System.out.println("XXX-error to get parent URI from the path from " +
                               currentLocation);
            return;
        } else {
            parentLocation = currentLocation.substring(0, i);
            parentURL = parentLocation;
        }
    }

    public static String getParentLocation(TestJobElement job) {
        String currentLocation = job.getLocationPath();
        String parentLocation = "";
        int i = currentLocation.lastIndexOf(job.getName());
        if (i == -1) {
            System.out.println("XXX-Could not found " + job.getName() +
                               " in the locationPath " + currentLocation);
            return null;
        } else {
            parentLocation = currentLocation.substring(0, i - 1);
            System.out.println("...return parent Location " + parentLocation);
        }
        return parentLocation;
    }

    public static boolean copyFile(String oldPath, String newPath,
                                   boolean ifdelOld) {
        boolean copySuccess = false;
        try {
            createParentDir(newPath);
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //??????
                InputStream inStream =
                    new FileInputStream(oldPath); //??????
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //???? ????
                    // System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                copySuccess = true;
            } else {
                copySuccess = false;
            }
            if (ifdelOld) {
                File f = new File(oldPath);
                f.delete();
            }

        } catch (Exception e) {
            outputLog(LOGMSG.FILE_COPY_EXCEPTION + e.getMessage());
            e.printStackTrace();
        }
        return copySuccess;
    }

    public static void createParentDir(File f) {
        if (!f.exists()) {
            String dirpath = f.getParent();
            // System.out.println("dirpath is "+dirpath);
            File dir = new File(dirpath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // f.createNewFile();
        }
    }

    public static void createParentDir(String pathname) {
        File f = new File(pathname);
        if (!f.exists()) {
            String dirpath = f.getParent();
            // System.out.println("dirpath is "+dirpath);
            File dir = new File(dirpath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // f.createNewFile();
        }
    }

    public static String getAbsolutePath(String path) {
        //this have potential risk when running on linux
        if (isRemoteFileSys) {
            //if this is a http://dsdsds/file.xml, no need to handle
            if (!path.startsWith("http")) {
                //System.out.println("***Append URL parent "+parentURL);
                path = parentURL + "/" + path;
            }
            return path;
        }
        String testPath = "";
        switch (OSCmdUtil.getOSType()) {
        case OSCmdUtil.WINDOWS:
            {
                if (!path.contains(":")) {
                    //this is an aboslolute directory
                    testPath =
                            FileUtil.getCurrentDir() + File.separator + path;
                } else {
                    testPath = path;
                }
            }
            break;
        case OSCmdUtil.LINUX:
            {
                //outputLog("----this is on linux the path is "+path);
                if (!path.startsWith("/")) {
                    //this is an aboslolute directory in linux
                    //outputLog("----this is on linux with relateive path being converted: "+path);
                    testPath =
                            FileUtil.getCurrentDir() + File.separator + path;
                } else {
                    outputLog("----this is on linux with ablotue path not being converted: " +
                              path);
                    testPath = path;
                }
            }
            break;
        case OSCmdUtil.MAC:
            {
                //outputLog("----this is on linux the path is "+path);
                if (!path.startsWith("/")) {
                    //this is an aboslolute directory in linux
                    //outputLog("----this is on linux with relateive path being converted: "+path);
                    testPath =
                            FileUtil.getCurrentDir() + File.separator + path;
                } else {
                    outputLog("----this is on MAC with ablotue path not being converted: " +
                              path);
                    testPath = path;
                }
            }
            break;
        default:
            {
                System.out.println("XXXXX----not supported OS yet, please consult with cesoo.info : " +
                                   OSCmdUtil.getOSType());
            }
        }


        return testPath;
    }

    public static void mkDirsIfNotExists(String dirPath) {
        dirPath = OSCmdUtil.pathReSettle2OS(dirPath);
        File f = new File(dirPath);
        if (!f.exists()) {
            //System.out.println("make dir to :"+dirPath);
            f.mkdirs();
        }

    }

    public static boolean isFileExists(String filepath) {

        String testPath;
        File f = new File(filepath);
        return f.exists();
    }

    public static String getCurrentDir() {
        return System.getProperty("user.dir");
    }

    public static String getWorkingDir() {
        String work_dir = getCurrentDir();
        if (WORKING_BASE.equalsIgnoreCase("")) {
            return work_dir;
        } else {
            work_dir = work_dir + File.separator + WORKING_BASE;
            return work_dir;
        }

    }

    public static boolean writeStrToFile(String filecontent,
                                         String filefullpath) {
        boolean b = true;
        try {
            OutputStreamWriter utput =
                new OutputStreamWriter(new FileOutputStream(filefullpath),
                                       "UTF-8");
            utput.write(filecontent, 0, filecontent.length());
            utput.flush();
        } catch (Exception e) {
            b = false;
            outputLog("XXXXX-Exception in FileUtil.writeStrToFile " +
                      e.getMessage());
            e.printStackTrace();
        }
        return b;
    }

    public static void main(String[] args) {
        FileUtil fileUtil = new FileUtil();
        FileUtil.clearFile("results/local_ts_120412154227");
    }
}
