package com.sheng.jobframework.jobdef.IOS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class fileGene extends Utilities {
    public fileGene() {
    }

    public int transGenerate(String path_param, String path_xlif,
                             String path_trans) {

        try {
            int co_trans, co_xlif, tp_last = 0;
            String source = "", target = "";
            Node param, xlif;
            Element node;

            // ~ create translation folder ~
            if (path_trans.lastIndexOf(File.separator) > 0)
                tp_last = path_trans.lastIndexOf(File.separator);
            File file_trans = new File(path_trans);
            File file_trans_folder =
                new File(path_trans.substring(0, tp_last));
            if (!file_trans_folder.exists())
                if (!file_trans_folder.mkdirs())
                    quit(2);
            // ~ create trans file writer ~
            Writer w_file_trans =
                new BufferedWriter(new FileWriter(file_trans));

            // ~ prepare trans file ~
            XMLStreamWriter writer =
                XMLOutputFactory.newInstance().createXMLStreamWriter(w_file_trans);
            writer.writeProcessingInstruction("xml",
                                              "version=\"1.0\" encoding=\"UTF-8\"");
            writer.writeStartElement("parameters");
            writer.writeAttribute("file", "");
            writer.writeEndElement();
            writer.flush();
            writer.close();


            // ~ get params ~
            //InputStream is_param = new FileInputStream(path_param);
            //Document doc_param = dombuilder.parse(is_param);
            Document doc_param = this.getParamXMLDoc(path_param);
            NodeList list_param_id = doc_param.getElementsByTagName("param");

            // ~ get xlif ~
            //InputStream is_xlif = new FileInputStream(path_xlif);
            //Document doc_xlif = dombuilder.parse(is_xlif);
            Document doc_xlif = this.getParamXMLDoc(path_xlif);
            NodeList list_xlif_id =
                doc_xlif.getElementsByTagName("trans-unit");

            // ~ set translation ~
            //InputStream is_trans = new FileInputStream(path_trans);
            //Document doc_trans = dombuilder.parse(is_trans);
            Document doc_trans = this.getParamXMLDoc(path_trans);

            // ~ write and create ~
            co_trans = 0;
            while (co_trans < list_param_id.getLength()) {
                // ~ create new node ~
                param = list_param_id.item(co_trans++);
                node = doc_trans.createElement("param");
                // ~ get trans-unit source from params ~
                source =
                        param.getTextContent(); //.getAttributes().getNamedItem("name").getNodeValue();

                // ~ get target content from xliff by trans-unit source ~
                co_xlif = 0;
                while (co_xlif < list_xlif_id.getLength()) { //while
                    Node temp;
                    boolean find = false;

                    xlif = list_xlif_id.item(co_xlif++);
                    for (temp = xlif.getFirstChild(); temp != null;
                         temp = temp.getNextSibling()) { // for
                        if (temp.getNodeType() == Node.ELEMENT_NODE) {
                            // ~ if unit source, equals the source string ~
                            if (temp.getNodeName().equals("source")) {
                                if (source.equals(temp.getTextContent().toString()))
                                    find = true;
                            } else if (find)
                                if (temp.getNodeName().equals("target")) {
                                    target = temp.getTextContent();
                                    break; // from for
                                }
                        }
                    } //for
                    // ~ find target ~
                    if (temp != null)
                        break; // from while
                } //while
                // ~ did not find ~
                if (co_xlif == list_xlif_id.getLength())
                    //if(tag_runMain)
                    //target = "errrrr";
                    target =
                            this.getACfgParam("tag_check_errMsg"); //quit(2, source);
                // ~ set trans value ~
                node.setTextContent(target);
                node.setAttribute("name", source);
                doc_trans.getDocumentElement().appendChild(node);
            }

            // ~ write the content into xml file ~
            TransformerFactory transformerFactory =
                TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc_trans);
            StreamResult result = new StreamResult(new File(path_trans));
            transformer.transform(domSource, result);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    public int runtimeGenerate(iphoneAuto amelia, String path_xml) {
        try {
            int co_xml = 0;
            Node param;
            String title, content;

            Document doc_xml = this.getParamXMLDoc(path_xml);
            NodeList list_xml_id = doc_xml.getElementsByTagName("param");

            while (co_xml < list_xml_id.getLength()) {
                param = list_xml_id.item(co_xml++);
                title =
                        param.getAttributes().getNamedItem("name").getNodeValue();
                // ~ .... get some one else's param, it should be transformed ~
                if (title.startsWith("value_"))
                    title = title.replaceFirst("value_", "");
                //content = amelia.getEnvParam(title, this.getACfgParam("tag_check_errMsg"));
                content = amelia.getEnvParam(title);
                // ~ if param has "const_", do nothing ~
                if (!title.startsWith("const_"))
                    param.setTextContent(content);
                print("title: " + title);
                print("content: " + param.getTextContent());

            }
            // ~ write the content into xml file ~
            TransformerFactory transformerFactory =
                TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc_xml);
            StreamResult result = new StreamResult(new File(path_xml));
            transformer.transform(domSource, result);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }


    /**
     * Name: fileInterpret
     * Usages: Find the strings(name) in path_target, replace them with the value which from path_xml.
     *        e.g.  target "XX amelia XX",  xml "<param name="amelia">Leader</param>"
     *           => target "XX Leader XX"
     * Prerequisite: The strings to be interpreted in path_target file,
     *               should have the same title with the param name in path_xml.
     *
     * @param    path_target   The testcase javascript file, full path
     * @param    path_xml      The runtime param xml file, full path
     * @return   int      1/0
     * @author  amelia.han@oracle.com
     */
    public int fileInterpret(String path_target, String path_xml) {
        try {
            int tp_last = 0;
            if (path_target.lastIndexOf(File.separator) > 0)
                tp_last = path_target.lastIndexOf(File.separator);

            // ~ create new file with title: "runtime_xxx.js" ~
            String path_runtime =
                path_target.substring(0, tp_last + 1) + "runtime_" +
                path_target.substring(tp_last + 1, path_target.length());
            File file_runtime = new File(path_runtime);
            if (!file_runtime.exists())
                if (!file_runtime.createNewFile())
                    quit(2);
            // ~ read runtime xml file ~
            Document doc_xml = this.getParamXMLDoc(path_xml);
            NodeList list_xml_id = doc_xml.getElementsByTagName("param");
            Node xml_id;
            // ~ read original js file ~
            BufferedReader reader_target =
                new BufferedReader(new InputStreamReader(new FileInputStream(path_target),
                                                         "UTF-8"));
            String line, name, content;
            // ~ create new file ~
            BufferedWriter w_file_runtime =
                new BufferedWriter((new OutputStreamWriter(new FileOutputStream(path_runtime),
                                                           "UTF-8")));
            // ~ start ~
            print("   **Amelia: interpret file:" + path_target + ", to be:" +
                  path_runtime);
            line = reader_target.readLine();
            while (line != null) {
                for (int i = 0; i < list_xml_id.getLength(); i++) {
                    xml_id = list_xml_id.item(i);
                    name =
xml_id.getAttributes().getNamedItem("name").getNodeValue();
                    content = xml_id.getTextContent();
                    if (line.contains(name)) {
                        String temp = "";
                        String[] split = line.split(name);
                        String char_before = " [(+.*;";
                        String char_after = " .*+;)]";

                        // ~ 1. "ameliaXXameliaOOamelia", 2. "XXameliaOOamelia" ~
                        // ~ starts ~ : .split, if start with name, the split[0] == ""
                        temp = split[0];
                        // ~ others ~
                        if (split.length > 1) {
                            for (int ii = 1; ii < split.length; ii++) {
                                if ((temp.equals("") ||
                                     char_before.contains(String.valueOf(temp.charAt(temp.length() -
                                                                                     1)))) &&
                                    char_after.contains(String.valueOf(split[ii].charAt(0)))) {
                                    // ~ only add "" for the ones starts with "value_" ~
                                    if (name.startsWith("value_"))
                                        content = "\"" + content + "\"";
                                    temp = temp + content + split[ii];
                                } else
                                    temp = temp + name + split[ii];
                            }
                        }
                        // ~ present ~
                        if (!line.equals(temp)) {
                            print(" *before: " + line);
                            print("after: " + temp);
                        }
                        line = temp;
                    }
                }
                w_file_runtime.write(line + "\r\n");
                line = reader_target.readLine();
            }
            w_file_runtime.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    /*
  public static void main(String[] args)
  {
      fileGene gogo = new fileGene();
      gogo.transGenerate("C:\\JDeveloper\\Auto_iphone\\data\\param\\communicator\\communicator_param_check.xml",
                        "C:\\JDeveloper\\Auto_iphone\\data\\xlif\\communicator\\oracle.ocs.mobileclient.iphone.communicator_zh_CN.xlf",
                        "C:\\JDeveloper\\Auto_iphone\\data\\param\\communicator\\kd.xml");
  }
  */
}
