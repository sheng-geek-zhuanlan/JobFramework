package com.sheng.jobframework.jobdef.JDBC;

import com.sheng.jobframework.annotation.LOGMSG;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.ACJobEngine;
import com.sheng.jobframework.jobdef.ACTestEnv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Properties;


public class JDBCEngine extends ACJobEngine {
    public JDBCEngine() {
    }

    public void runEntityJob() {
        ACJDBCSet jdbcset =
            (ACJDBCSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_JDBC);
        ACTestEnv testenv =
            (ACTestEnv)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_ENV);
        Properties envprop = testenv.getEnvSetting();
        ArrayList djdbcJobArr = jdbcset.getJDBCJobArr();
        Connection conn = setupDBConnection();
        if (conn == null) {
            TestJobElement connJob = new TestJobElement();
            connJob.setName("DB Connect");
            TestJobElement steps = new TestJobElement(TestJobType.STEP);
            steps.addProperty("fail", "failed to connect DB");
            connJob.setResults(TestResult.FAIL);
            connJob.addChildJob(steps);
            addChildJob(connJob);
            return;
        }
        int isize = djdbcJobArr.size();
        outputFrameLog("---Total jdbc job size is  " + isize + " --------");
        for (int i = 0; i < isize; i++) {
            JDBCJob jdbcjob = (JDBCJob)djdbcJobArr.get(i);
            outputFrameLog("---JDBC Job running " + jdbcjob.getName() +
                           " --------");
            //jdbcjob.setWorkingJobDOM(rootJob);
            jdbcjob.setConn(conn);
            jdbcjob.runJDBCJob();
            addChildJob(jdbcjob);
        }
    }

    public Connection setupDBConnection() {
        ACJDBCSet jdbcset =
            (ACJDBCSet)getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_JDBC);
        String dbhost = jdbcset.host;
        String dbport = jdbcset.port;
        String user = jdbcset.user;
        String sid = jdbcset.sid;
        String passwd = jdbcset.passwd;
        String type = jdbcset.type;
        if (type.equalsIgnoreCase(ACJDBCSet.ORACLE)) {
            try {
                outputFrameLog("Trying to setup Oracle driver");
                Class.forName("oracle.jdbc.driver.OracleDriver");
            } catch (ClassNotFoundException e) {
                outputFrameLog("Cannot find the driver: oracle.jdbc.driver.OracleDriver");
                return null;
            }
        } else if (type.equalsIgnoreCase(ACJDBCSet.DB2)) {
            try {
                outputFrameLog("Trying to setup DB2 driver");
                Class.forName("com.ibm.db2.jcc.DB2Driver");
            } catch (ClassNotFoundException e) {
                outputFrameLog("Cannot find the driver: com.ibm.db2.jcc.DB2Driver");
                return null;
            }
        } else if (type.equalsIgnoreCase(ACJDBCSet.MYSQL)) {
            outputFrameLog("Trying to setup mysql driver");
        } else {
            outputFrameLog(LOGMSG.JDBC_DRIVER_NOTSUPPORT + type);
            return null;
        }

        try {
            //String serverName = "sgttest9.us.oracle.com";
            //String portNumber = "1521";
            //String sid = "DB11G";
            String url = "";
            Connection connection = null;
            if (type.equalsIgnoreCase(ACJDBCSet.ORACLE)) {
                url = "jdbc:oracle:thin:@" + dbhost + ":" + dbport + ":" + sid;
                String username = user;
                String password = passwd;
                connection =
                        DriverManager.getConnection(url, username, password);
            } else if (type.equalsIgnoreCase(ACJDBCSet.DB2)) {
                //url ="jdbc:oracle:thin:@" + dbhost + ":" + dbport + ":" +sid;
                url = "jdbc:db2://" + dbhost + ":" + dbport + "/" + sid;
                String username = user;
                String password = passwd;
                connection =
                        DriverManager.getConnection(url, username, password);
            }

            outputFrameLog(LOGMSG.JDBC_SUCCESS_SETUPCONN);
            return connection;
        } catch (SQLException e) {
            outputFrameLog(LOGMSG.JDBC_FAIL_SETUPCONN);
            e.printStackTrace();
        }
        return null;
    }
}
