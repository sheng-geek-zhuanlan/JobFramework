package com.sheng.jobframework.jobdef;

import java.util.Enumeration;
import java.util.Properties;

public class ACJobParaParser {
    public static String paraIndicator = "$";

    public ACJobParaParser() {
    }

    public static Properties formatProp2Para(String namePrefix,
                                             Properties prop) {
        //prop.list((System.out));
        //namePrefix should already contains "$", such as like "$ENV"
        //System.out.println("in formatProp2Para the envFileName is "+namePrefix);
        Properties prop2 = new Properties();
        int paraNum = prop.size();
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String value = prop.getProperty(strKey);
            if (!namePrefix.equalsIgnoreCase("")) {
                prop2.put(namePrefix + "." + strKey, value);
            } else {
                prop2.put(paraIndicator + strKey, value);
            }
        }
        //prop2.list((System.out));
        return prop2;
    }

}
