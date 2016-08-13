package com.acme.ecommerce.service;//

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
