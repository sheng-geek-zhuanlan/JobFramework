package com.sheng.jobframework.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import java.util.Properties;


public class TemplateUtil {
    //  private String templateRootDir=null;
    private static String appRootDir = null;
    private static String template = "template";
    private static TemplateUtil tu = null;

    private TemplateUtil() {
        System.out.println("Enter templateUtil()");
        getAppRootDir();
        //    getTemplateRootDir();
    }

    private void getAppRootDir() {
        System.out.println("enter the getAppRootDir()");
        appRootDir = ".";
        //url=file:/D:/work/oc4j/j2ee/home/applications/wactest/wactest/WEB-INF/classes/sgt/coral/wac/./
        String tmp = TemplateUtil.class.getResource(".").toString();
        System.out.println("In TemplateUtil.getAppRootDir the tmp is " + tmp);
        int pos1 = tmp.indexOf(":");
        if (pos1 == -1) {
            return;
        }
        int pos2 = tmp.lastIndexOf("WEB-INF");
        if (pos2 == -1) {
            //shining add
            int pos3 = tmp.lastIndexOf("sgt");
            if (pos3 == -1)
                return;
            else
                pos2 = pos3;
        }
        appRootDir = tmp.substring(pos1 + 1, pos2);
    }

    private void getTemplateRootDir() {
        /*
      //url=file:/D:/work/oc4j/j2ee/home/applications/wactest/wactest/WEB-INF/classes/sgt/coral/wac/./
      String tmp=TemplateUtil.class.getResource(".").toString();
      int pos1=tmp.indexOf(":");
      if(pos1==-1){
        rootDir=".";
        return;
      }
      int pos2=tmp.lastIndexOf("WEB-INF");
      if(pos2==-1){
        rootDir=".";
        return;
      }
      rootDir=tmp.substring(pos1+1,pos2)+template;
*/
        //      templateRootDir=getAppRootDir()+template;
    }

    public static TemplateUtil getInstance() {
        if (tu == null) {
            System.out.println("In TemplateUtil.getInstance():the tu object is null, should construct!");
            tu = new TemplateUtil();
        } else
            System.out.println("In TemplateUtil.getInstance():the tu object is not null");
        return tu;
    }

    public String getTemplate(String fileName) {
        System.out.println("rootDir=" + appRootDir);
        String content = "";
        String templateRootDir = appRootDir + template;
        //String templateRootDir="D:\\coral_projs\\coral_2006\\public_html\\"+template;
        File file = new File(templateRootDir + "/" + fileName);
        try {
            if (file.exists()) {
                BufferedReader rd = new BufferedReader(new FileReader(file));
                String line;
                StringBuffer sb = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    sb.append(line + "\r\n");
                }
                content = sb.toString();
                content = content.trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    public Properties getConfigProperties() {

        String configFile = appRootDir + "WEB-INF/config.ini";
        //System.out.println("appRootDir="+appRootDir);
        //configFile="D:\\coral_projs\\coral_2006\\public_html\\"+"WEB-INF\\config.ini";
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(configFile));
        } catch (Exception e) {
            System.out.println("Error in TemplateUtil.getConfigProperties(): can't read file " +
                               configFile);
            ;
            e.printStackTrace();
        }
        return p;
    }

}
