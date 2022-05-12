package com.sheng.jobframework.jobdef.Script;

import com.sheng.jobframework.jobdom.JOM;
import com.sheng.jobframework.jobdef.ACJobAppender;

import org.w3c.dom.Node;


public class ACScriptSet extends ACJobAppender {
    public ACScriptSet() {
    }

    public void parseNode(Node node) {
        String antContent = prop.getProperty("antContent", "");
        antContent = antContent + JOM.getOuterXML(node);
        prop.put("antContent", antContent);
        //ACLibSet libset = new ACLibSet();
        //libset.addLib(libpath);
        //return libpath;
    }

    public void initialize() {
    }

    public String getAntTarget() {
        String outerxml = prop.getProperty("antContent");
        String trimedStr = outerxml.trim();
        /*
       * the xml has been removed <Ant> tag before parseNode.
      if(!trimedStr.startsWith("<Ant")&&(!trimedStr.startsWith("<ANT"))){
        outputFrameLog("XXXX-No parent job "+outerxml);
        outputFrameLog("*****Ant job apppended!");
        outerxml = "<Ant>"+outerxml+"</Ant>";
      }
      */
        // return (String)prop.get("antContent");
        return outerxml;
    }
}
