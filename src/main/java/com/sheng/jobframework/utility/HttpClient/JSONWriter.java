
package com.sheng.jobframework.utility.HttpClient;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;


public class JSONWriter {

    private StringBuffer buf = new StringBuffer();
    private Stack<Object> calls = new Stack<Object>();
    boolean emitClassName = true;
    boolean formatForHtml = false;
    boolean useIndent = true;
    int indent = 0;

    public JSONWriter(boolean formatForHtml) {
        this.formatForHtml = formatForHtml;
    }

    public JSONWriter() {
        this(true);
    }

    public String write(Object object) {
        buf.setLength(0);
        value("", object);
        String ret = buf.toString();
        while (!ret.endsWith("}"))
            ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    public String write(long n) {
        return String.valueOf(n);
    }

    public String write(double d) {
        return String.valueOf(d);
    }

    public String write(char c) {
        return "\"" + c + "\"";
    }

    public String write(boolean b) {
        return String.valueOf(b);
    }
    // currently unused but can be used to make number arrays more readable by setting inArray to true
    // as needed
    private int arrayIndex = 0;
    private boolean inArray = false;

    private void value(String namePart, Object object) {
        if (object == null || cyclic(object)) {
            add("null");
        } else {
            calls.push(object);
            if (object instanceof byte[]) {
                try {
                    string(namePart, Base64.to64((byte[])object));
                } catch (Exception e) {
                    System.err.println("Error encountered during base64 encoding: " +
                                       e);
                }
            } else if (object instanceof Class)
                string(namePart, object);
            else if (object instanceof Boolean)
                bool(namePart, ((Boolean)object).booleanValue());
            else if (object instanceof Number)
                number(namePart, object);
            else if (object instanceof String)
                string(namePart, object);
            else if (object instanceof Character)
                string(namePart, object);
            else if (object instanceof Map)
                map(namePart, (Map)object);
            else if (object.getClass().isArray())
                array(namePart, object);
            else if (object instanceof Iterator)
                array(namePart, (Iterator)object);
            else if (object instanceof Collection)
                array(namePart, ((Collection)object).iterator());
            else if (object instanceof Enum)
                string(namePart, object, false);
            /*
	    ** IMPORTANT!
	    ** Make sure that all javax.xml.datatype.* objects from th
	    ** BDK schema are handled
	    */
            else if (object instanceof XMLGregorianCalendar)
                string(namePart, object);
            else if (object instanceof Duration)
                string(namePart, object);
            else
                bean(namePart, object);
            calls.pop();
        }
    }

    private boolean cyclic(Object object) {
        Iterator it = calls.iterator();
        while (it.hasNext()) {
            Object called = it.next();
            if (object == called)
                return true;
        }
        return false;
    }

    private void addIndent() {
        if (useIndent) {
            for (int i = 0; i < indent; i++) {
                add("  ");
            }
        }
    }

    private void removeIndent() {
        if (useIndent) {
            buf.delete(buf.length() - 2, buf.length());
        }
    }

    private void addOpen(String opener) {
        indent++;
        add(opener + "\n");
        addIndent();
    }

    private void addSame(String opener) {
        if (inArray) {
            add(opener);
            if (arrayIndex == 16) {
                add("\n");
                addIndent();
                arrayIndex = 0;
            }
            arrayIndex++;
        } else {
            add(opener + "\n");
            addIndent();
        }
    }

    private void addClose(String closer) {
        indent--;
        removeIndent();
        add(closer + ",\n");
        addIndent();
        inArray = false;
        arrayIndex = 0;
    }

    private void bean(String namePart, Object object) {
        add(namePart);
        addOpen("{");
        BeanInfo info;
        boolean addedSomething = false;
        try {
            info = Introspector.getBeanInfo(object.getClass());
            PropertyDescriptor[] props = info.getPropertyDescriptors();
            for (int i = 0; i < props.length; ++i) {
                PropertyDescriptor prop = props[i];
                String name = prop.getName();
                Method accessor = prop.getReadMethod();
                if ((emitClassName == true || !"class".equals(name)) &&
                    accessor != null) {
                    if (!accessor.isAccessible())
                        accessor.setAccessible(true);
                    Object value = accessor.invoke(object, (Object[])null);
                    if (value != null) {
                        add(name, value);
                        addedSomething = true;
                    }
                }
            }
            Field[] ff = object.getClass().getFields();
            for (int i = 0; i < ff.length; ++i) {
                Field field = ff[i];
                add(field.getName(), field.get(object));
                addedSomething = true;
            }
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (InvocationTargetException ite) {
            ite.getCause().printStackTrace();
            ite.printStackTrace();
        } catch (IntrospectionException ie) {
            ie.printStackTrace();
        }
        addClose("}");
    }

    private static final String HTML_PREFIX = "<b><font color=\"#770000\">";
    private static final String HTML_SUFFIX = "</font></b>";

    public static String stripFormat(String s) {
        s = s.replace(HTML_PREFIX, "").replace(HTML_SUFFIX, "");
        return s;
    }

    private void add(String name, Object value) {
        if (value != null) {
            boolean isClass = name.equals("class");
            //String namePart = "\""+(isClass ? "beeType" : (value instanceof java.util.List ? Inflector.getInstance().singularize(name) : name))+"\":";
            String namePart =
                "\"" + (formatForHtml ? HTML_PREFIX : "") + (isClass ?
                                                             "beeType" :
                                                             name) +
                (formatForHtml ? HTML_SUFFIX : "") + "\":";
            if (isClass) {
                String[] words = (value + "").split("\\.");
                String cname = words[words.length - 1];
                value(namePart,
                      cname.substring(0, 1).toLowerCase() + cname.substring(1));
            } else {
                value(namePart, value);
            }
        }
    }

    private void map(String namePart, Map map) {
        namePart += "{";
        boolean hasElements = false;
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            if (!hasElements) {
                hasElements = true;
                addOpen(namePart);
            }
            Map.Entry e = (Map.Entry)it.next();
            if (e.getValue() != null) {
                value(e.getKey() + ":", e.getValue());
            }
        }
        if (hasElements)
            addClose("}");
    }

    private void array(String namePart, Iterator it) {
        namePart += "[";
        boolean hasElements = false;
        while (it.hasNext()) {
            if (!hasElements) {
                hasElements = true;
                addOpen(namePart);
            }
            value("", it.next());
        }
        if (hasElements)
            addClose("]");
    }

    private void array(String namePart, Object object) {
        namePart += "[";
        boolean hasElements = false;
        int length = Array.getLength(object);
        for (int i = 0; i < length; ++i) {
            if (!hasElements) {
                hasElements = true;
                addOpen(namePart);
            }
            value("", Array.get(object, i));
        }
        if (hasElements)
            addClose("]");
    }

    private void number(String namePart, Object o) {
        add(namePart);
        add(o);
        addSame(",");
    }

    private void bool(String namePart, boolean b) {
        add(namePart);
        add(b ? "true" : "false");
        addSame(",");
    }

    private void string(String namePart, Object obj) {
        string(namePart, obj, true);
    }

    private void string(String namePart, Object obj, boolean quoteObj) {
        add(namePart);
        if (quoteObj)
            add('"');
        CharacterIterator it = new StringCharacterIterator(obj.toString());
        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
            if (c == '"')
                add("\\\"");
            else if (c == '\\')
                add("\\\\");
            else if (c == '/')
                add("\\/");
            else if (c == '\b')
                add("\\b");
            else if (c == '\f')
                add("\\f");
            else if (c == '\n')
                add("\\n");
            else if (c == '\r')
                add("\\r");
            else if (c == '\t')
                add("\\t");
            else if (Character.isISOControl(c)) {
                unicode(c);
            } else {
                add(c);
            }
        }
        if (quoteObj)
            add('"');
        addSame(",");
    }

    private void add(Object obj) {
        buf.append(obj);
    }

    private void add(char c) {
        buf.append(c);
    }

    static char[] hex = "0123456789ABCDEF".toCharArray();

    private void unicode(char c) {
        add("\\u");
        int n = c;
        for (int i = 0; i < 4; ++i) {
            int digit = (n & 0xf000) >> 12;
            add(hex[digit]);
            n <<= 4;
        }
    }
}

