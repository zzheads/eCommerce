package com.acme.ecommerce.exceptions;//

// eCommerce
// com.acme.ecommerce.service created by zzheads on 13.08.2016.
//
public class ShoppingCartNotFoundException extends Exception {

    public ShoppingCartNotFoundException() {
    }

    public ShoppingCartNotFoundException(String message) {
        super(message);
    }
}
