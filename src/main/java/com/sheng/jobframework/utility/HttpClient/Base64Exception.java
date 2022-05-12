
package com.sheng.jobframework.utility.HttpClient;


/**
 *  Exception thrown during base64 encoding/decoding.
 */

public class Base64Exception extends Exception {
    static final long serialVersionUID = 1000002L;

    public Base64Exception() {
        super();
    }

    public Base64Exception(String msg) {
        super(msg);
    }

    public Base64Exception(Throwable cause) {
        super(cause);
    }

    public Base64Exception(String msg, Throwable cause) {
        super(msg, cause);
    }
}
