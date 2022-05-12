package com.sheng.jobframework.jobdef.TestNG;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReadXML extends DataSource{
      private String xmlDataFile = "";

      public ReadXML() {

      }

      public boolean loadData(String filepath) {
          xmlDataFile = filepath;
          loadXMLFromFile();
          return true;
      }

      public ReadXML(String filepath) {

      }
      
      private boolean CheckType(String paramvalue, String paramtype){
          if (paramtype.startsWith("int")||(paramtype.startsWith("long"))){
             if (paramvalue.matches("[\\d]+")) return true;
             else return false;       
          }else if (paramtype.startsWith("double")){
              if (paramvalue.matches("[\\d.]+")) return true;
              else return false;
          }
          return true;
      }
      
      public void loadXMLFromFile(){
          Element rootElement;
          try {
              DocumentBuilderFactory factory =
                  DocumentBuilderFactory.newInstance();
              DocumentBuilder builder = factory.newDocumentBuilder();
              Document document = builder.parse(xmlDataFile);
              rootElement = document.getDocumentElement();
              NodeList dataNodeList =
                  rootElement.getElementsByTagName(DataSourceDefine.SUB_DATA_NODE);
              int datasize = dataNodeList.getLength();
              for (int i = 0; i < datasize; i++) {
                  Node datanode = dataNodeList.item(i);
                  Element ele = (Element)dataNodeList.item(i);
                  String indicator =
                      ele.getAttribute(DataSourceDefine.DATA_INDICATOR_ATTRIB);
                  NodeList paraNodeList = datanode.getChildNodes();
                  int parasize = paraNodeList.getLength();
                  //System.out.println("parasize:"+parasize);
                  Hashtable hs=new Hashtable();
                  boolean tag=false;
                  for (int j = 0; j < parasize; j++) {
                      Node paranode = paraNodeList.item(j);
                      if (paranode.getNodeType() == Node.TEXT_NODE)
                         continue;
                  
                     String paraname = paranode.getNodeName();
                     String paramvalue=paranode.getTextContent();

                     NamedNodeMap paratypes =paranode.getAttributes();
                     Node typenode=paratypes.getNamedItem(DataSourceDefine.DATA_TYPE_ATTRIB);                    
                    if( typenode==null){
                       // System.out.println("typenode is null ");
                        hs.put(paraname, paramvalue);             
                    }else{
                      String paramtype=typenode.getNodeValue();
                      if (CheckType(paramvalue,paramtype)){
                         hs.put(paraname, paramvalue);
                         // System.out.println(paraname+" "+paramvalue);
                         
                      } else {
                          tag=true;
                          System.out.println("indicator is '"+indicator+"' in file "+xmlDataFile);
                          System.out.println(paraname+"'s value '"+paramvalue +"' is not match its type '"+paramtype +"'");
                          break;
                      }
                   }
                  }
                 // System.out.println("hs.size() "+hs.size());
                  if( hs.size()==0||tag) continue;
                  testDataHashArr.put(indicator, hs);
                  
              }
          } catch (Exception e) {
              e.printStackTrace();
          }
      }

      public static void main(String[] args) {
          ReadXML readxml = new ReadXML();        
          readxml.loadData("testdata.xml");
          Hashtable datahash = readxml.testDataHashArr;
          Enumeration datakeys = datahash.keys();
          while (datakeys.hasMoreElements()) {
              String indicator = (String)datakeys.nextElement();
              System.out.println("indicator is : " + indicator);
              Hashtable hs= (Hashtable)datahash.get(indicator);
              Enumeration propkeys = hs.keys();
              while (propkeys.hasMoreElements()){
                 String propname = (String)propkeys.nextElement();
                 String propvalue =(String)hs.get(propname);
                 System.out.println("-----------get data name value: " +
                                    propname + " " + propvalue);
             }
          }
      }

    
}
