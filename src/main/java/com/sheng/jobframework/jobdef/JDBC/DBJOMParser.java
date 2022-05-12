package com.sheng.jobframework.jobdef.JDBC;

import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdef.ACJobAppender;
import com.sheng.jobframework.jobdef.JOMParser;

import org.w3c.dom.Node;


//import framework.JobEngine.JDBCEngine;

public class DBJOMParser extends JOMParser {
    public DBJOMParser() {
    }

    public TestJobElement loadOwnJOM(Node node) {
        TestJobElement acjob = new JDBCEngine();
        ACJDBCSet jdbcset =
            (ACJDBCSet)acjob.getAppenderInstance(ACJobAppender.APPENDER_CLASS_NAME_JDBC);
        jdbcset.parseNode(node);
        return acjob;
    }
}
