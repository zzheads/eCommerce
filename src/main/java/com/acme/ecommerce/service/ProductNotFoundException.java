package com.acme.ecommerce.service;//

// eCommerce
// com.acme.ecommerce.service created by zzheads on 13.08.2016.
//
public class ProductNotFoundException extends Exception {

    public ProductNotFoundException() {
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
