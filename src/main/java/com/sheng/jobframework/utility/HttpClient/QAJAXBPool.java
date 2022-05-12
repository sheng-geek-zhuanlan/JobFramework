

package com.sheng.jobframework.utility.HttpClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


public class QAJAXBPool {
    // The JAXB context
    private JAXBContext m_context;

    // The marshaller and unmarshaller queues
    private Queue<Marshaller> m_marshallers;
    private Queue<Unmarshaller> m_unmarshallers;

    // The singleton instance
    private static QAJAXBPool s_this;
    private static Object s_lock = new Object();

    private QAJAXBPool() throws JAXBException {
        // Create the context
        m_context =
                JAXBContext.newInstance("com.oracle.beehive:com.oracle.beehive.rest");
        m_marshallers = new ConcurrentLinkedQueue<Marshaller>();
        m_unmarshallers = new ConcurrentLinkedQueue<Unmarshaller>();
    }

    public static QAJAXBPool getInstance() throws JAXBException {
        if (s_this == null) {
            synchronized (s_lock) {
                if (s_this == null) {
                    s_this = new QAJAXBPool();
                }
            }
        }
        return s_this;
    }

    public Marshaller getMarshaller() throws JAXBException {
        Marshaller m = m_marshallers.poll();
        if (m == null) {
            m = m_context.createMarshaller();
        }
        return m;
    }

    public Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller um = m_unmarshallers.poll();
        if (um == null) {
            um = m_context.createUnmarshaller();
        }
        return um;
    }

    public void putMarshaller(Marshaller m) {
        m_marshallers.add(m);
    }

    public void putUnmarshaller(Unmarshaller um) {
        m_unmarshallers.add(um);
    }

    public static InputStream marshallXML(Object o) throws Exception {
        return marshallXML(o, true);
    }

    public static InputStream marshallXML(Object o,
                                          boolean showRawXml) throws Exception {
        Marshaller m = QAJAXBPool.getInstance().getMarshaller();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshallXML(o, baos, showRawXml);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            return new ByteArrayInputStream(baos.toByteArray());
        } finally {
            QAJAXBPool.getInstance().putMarshaller(m);
        }
    }

    public static void marshallXML(Object o,
                                   ByteArrayOutputStream out) throws Exception {
        marshallXML(o, out, true);
    }

    public static void marshallXML(Object o, ByteArrayOutputStream out,
                                   boolean showRawXml) throws Exception {
        Marshaller m = QAJAXBPool.getInstance().getMarshaller();
        try {
            m.marshal(o, out);
            InputStream is = new ByteArrayInputStream(out.toByteArray());
            if (showRawXml) {
                System.out.println("____Marshalled: " +
                                   XmlJsonUtils.getStreamContent(is));
            } else {
                System.out.println("____Marshalled: &lt;raw xml display suppressed&gt;");
            }
        } finally {
            QAJAXBPool.getInstance().putMarshaller(m);
        }
    }


    public static Object unmarshallXML(InputStream wris) throws Exception {

        if (wris.available() <= 0) {
            return null;
        }

        Unmarshaller um = QAJAXBPool.getInstance().getUnmarshaller();
        try {
            Object ret = um.unmarshal(wris);
            if (ret instanceof JAXBElement) {
                return ((JAXBElement)ret).getValue();
            }
            return ret;
        } finally {
            QAJAXBPool.getInstance().putUnmarshaller(um);
        }
    }

}

