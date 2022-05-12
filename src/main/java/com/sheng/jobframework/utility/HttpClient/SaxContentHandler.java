
package com.sheng.jobframework.utility.HttpClient;

import java.io.CharArrayWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class SaxContentHandler extends DefaultHandler {

    Map<String, Rest2SoapMap> m_r2sMap;

    public SaxContentHandler(Map<String, Rest2SoapMap> r2sMap) {
        m_r2sMap = r2sMap;

        if (m_r2sMap == null) {
            m_r2sMap = new HashMap<String, Rest2SoapMap>();
        }

    }

    private Rest2SoapMap current_map;
    private String current_verb;
    private Vector current_params;
    private Rest2SoapParamMap current_param;

    private CharArrayWriter contents = new CharArrayWriter();


    // Override methods of the DefaultHandler class
    // to gain notification of SAX Events.
    //
    // See org.xml.sax.ContentHandler for all available events.
    //

    public void startDocument() throws SAXException {
        //     System.out.println( "SAX Event: START DOCUMENT" );
    }

    public void endDocument() throws SAXException {
        //     System.out.println( "SAX Event: END DOCUMENT" );
    }

    public void startElement(String namespaceURI, String localName,
                             String qName,
                             Attributes attr) throws SAXException {

        contents.reset();

        if (localName.equals("mapping")) {
            current_map = new Rest2SoapMap();
        } else if (localName.equals("restUri")) {
            current_verb = attr.getValue("verb");
        } else if (localName.equals("parameters")) {
            current_params = new Vector();
            current_map.params = current_params;
        } else if (localName.equals("parameter")) {
            current_param = new Rest2SoapParamMap();
            current_params.add(current_param);
        } else if (localName.equals("restInput")) {
            current_param.restParamType = attr.getValue("type");
        }

        /*
         System.out.println( "SAX Event: START ELEMENT[ "  + localName + " ]" );

      // Also, let's print the attributes if
      // there are any...
                for ( int i = 0; i < attr.getLength(); i++ ){
                 System.out.println( " ATTRIBUTE: "  + attr.getLocalName(i)  + " VALUE: "  + attr.getValue(i) );
      }
*/

    }

    public void endElement(String namespaceURI, String localName,
                           String qName) throws SAXException {

        if (localName.equals("restUri")) {
            String uri = contents.toString().toLowerCase();
            current_map.restVerb = current_verb;
            current_map.restUri = uri;
            m_r2sMap.put(current_verb + " " + uri, current_map);
        } else if (localName.equals("soapApi")) {
            current_map.soapApi = contents.toString();
        } else if (localName.equals("service")) {
            current_map.service = contents.toString();
        } else if (localName.equals("restOutput")) {
            current_map.restOutput = contents.toString();
        } else if (localName.equals("output")) {
            current_map.restOutput = contents.toString();
        } else if (localName.equals("restInput")) {
            current_param.restParam = contents.toString();
        } else if (localName.equals("soapInput")) {
            current_param.soapParam = contents.toString();
        }

        //     System.out.println( "SAX Event: END ELEMENT[ "  + localName + " ]" );

    }

    public void characters(char[] ch, int start,
                           int length) throws SAXException {

        contents.write(ch, start, length);
        /*
      System.out.print( "SAX Event: CHARACTERS[ " );

      try {
         OutputStreamWriter outw = new OutputStreamWriter(System.out);
         outw.write( ch, start,length );
         outw.flush();
      } catch (Exception e) {
         e.printStackTrace();
      }

      System.out.println( " ]" );
*/

    }

}
