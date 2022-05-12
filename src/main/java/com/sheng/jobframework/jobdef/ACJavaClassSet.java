package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.utility.ACJCL;
import com.sheng.jobframework.utility.ClassLoaderUtil;
import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.utility.OSCmdUtil;

import com.sheng.jobframework.annotation.TestJobDOM;

import java.io.File;

import java.util.Enumeration;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


//import framework.ACUtility.JarLoader;


public class ACJavaClassSet extends ACJobAppender {
    int i = 1;
    /*
     * new classloader 02-15 to fix the class crossed between different jobs
     */
    //transient private ACJobClassLoader classloader = new ACJobClassLoader();
    transient private ACJCL classloader = new ACJCL();

    public ACJCL getClassLoader() {
        return classloader;
    }
    //end modify

    public ACJavaClassSet() {
    }

    public void addURL(String classpath) {
        //libArr.add(libpath);
        prop.put("classpath" + i, classpath);
        //System.out.println("add claspath "+classpath);
        //initialize();
        i++;
    }

    public void parseNode(Node node) {
        Element ele = (Element)node;
        String url = ele.getAttribute(TestJobDOM.node_attribute_location);
        addURL(url);
        //ACLibSet libset = new ACLibSet();
        //libset.addLib(libpath);
        //return libpath;
    }

    public void initialize() {
        //ClassLoader loader = ClassLoader.getSystemClassLoader();
        //
        /*
         * new classloader 02-15 to fix the class crossed between different jobs
         */
        ClassLoaderUtil urlloader = new ClassLoaderUtil();

        //end modify
        //ClassLoaderUtil urlloader = ClassLoaderUtil.getClassLoaderInstance();
        //URLClassLoader urlloader = ClassLoaderUtil.getClassLoaderInstance();
        //String separator = System.getProperty("path.separator");
        try {


            Enumeration keys = prop.keys();
            while (keys.hasMoreElements()) {
                String strKey = (String)keys.nextElement();
                String classpath = prop.getProperty(strKey, "");
                //shining 2012-03-21

                classpath = OSCmdUtil.pathReSettle2OS(classpath);
                //end
                //System.out.println("try to load the class from "+classpath);
                //shining modified on 2010-4-22

                classpath = FileUtil.getAbsolutePath(classpath);
                File f = new File(classpath);
                if (f.isDirectory()) {
                    File[] jarfiles = f.listFiles();
                    int itotal = jarfiles.length;
                    if (itotal == 0)
                        System.out.println("XXXXX-no files under the class dir " +
                                           f.getAbsolutePath());
                    for (int l = 0; l < itotal; l++) {
                        File jarfile = jarfiles[l];
                        if (FileUtil.getFileExtension(jarfile.getName()).equalsIgnoreCase("jar") ||
                            FileUtil.getFileExtension(jarfile.getName()).equalsIgnoreCase("class")) {
                            String filepath = jarfile.getAbsolutePath();
                            outputFrameLog("------ACClassloder: loading class path: " +
                                           filepath);
                            urlloader.addRuntimeClassPath(filepath);
                        }
                    }
                } else {
                    if (f.exists()) {
                        //System.out.println("trying to load class "+classpath);
                        //System.out.println("before load jar: "+System.getProperty("java.class.path"));
                        outputFrameLog("------ACClassloder: loading jar: " +
                                       classpath);
                        //urlloader.addClassPath(classpath);
                        /*
                   * new classloader 02-15 to fix the class crossed between different jobs
                   */
                        urlloader.addRuntimeClassPath(classpath);
                        //classloader.addRuntimeClassPath(classpath);
                        //end modification
                        /*
                  URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
                  URL[] urlarry = sysloader.getURLs();
                  int leng = urlarry.length;
                  for(int i=0;i<leng;i++){
                      URL url = urlarry[i];
                      outputFrameLog("url is "+url.getPath());
                  }
                  */
                        // urlloader.addRuntimeClassPath(classpath);
                        //System.out.println("after load jar: "+System.getProperty("java.class.path"));
                        //urlloader.loadClass("oralce.sgt.automation.email.SendReceiveMail").newInstance();
                        //byte[] resource = JarLoader.getDataSource(classpath);
                        //JarLoader.load(resource);
                    } else
                        outputFrameLog("Error in ACJavaClassSet: class not found in " +
                                       classpath);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in ACJavaClassSet jar loader " +
                               e.getMessage());
            e.printStackTrace();
        }
    }
}
