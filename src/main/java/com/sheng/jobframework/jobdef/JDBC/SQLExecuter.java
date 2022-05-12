package com.sheng.jobframework.jobdef.JDBC;

import com.sheng.jobframework.utility.FileUtil;
import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobDOM;

import java.io.FileInputStream;
import java.io.InputStream;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SQLExecuter extends JDBCJob {
    ArrayList sqlFileArr = new ArrayList(100);
    ArrayList sqlLineArr = new ArrayList(100);
    public static String SQL_FILE = "SQLFile";
    public static String SQL_LINE = "SQLLine";
    //Connection conn;

    public SQLExecuter() {
    }

    public void parseEnv2Job(Properties prop) {

    }

    public void runJDBCJob() {
        try {
            int fileSize = sqlFileArr.size();
            for (int i = 0; i < fileSize; i++) {
                String sqlFile = (String)sqlFileArr.get(i);
                runSqlFile(sqlFile);
            }
            int lineSize = sqlLineArr.size();
            for (int i = 0; i < lineSize; i++) {
                String sqlLine = (String)sqlLineArr.get(i);
                runSqlLine(sqlLine);
            }
        } catch (Exception e) {
            outputFrameLog(LOGMSG.JDBC_LOADDATA_EXCEP + e.getMessage());
            e.printStackTrace();
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
                if (arg.getNodeName().endsWith(SQL_FILE)) {
                    String sqlfile = arg.getTextContent();
                    sqlFileArr.add(sqlfile);
                }
                if (arg.getNodeName().endsWith(SQL_LINE)) {
                    String sqlline = arg.getTextContent();
                    sqlLineArr.add(sqlline);
                }
            }
        }
    }

    private void runSqlLine(String sqlline) {
        try {
            reportPass("is running sql line " + sqlline);
            sqlline = sqlline.replace(";", "");
            Statement stmt = null;
            stmt = conn.createStatement();
            if ((sqlline.startsWith("insert")) ||
                sqlline.startsWith("update") || sqlline.startsWith("delete")) {
                //this is a ddl sql
                //stmt.addBatch(sqlline);
                stmt.executeUpdate(sqlline);
                int[] rows = stmt.executeBatch();
            } else {
                ResultSet resultset = stmt.executeQuery(sqlline);

            }

        } catch (Exception e) {
            reportFail(LOGMSG.JDBC_LOADDATA_EXCEP + sqlline + e.getMessage());
            e.printStackTrace();
        }
    }

    private List loadSqlFile(String sqlFile) throws Exception {
        List sqlList = new ArrayList();
        try {
            outputFrameLog("is loading sql file " + sqlFile);
            sqlFile = FileUtil.getAbsolutePath(sqlFile);
            InputStream sqlFileIn = new FileInputStream(sqlFile);
            StringBuffer sqlSb = new StringBuffer();
            byte[] buff = new byte[1024];
            int byteRead = 0;
            //outputFrameLog("2 Trying to run sql file "+sqlFile);
            while ((byteRead = sqlFileIn.read(buff)) != -1) {
                sqlSb.append(new String(buff, 0, byteRead));
            }
            // Windows 下换行是 \r\n, Linux 下是 \n
            // String[] sqlArr = sqlSb.toString().split("(;\\s*\r\n)(;\\s*\n)");
            String[] sqlArr = sqlSb.toString().split(";");
            for (int i = 0; i < sqlArr.length; i++) {
                String sql = sqlArr[i].replaceAll("--.*", "").trim();
                if (!sql.equals("")) {
                    //outputFrameLog("Trying to run add sql line "+sql);
                    sqlList.add(sql);
                }
            }
            outputFrameLog("succeed to load sql file " + sqlFile);
            return sqlList;

        } catch (Exception ex) {
            reportFail(LOGMSG.JDBC_LOADDATA_EXCEP + ex.getMessage());
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        }
    }

    /**
     *
     *
     * @param conn connection
     * @param sqlFile SQL sqlfile
     * @throws Exception
     */
    public void runSqlFile(String sqlFile) throws Exception {
        Statement stmt = null;
        List sqlList = loadSqlFile(sqlFile);
        //outputFrameLog("has loaded sql file "+sqlFile);
        stmt = conn.createStatement();
        int isqls = sqlList.size();
        for (int i = 0; i < isqls; i++) {
            String sql = (String)sqlList.get(i);
            stmt.addBatch(sql);
            //outputFrameLog("Trying to execute sql line "+sql);
        }
        int[] rows = stmt.executeBatch();
        outputFrameLog(LOGMSG.JDBC_SUCCESS_LOADDATA + Arrays.toString(rows));
    }

    public void connectToDB() {
    }
}
