package com.acme.ecommerce.exceptions;//

// eCommerce
// com.acme.ecommerce.service created by zzheads on 13.08.2016.
//
public class ExceedsProductQuantityException extends RuntimeException {

    private int code;
    private String message;

    public ExceedsProductQuantityException() {
        this.code = 403;
    }

    public ExceedsProductQuantityException(String message) {
        this.code = 403;
        this.message = message;
    }

    public ExceedsProductQuantityException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override public String toString() {
        return "ExceedsProductQuantityException{" +
            "code=" + code +
            ", message='" + message + '\'' +
            '}';
    }
}
