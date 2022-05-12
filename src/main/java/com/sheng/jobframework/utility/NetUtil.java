package com.sheng.jobframework.utility;


import com.sheng.jobframework.jobdom.ACElement;
import frameservice.ACFrameService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.InetAddress;
import java.net.Socket;


public class NetUtil extends ACElement {
    private Socket client;

    public NetUtil() {
        super();
    }

    public static void sendCommand(String host, int port,
                                   String cmd) throws Exception {
        //try{
        Socket client = new Socket(InetAddress.getByName(host), port);
        if (!client.isConnected()) {
            //return false;
        }
        System.out.println("[info] Connect to " + host + " successful.");
        BufferedReader systemIn =
            new BufferedReader(new InputStreamReader(System.in));
        BufferedInputStream bis =
            new BufferedInputStream(client.getInputStream());
        BufferedOutputStream bos =
            new BufferedOutputStream(client.getOutputStream());
        String command = ACFrameService.PREFIX_FRAMECOMMAND + cmd;
        bos.write((command).getBytes());
        bos.flush();
        client.close();
        /*}catch(Exception e){
          System.out.println("XXXX-Exception occurs in NetUtil: "+e.getMessage());
          e.printStackTrace();
      }*/
    }
}
