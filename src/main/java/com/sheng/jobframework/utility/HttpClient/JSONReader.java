
package com.sheng.jobframework.utility.HttpClient;
// copied from package org.stringtree.json;

import java.lang.reflect.Method;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;


public class JSONReader {

    private static final Object OBJECT_END = new Object();
    private static final Object ARRAY_END = new Object();
    private static final Object COLON = new Object();
    private static final Object COMMA = new Object();
    public static final int FIRST = 0;
    public static final int CURRENT = 1;
    public static final int NEXT = 2;

    private static Map<Character, Character> escapes =
        new HashMap<Character, Character>();
    static {
        escapes.put(Character.valueOf('"'), Character.valueOf('"'));
        escapes.put(Character.valueOf('\\'), Character.valueOf('\\'));
        escapes.put(Character.valueOf('/'), Character.valueOf('/'));
        escapes.put(Character.valueOf('b'), Character.valueOf('\b'));
        escapes.put(Character.valueOf('f'), Character.valueOf('\f'));
        escapes.put(Character.valueOf('n'), Character.valueOf('\n'));
        escapes.put(Character.valueOf('r'), Character.valueOf('\r'));
        escapes.put(Character.valueOf('t'), Character.valueOf('\t'));
    }

    private CharacterIterator it;
    private char c;
    private Object token;
    private StringBuffer buf = new StringBuffer();

    private char next() {
        c = it.next();
        return c;
    }

    private void skipWhiteSpace() {
        while (Character.isWhitespace(c)) {
            next();
        }
    }

    public Object read(CharacterIterator ci, int start) {
        it = ci;
        switch (start) {
        case FIRST:
            c = it.first();
            break;
        case CURRENT:
            c = it.current();
            break;
        case NEXT:
            c = it.next();
            break;
        }
        return read();
    }

    public Object read(CharacterIterator it) {
        return read(it, NEXT);
    }

    public Object parse(String string) {
        Object ret = read(new StringCharacterIterator(string), FIRST);
        if (ret != null)
            ret = MapToObject.process(ret);
        return ret;
    }

    private Object read() {
        skipWhiteSpace();
        char ch = c;
        next();
        switch (ch) {
        case '"':
            token = string();
            break;
        case '[':
            token = array();
            break;
        case ']':
            token = ARRAY_END;
            break;
        case ',':
            token = COMMA;
            break;
        case '{':
            token = object();
            break;
        case '}':
            token = OBJECT_END;
            break;
        case ':':
            token = COLON;
            break;
        case 't':
            next();
            next();
            next(); // assumed r-u-e
            token = Boolean.TRUE;
            break;
        case 'f':
            next();
            next();
            next();
            next(); // assumed a-l-s-e
            token = Boolean.FALSE;
            break;
        case 'n':
            next();
            next();
            next(); // assumed u-l-l
            token = null;
            break;
        default:
            c = it.previous();
            if (Character.isDigit(c) || c == '-') {
                token = number();
            }
        }
        // System.out.println("token: " + token); // enable this line to see the token stream
        return token;
    }

    private Object object() {
        Map<Object, Object> ret = new HashMap<Object, Object>();
        Object key = read();
        while (token != OBJECT_END) {
            read(); // should be a colon
            if (token != OBJECT_END) {
                ret.put(key, read());
                if (read() == COMMA) {
                    key = read();
                }
            }
        }

        return ret;
    }

    private Object array() {
        List<Object> ret = new ArrayList<Object>();
        Object value = read();
        while (token != ARRAY_END) {
            ret.add(value);
            if (read() == COMMA) {
                value = read();
            }
        }
        return ret;
    }

    private Object number() {
        int length = 0;
        boolean isFloatingPoint = false;
        buf.setLength(0);

        if (c == '-') {
            add();
        }
        length += addDigits();
        if (c == '.') {
            add();
            length += addDigits();
            isFloatingPoint = true;
        }
        if (c == 'e' || c == 'E') {
            add();
            if (c == '+' || c == '-') {
                add();
            }
            addDigits();
            isFloatingPoint = true;
        }

        String s = buf.toString();
        return isFloatingPoint ?
               (length < 17) ? (Object)Double.valueOf(s) : new BigDecimal(s) :
               (length < 19) ? (Object)Long.valueOf(s) : new BigInteger(s);
    }

    private int addDigits() {
        int ret;
        for (ret = 0; Character.isDigit(c); ++ret) {
            add();
        }
        return ret;
    }

