package com.acme.ecommerce.exceptions;//

// eCommerce
// com.acme.ecommerce.service created by zzheads on 13.08.2016.
//
public class ExceedsProductQuantityException extends Exception {

    public ExceedsProductQuantityException() {
    }

    public ExceedsProductQuantityException(String message) {
        super(message);
    }
}
