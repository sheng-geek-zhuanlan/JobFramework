package com.sheng.jobframework.utility;

import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;


public class ACJCL {
    private JarClassLoader jcl = new JarClassLoader();

    public ACJCL() {
        super();

    }

    public void addRuntimeClassPath(String path) {
        jcl.add(path);
    }

    public Object loadClass(String classname) {
        jcl.getParentLoader().setEnabled(false);
        JclObjectFactory factory = JclObjectFactory.getInstance();
        Object obj = factory.create(jcl, classname);

        return obj;
    }

    public void unloadClass(String classname) {
        jcl.unloadClass(classname);
    }
}