    private Object string() {
        buf.setLength(0);
        while (c != '"') {
            if (c == '\\') {
                next();
                if (c == 'u') {
                    add(unicode());
                } else {
                    Object value = escapes.get(Character.valueOf(c));
                    if (value != null) {
                        add(((Character)value).charValue());
                    }
                }
            } else {
                add();
            }
        }
        next();

        return buf.toString();
    }

    private void add(char cc) {
        buf.append(cc);
        next();
    }

    private void add() {
        add(c);
    }

    private char unicode() {
        int value = 0;
        for (int i = 0; i < 4; ++i) {
            switch (next()) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                value = (value << 4) + c - '0';
                break;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                value = (value << 4) + (c - 'a') + 10;
                break;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                value = (value << 4) + (c - 'A') + 10;
                break;
            }
        }
        return (char)value;
    }
}
/**
 *************************************************
 ** added by Guru Balse
 *************************************************
 */
@SuppressWarnings("unchecked")
class MapToObject {
    private MapToObject() {
    }
    private static boolean verbose = false;

    static void printv(String s) {
        if (verbose)
            print(s);
    }

    static void print(String s) {
        System.out.println(s);
    }

    static String capitalize(String key) {
        return key.substring(0, 1).toUpperCase() + key.substring(1);
    }

    static String getSetterName(String key) {
        return "set" + capitalize(key);
    }

    static String getGetterName(String key) {
        return "get" + capitalize(key);
    }

    static Method getSetter(Object obj, String key, Class param) {
        try {
            String mName = getSetterName(key);
            ;
            printv(mName);
            Method m = obj.getClass().getMethod(mName, new Class[] { param });
            printv(m + "");
            return m;
        } catch (Exception e) {
            return null;
        }
    }

    static void setObject(Object obj, Object value,
                          String key) throws Exception {
        Method[] ms = obj.getClass().getMethods();
        boolean found = false;
        for (Method m : ms) {
            if (m.getName().equals(getSetterName(key))) {
                Object newObj = getObjectFromMap((HashMap)value);
                if (newObj != null)
                    m.invoke(obj, newObj);
                found = true;
                break;
            }
        }
        if (!found)
            print("set object not found for " + obj.getClass() + "." +
                  getSetterName(key));
    }

    static void setArray(Object obj, Object value,
                         String key) throws Exception {
        Method[] ms = obj.getClass().getMethods();
        String mname = getGetterName(key);
        printv("mname = " + mname);
        //String mnames = Inflector.getInstance().pluralize(mname);
        String mnames = mname;
        //printv("mnames = "+mnames);
        Method mInvoke = null;
        for (Method m : ms) {
            if (m.getName().equals(mname)) {
                mInvoke = m;
                break;
            } else if (m.getName().equals(mnames)) {
                mInvoke = m;
                break;
            }
        }
        if (mInvoke == null)
            print("set array not found for " + obj.getClass() + "." +
                  getSetterName(key));
        printv("mInvoke = " + mInvoke);
        List destList = (List)(mInvoke.invoke(obj));
        ArrayList srcList = (ArrayList)value;
        // iterate through srcList and populate destList
        for (Iterator itr = srcList.iterator(); itr.hasNext(); ) {
            Object listO = itr.next();
            if (listO instanceof HashMap) {
                destList.add(getObjectFromMap((HashMap)listO));
            } else {
                destList.add(listO);
            }
        }
    }

    static void setNumber(Object obj, Object value,
                          String key) throws Exception {
        Method[] ms = obj.getClass().getMethods();
        boolean found = false;
        for (Method m : ms) {
            if (m.getName().equals(getSetterName(key))) {
                Class[] cs = m.getParameterTypes();
                for (Class c : cs) {
                    printv("m = " + m + " c = " + c);
                    if (c.getName().indexOf("Long") != -1) {
                        m.invoke(obj, (Long)value);
                        found = true;
                        break;
                    } else if (c.getName().indexOf("long") != -1) {
                        m.invoke(obj, ((Long)value).longValue());
                        found = true;
                        break;
                    } else if (c.getName().indexOf("Integer") != -1) {
                        m.invoke(obj, new Integer(value + ""));
                        found = true;
                        break;
                    } else if (c.getName().indexOf("int") != -1) {
                        m.invoke(obj, new Integer(value + "").intValue());
                        found = true;
                        break;
                    } else if (c.getName().indexOf("Double") != -1) {
                        m.invoke(obj, new Double(value + ""));
                        found = true;
                        break;
                    } else if (c.getName().indexOf("double") != -1) {
                        m.invoke(obj, new Double(value + "").doubleValue());
                        found = true;
                        break;
                    } else {
                        print("Unhandled m = " + m + " with param = " + c);
                    }
                }
            }
        }
        if (!found)
            print("set number not found for " + obj.getClass() + "." +
                  getSetterName(key));
    }

