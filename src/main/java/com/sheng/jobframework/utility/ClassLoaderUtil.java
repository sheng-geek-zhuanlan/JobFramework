package com.sheng.jobframework.utility;

import java.io.File;

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;


public class ClassLoaderUtil extends URLClassLoader {
    public static ClassLoaderUtil loader;
    public static URLClassLoader sysloader;

    public static URLClassLoader getClassLoaderInstance() {
        if (loader == null) {
            loader = new ClassLoaderUtil();
        }
        //return loader;
        return sysloader;

    }

    public ClassLoaderUtil() {
        super(new URL[0], ClassLoader.getSystemClassLoader());
        sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
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
            method.invoke(sysloader, new Object[] { pathToAdd.toURL() });
            //method.invoke(loader,new Object[]{pathToAdd.toURL()});
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
