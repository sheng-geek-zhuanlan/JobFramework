package com.sheng.jobframework.jobdef.JDBC;

import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdef.ACJobAppender;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ACJDBCSet extends ACJobAppender {
    public static String NODE_DBCONFIG = "DBConfig";
    public static String DB_HOST = "DB_HOST";
    public static String DB_PORT = "DB_PORT";
    public static String DB_SID = "DB_SID";
    public static String DB_USER = "DB_USER";
    public static String DB_PASSWD = "DB_PWD";
    public static String DB_TYPE = "DB_TYPE";
    public static String NODE_DATALOAD = "DataLoad";
    public static String NODE_DATACHECK = "DataFetch";
    public static String FETCH_SQL = "FetchDataSQL";
    public static String DATA_TYPE = "DataType";
    public static String EXPECTED_VALUE = "ExpectedValue";
    public static String PASS_WHEN = "PassWhen";
    public static String ORACLE = "ORACLE";
    public static String DB2 = "DB2";
    public static String MYSQL = "mysql";
    public String host = "";
    public String user = "";
    public String passwd = "";
    public String port = "";
    public String sid = "";
    public String type = "";
    public SQLExecuter sqljob = new SQLExecuter();
    public ArrayList jdbcJobArr = new ArrayList(100);

    public ACJDBCSet() {
    }

    public ArrayList getJDBCJobArr() {
        return jdbcJobArr;
    }

    public void initialize() {
        if(prop.getProperty("jdbctask","").equalsIgnoreCase(""))
            return;
        Node node = JOM.getNodeFromOuterXML(prop.getProperty("jdbctask"));
        NodeList nodes = node.getChildNodes();
        int ilen = nodes.getLength();
        try {
            for (int i = 0; i < ilen; i++) {
                Node childNode = nodes.item(i);
                if (childNode.getNodeType() != Node.TEXT_NODE) {
                    //System.out.println("begin to switch the node "+childNode.getNodeName());
                    String nodename = childNode.getNodeName();
                    if (nodename.equalsIgnoreCase(NODE_DBCONFIG)) {
                        NodeList argList = childNode.getChildNodes();
                        int iArg = argList.getLength();
                        //String host,port,user,passwd,sid,type;
                        for (int j = 0; j < iArg; j++) {
                            Node arg = argList.item(j);
                            if (arg.getNodeName().endsWith(DB_HOST)) {
                                host = arg.getTextContent();
                            }
                            if (arg.getNodeName().endsWith(DB_PORT)) {
                                port = arg.getTextContent();
                            }
                            if (arg.getNodeName().endsWith(DB_SID)) {
                                sid = arg.getTextContent();
                            }
                            if (arg.getNodeName().endsWith(DB_USER)) {
                                user = arg.getTextContent();
                            }
                            if (arg.getNodeName().endsWith(DB_PASSWD)) {
                                passwd = arg.getTextContent();
                            }
                            if (arg.getNodeName().endsWith(DB_TYPE)) {
                                type = arg.getTextContent();
                            }
                        }
                    } else if (nodename.equalsIgnoreCase(NODE_DATALOAD)) {
                        SQLExecuter sqljob = new SQLExecuter();
                        sqljob.parseNode(childNode);
                        jdbcJobArr.add(sqljob);
                    } else if (nodename.equalsIgnoreCase(NODE_DATACHECK)) {
                        SqlCheck sqljob = new SqlCheck();
                        sqljob.parseNode(childNode);
                        jdbcJobArr.add(sqljob);
                    } else {

                    }
                }
            }
        } catch (Exception e) {
            outputFrameLog(LOGMSG.JDBC_PARSE_NODE + e.getMessage());
            e.printStackTrace();
        }
    }

    public void parseNode(Node node) {
        String jdbcTask = JOM.getOuterXML(node);
        prop.put("jdbctask", jdbcTask);
    }
}
