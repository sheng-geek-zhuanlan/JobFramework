
package com.sheng.jobframework.utility.HttpClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;


/**
 * Class to provide XML/JSON utility methods
 * that allow for easy access to REST Web Services
 * Used by RestClientUtils and can also be used by test code
 */
public class XmlJsonUtils {

    /**
     * Returns stream from string
     * @param s string to convert to stream
     * @return input stream
     */
    public static InputStream getStreamFromString(String s) {
        try {
            return (new ByteArrayInputStream(s.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Exception occured so returning with default charset" +
                               e.getMessage());
            e.printStackTrace(System.out);
            return (new ByteArrayInputStream(s.getBytes()));
        }
    }

    /**
     * Converts stream to string
     * @param is input stream
     * @return String from contents of stream
     */
    public static String getStreamContent(InputStream is) throws Exception {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = is.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    /** @return formatted XML as [0] and Document as [1] */
    static Object[] parseXML(InputStream is) throws Exception {
        String content = null;
        DocumentBuilderFactory docBuilderFactory =
            DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(is);
        StringWriter stw = new StringWriter();
        TransformerFactory tf = null;
        try {
            tf = TransformerFactory.newInstance();
            tf.setAttribute("indent-number", new Integer(2));
        } catch (Exception e) {
            tf = TransformerFactory.newInstance();
            // ignore
        }
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.transform(new DOMSource(doc), new StreamResult(stw));
        content = stw.toString();
        return new Object[] { content, doc };
    }
}
