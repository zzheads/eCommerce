package com.acme.ecommerce.controller;

import com.acme.ecommerce.domain.Product;

public class FlashMessage {
    private String message;
    private Status status;

    public FlashMessage(String message, Status status) {
        this.message = message;
        this.status = status;
    }

    public static String outOfStock (Product product) {
        String text = "";
        if (product.getQuantity()>0) {
            text = String.format("You can't buy quantity of product more than in stock. We have only %d items of %s.", product.getQuantity(), product.getName());
        } else {
            text = String.format("You can't buy product out of stock. We dont have %s in stock.", product.getName());
        }
        return text;
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static enum Status {
        SUCCESS, FAILURE
    }

}
