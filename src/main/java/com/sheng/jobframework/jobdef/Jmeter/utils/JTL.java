package com.sheng.jobframework.jobdef.Jmeter.utils;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


/**
 *
 * Read the JMeter results in xml format using JDom, then analyzes the results.
 *
 ***/
public class JTL {

    //All the samples' list
    private List samples = null;
    //The parameter for JMeter results file path
    private String jmeterResultsPath = "";

    private int minTime = Integer.MAX_VALUE;
    private int maxTime = Integer.MIN_VALUE;
    private int avgTime = Integer.MIN_VALUE;
    private int sampleCount = 0;
    private int successSampleCount = 0;
    private int failureSampleCount = 0;

    public JTL(String filePath) {
        super();
        jmeterResultsPath = filePath;
        samples = readResults();
        analyzeResults();
    }

    /**
     *Using JDom to read jmeter results xml document
     *
     * @return JDom Element List of JMeter results(samples).
     *
     * @author yoyo.zhou@oracle.com
     *
     **/
    private List readResults() {

        Document doc = null;

        SAXBuilder builder = new SAXBuilder();

        try {

            doc = builder.build(new File(jmeterResultsPath));
            if (doc != null) {
                Element root = doc.getRootElement();
                samples = root.getChildren(); //all the samples
            }

        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }

        return samples;
    }

    private void analyzeResults() {

        int totalTime = 0;
        Iterator iterator = samples.iterator();
        while (iterator.hasNext()) {
            Element sample = (Element)iterator.next();

            int time =
                Integer.valueOf(sample.getAttributeValue("t")).intValue();
            if (time < minTime) {
                minTime = time;
            }
            if (time > maxTime) {
                maxTime = time;
            }
            totalTime += time;
            ++sampleCount;

            String isSuccess = sample.getAttributeValue("s");

            if (isSuccess.equalsIgnoreCase("true")) {
                ++successSampleCount;
            }
            if (!isSuccess.equalsIgnoreCase("true")) {
                ++failureSampleCount;
            }
        }

        avgTime = Math.round(totalTime * 1.0f / sampleCount);
    }


    public static void main(String[] args) {

        JTL jra = new JTL("U:\\JMeter\\bs.jtl");
        System.out.println("minTime of the test: " + jra.getMinTime());
        System.out.println("avgTime of the test: " + jra.getAvgTime());
        System.out.println("maxTime of the test: " + jra.getMaxTime());
        System.out.println("suceessful samples: " +
                           jra.getSuccessSampleCount());
        System.out.println("failure samples: " + jra.getFailureSampleCount());


    }

    public String getJmeterResultsPath() {
        return jmeterResultsPath;
    }

    public int getMinTime() {
        return minTime;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public int getAvgTime() {
        return avgTime;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public int getSuccessSampleCount() {
        return successSampleCount;
    }

    public int getFailureSampleCount() {
        return failureSampleCount;
    }
}
