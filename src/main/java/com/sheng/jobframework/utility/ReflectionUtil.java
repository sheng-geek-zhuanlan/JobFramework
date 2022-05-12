package com.sheng.jobframework.utility;

import java.lang.reflect.Method;

import java.util.ArrayList;


public class ReflectionUtil {
    public ReflectionUtil() {
    }

    public static ArrayList getDeclaredMethod(String name, String filter) {
        ArrayList testMethodArr = new ArrayList(100);
        try {
            Class c = Class.forName(name);
            Method m[] = c.getDeclaredMethods();
            for (int i = 0; i < m.length; i++) {
                String funcName = m[i].getName();
                //TO DO: contains here have risk, should use startof or other function
                if (funcName.contains(filter)) {
                    testMethodArr.add(funcName);
                }
                //System.out.println("name: "+m[i].getName());
                //System.out.println(m[i].toString());
            }

        } catch (Throwable e) {
            System.err.println(e);
        }
        return testMethodArr;
    }

    public static void main(String[] args) {
        ReflectionUtil.getDeclaredMethod("framework.ACDriverFactory.Junit.LeapYearTest",
                                         "test");
    }

}
