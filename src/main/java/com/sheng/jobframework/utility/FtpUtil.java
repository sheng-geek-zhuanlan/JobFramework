package com.sheng.jobframework.utility;

import com.sheng.jobframework.jobdom.ACElement;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class FtpUtil extends ACElement {
    public String ftphost = "";
    public String password = "";
    public String username = "";
    public int port = 21;
    public FTPClient ftpClient = null;
    public boolean connected = false;

    public FtpUtil() {
        super();
    }

    public FtpUtil(String hostname, int port, String userName,
                   String password) {
        this.ftphost = hostname;
        this.port = port;
        this.username = userName;
        this.password = password;
        doConnect();
    }

    public boolean doConnect() {
        boolean success = false;
        try {
            int reply;
            //ftpClient.connect(hostname,port);
            outputLog("****connecting " + ftphost + " on port " + port +
                      " with user/password:" + username + "/" + password);
            ftpClient = new FTPClient();
            ftpClient.connect(ftphost);
            ftpClient.login(username, password);
            reply = ftpClient.getReplyCode();
            outputLog("****reply code is " + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return success;
            }
            outputLog("****succeed to connect " + ftphost + " on port " +
                      port + " with user/password:" + username + "/" +
                      password);
            connected = true;
            return true;
        } catch (Exception e) {
            outputLog("****failed to connect " + ftphost + " on port " + port +
                      " with user/password:" + username + "/" + password);
            //e.printStackTrace();
            //System.out.println("faliled to connenct ftp server "+hostname);
        }
        return success;
    }

    public boolean uploadDir2FTPServer(File localfilepath,
                                       String ftpServerDir) {
        if (ftpClient == null) {
            outputLog("XXXX-ftpClient did not initialized! will NOT upload report");
            return false;
        } else if (!connected) {
            outputLog("XXXX-connect failed, will NOT upload report!");
            return false;
        }

        boolean success = false;
        try {
            //shining add 03-09 to resolve the img not open after upload
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //ftpClient.setControlEncoding("GBK");
            //end of shining
            ftpClient.makeDirectory(ftpServerDir);
            ftpClient.changeWorkingDirectory(ftpServerDir);
            if (localfilepath.exists()) {
                if (localfilepath.isDirectory()) {
                    File sourceFile[] = localfilepath.listFiles();
                    for (int i = 0; i < sourceFile.length; i++) {
                        if (sourceFile[i].exists()) {
                            if (sourceFile[i].isDirectory()) {
                                uploadDir2FTPServer(sourceFile[i],
                                                    sourceFile[i].getName());
                                ftpClient.changeToParentDirectory();
                            } else {
                                InputStream inputHtml;
                                inputHtml = new FileInputStream(sourceFile[i]);
                                ftpClient.storeFile(sourceFile[i].getName(),
                                                    inputHtml);
                                inputHtml.close();
                                outputLog("the file has been uploaded to ftp server with path: " +
                                          ftpServerDir + File.separator +
                                          sourceFile[i].getName());
                            }
                        }
                    }
                }
            } else {
                outputLog("XXX-the file not exists : " +
                          localfilepath.getAbsolutePath());
                return false;
            }
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("faliled to upload file to ftp server " +
                               localfilepath);
        }
        return success;
    }

    public static void main(String[] args) {
        FtpUtil ftpUtil = new FtpUtil();
    }
}