    static void setSpecialString(Object obj, String value,
                                 String key) throws Exception {
        Method[] ms = obj.getClass().getMethods();
        boolean found = false;
        Method mFound = null;
        Class cFound = null;
        for (Method m : ms) {
            if (m.getName().equals(getSetterName(key))) {
                Class[] cs = m.getParameterTypes();
                // Handle enums and all javax.xml.datatype objects from the schema
                for (Class c : cs) {
                    printv("m = " + m + " c = " + c);
                    if (c.isEnum()) {
                        Method m2 =
                            c.getMethod("fromValue", new Class[] { String.class });
                        Enum e = (Enum)m2.invoke(null, value);
                        m.invoke(obj, e);
                        found = true;
                        break;
                    } else if (c.toString().indexOf("XMLGregorianCalendar") !=
                               -1) {
                        XMLGregorianCalendar xgc =
                            DatatypeFactory.newInstance().newXMLGregorianCalendar(value);
                        m.invoke(obj, xgc);
                        found = true;
                        break;
                    } else if (c.toString().indexOf("Duration") != -1) {
                        Duration dur =
                            DatatypeFactory.newInstance().newDuration(value);
                        m.invoke(obj, dur);
                        found = true;
                        break;
                    } else if (c.toString().indexOf("[B") != -1) {
                        // base64 ==> byte array
                        byte[] b = Base64.from64(value);
                        m.invoke(obj, b);
                        found = true;
                    } else if (c.toString().indexOf("Object") != -1) {
                        // set object (expecting string)
                        m.invoke(obj, value);
                        found = true;
                    } else {
                        mFound = m;
                        cFound = c;
                        break;
                    }
                }
            }
        }
        if (!found)
            print("set string not found for " + obj.getClass() + "." +
                  getSetterName(key) + " but found meth : " + mFound +
                  " param = " + cFound);
    }

    static void setString(Object obj, String value,
                          String key) throws Exception {
        Method m = getSetter(obj, key, String.class);
        if (m == null) {
            setSpecialString(obj, value, key);
        } else {
            m.invoke(obj, value);
        }
    }

    static void setBoolean(Object obj, Boolean value,
                           String key) throws Exception {
        Method m = getSetter(obj, key, boolean.class);
        if (m != null) {
            m.invoke(obj, value.booleanValue());
        } else {
            m = getSetter(obj, key, Boolean.class);
            m.invoke(obj, value);
        }
    }

    static Object getObjectFromMap(HashMap map) throws Exception {
        Object ret = null;
        if (map == null)
            return null;
        if (map.get("beeType") == null) {
            printv("beeType not found!");
            return map;
        } else {
            String val = (String)map.remove("beeType");
            String cname = "com.oracle.beehive." + capitalize(val);
            //printv("cname = "+cname);
            Class c = null;
            try {
                c = Class.forName(cname);
                ret = c.newInstance();
            } catch (Exception e) {
                cname = "com.oracle.beehive.rest." + capitalize(val);
                try {
                    c = Class.forName(cname);
                    ret = c.newInstance();
                } catch (Exception e1) {
                    print("Problem finding or instantiating class for : " +
                          val + " : " + e + " : " + e1);
                    return null;
                }
            }
        }
        for (Iterator itr = map.keySet().iterator(); itr.hasNext(); ) {
            String key = (String)itr.next();
            Object value = map.get(key);
            printv(key + " = " + value.getClass() + "\n" +
                    value + "\n");
            if (value instanceof String) {
                setString(ret, (String)value, key);
            } else if (value instanceof Boolean) {
                setBoolean(ret, (Boolean)value, key);
            } else if (value instanceof Long || value instanceof Double ||
                       value instanceof BigInteger ||
                       value instanceof BigDecimal) {
                setNumber(ret, value, key);
            } else if (value instanceof HashMap) {
                setObject(ret, value, key);
            } else if (value instanceof ArrayList) {
                setArray(ret, value, key);
            } else {
                print("Need to handle : " + value.getClass());
            }
        }
        return ret;
    }

    static Object process(Object jsonObject) {
        try {
            return (jsonObject == null ? null :
                    getObjectFromMap((HashMap)jsonObject));
        } catch (Exception e) {
            System.out.println("Exception : " + e +
                               " encountered in processing JSON object");
            e.printStackTrace();
            return null;
        }
    }
}
