

package com.sheng.jobframework.utility.HttpClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class Rest2Soap {

    private static final String MAPFILE = "mappingList.txt";

    public Rest2Soap() throws Exception {
    }

    static Map<String, Rest2SoapMap> r2sMap =
        new HashMap<String, Rest2SoapMap>();

    static {

        //   try {
        // Create SAX 2 parser...
        XMLReader xr = null;
        try {
            xr = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException("SAXException encountered when creating XMLReader");
        }

        // Set the ContentHandler...
        xr.setContentHandler(new SaxContentHandler(r2sMap));

        //String[] mapFiles = getResourceListing(Rest2Soap.class,path);
        String[] mapFiles = null;
        try {
            mapFiles = getMappingFiles();
        } catch (Rest2SoapException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException("Rest2SoapException encountered when retrieving mapping files");
        } catch (IOException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException("IOException encountered when retrieving mapping files");
        }

        for (String f : mapFiles) {
            // Parse the file...
            if (f.endsWith("xml")) {
                try {
                    System.out.println("\nProcessing mapping file " + f);
                    xr.parse(new InputSource(Rest2Soap.class.getClassLoader().getResourceAsStream(f)));
                    System.out.println("DONE");
                } catch (SAXException e) {
                    System.out.println("\nSAXException encountered when parsing mapping file " +
                                       f + "\n");
                    e.printStackTrace(System.out);
                    //throw new RuntimeException("SAXException encountered when parsing mapping file " + f);
                } catch (IOException e) {
                    System.out.println("\nIOException encountered when parsing mapping file " +
                                       f + "\n");
                    e.printStackTrace(System.out);
                    //throw new RuntimeException("IOException encountered when parsing mapping file " + f);
                }
            }
        }
    }

    private static String[] getMappingFiles() throws Rest2SoapException,
                                                     IOException {
        InputStream mis =
            Rest2Soap.class.getClassLoader().getResourceAsStream(MAPFILE);
        if (mis == null) {
            throw new Rest2SoapException("Mapping List not found, cannot proceed");
        }
        InputStreamReader isr = new InputStreamReader(mis);
        BufferedReader br = new BufferedReader(isr);
        String line;
        Set<String> result = new HashSet<String>();
        while ((line = br.readLine()) != null) {
            result.add(line);
        }
        br.close();
        isr.close();
        mis.close();

        return result.toArray(new String[result.size()]);
    }


    /*
   * List directory contents for a resource folder. Not recursive.
   * This is basically a brute-force implementation.
   * Works for regular files and also JARs.
   *
   * @author Greg Briggs
   * @param clazz Any java class that lives in the same place as the resources you want.
   * @param path Should end with "/", but not start with one.
   * @return Just the name of each member item, not the full paths.
   * @throws URISyntaxException
   * @throws IOException
  private static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
      URL dirURL = clazz.getClassLoader().getResource(path);
      if (dirURL != null && dirURL.getProtocol().equals("file")) {
        // A file path: easy enough
        return new File(dirURL.toURI()).list();
      }

      if (dirURL == null) {

        //  In case of a jar file, we can't actually find a directory.
        //  Have to assume the same jar as clazz.

        String me = clazz.getName().replace(".", "/")+".class";
        dirURL = clazz.getClassLoader().getResource(me);
      }

      System.out.println("Dir: " + dirURL.toString());
      System.out.println("Dir Protocol: " + dirURL.getProtocol());
      if ((dirURL.getProtocol().equals("jar")) || (dirURL.getProtocol().equals("zip"))) {
        // A JAR path
        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
        while(entries.hasMoreElements()) {
          String name = entries.nextElement().getName();
          if (name.startsWith(path)) { //filter according to the path
            String entry = name.substring(path.length());
            int checkSubdir = entry.indexOf("/");
            if (checkSubdir >= 0) {
              // if it is a subdirectory, we just return the directory name
              entry = entry.substring(0, checkSubdir);
            }
            result.add(entry);
          }
        }
        return result.toArray(new String[result.size()]);
      }

      throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
  }
   */


    protected static void r2s(SoapClientUtils scu, String verb, String mauiUrl,
                              String mauiQueryParams, Object payload,
                              String uploadScopeId,
                              Map<String, Map<String, Object>> uploadedContent) throws Exception {

        if (uploadScopeId != null) {
            //setContent(payload,uploadScopeId,uploadedContent);
        }
        r2s(scu, verb, mauiUrl, mauiQueryParams, payload);
    }

    /*
  private static void setContent(Object payload,String uploadScopeId,Map<String,Map<String,Object>> uploadedContent) throws Exception{
    ContentUpdater cu = null;
    if (payload instanceof DocumentCreator) {
      DocumentUpdater du = ((DocumentCreator)payload).getUpdater();
      cu = getDocumentContentUpdater(du);
      setContentNoContentStreamId(cu,uploadScopeId,uploadedContent);
    } else if (payload instanceof DocumentUpdater) {
      cu = getDocumentContentUpdater((DocumentUpdater)payload);
      setContentNoContentStreamId(cu,uploadScopeId,uploadedContent);
    } else if (payload instanceof WikiPageCreator) {
      WikiPageUpdater wu = ((WikiPageCreator)payload).getUpdater();
      cu = getWikiPageContentUpdater(wu);
      setContentNoContentStreamId(cu,uploadScopeId,uploadedContent);
    } else if (payload instanceof WikiPageUpdater) {
      cu = getWikiPageContentUpdater((WikiPageUpdater)payload);
      setContentNoContentStreamId(cu,uploadScopeId,uploadedContent);
    } else if (payload instanceof EmailMessageContentUpdater) {
      cu = ((EmailMessageContentUpdater)payload).getBodyUpdater();
      ((EmailMessageContentUpdater)payload).setBodyUpdater(setContentRemoveContentStreamId(cu,uploadScopeId,uploadedContent));
    }
  }


  private static  void setContentNoContentStreamId(ContentUpdater cu,String uploadScopeId,Map<String,Map<String,Object>> uploadedContent) throws Exception {

    if (cu != null) {
      if (cu instanceof IdentifiableSimpleContentUpdater) {
        byte[] bytes = Rest2Soap.getContentBytes(uploadScopeId,null,uploadedContent);
        if (bytes != null) {
          ((IdentifiableSimpleContentUpdater)cu).setContentStream(bytes);
        }
      }
    }
  }


  private static  ContentUpdater setContentRemoveContentStreamId(ContentUpdater cu,String uploadScopeId,Map<String,Map<String,Object>> uploadedContent) throws Exception {
    ContentUpdater cuNew = null;
    if (cu != null) {
      if (cu instanceof MultiContentUpdater) {
        cuNew = setContentMultiContentUpdater((MultiContentUpdater)cu,uploadScopeId,uploadedContent);
      } else if (cu instanceof StreamedSimpleContentUpdater) {
        String csId = ((StreamedSimpleContentUpdater)cu).getContentStreamId();
        byte[] bytes = Rest2Soap.getContentBytes(uploadScopeId,csId,uploadedContent);
        cuNew = createStreamedSimpleContentUpdater((StreamedSimpleContentUpdater)cu);
        if (bytes != null) {
          ((StreamedSimpleContentUpdater)cuNew).setContentStream(bytes);
        }
      }
    }
    return cuNew;
  }

  private static MultiContentUpdater setContentMultiContentUpdater(MultiContentUpdater mu,String uploadScopeId,Map<String,Map<String,Object>> uploadedContent) throws Exception {
    MultiContentUpdater muNew = null;
    if (mu != null) {
      MultiContentListUpdater mclu = mu.getParts();
      List<ContentUpdater> cuList = mclu.getAdd();
      muNew = new MultiContentUpdater();
      if (mu.getMultiContentType() != null) {
        muNew.setMultiContentType(mu.getMultiContentType());
      }
      MultiContentListUpdater mcluNew = new MultiContentListUpdater();
      muNew.setParts(mcluNew);
      List<ContentUpdater> cuListNew = mcluNew.getAdd();
      for (ContentUpdater cu : cuList) {
        if (cu instanceof StreamedSimpleContentUpdater) {
          String csId = ((StreamedSimpleContentUpdater)cu).getContentStreamId();
          byte[] bytes = Rest2Soap.getContentBytes(uploadScopeId,csId,uploadedContent);
          StreamedSimpleContentUpdater sscu = createStreamedSimpleContentUpdater((StreamedSimpleContentUpdater)cu);
          if (bytes != null) {
            sscu.setContentStream(bytes);
          }
          cuListNew.add(sscu);
        } else if (cu instanceof MultiContentUpdater) {
          MultiContentUpdater mcu = setContentMultiContentUpdater((MultiContentUpdater)cu,uploadScopeId,uploadedContent);
          cuListNew.add(mcu);
        } else {
          throw new Rest2SoapException("Unsupported ContentUpdater, please contact MAUI QA Lead");
        }
      }
    }
    return muNew;
  }


  private static StreamedSimpleContentUpdater createStreamedSimpleContentUpdater(StreamedSimpleContentUpdater orig) {
    StreamedSimpleContentUpdater ret = new StreamedSimpleContentUpdater();

    // temporary fix - check each known property - set on new StreamedSimpleContentUpdater each property that is set on original StreamedSimpleContentUpdater
    // permanent fix, to do - use Java Reflection, so updates on StreamedSimpleContentUpdater will be transparent

    if (orig.getCharacterEncoding() != null) {
      ret.setCharacterEncoding(orig.getCharacterEncoding());
    }

    if (orig.getContentDisposition() != null) {
      ret.setContentDisposition(orig.getContentDisposition());
    }

    if (orig.getContentEncoding() != null) {
      ret.setContentEncoding(orig.getContentEncoding());
    }

    if (orig.getContentId() != null) {
      ret.setContentId(orig.getContentId());
    }

    if (orig.getContentLanguage() != null) {
      ret.setContentLanguage(orig.getContentLanguage());
    }

    if (orig.getMediaType() != null) {
      ret.setMediaType(orig.getMediaType());
    }

    if (orig.getMimeHeaders() != null) {
      ret.setMimeHeaders(orig.getMimeHeaders());
    }

    if (orig.getName() != null) {
      ret.setName(orig.getName());
    }

    return ret;
  }


  private static ContentUpdater getDocumentContentUpdater(DocumentUpdater du) {
    ContentUpdater cu = null;
    if (du != null) {
      cu = du.getContentUpdater();
    }
    return cu;
  }


  private static ContentUpdater getWikiPageContentUpdater(WikiPageUpdater wu) {
    ContentUpdater cu = null;
    if (wu != null) {
      cu = wu.getUpdatedBody();
    }
    return cu;
  }


  private static byte[] getContentBytes(String uploadScopeId,String contentId,Map<String,Map<String,Object>> uploadedContent) throws Exception{

    Map<String,Object> contentIdMap = uploadedContent.get(uploadScopeId);
    if (contentIdMap == null) {
      throw new Rest2SoapException("Upload Scope " + uploadScopeId + " is not found");
    }

    Object co = null;
    byte[] bytes = null;
    if (contentId == null) {
      co = contentIdMap.values().iterator().next();
      if (co == null) {
        //throw new Rest2SoapException("No Content available");
        System.out.println("\n NO STREAMED DATA for uploadscope " + uploadScopeId);
      }
    } else {
      co = contentIdMap.get(contentId);
      if (co == null) {
        //throw new Rest2SoapException("Content " + contentId + " is not found");
        System.out.println("\n NO STREAMED DATA for uploadscope " + uploadScopeId + " contentId " + contentId);
      }
    }

    if (co != null) {
      if (!(co instanceof InputStream)) {
        throw new Rest2SoapException("Content is not an instance of InputStream");
      }

      InputStream is = (InputStream)co;

      // Get the size of the InputStream
      long length = is.available();

      // Create the byte array to hold the data
      bytes = new byte[(int)length];

      // Read in the bytes
      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
        offset += numRead;
      }

      // Ensure all the bytes have been read in
      if (offset < bytes.length) {
        throw new Rest2SoapException("Could not completely read InputStream");
      }

      // Close the input stream and return bytes
      is.close();
    }
    return bytes;
  }

*/

    protected static void r2s(SoapClientUtils scu, String verb, String mauiUrl,
                              String mauiQueryParams,
                              Object payload) throws Exception {
        SoapServerResponse ssr = new SoapServerResponse();

        if ((mauiUrl == null) || (mauiUrl.trim().equals(""))) {
            throw new Rest2SoapException("URI is null or empty string");
        }

        //System.out.println("Maui Url: " + mauiUrl);

        mauiUrl = mauiUrl.toLowerCase();

        String[] split = splitId(mauiUrl);
        String uri = split[0];
        String id = split[1];
        if ((uri == null) || (uri.trim().equals(""))) {
            throw new Rest2SoapException("URI is invalid");
        }

        if ((id != null) && (id.length() > 0)) {
            uri = uri + "/{id}";
        }

        String r2sKey = verb + " " + uri;

        Rest2SoapMap r2sm = r2sMap.get(r2sKey);
        if (r2sm == null) {
            throw new Rest2SoapException("Rest2Soap Mapping for " + r2sKey +
                                         " does not exist");
        }

        Object[] paramObjects = null;
        Class[] paramTypes = null;
        if (r2sm.params != null) {
            int i = r2sm.params.size();
            paramObjects = new Object[i];
            paramTypes = new Class[i];
            int j = 0;
            Iterator it = r2sm.params.iterator();
            while (it.hasNext()) {
                Rest2SoapParamMap r2sp = (Rest2SoapParamMap)(it.next());
                paramTypes[j] = Class.forName(r2sp.soapParam);
                Object o = null;
                if (r2sp.restParamType.equals("parameter")) {
                    if (r2sp.restParam.equals("projection")) {
                        //o = getProjection(mauiQueryParams);
                    } else if (r2sp.restParam.equals("sent_folder")) {
                        //o = getBeeId(getParamValue(mauiQueryParams,"sent_folder"));
                    } else if (r2sp.restParam.equals("labelid")) {
                        //o = getBeeId(getParamValue(mauiQueryParams,"labelid"));
                    } else if (r2sp.restParam.equals("type")) {
                        //o = getLabelApplicationType(mauiQueryParams);
                    } else if (r2sp.restParam.equals("member_update_mode")) {
                        //o = getMemberUpdateMode(mauiQueryParams);
                    } else if (r2sp.restParam.equals("status")) {
                        //o = getConnectionStatus(mauiQueryParams);
                    } else if (r2sp.restParam.equals("parent")) {
                        //o = getBeeId(getParamValue(mauiQueryParams,"parent"));
                    } else if (r2sp.restParam.equals("visibility")) {
                        //o = getRelationshipVisibility(mauiQueryParams);
                    } else if (r2sp.soapParam.equals("java.lang.String")) {
                        //o = getParamValue(mauiQueryParams,r2sp.restParam);
                    } else {
                        throw new Rest2SoapException("Unsupported REST query parameter: " +
                                                     r2sp.restParam +
                                                     ".  Please contact MAUI QA lead");
                    }
                } else if (r2sp.restParamType.equals("id")) {
                    //o = getBeeId(id);
                } else if (r2sp.restParamType.equals("payload")) {
                    /*
          if (payload instanceof BeeList) {
            o = ((BeeList)payload).getElements();
          } else if (payload instanceof BeeIdList) {
            o = ((BeeIdList)payload).getBeeId();
          } else {
            o = payload;
          }
          */
                } else {
                    throw new Rest2SoapException("Invalid restInput type: " +
                                                 r2sp.restParamType);
                }
                paramObjects[j] = o;
                j++;
            }
            //r2sm.printValues();
        }

        scu.sendSoapRequest(r2sm.service, r2sm.soapApi, paramTypes,
                            paramObjects);

        //Update SoapServerResponse ResponseObject if corresponding REST output is BeeList or BeeIdList
        Object soapRet = scu.getSoapServerResponse().getResponseObject();
        if (soapRet != null) {
            if ((r2sm.restOutput.equals("com.oracle.beehive.BeeList")) &&
                (soapRet instanceof ArrayList)) {
                //scu.replaceArrayListWithBeeList((ArrayList)soapRet);
            } else if ((r2sm.restOutput.equals("com.oracle.beehive.BeeIdList")) &&
                       (soapRet instanceof ArrayList)) {
                //scu.replaceArrayListWithBeeIdList((ArrayList)soapRet);
            }
        }
    }


    protected static String[] splitId(String uri) {
        String[] split = new String[2];
        int last = uri.lastIndexOf("/");
        if (last == -1) {
            split[0] = uri;
            split[1] = null;
        } else {
            String splitA = uri.substring(0, last);
            String splitB = uri.substring(last + 1);
            if (splitB.contains(":")) {
                split[0] = splitA;
                split[1] = splitB;
            } else {
                split[0] = uri;
                split[1] = null;
            }
        }
        //System.out.println("URI: " + split[0]);
        //System.out.println("id: " + split[1]);
        return split;
    }

    /*
  protected static BeeId getBeeId(String id) {
   BeeId b = new BeeId();
   b.setId(id);
   return b;
  }


  private static Projection getProjection(String mauiQueryParams) {
    Projection proj = null;
    String projStr = getParamValue(mauiQueryParams,"projection");
    if ((projStr != null) && (projStr.trim().length() > 0)) {
      proj = new Projection();
      proj.setValue(projStr);
    }
    return proj;
  }


  private static LabelApplicationType getLabelApplicationType(String mauiQueryParams) throws Rest2SoapException {
    LabelApplicationType lat = null;
    String latStr = getParamValue(mauiQueryParams,"type").toLowerCase();
    if ((latStr != null) && (latStr.trim().length() > 0)) {
      if (latStr.equals("public")) {
        lat = LabelApplicationType.PUBLIC;
      } else if (latStr.equals("private")) {
        lat = LabelApplicationType.PRIVATE;
      } else {
        throw new Rest2SoapException("Invalid value for Label Application Type, cannot convert call to SOAP");
      }
    }
    return lat;
  }


  private static MemberUpdateMode getMemberUpdateMode(String mauiQueryParams) throws Rest2SoapException {
    MemberUpdateMode mum = null;
    String mumStr = getParamValue(mauiQueryParams,"member_update_mode").toLowerCase();
    if ((mumStr != null) && (mumStr.trim().length() > 0)) {
      if (mumStr.equals("abort_on_error")) {
        mum = MemberUpdateMode.ABORT_ON_ERROR;
      } else if (mumStr.equals("ignore_errors")) {
        mum = MemberUpdateMode.IGNORE_ERRORS;
      } else {
        throw new Rest2SoapException("Invalid value for MemberUpdateMode, cannot convert call to SOAP");
      }
    }
    return mum;
  }


  private static ConnectionStatus getConnectionStatus(String mauiQueryParams) throws Rest2SoapException {
    ConnectionStatus cs = null;
    String csStr = getParamValue(mauiQueryParams,"status").toLowerCase();
    if ((csStr != null) && (csStr.trim().length() > 0)) {
      if (csStr.equals("approved")) {
        cs = ConnectionStatus.APPROVED;
      } else if (csStr.equals("pending")) {
        cs = ConnectionStatus.PENDING;
      } else if (csStr.equals("rejected")) {
        cs = ConnectionStatus.REJECTED;
      } else {
        throw new Rest2SoapException("Invalid value for ConnectionStatus, cannot convert call to SOAP");
      }
    }
    return cs;
  }


  private static RelationshipVisibility getRelationshipVisibility(String mauiQueryParams) throws Rest2SoapException {
    RelationshipVisibility rv = null;
    String rvStr = getParamValue(mauiQueryParams,"visibility").toLowerCase();
    if ((rvStr != null) && (rvStr.trim().length() > 0)) {
      if (rvStr.equals("private")) {
        rv = RelationshipVisibility.PRIVATE;
      } else if (rvStr.equals("protected")) {
        rv = RelationshipVisibility.PROTECTED;
      } else if (rvStr.equals("public")) {
        rv = RelationshipVisibility.PUBLIC;
      } else {
        throw new Rest2SoapException("Invalid value for RelationshipVisibility, cannot convert call to SOAP");
      }
    }
    return rv;
  }

*/

    protected static String getParamValue(String mauiQueryParams,
                                          String param) {
        String str = null;

        if ((mauiQueryParams != null) && (mauiQueryParams.contains(param))) {
            str =
mauiQueryParams.substring(mauiQueryParams.indexOf(param + "=") +
                          param.length() + 1);
            if (str.contains("&")) {
                str = str.substring(0, str.indexOf("&"));
            }
        }

        //System.out.println("Parameter: " + param + " Value: " + str);
        return str;

    }

}

