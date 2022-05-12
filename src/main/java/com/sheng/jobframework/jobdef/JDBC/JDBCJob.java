package com.sheng.jobframework.jobdef.JDBC;

import com.sheng.jobframework.jobdom.ACJavaJob;

import java.sql.Connection;

import java.util.Properties;

import org.w3c.dom.Node;


public abstract class JDBCJob extends ACJavaJob {
    public Connection conn;

    public JDBCJob() {
    }

    public void setConn(Connection connection) {
        conn = connection;
    }

    public abstract void runJDBCJob();

    public abstract void parseNode(Node node);

    public abstract void parseEnv2Job(Properties prop);

    public void Init() {

    }

    public void Run() {

    }

    public void End() {

    }
}
