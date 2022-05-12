package com.sheng.jobframework.jobdef.Script;

import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.jobdom.ACElement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.util.Enumeration;
import java.util.Properties;


public class ScriptUtil extends ACElement {
    public ScriptUtil() {
        super();
    }

    public static boolean writeImportParams(String filepath, String filename,
                                            Properties importProp) {
        try {
            filepath = FileUtil.getAbsolutePath(filepath);
            FileUtil.mkDirsIfNotExists(filepath);
            File f = new File(filepath + File.separator + filename);
            if (!f.exists()) {
                f.createNewFile();
            }
            BufferedWriter utput = new BufferedWriter(new FileWriter(f));
            Enumeration keys = importProp.keys();
            while (keys.hasMoreElements()) {
                String strKey = (String)keys.nextElement();
                String strValue = importProp.getProperty(strKey);
                utput.write(strKey + "=" + strValue + "\n");
            }
            utput.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        ScriptUtil scriptUtil = new ScriptUtil();
    }
}
