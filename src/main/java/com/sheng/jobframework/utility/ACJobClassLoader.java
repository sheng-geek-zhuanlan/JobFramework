package com.sheng.jobframework.utility;

import java.io.File;

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;


public class ACJobClassLoader extends URLClassLoader {

    public ACJobClassLoader() {
        // super(new URL[0], ClassLoader.getSystemClassLoader());
        super(new URL[0]);
        //loader=this;
        //sysloader=this;
    }

    public void addClassPath(String path) {
        try {
            File pathToAdd = new File(path).getCanonicalFile();
            System.out.println("in loaderUtil " + pathToAdd.toURL());
            addURL(pathToAdd.toURL());
            String classPath = System.getProperty("java.class.path");
            classPath = classPath + ";" + pathToAdd.getAbsolutePath();
            System.setProperty("java.class.path", classPath);
            //loadClass("oralce.sgt.automation.email.SendReceiveMail").newInstance();
        } catch (Exception e) {
            System.out.println("Exception in classloadUtil to load path " +
                               path + " msg is " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addRuntimeClassPath(String path) {
        Class sysclass = URLClassLoader.class;
        Class[] parameters = new Class[] { URL.class };
        try {
            File pathToAdd = new File(path).getCanonicalFile();
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            //method.invoke(sysloader,new Object[]{pathToAdd.toURL()});
            method.invoke(this, new Object[] { pathToAdd.toURL() });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public Class getLoadedClass(String name) {
        try {
            Class cl = findLoadedClass(name);
            return cl;
        } catch (Exception e) {
            System.out.println("Exception in findClass");
            e.printStackTrace();
            return null;
        }
    }
}
