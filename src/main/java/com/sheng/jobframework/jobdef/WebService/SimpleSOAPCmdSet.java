package com.sheng.jobframework.jobdef.WebService;

import com.sheng.jobframework.annotation.TestJobDOM;
import com.sheng.jobframework.jobdef.ACJobAppender;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SimpleSOAPCmdSet extends ACJobAppender {
    private static String HTTP_GET_CMD = "sendGet";
    private static String HTTP_POST_CMD = "sendPost";
    private static String HTTP_PUT_CMD = "sendPut";
    private static String HTTP_DEL_CMD = "sendDelete";
    private static String HTTP_SAVE_PARAM = "capture_response_save_param";
    private static String SAVE_PARA_NAME = "paraname";
    private static String PARA_LEFT_BOUNDARY = "LB";
    private static String PARA_RIGHT_BOUNDARY = "RB";
    private static String PARA_OCCURENCY = "OCCURENCE";

    private static String SOAP_SEND_CMD = "sendSoap";
    private static String SOAP_SESSION_CONF = "session";
    private static String WS_SESSION_WSHOST = "WSHOST";
    private static String WS_SESSION_WSPORT = "WSPORT";
    private static String WS_SESSION_WSUSER = "WSUSER";
    private static String WS_SESSION_WSPWD = "WSPWD";
    private static String CMD_URI = "URL";
    private static String CMD_PARAS = "PARAS";
    private static String PARA = "PARA";
    private static String CMD_HEADERS = "HEADERS";
    private static String HEADER = "HEADER";
    private static String CMD_HTTP_BODY = "BODY";
    private String wshost = "";
    private String wsport = "";
    private String wsuser = "";
    private String wspasswd = "";
    private String wsurlprefix = "";
    private ArrayList cmdArr = new ArrayList(100);

    public SimpleSOAPCmdSet() {
        super();
    }

    public void initialize() {

    }

    public String getWSHost() {
        if (wshost.equalsIgnoreCase("")) {
            outputFrameLog("XXXX--WARNING: the wshost is null!");
        }
        return wshost;
    }

    public String getWSPort() {
        if (wsport.equalsIgnoreCase("")) {
            outputFrameLog("XXXX--WARNING: the wsport is null!");
        }
        return wsport;
    }

    public String getWSUser() {
        if (wsuser.equalsIgnoreCase("")) {
            outputFrameLog("XXXX--WARNING: the wsuser is null!");
        }
        return wsuser;
    }

    public String getWSPwd() {
        if (wspasswd.equalsIgnoreCase("")) {
            outputFrameLog("XXXX--WARNING: the wspasswd is null!");
        }
        return wspasswd;
    }

    public ArrayList getCmdSet() {
        return cmdArr;
    }

    public void parseNode(Node node) {
        if (node.getNodeType() != Node.TEXT_NODE) {
            String nodeTag = node.getNodeName();
            Element tempele = (Element)node;
            String cmdName =
                tempele.getAttribute(TestJobDOM.node_attribute_name);
            if (nodeTag.equalsIgnoreCase(HTTP_GET_CMD)) {
                SOAPCmd getCmd = parseCMD(node);
                getCmd.setType(SOAPCmd.CMD_TYPE_GET);
                getCmd.setName(cmdName);
                cmdArr.add(getCmd);

            } else if (nodeTag.equalsIgnoreCase(HTTP_POST_CMD)) {
                SOAPCmd postCmd = parseCMD(node);
                postCmd.setType(SOAPCmd.CMD_TYPE_POST);
                postCmd.setName(cmdName);
                cmdArr.add(postCmd);

            } else if (nodeTag.equalsIgnoreCase(SOAP_SESSION_CONF)) {
                parseSessionInfo(node);

            } else if (nodeTag.equalsIgnoreCase(HTTP_PUT_CMD)) {
                SOAPCmd putCmd = parseCMD(node);
                putCmd.setType(SOAPCmd.CMD_TYPE_PUT);
                putCmd.setName(cmdName);
                cmdArr.add(putCmd);
            } else if (nodeTag.equalsIgnoreCase(HTTP_SAVE_PARAM)) {
                SOAPCmd captureCmd = parseCaptureCMD(node);
                captureCmd.setType(SOAPCmd.CMD_TYPE_PARA);
                captureCmd.setName("capture_save_param");
                cmdArr.add(captureCmd);
            } else if (nodeTag.equalsIgnoreCase(SOAP_SEND_CMD)) {
                SOAPCmd soapCmd = parseCMD(node);
                soapCmd.setType(SOAPCmd.CMD_TYPE_SOAP);
                soapCmd.setName(cmdName);
                cmdArr.add(soapCmd);
            } else {
                //outputFrameLog("XXXX-unrecoginzed TAG in WS "+nodeTag);
            }
        }
    }
    /*
     * parse the SOAPCmd
     */

    private SOAPCmd parseCMD(Node node) {
        SOAPCmd cmd = new SOAPCmd();
        if (node.hasChildNodes()) {
            NodeList cmdAtributeList = node.getChildNodes();
            int iTests = cmdAtributeList.getLength();
            for (int i = 0; i < iTests; i++) {
                Node attributeNode = cmdAtributeList.item(i);
                if (attributeNode.getNodeType() != Node.TEXT_NODE) {
                    String attrName = attributeNode.getNodeName();
                    if (attrName.equalsIgnoreCase(CMD_URI)) {
                        Element tempele = (Element)attributeNode;
                        String URIValue =
                            tempele.getAttribute(TestJobDOM.node_attribute_path);
                        cmd.setURI(URIValue);
                    } else if (attrName.equalsIgnoreCase(CMD_PARAS)) {
                        if (attributeNode.hasChildNodes()) {
                            //parse the URL paras
                            NodeList paraLists = attributeNode.getChildNodes();
                            int iParas = paraLists.getLength();
                            for (int j = 0; j < iParas; j++) {
                                Node paraNode = paraLists.item(j);
                                if (paraNode.getNodeType() != Node.TEXT_NODE) {
                                    if (paraNode.getNodeName().equalsIgnoreCase(PARA)) {
                                        Element paraEle = (Element)paraNode;
                                        String paraname =
                                            paraEle.getAttribute(TestJobDOM.node_attribute_name);
                                        String paravalue =
                                            paraNode.getTextContent();
                                        cmd.addParas(paraname, paravalue);
                                    } else {
                                        outputFrameLog("Warning the para node name was not recoginzed: " +
                                                       paraNode.getNodeName());
                                    }
                                }
                            }
                        }
                    } else if (attrName.equalsIgnoreCase(CMD_HEADERS)) {
                        if (attributeNode.hasChildNodes()) {
                            //parse the URL paras
                            NodeList headerLists =
                                attributeNode.getChildNodes();
                            int iParas = headerLists.getLength();
                            for (int j = 0; j < iParas; j++) {
                                Node headerNode = headerLists.item(j);
                                if (headerNode.getNodeType() !=
                                    Node.TEXT_NODE) {
                                    if (headerNode.getNodeName().equalsIgnoreCase(HEADER)) {
                                        Element headerEle =
                                            (Element)headerNode;
                                        String headername =
                                            headerEle.getAttribute(TestJobDOM.node_attribute_name);
                                        String headervalue =
                                            headerNode.getTextContent();
                                        cmd.addHeaders(headername,
                                                       headervalue);
                                    } else {
                                        outputFrameLog("Warning the header node name was not recoginzed: " +
                                                       headerNode.getNodeName());
                                    }
                                }
                            }
                        }
                    } else if (attrName.equalsIgnoreCase(CMD_HTTP_BODY)) {
                        String soapBody = attributeNode.getTextContent();
                        cmd.setBody(soapBody);
                    } else {
                        //outputFrameLog("XXXX--unrecoginzed attribute name in parsing command "+attrName);
                    }
                }
            }
        }
        return cmd;
    }
    /*
    *
    */

    private SOAPCmd parseCaptureCMD(Node node) {
        SOAPCmd cmd = new SOAPCmd();
        if (node.hasChildNodes()) {
            NodeList cmdAtributeList = node.getChildNodes();
            int iTests = cmdAtributeList.getLength();
            for (int i = 0; i < iTests; i++) {
                Node attributeNode = cmdAtributeList.item(i);
                if (attributeNode.getNodeType() != Node.TEXT_NODE) {
                    String attrName = attributeNode.getNodeName();
                    if (attrName.equalsIgnoreCase(SAVE_PARA_NAME)) {
                        String paraname = attributeNode.getTextContent();
                        cmd.setParaname(paraname);
                    } else if (attrName.equalsIgnoreCase(PARA_LEFT_BOUNDARY)) {
                        String lb = attributeNode.getTextContent();
                        cmd.setLB(lb);
                    } else if (attrName.equalsIgnoreCase(PARA_RIGHT_BOUNDARY)) {
                        String rb = attributeNode.getTextContent();
                        cmd.setRB(rb);
                    } else if (attrName.equalsIgnoreCase(PARA_OCCURENCY)) {
                        String occurence = attributeNode.getTextContent();
                        cmd.setOcurrence(occurence);
                    } else {
                        outputFrameLog("XXXX--unrecoginzed attribute name in parsing capture command " +
                                       attrName);
                    }
                }
            }
        }
        return cmd;
    }

    private void parseSessionInfo(Node node) {
        if (node.hasChildNodes()) {
            NodeList cmdAtributeList = node.getChildNodes();
            int iTests = cmdAtributeList.getLength();
            for (int i = 0; i < iTests; i++) {
                Node attributeNode = cmdAtributeList.item(i);
                if (attributeNode.getNodeType() != Node.TEXT_NODE) {
                    String attrName = attributeNode.getNodeName();
                    if (attrName.equalsIgnoreCase(WS_SESSION_WSHOST)) {
                        Element tempele = (Element)attributeNode;
                        String value =
                            tempele.getAttribute(TestJobDOM.node_attribute_value);
                        wshost = value;
                    } else if (attrName.equalsIgnoreCase(WS_SESSION_WSPORT)) {
                        Element tempele = (Element)attributeNode;
                        String value =
                            tempele.getAttribute(TestJobDOM.node_attribute_value);
                        wsport = value;
                    } else if (attrName.equalsIgnoreCase(WS_SESSION_WSUSER)) {
                        Element tempele = (Element)attributeNode;
                        String value =
                            tempele.getAttribute(TestJobDOM.node_attribute_value);
                        wsuser = value;
                    } else if (attrName.equalsIgnoreCase(WS_SESSION_WSPWD)) {
                        Element tempele = (Element)attributeNode;
                        String value =
                            tempele.getAttribute(TestJobDOM.node_attribute_value);
                        wspasswd = value;
                    } else {
                        outputFrameLog("XXXX--unrecoginzed attribute name in parsing session " +
                                       attrName);
                    }
                }
            }
        }
    }
}
