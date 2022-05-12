package com.sheng.jobframework.jobdef.Jmeter.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;


/**
 *
 *This JMX class is designed for JMeter JMX XML file version 1.2.
 *
 * @author yoyo.zhou@oracle.com
 * @date 2011-07-28
 **/
public class JMX {

    private Document jmxdoc = null;
    private String jmx = "";

    public JMX() {
        super();
    }

    public JMX(String jmx) {
        super();
        this.jmx = jmx;
        this.jmxdoc = load(jmx);

    }

    /**
     *Using JDom to load jmeter jmx document
     *
     * @param jmx JMeter JMX file path
     *
     * @return JMX XML document
     *
     * @author yoyo.zhou@oracle.com
     *
     **/
    private Document load(String jmx) {

        Document jmxdoc = null;

        SAXBuilder builder = new SAXBuilder();

        try {

            jmxdoc = builder.build(new File(jmx));


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }

        return jmxdoc;
    }

    public void save() {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());

        try {
            FileOutputStream fos = new FileOutputStream(new File(jmx));
            outputter.output(jmxdoc, fos);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Element getThreadGroupElement() {
        Element threadGroup = null;

        try {
            XPath xPath = XPath.newInstance("//ThreadGroup");
            threadGroup = (Element)xPath.selectSingleNode(jmxdoc);

        } catch (JDOMException e) {
            e.printStackTrace();
        }

        return threadGroup;

    }

    public Element getJavaSamplerElement() {

        Element javaSampler = null;

        try {
            XPath xPath = XPath.newInstance("//JavaSampler");
            javaSampler = (Element)xPath.selectSingleNode(jmxdoc);

        } catch (JDOMException e) {
            e.printStackTrace();
        }

        return javaSampler;

    }

    public String getJavaSamplerClass() {

        Element javaSampler = getJavaSamplerElement();

        return getProp(javaSampler, "classname");

    }

    public void setJavaSamplerClass(String fullClassName) {

        Element javaSampler = getJavaSamplerElement();

        setProp(javaSampler, "classname", fullClassName);

    }

    public String getThreadGroupProp(String propName) {

        Element threadGroup = getThreadGroupElement();

        return getProp(threadGroup, propName);
    }

    /**
     * Set threadGroup property by property name
     * propName list maybe:
     * <li>loops<li>
     * <li>num_threads<li>
     * <li>ramp_time<li>
     * for more please see the JMX file document.
     *
     * @param propName threadGroup property name
     * @param propValue threadGroup property value
     *
     **/

    public void setThreadGroupProp(String propName, String propValue) {

        Element threadGroup = getThreadGroupElement();
        setProp(threadGroup, propName.toLowerCase(), propValue);
    }

    public String getProp(Element element, String propName) {

        String propValue = "";
        for (Element prop : (List<Element>)element.getChildren()) {
            if (prop.getName().equalsIgnoreCase("elementProp")) {
                for (Element elementProp : (List<Element>)prop.getChildren()) {
                    if (elementProp.getAttributeValue("name").contains(propName)) {
                        propValue = elementProp.getTextTrim();
                        break;
                    }
                }
            } else {
                if (prop.getAttributeValue("name").contains(propName)) {
                    propValue = prop.getTextTrim();
                    break;
                }

            }

        }

        return propValue;
    }

    public void setProp(Element element, String propName, String propValue) {

        for (Element prop : (List<Element>)element.getChildren()) {
            if (prop.getName().equalsIgnoreCase("elementProp")) {
                for (Element elementProp : (List<Element>)prop.getChildren()) {
                    if (elementProp.getAttributeValue("name").contains(propName)) {
                        elementProp.setText(propValue);
                        break;
                    }
                }
            } else {
                if (prop.getAttributeValue("name").contains(propName)) {
                    prop.setText(propValue);
                    break;
                }

            }

        }
    }

    public static void main(String[] args) {
        JMX jmx = new JMX("U:\\JMeter_home\\SMTP.jmx");
        System.out.println("num_threads:" +
                           jmx.getThreadGroupProp("num_threads"));
        System.out.println("loops:" + jmx.getThreadGroupProp("loops"));
        System.out.println("JavaSamplerClass:" + jmx.getJavaSamplerClass());
        jmx.setThreadGroupProp("Num_threads", "3");
        jmx.setThreadGroupProp("loops", "3");
        jmx.setThreadGroupProp("duration", "");
        jmx.setJavaSamplerClass("com.oracle.beehive.jmeter.smtp.SendEmailMessageSMTPJavaSampler");
        jmx.save();

    }
}
