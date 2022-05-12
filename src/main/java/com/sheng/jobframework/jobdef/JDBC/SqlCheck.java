package com.sheng.jobframework.jobdef.JDBC;

import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SqlCheck extends JDBCJob {
    public static String QUERY_SQL = "FetchDataSQL";
    public static String DATA_TYPE = "DataType";
    public static String EXPECTED_VALUE = "ExpectedValue";
    public static String PASS_WHEN = "PassWhen";
    public static String TO_OUTPUT = "Output";
    public static String PASS_CONDITION_EQUAL = "equal";

    private String querySQL = "";
    private String checkType = "";
    private String expectedValue = "";
    private String passCondition = "";
    private String outputName = "";

    public SqlCheck() {
    }

    public void parseEnv2Job(Properties prop) {
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String strKey = (String)keys.nextElement();
            String envValue = prop.getProperty(strKey);
            querySQL = querySQL.replace(strKey, envValue);
        }
    }

    public void runJDBCJob() {
        try {
            reportPass("is running sql line " + querySQL);
            Statement stmt = null;
            querySQL = querySQL.replace(";", "");
            stmt = conn.createStatement();
            if ((querySQL.startsWith("insert")) ||
                querySQL.startsWith("update") ||
                querySQL.startsWith("delete")) {
                //this is a ddl sql
                //stmt.addBatch(sqlline);
                reportFail("XXXX---DB check point does not support DDL operation, must be select query!");

            } else {
                ResultSet resultset = stmt.executeQuery(querySQL);
                ArrayList resultArr = populate(resultset);
                if (resultArr.size() > 1) {
                    reportFail("more than one record return after execute query " +
                               querySQL);
                    outputRecord(resultArr);
                } else if (resultArr.size() == 0) {
                    reportFail("0 record return after executre " + querySQL);
                } else {
                    Properties prop = (Properties)resultArr.get(0);
                    Enumeration elmentInfoKeys = prop.keys();
                    String value = "";
                    while (elmentInfoKeys.hasMoreElements()) {
                        String strKey = (String)elmentInfoKeys.nextElement();
                        outputFrameLog("--RECORD output---");
                        outputFrameLog("        column name/value: " + strKey +
                                       "\\" + (String)prop.get(strKey));
                        value = (String)prop.get(strKey);
                    }
                    boolean outputType = false;
                    if (!outputName.equalsIgnoreCase("")) {
                        reportPass("----Will wirte data into AC Channel! ");
                        reportPass("        Name is " + outputName);
                        reportPass("        Value is " + value);
                        writeIntoACChannel(outputName, value);
                        outputType = true;
                    }
                    if (!expectedValue.equalsIgnoreCase("")) {
                        if (passCondition.equalsIgnoreCase(PASS_CONDITION_EQUAL)) {
                            if (value.equalsIgnoreCase(expectedValue)) {
                                reportPass("------DB EQUAL Check Point Pass: actual value is " +
                                           value + " expected value is " +
                                           expectedValue);
                            } else {
                                reportFail("XXXX--DB EQUAL Check Point failed: actual value is " +
                                           value + " expected value is " +
                                           expectedValue);
                            }
                        } else {
                            if (!value.equalsIgnoreCase(expectedValue)) {
                                reportPass("------DB NOT EQUAL Check Point Pass: actual value is " +
                                           value + " expected value is " +
                                           expectedValue);
                            } else {
                                reportFail("XXXX--DB NOT EQUAL Check Point failed: actual value is " +
                                           value + " expected value is " +
                                           expectedValue);
                            }
                        }
                    } else {
                        if (outputType) {
                            reportPass("-----No check point defined, only output");
                        } else {
                            reportFail("XXXX--Must define one of datacheck or output!");
                        }


                    }


                }
            }

        } catch (Exception e) {
            reportFail(LOGMSG.JDBC_LOADDATA_EXCEP + querySQL + e.getMessage());
            e.printStackTrace();
        }
    }

    public void outputRecord(ArrayList arr) {
        int isize = arr.size();
        for (int i = 0; i < isize; i++) {
            Properties prop = (Properties)arr.get(i);
            Enumeration elmentInfoKeys = prop.keys();
            while (elmentInfoKeys.hasMoreElements()) {
                String strKey = (String)elmentInfoKeys.nextElement();
                outputFrameLog("--RECORD output---");
                outputFrameLog("        column name/value: " + strKey + "\\" +
                               (String)prop.get(strKey));
            }
        }
    }

    public void parseNode(Node node) {
        Element ele = (Element)node;
        //outputFrameLog("parse node "+node.getNodeName());
        setName(node.getNodeName());
        String todomark = ele.getAttribute(TestJobDOM.node_attribute_todo);
        if (node.hasChildNodes()) {
            NodeList argList = node.getChildNodes();
            int iArg = argList.getLength();
            //outputFrameLog("node has childnodes number: "+iArg);
            for (int i = 0; i < iArg; i++) {
                Node arg = argList.item(i);
                //outputFrameLog("parse node "+arg.getNodeName());
                if (arg.getNodeName().endsWith(QUERY_SQL)) {
                    querySQL = arg.getTextContent();
                }
                if (arg.getNodeName().endsWith(DATA_TYPE)) {
                    checkType = arg.getTextContent();
                }
                if (arg.getNodeName().endsWith(EXPECTED_VALUE)) {
                    expectedValue = arg.getTextContent();
                }
                if (arg.getNodeName().endsWith(PASS_WHEN)) {
                    passCondition = arg.getTextContent();
                }
                if (arg.getNodeName().endsWith(TO_OUTPUT)) {
                    outputName = arg.getTextContent();
                }
            }
        }
    }

    public static ArrayList populate(ResultSet rs) throws Exception {
        ResultSetMetaData metaData = rs.getMetaData();
        int colCount = metaData.getColumnCount();
        ArrayList ret = new ArrayList();
        while (rs.next()) {
            Properties recordProp = new Properties();
            for (int i = 1; i <= colCount; i++) {
                try {
                    Object value = rs.getObject(i);
                    String columnname = metaData.getColumnName(i);
                    recordProp.put(columnname, value);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
            ret.add(recordProp);
        }
        return ret;
    }
}
