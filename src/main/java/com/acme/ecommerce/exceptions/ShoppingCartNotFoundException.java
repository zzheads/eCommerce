package com.acme.ecommerce.exceptions;//

// eCommerce
// com.acme.ecommerce.service created by zzheads on 13.08.2016.
//
public class ShoppingCartNotFoundException extends RuntimeException {

    private int code;
    private String message;

    public ShoppingCartNotFoundException() {
        this.code = 404;
    }

    public ShoppingCartNotFoundException(String message) {
        this.code = 404;
        this.message = message;
    }

    public ShoppingCartNotFoundException(int code, String message) {
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
        return "ShoppingCartNotFoundException{" +
            "code=" + code +
            ", message='" + message + '\'' +
            '}';
    }
}
