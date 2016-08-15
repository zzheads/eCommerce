package com.acme.ecommerce.exceptions;//

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// eCommerce
// com.acme.ecommerce.service created by zzheads on 13.08.2016.
//
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

    private int code;

    private String message;

    public ProductNotFoundException() {
        code = 404;
        message = "Product not found";
    }

    public ProductNotFoundException(String message) {
        super(message);
        code = 404;
        this.message = message;
    }

    public ProductNotFoundException(int code, String message) {
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
        return "Product Not Found Exception { " +
            "error = " + code +
            ", message = '" + message + '\'' +
            '}';
    }
}
