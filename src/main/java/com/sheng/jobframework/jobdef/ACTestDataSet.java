package com.sheng.jobframework.jobdef;

import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.utility.OSCmdUtil;
import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.annotation.TestJobDOM;

import com.sheng.jobframework.datasource.DataSource;
import com.sheng.jobframework.datasource.ReadExcel;
import com.sheng.jobframework.datasource.ReadXML;

import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class ACTestDataSet extends ACJobAppender {
    public static final String DATA_TYPE_XSL = "XLS";
    public static final String DATA_TYPE_XML = "XML";
    public static final String DATA_TYPE_ITERATION = "iteration";
    public static final String DATA_TYPE_I18N = "i18n";
    public static final String DATA_TYPE_SEPERATOR = "#";
    public ArrayList datafilelist = new ArrayList(100);
    public ArrayList testDataArr = new ArrayList(100);
    Properties dataProp = new Properties();
    public Hashtable testDataHash = new Hashtable();
    public int i = 0;
    String iteration = "";

    public ACTestDataSet() {
    }

    public void initialize() {
        outputFrameLog("***ACTestDataSet is initializing...");
        loadDataToSet();
    }

    public void addDataFile(String type, String filepath) {
        // datafilelist.add(filepath);
        prop.put(type + "_" + i, filepath);
        i++;
    }

    public int getIterationSize() {
        return testDataHash.size();
    }

    public Hashtable getDataHashArr() {
        return testDataHash;
    }

    public boolean setDataIteration(String indicator) {
        if (testDataHash.containsKey(indicator)) {
            iteration = indicator;
            Properties datafromhash = (Properties)testDataHash.get(iteration);
            //dataProp = Utility.mergeTwoProperties(datafromhash,dataProp);
            //datafromhash will overwrite the same para name in dataProp
            dataProp = Utility.mergeTwoProperties(dataProp, datafromhash);
            return true;
        } else {
            outputFrameLog("Error: indicator " + indicator +
                           " could not be recogized in datasource!");
            return false;
        }
    }

    public String getData(String key) {
        //System.out.println("in actestdataset, the iteration is "+iteration);
        return dataProp.getProperty(key, "");
        // return dataProp.getProperty(key,"");
    }
    /*
     * add 2010-12-01
     * provide user self add data during job running
     */

    public void addData(String key, String value) {
        dataProp.setProperty(key, value);
    }

    public Properties getDataProperties() {
        return dataProp;
        // return dataProp.getProperty(key,"");
    }

    public void addTestDataSet(ACTestDataSet dataset) {
        Properties prop1 = dataset.getDataProperties();
        dataProp = Utility.mergeTwoProperties(dataProp, prop1);
    }

    public void loadXMLData(String filepath) {
        filepath = FileUtil.getAbsolutePath(filepath);
        //System.out.println("in TestDataSet try to load xml data"+filepath);
        Properties propToLoad = new Properties();
        try {
            //System.out.println("in TestDataSet loading xml data"+filepath);
            FileInputStream fis = new FileInputStream(filepath);
            propToLoad.loadFromXML(fis);
        } catch (Exception e) {
            System.out.println("ACTestDataSet loadXML data exception: " +
                               e.getMessage());
            e.printStackTrace();
        }
        dataProp = Utility.mergeTwoProperties(dataProp, propToLoad);
    }

    public void loadXSLData(String filepath) {
        //System.out.println("ACTestDataSet loadXML data exception: Not implemented yet for XSL ");
    }

    public void parseNode(Node node) {
        Element ele = (Element)node;
        //ACTestDataSet dataset = new ACTestDataSet();
        //String type = ele.getAttribute(TestJobDOM.node_attribute_datatype);
        String location = ele.getAttribute(TestJobDOM.node_attribute_location);
        String type = ele.getAttribute(TestJobDOM.node_attribute_type);
        addDataFile(type, location);
    }

    public String getQTPFormatData() {
        String ret = "";
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            if (!ret.equalsIgnoreCase(""))
                ret = ret + ";";
            String strKey = (String)keys.nextElement();
            if (strKey.contains(DATA_TYPE_ITERATION)) {
                ret =
ret + DATA_TYPE_ITERATION + DATA_TYPE_SEPERATOR + prop.getProperty(strKey);
            } else {
                ret = ret + prop.getProperty(strKey);
            }

        }
        return ret;
        /*
        String ret="";
        int isize = datafilelist.size();
        for(int i=0;i<isize;i++){
            if(!ret.equalsIgnoreCase(""))
            ret=ret+";";
            String filepath = (String)datafilelist.get(i);
            ret = ret+filepath;
        }
        return ret;
        */
    }

    public void loadDataToSet() {
        //int iSize = datafilelist.size();
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            //for(int i=0;i<iSize;i++){
            //String filepath = (String)datafilelist.get(i);
            String strKey = (String)keys.nextElement();
            String filepath = (String)prop.getProperty(strKey);
            //shining 2012-03-21
            filepath = OSCmdUtil.pathReSettle2OS(filepath);
            //end
            //System.out.println("in TestDataSet get xml data file "+filepath);
            int iPos = filepath.lastIndexOf(".");
            String fileextension = filepath.substring(iPos + 1);
            filepath = FileUtil.getAbsolutePath(filepath);
            if (!FileUtil.isFileExists(filepath)) {
                outputFrameLog("XXXXX-Error data file is not found at path: " +
                               filepath);
                continue;
            }
            //System.out.println("in TestDataSet get file extension "+fileextension);
            DataSource dataLoader;
            if (strKey.startsWith(DATA_TYPE_ITERATION)) {
                //System.out.println("in ACTestDataSet: TRY TO LOAD ITERATION TYPE DATA "+filepath);
                if (fileextension.equalsIgnoreCase(DATA_TYPE_XSL)) {
                    dataLoader = new ReadExcel();
                    //loadXSLData(filepath);
                } else if (fileextension.equalsIgnoreCase(DATA_TYPE_XML)) {
                    dataLoader = new ReadXML();
                    //loadXMLData(filepath);
                } else {
                    dataLoader = null;
                }
                dataLoader.loadData(filepath);
                Hashtable datahash = dataLoader.getTestDataHash();
                //debug purpose
                //System.out.println("in ACTestDataSet: datahash has "+datahash.size());
                //Properties prop = (Properties)datahash.get("fr");
                //prop.list(System.out);
                //debug purpose
                mergeTwoDataHash(testDataHash, datahash);
            } else {
                if (fileextension.equalsIgnoreCase(DATA_TYPE_XSL)) {
                    loadXSLData(filepath);
                } else if (fileextension.equalsIgnoreCase(DATA_TYPE_XML)) {
                    loadXMLData(filepath);
                }
            }

        }
    }

    public void mergeTwoDataHash(Hashtable data1, Hashtable data2) {
        Enumeration datakeys = data1.keys();
        ArrayList mergedArr = new ArrayList();
        while (datakeys.hasMoreElements()) {
            String indicator1 = (String)datakeys.nextElement();
            Enumeration datakeys2 = data2.keys();
            while (datakeys2.hasMoreElements()) {
                String indicator2 = (String)datakeys2.nextElement();
                if (indicator2.equalsIgnoreCase(indicator1)) {
                    Properties prop1 = (Properties)data1.get(indicator1);
                    Properties prop2 = (Properties)data2.get(indicator1);
                    Utility.mergeTwoProperties(prop1, prop2);
                    mergedArr.add(indicator1);
                }
            }
        }
        Enumeration datakeys2 = data2.keys();
        while (datakeys2.hasMoreElements()) {
            String indicator2 = (String)datakeys2.nextElement();
            if (!mergedArr.contains(indicator2)) {
                Properties prop2 = (Properties)data2.get(indicator2);
                data1.put(indicator2, prop2);
            }
        }
    }

}
